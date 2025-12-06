Great — let’s make this **concrete and exact** so you can implement it. I’ll show three common deployment patterns for the same public API domain `https://api.example.com` and how a request to `api/v1/order` or `api/v1/payment` reaches the correct microservice:

1. **API Gateway (recommended for public APIs, auth, rate-limiting) → VPC Link → NLB → ALB/Ingress → EKS services**
2. **ALB (Ingress) directly in front of EKS**
3. **CloudFront in front of either API Gateway or ALB (for WAF, caching, global edge)**

For each pattern I’ll show:

* the DNS / Route53 wiring,
* the ingress routing rules (path-based),
* how path/header rewriting and auth/tracing works,
* a Kubernetes Ingress example (ALB) and API Gateway route example,
* a short request flow (what headers are present).

---

# 1) Pattern A — API Gateway (HTTP API) as the public router → EKS services via VPC Link / NLB

**When to use:** you want managed auth (JWT/Cognito), throttling, request validation, WAF, and a single public API gateway.

**High-level**
`Browser -> Route53 (api.example.com) -> API Gateway (custom domain) -> VPC Link -> NLB -> ALB/Ingress -> EKS Service (order/payment pods)`

**Route53**

* Create a custom domain `api.example.com` in API Gateway, attach ACM cert (same region for regional APIs).
* Add Route53 **ALIAS** `A` record for `api.example.com` pointing to the API Gateway domain (regional or edge as set).

**API Gateway routing**

* Create routes:

  * `POST /v1/order` → Integration: VPC Link to NLB target that forwards to your ALB (or directly to NLB if you expose services that way).
  * `POST /v1/payment` → same, different path.
* API Gateway can validate JWTs (authorizer), do request/response transformations, enforce throttling, log to CloudWatch / X-Ray.

**Why VPC Link?**
API Gateway cannot talk directly to an internal ALB; VPC Link connects it to a Network Load Balancer (NLB) in your VPC which forwards to your ALB or to the pods.

**Flow (example)**

1. SPA `POST https://api.example.com/v1/order` with `Authorization: Bearer <jwt>` and `X-Request-ID`.
2. Route53 resolves `api.example.com` → API Gateway.
3. API Gateway enforces auth, then forwards request to NLB via VPC Link.
4. NLB → ALB / K8s Ingress → pods (order service).
5. Response returns through the same chain.

**Benefits:** managed auth & protections, easy rate-limiting and WAF, unified API surface.

---

# 2) Pattern B — ALB (Application Load Balancer) + Kubernetes Ingress → EKS services

**When to use:** you prefer to expose EKS directly, want path-based routing at the load balancer, and handle auth in your services or via an ingress-based auth service.

**High-level**
`Browser -> Route53 (api.example.com) -> ALB -> Kubernetes Ingress rules -> Service/order or Service/payment`

**Route53**

* Create Route53 **ALIAS** `A` record for `api.example.com` pointing at the ALB DNS name (select ALB as Alias target).

**ALB Ingress path rules**
ALB supports path-based routing. The ALB Ingress Controller (AWS Load Balancer Controller) will create an ALB and configure rules such that:

* `/v1/order*` → target group pointing to `orders-svc` (port 8080)
* `/v1/payment*` → target group pointing to `payment-svc` (port 8080)

**Kubernetes Ingress example (ALB ingress annotations):**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  namespace: prod
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP":80},{"HTTPS":443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:...   # optional
spec:
  rules:
  - http:
      paths:
      - path: /v1/order/*
        pathType: Prefix
        backend:
          service:
            name: orders-svc
            port:
              number: 8080
      - path: /v1/payment/*
        pathType: Prefix
        backend:
          service:
            name: payment-svc
            port:
              number: 8080
```

**Notes about path & host matching**

* If you have multiple hostnames you can also match host + path. For a single host `api.example.com` the ALB will use path rules to route.
* ALB supports rewriting the path if needed via target group rules or Ingress annotations, but often you keep the pod route same as external path (e.g., service accepts `/v1/order`).

**Flow (example)**

1. SPA -> Route53 -> ALB.
2. ALB checks listener rules, path `/v1/order` → forwards to target group `orders-svc`.
3. Target group does health checks and load balances to pods. Response flows back upstream.

**Benefits:** simpler, direct mapping to Kubernetes Services, lower latency (no API Gateway). But you must handle auth, rate-limiting, and WAF yourself (ALB + AWS WAF can cover WAF).

---

# 3) Pattern C — CloudFront in front of APIs (optional layer)

**When to use:** you want WAF + edge caching + global TLS termination + DDoS protection via CloudFront in front of API Gateway or ALB.

**High-level**
`Browser -> Route53 (api.example.com) -> CloudFront distribution -> origin: API Gateway or ALB`

**CloudFront behaviors**

* Create a behavior for path pattern `/api/*` or `/v1/*` with origin set to API Gateway or ALB.
* For dynamic APIs, set caching disabled (Cache-Control: no-cache) and forward all headers (or specific headers like Authorization, Cookie, X-Request-ID).
* CloudFront can also forward `/config.json` from S3 for runtime app config.

**Route53**

* Alias `api.example.com` → CloudFront distribution domain (ALIAS).

**Flow differences**

* CloudFront will be the public entrypoint that forwards to your API origin. You must ensure CloudFront forwards Authorization headers and any custom headers required by API Gateway or ALB.

---

# 4) Path-based routing details & path stripping

**Two behaviors to control:**

* **Preserve path as-is** (typical): external path `/v1/order` forwarded to backend `/v1/order`.
* **Strip prefix** (if backend expects `/`): you can configure Ingress path rewriting (e.g., nginx ingress supports annotation `nginx.ingress.kubernetes.io/rewrite-target: /$2`) or use API Gateway mapping templates to transform path. ALB has limited rewrite features but supports redirect; the AWS Load Balancer Controller also supports certain rewrite annotations.

**Example NGINX Ingress rewrite:**

```yaml
annotations:
  nginx.ingress.kubernetes.io/rewrite-target: /$1
paths:
- path: /v1/order/(.*)
  pathType: Prefix
  backend: ...
```

---

# 5) Authentication, headers, tracing — what’s forwarded

**Headers you usually forward:**

* `Authorization: Bearer <jwt>` (for auth)
* `X-Request-ID` (for correlation)
* `X-Forwarded-For` / `X-Forwarded-Proto` / `X-Forwarded-Host` (ALB/CloudFront set these)
* `traceparent` or `X-Amzn-Trace-Id` (for tracing)

**Where auth happens:**

* API Gateway: validates JWT (Cognito or custom OIDC) before hitting backend, can inject claims into context.
* ALB: can use Cognito authentication action on listener rules (ALB supports Cognito auth), or your services handle JWT validation.
* CloudFront: can do Lambda@Edge auth checks, but commonly leave auth to API Gateway/ALB/Service.

**Example cURL request:**

```bash
curl -X POST https://api.example.com/v1/order \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"productId":"sku-1", "qty":1}'
```

* This arrives at API Gateway or ALB; the chosen router validates Authorization or passes it to the service which validates the JWT.

---

# 6) Example: full request journey (API Gateway -> EKS)

1. `curl POST https://api.example.com/v1/order`
2. DNS: `api.example.com` → Route53 ALIAS → API Gateway custom domain.
3. API Gateway: validates JWT (Authorizer), applies throttle & WAF, then routes `POST /v1/order` to the configured integration (VPC Link to NLB).
4. NLB receives request and forwards to target (ALB or nodePort) which reaches K8s Ingress.
5. K8s Ingress matches `/v1/order` → `orders-svc` → pod receives request.
6. Pod processes, talks to DB (via Secrets), returns JSON.
7. Response flows back API Gateway → client.

---

# 7) How frontend is maintained / how to change routing without redeploying clients

* **Use stable DNS `api.example.com`** (Route53 alias). If you move from ALB to API Gateway, update Route53 / CloudFront alias — the SPA needs no change.
* **Prefer runtime config** (e.g., `/config.json`) so you can change API URL or add new endpoints without rebuilding.
* **Short TTLs**: for rapid failover, use shorter DNS TTLs on non-alias records (but Route53 alias auto-updates the underlying AWS target; TTL is less of a concern).

---

# 8) Comparison / recommendation (quick)

* **Use API Gateway** when you need managed auth, request validation, throttling, WAF, analytics, or you want a single public surface and serverless option. Good for public REST/GraphQL APIs.
* **Use ALB + Ingress** when you want simpler, lower-latency direct access to EKS, and you handle auth/rate-limiting in the cluster.
* **Use CloudFront** in front of either when you want edge caching, WAF at the edge, TLS termination, or global low-latency endpoints.

---

# 9) Copy-paste artifacts

## ALB Ingress (K8s) — path routing to `/v1/order` and `/v1/payment`

(see YAML earlier; reuse it)

## API Gateway HTTP API route example (conceptual)

* Create routes:

  * `POST /v1/order` → Integration: VPC Link target `https://nlb-internal:443/orders` (or NLB DNS)
  * `POST /v1/payment` → Integration: `https://nlb-internal:443/payments`
* Enable JWT authorizer and map claims into the `X-User-Id` header if you want.

(Exact UI/CLI JSON is verbose; tell me if you want the `aws apigatewayv2` JSON and I’ll produce it.)

---