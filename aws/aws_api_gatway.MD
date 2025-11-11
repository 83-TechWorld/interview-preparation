Nice — I’ll walk you through everything end-to-end: what **API Gateway** and **Amazon EKS (Kubernetes)** are, why you’d use each, how they work together (how Gateway reaches your services running in EKS), how scaling and maintenance work, how secrets are managed, and I’ll give practical **Terraform** examples + Kubernetes manifests you can adapt.

I’ll cite official AWS docs and trusted sources for the most important claims so you can follow details or copy/paste examples into your infra repo. ([AWS Documentation][1])

---

# 1) Quick conceptual summary

**API Gateway (Amazon API Gateway)**

* Managed service that lets you create, publish, secure and operate REST / HTTP / WebSocket APIs at scale. It fronts your backend services and provides features like routing, authentication/authorization, request/response transformations, throttling, caching, monitoring, usage plans, and private integrations into VPCs. ([AWS Documentation][2])

**Amazon EKS (Elastic Kubernetes Service)**

* AWS’s managed Kubernetes control plane. EKS runs (and manages / patches / scales) the Kubernetes control plane (API server, etcd etc.) while you run worker nodes (EC2 or Fargate) that host your Pods. This gives you the full Kubernetes API and ecosystem with AWS managed reliability. ([AWS Documentation][1])

Why use them together?

* API Gateway gives you a stable, secure API front that can route requests externally or privately into your VPC. EKS runs your microservices with Kubernetes features (Deployments, HPA, Service discovery, Ingress). Together you get a central, secure API surface + scalable container platform.

---

# 2) How API Gateway talks to services in EKS (architectural patterns)

There are two common patterns:

A. **API Gateway → VPC Link → NLB → Service (private integration)**

* API Gateway’s *VPC Link* can connect to internal ALBs or NLBs in your VPC. The typical flow for private EKS services is:
  `Client -> API Gateway -> VPC Link -> Network Load Balancer -> Kubernetes service (NodePort / target group)`
* You make an NLB that forwards to the EKS service (either to nodes or to pod IPs using the `nlb-ip` mode). API Gateway uses the VpcLink resource to forward requests. ([AWS Documentation][3])

B. **Client → API Gateway → Public ALB / ALB Ingress → Kubernetes Ingress (ALB)**

* Use the AWS Load Balancer Controller (ALB) so Kubernetes `Ingress` resources create ALBs. API Gateway can call that ALB (publicly) or you can use private ALB + VPC Link. ALB works great for path-based routing and host-based routing if you want L7 features. ([AWS Documentation][4])

Important: **API Gateway does not “discover” pods** — it routes to an IP/hostname (load balancer, Lambda, or HTTP endpoint). Service discovery inside the cluster is handled by Kubernetes (ClusterIP services, DNS, Ingress), and the load balancer provides a single stable endpoint for API Gateway to call.

---

# 3) How internal services will “know” the API Gateway / how routing is wired

* **From Gateway → cluster**: API Gateway targets a load balancer (NLB/ALB) or public/private endpoint. The load balancer is configured (manually or by the AWS Load Balancer Controller) to forward traffic to the appropriate Kubernetes `Service` (ClusterIP/NodePort/TargetGroup).
* **Inside the cluster**: Kubernetes maps Service → Pod(s) with kube-proxy / iptables or ipvs (or via `aws-node` CNI for pod IPs). Service discovery and resolution among microservices is via Kubernetes DNS (e.g., `orders.svc.cluster.local`) or via a service mesh if you use one (App Mesh, Istio).
* **If you need internal clients to call Gateway** (e.g., for API composition), they call the API Gateway endpoint; they must have network access to it (public endpoint or through a VPC endpoint / private link pattern).

---

# 4) Scaling & maintenance (API Gateway, EKS, Load Balancers, nodes)

**API Gateway**

* Managed & auto-scales to handle traffic. You can configure throttling (rate & burst) and usage plans to protect backends. There are per-region/account quotas (you can request increases). For very high-throughput systems use caching, throttling, and stage/usage plans to smooth bursts. ([Amazon Web Services, Inc.][5])

**EKS control plane**

* AWS manages the control plane (HA across AZs) — you don’t manage masters directly. EKS ensures availability and patching of control plane components. ([AWS Documentation][1])

**Kubernetes app scaling**

* **Horizontal Pod Autoscaler (HPA)** — scale pods by CPU/memory/custom metrics.
* **Cluster Autoscaler / Karpenter** — scale node capacity (add/remove EC2 nodes) when pods can’t be scheduled.
* **Pod-level**: Replicasets & Deployments; use Readiness/Liveness probes to keep healthy pods.
* Use **target tracking** or custom metrics (via Prometheus / CloudWatch Container Insights) for autoscaling decisions.

**Load Balancers**

* ALB/NLB scale automatically. ALB is good for L7 routing (host/path, SSL), NLB is low-latency L4 with static IPs — useful for API Gateway VPC Link which commonly uses NLB. AWS Load Balancer Controller will provision ALBs/NLBs for Kubernetes Ingress/Service resources. ([AWS Documentation][4])

---

# 5) Secrets management (best practices & options for EKS)

Options and recommended practices:

1. **AWS Secrets Manager / SSM Parameter Store**

   * Store secrets centrally, encrypted by AWS KMS. Manage rotation, access control, auditing.

2. **ASCP (AWS Secrets & Configuration Provider) or Secrets Store CSI driver**

   * Use the **Secrets Store CSI Driver** + the **AWS provider** to mount Secrets Manager / Parameter Store secrets into pods as files (or sync to Kubernetes secrets). This supports IRSA (IAM Roles for Service Accounts) so pods fetch secrets using a least-privileged IAM role — no node IAM leakage. ([secrets-store-csi-driver.sigs.k8s.io][6])

3. **Kubernetes Secrets** (encrypted at rest)

   * If used, enable encryption at rest with KMS for `secrets` in the cluster. But for enterprise use, prefer Secrets Manager + CSI driver for rotation and centralized control. ([AWS Documentation][7])

**Recommendation**: Use Secrets Manager + Secrets Store CSI driver + IRSA (IAM Roles for Service Accounts). That gives pod-level access control and central rotation/audit.

---

# 6) Terraform: practical examples / patterns

Below are **compact example snippets** you can adapt. They show the typical pieces:

* Create EKS cluster using `terraform-aws-modules/eks/aws` module.
* Create an ECR image (optional), K8s Service annotation for NLB or ALB.
* Create API Gateway VPC Link (for private integration) and an HTTP API that integrates to an NLB.

> NOTE: these snippets are simplified for clarity — you’ll need to expand them (VPC CIDRs, security groups, IAM roles, full providers, variables, outputs). Use `terraform-aws-modules/eks/aws` for production-grade EKS configuration. ([GitHub][8])

---

## 6A — EKS cluster (Terraform, using community module)

```hcl
# providers.tf
provider "aws" {
  region = "ap-south-1"
}

provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  token                  = data.aws_eks_cluster_auth.cluster.token
}

# main.tf
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "4.0.0"
  name = "eks-vpc"
  cidr = "10.0.0.0/16"
  azs = ["ap-south-1a","ap-south-1b","ap-south-1c"]
  public_subnets = ["10.0.1.0/24","10.0.2.0/24","10.0.3.0/24"]
  private_subnets = ["10.0.11.0/24","10.0.12.0/24","10.0.13.0/24"]
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "19.0.0"

  cluster_name    = "my-eks-cluster"
  cluster_version = "1.27"
  subnets         = module.vpc.private_subnets
  vpc_id          = module.vpc.vpc_id

  node_groups = {
    on_demand = {
      desired_capacity = 2
      max_capacity     = 4
      min_capacity     = 1
      instance_types   = ["t3.medium"]
    }
  }

  # enable IRSA for pod-to-AWS access (recommended)
  iam_roles = {
    # module options to create necessary IAM roles
  }
}

data "aws_eks_cluster" "cluster" {
  name = module.eks.cluster_id
}
data "aws_eks_cluster_auth" "cluster" {
  name = module.eks.cluster_id
}
```

This creates the cluster and the worker node groups. After `apply`, configure `kubectl` by using the cluster endpoint and CA above (or use `aws eks update-kubeconfig`).

---

## 6B — Expose service in EKS with NLB (Kubernetes manifest + annotation)

Use this when you want **API Gateway VPC Link → NLB → service** (NLB is recommended for private integrations).

```yaml
# service-nlb.yaml
apiVersion: v1
kind: Service
metadata:
  name: my-api-service
  namespace: default
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"           # create an NLB
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"     # internal NLB
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
  selector:
    app: my-api
```

When this Service is created, AWS will create an internal **NLB** and a Target Group that points to node ports (or pod IPs if using `nlb-ip` mode). You will get a DNS name for the NLB. Use that as the integration target for API Gateway (via VPC Link).

---

## 6C — Create API Gateway VPC Link + HTTP API (Terraform)

```hcl
# create a VPC link to the NLB
resource "aws_apigatewayv2_vpc_link" "nlb_link" {
  name = "api-gw-nlb-link"
  security_group_ids = [aws_security_group.apigw_sg.id]   # if needed
  subnet_ids = module.vpc.private_subnets
  # For HTTP APIs, you use aws_apigatewayv2_vpc_link (v2)
}

# create the HTTP API
resource "aws_apigatewayv2_api" "http_api" {
  name          = "my-http-api"
  protocol_type = "HTTP"
}

# integration to the NLB (private integration)
resource "aws_apigatewayv2_integration" "nlb_integration" {
  api_id = aws_apigatewayv2_api.http_api.id
  integration_type = "HTTP_PROXY"
  integration_uri  = "http://${aws_lb.my_nlb.dns_name}"   # or the NLB DNS returned
  connection_type  = "VPC_LINK"
  connection_id    = aws_apigatewayv2_vpc_link.nlb_link.id
}

# route and stage
resource "aws_apigatewayv2_route" "route" {
  api_id    = aws_apigatewayv2_api.http_api.id
  route_key = "GET /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.nlb_integration.id}"
}
resource "aws_apigatewayv2_stage" "prod" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true
}
```

Notes:

* In practice you will point the integration URI to the *NLB DNS name* and use `connection_type = "VPC_LINK"`. See the AWS docs on private integrations for REST/HTTP APIs. ([AWS Documentation][9])

---

## 6D — Alternative: ALB Ingress Controller (ALB) for L7 routing

If you prefer L7 features (path-based routing, certs on ALB), install the **AWS Load Balancer Controller** into the cluster (it reconciles Kubernetes Ingress objects into ALBs). Then create a Kubernetes `Ingress` and API GW can target the ALB.

Ingress example (path-based):

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internal
spec:
  rules:
    - host: api.internal.example
      http:
        paths:
          - path: /orders
            pathType: Prefix
            backend:
              service:
                name: orders-service
                port:
                  number: 80
```

The Load Balancer Controller will create an ALB and target groups for services. API Gateway can call the ALB (public or private via VPC Link).

Docs: AWS Load Balancer Controller / ALB Ingress. ([kubernetes-sigs.github.io][10])

---

# 7) Scaling considerations (practical checklist)

* **API Gateway**: autoscaled by AWS; protect backends with throttling/usage plans + caching. Monitor CloudWatch metrics, set alarms. ([Amazon Web Services, Inc.][5])
* **Ingress layer (ALB/NLB)**: Managed by AWS; they scale as traffic grows. Use internal vs public depending on security. ([AWS Documentation][4])
* **Pods**: use HPA (CPU/memory/custom metrics) to scale replicas.
* **Nodes**: use Cluster Autoscaler or Karpenter to add EC2 nodes when pods are pending. See `terraform-aws-modules/eks` for node group lifecycle options. ([GitHub][8])
* **Cold starts**: for services on EC2/EKS you control startup time; for highly spiky workloads consider Lambda behind API Gateway for fast scaling (if architecture allows).

---

# 8) Security & networking essentials

* Put your EKS worker nodes & databases in private subnets. Use internal NLB/ALB + API Gateway **private integration** via VPC Link for private APIs. ([AWS Documentation][3])
* Use **IRSA (IAM Roles for Service Accounts)** so each pod gets a minimal IAM role for accessing Secrets Manager or S3, rather than node IAM roles. This reduces blast radius. ([AWS Documentation][11])
* Terminate TLS at ALB (recommended) and use mTLS or service meshes for internal trust if needed.
* Monitor with CloudWatch Container Insights and Prometheus + Grafana for app metrics.

---

# 9) Secrets & config: practical recipe (recommended)

1. Store secrets in **AWS Secrets Manager**.
2. Install **Secrets Store CSI Driver** + **aws provider** in EKS. Create a `SecretProviderClass` that references Secrets Manager. Pods mount secrets as files; you can optionally sync those to Kubernetes Secrets. This supports rotation and central auditing. ([secrets-store-csi-driver.sigs.k8s.io][6])

Example (high level):

```yaml
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: aws-secrets
spec:
  provider: aws
  parameters:
    objects: |
      - objectName: "arn:aws:secretsmanager:ap-south-1:123456789012:secret/my-db-creds"
        objectType: "secretsmanager"
```

Use IRSA to give the ServiceAccount permission to call `secretsmanager:GetSecretValue`.

---

# 10) Operational maintenance and best practices

* Use Infrastructure as Code (Terraform) and GitOps for Kubernetes manifests (Flux/ArgoCD) — separate cluster infra from app manifests. ([HashiCorp Developer][12])
* Centralize logs (CloudWatch, Fluentd/FluentBit), metrics (Prometheus + remote_write), and traces (X-Ray / Jaeger).
* Use readiness probes to avoid sending traffic to unready pods.
* Test scaling (load tests) and throttling behaviors in staging.
* Keep node AMIs updated (managed node groups or EKS AMIs) and use rolling updates for node groups.

---

# 11) Short checklist to implement this in your infra

1. Create VPC with private & public subnets.
2. Create EKS with `terraform-aws-modules/eks/aws` (enable IRSA). ([GitHub][8])
3. Deploy the AWS Load Balancer Controller to the cluster (or use Service type LoadBalancer with NLB). ([AWS Documentation][4])
4. Deploy your services and configure Service/Ingress with the appropriate annotations (ALB vs NLB).
5. Create API Gateway HTTP API or REST API with a **VPC Link** pointed to the NLB (for private) or call the ALB endpoint. ([AWS Documentation][9])
6. Set up Secrets Manager + Secrets Store CSI driver + IRSA for secrets. ([secrets-store-csi-driver.sigs.k8s.io][6])
7. Implement HPA + Cluster Autoscaler/Karpenter for scaling.

---

# 12) Links to jump into docs / modules (to save you time)

* Amazon EKS overview (managed Kubernetes): ([AWS Documentation][1])
* API Gateway VPC Link & private integrations: ([AWS Documentation][3])
* AWS Load Balancer Controller (ALB Ingress): ([kubernetes-sigs.github.io][10])
* Terraform EKS module (recommended): ([GitHub][8])
* Secrets Store CSI Driver & AWS provider: ([secrets-store-csi-driver.sigs.k8s.io][6])

---

Excellent — let’s go *hands-on* and build this end-to-end with your **Spring Boot microservice**, running inside **AWS EKS**, fronted by an **API Gateway → VPC Link → NLB → EKS Service** chain.
This guide shows you exactly how to deploy, configure, and test it (you can copy-paste each step).

---

# 🧭 End-to-End Flow

```
Client (curl/Postman/browser)
   ↓
Amazon API Gateway (HTTP API)
   ↓   [VPC Link – private connection]
AWS Network Load Balancer (NLB)
   ↓
Kubernetes Service  (type=LoadBalancer)
   ↓
Spring Boot Pod(s) running in EKS
```

---

## ⚙️ Step 1 – Prepare prerequisites

Install and configure:

```bash
aws configure                         # set region, credentials
kubectl version --client
terraform -v
helm version
docker -v
```

Make sure you have a working AWS account and permissions to create EKS, VPCs, Load Balancers, and API Gateway.

---

## 🏗️ Step 2 – Create EKS cluster (Terraform)

Use the official module for simplicity:

```hcl
# main.tf
provider "aws" { region = "ap-south-1" }

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  name    = "eks-vpc"
  cidr    = "10.0.0.0/16"
  azs     = ["ap-south-1a", "ap-south-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.11.0/24", "10.0.12.0/24"]
}

module "eks" {
  source          = "terraform-aws-modules/eks/aws"
  cluster_name    = "springboot-eks"
  cluster_version = "1.29"
  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.private_subnets

  node_groups = {
    default = {
      desired_capacity = 2
      instance_types   = ["t3.medium"]
    }
  }
}

output "cluster_name" { value = module.eks.cluster_name }
```

Apply it:

```bash
terraform init
terraform apply -auto-approve
aws eks update-kubeconfig --name springboot-eks --region ap-south-1
kubectl get nodes
```

---

## 🧩 Step 3 – Install the AWS Load Balancer Controller

```bash
helm repo add eks https://aws.github.io/eks-charts
kubectl create ns kube-system
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=springboot-eks \
  --set serviceAccount.create=false \
  --set region=ap-south-1 \
  --set vpcId=$(aws eks describe-cluster --name springboot-eks --query "cluster.resourcesVpcConfig.vpcId" --output text)
```

Confirm:

```bash
kubectl get deployment -n kube-system aws-load-balancer-controller
```

---

## ☕ Step 4 – Package your Spring Boot app as Docker image

Example minimal API:

```java
@RestController
@RequestMapping("/api")
public class HelloController {
    @GetMapping("/hello")
    public String hello() { return "Hello from Spring Boot in EKS!"; }
}
```

`Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
COPY target/demo.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and push:

```bash
docker build -t my-springboot .
aws ecr create-repository --repository-name my-springboot
docker tag my-springboot:latest <account-id>.dkr.ecr.ap-south-1.amazonaws.com/my-springboot:latest
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-south-1.amazonaws.com
docker push <account-id>.dkr.ecr.ap-south-1.amazonaws.com/my-springboot:latest
```

---

## 🚀 Step 5 – Deploy Spring Boot app into EKS

`deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      containers:
      - name: springboot-app
        image: <account-id>.dkr.ecr.ap-south-1.amazonaws.com/my-springboot:latest
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: springboot-svc
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: springboot-app
```

Deploy it:

```bash
kubectl apply -f deployment.yaml
kubectl get svc springboot-svc
```

When status becomes *Active*, note the NLB DNS name (e.g., `a1234567890abcdef.elb.ap-south-1.amazonaws.com`).

---

## 🌉 Step 6 – Create API Gateway VPC Link (Terraform)

```hcl
resource "aws_apigatewayv2_vpc_link" "nlb_link" {
  name           = "springboot-vpc-link"
  subnet_ids     = module.vpc.private_subnets
  security_group_ids = [] # optional
}

resource "aws_apigatewayv2_api" "http_api" {
  name          = "springboot-http-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "springboot_integration" {
  api_id           = aws_apigatewayv2_api.http_api.id
  integration_type = "HTTP_PROXY"
  integration_uri  = "http://${aws_lb.nlb.dns_name}"   # or paste your NLB DNS
  connection_type  = "VPC_LINK"
  connection_id    = aws_apigatewayv2_vpc_link.nlb_link.id
}

resource "aws_apigatewayv2_route" "root" {
  api_id    = aws_apigatewayv2_api.http_api.id
  route_key = "GET /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.springboot_integration.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true
}

output "api_endpoint" {
  value = aws_apigatewayv2_api.http_api.api_endpoint
}
```

`terraform apply`, then check the output endpoint.

---

## 🔗 Step 7 – Test your API Gateway endpoint

```bash
curl https://<api_id>.execute-api.ap-south-1.amazonaws.com/api/hello
# → "Hello from Spring Boot in EKS!"
```

You’ve successfully exposed your internal EKS service securely through API Gateway!

---

## 🧠 Step 8 – How the Spring Boot app “knows” API Gateway

Your **Spring Boot app does not need special code** for API Gateway.
API Gateway simply forwards HTTP requests to the NLB endpoint (which routes to your service).
From Spring Boot’s perspective, the incoming request’s headers (e.g., `X-Forwarded-For`, `X-Forwarded-Proto`, `Host`) show it’s proxied — you can inspect those if you need client IP or URL rewriting.

Example in Spring:

```java
@GetMapping("/hello")
public String hello(HttpServletRequest request) {
    return "Hello from " + request.getHeader("X-Forwarded-For");
}
```

If you define multiple microservices (e.g., `/orders`, `/payments`), API Gateway can map *routes* (`GET /orders/*`, `POST /payments/*`) to different integrations (different NLBs or ALBs).

---

## 📈 Step 9 – Scaling and maintenance

| Layer       | Scaling Mechanism                                | Who Manages |
| ----------- | ------------------------------------------------ | ----------- |
| API Gateway | Auto-scales                                      | AWS         |
| NLB         | Auto-scales                                      | AWS         |
| Pods        | Horizontal Pod Autoscaler                        | You         |
| Nodes       | Cluster Autoscaler / Karpenter                   | You         |
| Spring Boot | Use readiness/liveness probes, multiple replicas | You         |

---

## 🔐 Step 10 – Secrets & Configuration

Use AWS Secrets Manager + Secrets Store CSI Driver:

1. Store DB creds in Secrets Manager.
2. Create a `SecretProviderClass` referencing it.
3. Mount it in your Deployment or sync to Kubernetes Secret.
4. Inject into Spring Boot via environment variables or `application.yml`.

Example:

```yaml
env:
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-secret
        key: password
```

---

# ✅ Summary

| Component                        | Purpose                                    | Managed by                       |
| -------------------------------- | ------------------------------------------ | -------------------------------- |
| **EKS**                          | Runs Spring Boot pods                      | AWS (control plane) + You (apps) |
| **AWS Load Balancer Controller** | Creates ALB/NLB for Services               | You                              |
| **API Gateway**                  | External API exposure, auth, rate limiting | AWS                              |
| **VPC Link**                     | Secure connection Gateway → VPC            | AWS                              |
| **Secrets Manager + CSI Driver** | Secure secret delivery                     | AWS + You                        |

---

Nice — I’ve extended the hands-on walkthrough to include **(A) API Gateway security (JWT/OAuth2 + API Key + Lambda authorizer options)**, **(B) an end-to-end CI/CD pipeline using GitHub Actions (build → ECR → kubectl deploy)**, and **(C) multi-service routing (API Gateway → different EKS services via NLBs or ALB path routing)** — with concrete Terraform/Kubernetes/Spring Boot/GitHub Actions examples you can copy & adapt.

I’ll break it into sections so you can follow each step and paste code. I’ll also call out important AWS limitations and recommended patterns and include official doc links for the heavy claims. ([AWS Documentation][1])

---

# A — API Gateway security (JWT/OAuth2, API Key, Lambda authorizer)

## 1) Recommended approaches (short)

* **JWT (OIDC/OAuth2) authorizer (HTTP APIs)** — easiest & performant. API Gateway validates tokens (iss, aud) against an OIDC issuer (Cognito, Auth0, Okta). Good for standard OAuth flows. ([AWS Documentation][1])
* **Lambda authorizer** — fully custom validation or token types; suitable when you need bespoke logic (e.g., introspect opaque tokens). Slightly higher latency. ([AWS Documentation][2])
* **API Key** — simple, cheap; useful for rate limiting and basic client identification but NOT secure for user authentication (no identity). Combine with usage plans for throttling.
* **Cognito + API Gateway** — built-in user pool support if you want AWS-managed user store and token issuance. ([AWS Documentation][3])

---

## 2) Terraform: create a JWT authorizer for an HTTP API

Minimal Terraform snippet (API Gateway V2 `aws_apigatewayv2_authorizer`) — replace issuer/audience with your IdP (e.g., Cognito or Auth0).

```hcl
resource "aws_apigatewayv2_api" "api" {
  name          = "services-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_authorizer" "jwt" {
  api_id = aws_apigatewayv2_api.api.id
  name   = "jwt-authorizer"
  authorizer_type = "JWT"

  identity_sources = ["$request.header.Authorization"]
  jwt_configuration {
    issuer = "https://your-oidc-issuer.example.com/"
    audience = ["your-audience-or-client-id"]
  }
}
```

Attach the authorizer to a route:

```hcl
resource "aws_apigatewayv2_route" "orders" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "GET /orders"
  target    = "integrations/${aws_apigatewayv2_integration.orders.id}"
  authorization_type = "JWT"                 # route-level
  authorizer_id       = aws_apigatewayv2_authorizer.jwt.id
}
```

Notes:

* HTTP API JWT authorizers validate the token signature and claims for you (iss/aud) — simpler than Lambda authorizers. ([AWS Documentation][1])

---

## 3) Spring Boot: accept and validate JWTs (resource server)

Add dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

`application.yml` (example with OIDC issuer):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-oidc-issuer.example.com/
```

Security config (optional, for route protections):

```java
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth ->
        auth.requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/orders/**").hasAuthority("SCOPE_orders:read")
            .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth2 -> oauth2.jwt());
    return http.build();
  }
}
```

This lets Spring Boot rely on the same token semantics that API Gateway validated (good defense in depth). Use scopes or custom claims for RBAC.

---

## 4) API Key or Lambda authorizer options

* **API Key**: create API key in API Gateway and set the route to require it. Not a replacement for JWT when user identity matters.
* **Lambda authorizer (Terraform)**: can be used for custom checks or opaque token introspection. Example resource: `aws_apigatewayv2_authorizer` with `authorizer_type = "REQUEST"` and `authorizer_uri` pointing to a Lambda invoke URL. ([Terraform Registry][4])

---

# B — CI/CD: GitHub Actions pipeline (build → ECR → deploy to EKS)

Here’s a practical GitHub Actions workflow that:

1. Builds Docker image
2. Logs into ECR and pushes image
3. Updates a Kubernetes `Deployment` image using `kubectl set image` (you can use `kubectl apply` with patched manifests or Helm).

Create `.github/workflows/ci-cd.yml`:

```yaml
name: CI/CD to EKS

on:
  push:
    branches: [ "main" ]

env:
  AWS_REGION: ap-south-1
  ECR_REPOSITORY: my-springboot
  IMAGE_TAG: ${{ github.sha }}

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-region: ${{ env.AWS_REGION }}
          role-to-assume: arn:aws:iam::123456789012:role/GitHubActionsECRPushRole
          role-session-name: gha-session  # (or use access_key/secret in secrets)

      - name: Login to ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1

      - name: Build Docker image
        run: |
          docker build -t $ECR_REPOSITORY:${IMAGE_TAG} .
          docker tag $ECR_REPOSITORY:${IMAGE_TAG} ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/$ECR_REPOSITORY:${IMAGE_TAG}

      - name: Push to ECR
        run: |
          docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/$ECR_REPOSITORY:${IMAGE_TAG}

      - name: Set up kubectl
        uses: azure/setup-kubectl@v4
        with:
          version: '1.29.0'

      - name: Update kubeconfig
        run: |
          aws eks update-kubeconfig --region ${AWS_REGION} --name springboot-eks

      - name: Deploy to EKS - set image
        run: |
          kubectl -n default set image deployment/springboot-app springboot-app=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/$ECR_REPOSITORY:${IMAGE_TAG}
          kubectl rollout status deployment/springboot-app -n default --timeout=120s
```

Notes / best practice:

* Use **IRSA** or an IAM role for GitHub Actions (OIDC federation) rather than long-lived secrets. AWS docs provide patterns. ([AWS Documentation][5])
* For complex charts/manifests use **Helm** or **kustomize** and run `helm upgrade --install` or `kubectl apply -f` from the workflow.
* You can add steps to run tests, security scans, and image vulnerability scans in the pipeline.

References: AWS prescriptive guidance for GitHub Actions → ECR/EKS. ([AWS Documentation][5])

---

# C — Multi-service routing (API Gateway → /employee, /orders, /payments → different EKS services)

There are two widely used patterns to route requests to multiple backend microservices running inside EKS:

### Pattern 1 — **Per-service NLB + VPC Link integrations**

* Create a **Service type=LoadBalancer** per microservice with `nlb` annotation. Each Service will create its own **NLB**.
* Create **multiple API Gateway integrations** (one per route) that use the same VPC Link (HTTP API VPC Link can integrate with multiple NLBs more easily than REST APIs). Each integration points at the specific NLB DNS name (or target ARN). ([Amazon Web Services, Inc.][6])

**Kubernetes manifests (three services)**

`orders-deploy.yaml` (deploy + svc with internal NLB):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: orders-app }
spec:
  replicas: 2
  selector: { matchLabels: { app: orders } }
  template:
    metadata: { labels: { app: orders } }
    spec:
      containers:
      - name: orders
        image: <account>.dkr.ecr.ap-south-1.amazonaws.com/orders:latest
        ports: [{ containerPort: 8080 }]
---
apiVersion: v1
kind: Service
metadata:
  name: orders-svc
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"
spec:
  type: LoadBalancer
  selector: { app: orders }
  ports:
  - port: 80
    targetPort: 8080
```

Repeat similar manifests for `payments` and `employee`. After `kubectl apply -f`, note each Service’s `status.loadBalancer.ingress[0].hostname` (the NLB DNS).

**Terraform (API Gateway integrations)** — example routes:

```hcl
# orders integration -> orders NLB
resource "aws_apigatewayv2_integration" "orders" {
  api_id = aws_apigatewayv2_api.api.id
  integration_type = "HTTP_PROXY"
  integration_uri  = "http://${var.orders_nlb_dns}"
  connection_type  = "VPC_LINK"
  connection_id    = aws_apigatewayv2_vpc_link.vpclink.id
}
resource "aws_apigatewayv2_route" "orders_route" {
  api_id = aws_apigatewayv2_api.api.id
  route_key = "ANY /orders/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.orders.id}"
}
```

**Pros:** Clean separation per service, independent scaling & certificates on ALB not necessary.
**Cons / Limits:** Creating many NLBs per service may increase cost and complexity. REST API VPC Links had earlier limits tied to single NLB but HTTP APIs are more flexible — check docs for details. ([Amazon Web Services, Inc.][6])

---

### Pattern 2 — **Single ALB (Ingress) with path-based routing** (recommended for many microservices)

* Install **AWS Load Balancer Controller** in the cluster (already done).
* Use Kubernetes `Ingress` with ALB annotations so one **ALB** handles multiple paths (`/orders/*`, `/payments/*`, `/employee/*`) and maps them to different services.
* Configure API Gateway to forward to the ALB (via VPC Link or public ALB endpoint). This is cost efficient and centralizes certs and routing. ([AWS Documentation][7])

**Ingress example:**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: services-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internal
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP":80}]'
spec:
  rules:
  - http:
      paths:
      - path: /orders/*
        pathType: Prefix
        backend:
          service:
            name: orders-svc
            port:
              number: 80
      - path: /payments/*
        pathType: Prefix
        backend:
          service:
            name: payments-svc
            port:
              number: 80
      - path: /employee/*
        pathType: Prefix
        backend:
          service:
            name: employee-svc
            port:
              number: 80
```

Then **API Gateway** integration can be a single integration to that ALB DNS, and API routes map to `ANY /orders/{proxy+}`, `ANY /payments/{proxy+}` etc., or you can use a single catch-all that passes path through.

**Pros:** simpler architecture (one ALB), L7 features (path/host routing), central cert management.
**Cons:** ALB has more L7 latency than NLB for pure L4; ALB might be public unless you choose `internal`.

---

## Choosing between ALB vs NLB patterns

* If you need path-based, host-based routing and TLS termination at L7 → **ALB Ingress**. Use this when you have many services and want a single entrypoint. ([AWS Documentation][7])
* If you need very low latency L4 + static IPs + VPC Link simplicity → **NLB per service**. Use for performance sensitive services or when using REST VPC Links with restrictions. ([Amazon Web Services, Inc.][6])

Important note: REST API VPC Link historically was single-NLB limited; HTTP APIs are more flexible but verify current limits for your use case. ([Amazon Web Services, Inc.][6])

---

# D — Putting it all together: example repo layout & recommended steps

```
infra/
  terraform/          # VPC, EKS, ALB controller IAM, API Gateway resources
  k8s/                # base manifests, ingress template, secrets providerclass
app/
  springboot/         # Spring Boot service(s) (orders, payments, employee)
  Dockerfile
.github/workflows/
  ci-cd.yml           # build -> ECR -> kubectl/helm
```

Suggested deployment steps:

1. `terraform apply` → creates VPC, EKS cluster, node groups, (optionally) API Gateway & VPC Link placeholders. ([Kubernetes Sigs][8])
2. Install AWS Load Balancer Controller (with proper IAM). ([AWS Documentation][7])
3. Build & push Docker images (locally or via CI).
4. `kubectl apply -f k8s/` to create Deployments/Services/Ingress. For NLB approach, create per-service `Service` with NLB annotation.
5. If using ALB Ingress, check the created ALB DNS and feed it to API Gateway integration.
6. Configure API Gateway routes & authorizers (JWT authorizer for protected routes). ([AWS Documentation][1])
7. Use GitHub Actions to automate steps 3–6 for production deploys.

---

# E — Additional operational tips

* **Authorization end-to-end**: Validate tokens at API Gateway (quick rejection) and again in Spring Boot (defense in depth). Use scopes for microservice access. ([AWS Documentation][1])
* **Secrets**: continue to use Secrets Manager + Secrets Store CSI driver + IRSA for pod-level secret access.
* **Observability**: export application metrics (Prometheus), CloudWatch Container Insights for EKS metrics, and logs (Fluent Bit → CloudWatch).
* **Rate limiting & abuse protection**: use API Gateway usage plans, throttling, WAF if needed.
* **Cost**: watch ALB/NLB counts — many NLBs can become expensive; ALB path routing may be cheaper for many microservices. ([Amazon Web Services, Inc.][6])

---

# Quick reference docs (official & helpful)

* API Gateway HTTP API — JWT authorizers & HTTP APIs. ([AWS Documentation][1])
* Terraform `aws_apigatewayv2_authorizer` / apigatewayv2 resources. ([Terraform Registry][4])
* AWS Load Balancer Controller / ALB Ingress docs. ([AWS Documentation][7])
* VPC Link explanation & limitations. ([Amazon Web Services, Inc.][6])
* GitHub Actions → build & push to ECR pattern. ([AWS Documentation][5])

---


[1]: https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-jwt-authorizer.html?utm_source=chatgpt.com "Control access to HTTP APIs with JWT authorizers in API ..."
[2]: https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html?utm_source=chatgpt.com "Use API Gateway Lambda authorizers"
[3]: https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions-authorizer.html?utm_source=chatgpt.com "x-amazon-apigateway-authorizer object"
[4]: https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/apigatewayv2_authorizer?utm_source=chatgpt.com "aws_apigatewayv2_authorizer | Resources | hashicorp/aws"
[5]: https://docs.aws.amazon.com/prescriptive-guidance/latest/patterns/build-and-push-docker-images-to-amazon-ecr-using-github-actions-and-terraform.html?utm_source=chatgpt.com "Build and push Docker images to Amazon ECR using ..."
[6]: https://aws.amazon.com/blogs/compute/understanding-vpc-links-in-amazon-api-gateway-private-integrations/?utm_source=chatgpt.com "Understanding VPC links in Amazon API Gateway private ..."
[7]: https://docs.aws.amazon.com/eks/latest/userguide/alb-ingress.html?utm_source=chatgpt.com "Route application and HTTP traffic with Application Load ..."
[8]: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/guide/ingress/annotations/?utm_source=chatgpt.com "Ingress annotations - AWS Load Balancer Controller"
