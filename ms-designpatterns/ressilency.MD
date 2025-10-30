# Microservices Resiliency Patterns — a practical GitHub-ready guide

> This document explains core resiliency (fault-tolerance) patterns used in microservice architectures: **what problem each pattern solves, how it works, typical libraries, a concrete use-case, and a Java Spring Boot example** you can drop into a project. Copy-paste as `README.md` to your repo.

---

## Table of contents

1. [Intro: Why resiliency matters](#intro-why-resiliency-matters)
2. [Circuit Breaker](#circuit-breaker)
3. [Retry (with backoff)](#retry-with-backoff)
4. [Timeout / Time Limiter](#timeout--time-limiter)
5. [Bulkhead](#bulkhead)
6. [Rate Limiter](#rate-limiter)
7. [Fallback](#fallback)
8. [Backpressure (Reactive)](#backpressure-reactive)
9. [Combining patterns + monitoring & testing](#combining-patterns--monitoring--testing)
10. [Appendix: useful libraries & links](#appendix-useful-libraries--links)

---

## Intro — Why resiliency matters

Microservices communicate across networks and depend on other services. Failures are inevitable (latency spikes, partial outages, network partitions). Resiliency patterns let systems *fail fast*, *isolate* failures, *recover*, and *gracefully degrade* instead of collapsing.

(Recommended stable libraries for Java/Spring Boot: **Resilience4j**, **Spring Retry**, **Bucket4j**, **Project Reactor / Spring WebFlux**; Hystrix is deprecated—prefer Resilience4j or Spring Cloud Circuit Breaker.) ([Baeldung on Kotlin][1])

---

## Circuit Breaker

### Problem

Repeated calls to a failing downstream service waste resources and increase latency. A downstream failure can cascade and bring the whole system down.

### Solution

Circuit Breaker trips after a configurable number of failures (or failure rate), preventing further calls for a *sleep window*. After that window it allows a probe call to check recovery. This prevents repeated attempts while the remote service is unhealthy.

### When to use / Example use-case

Use when calling third-party APIs or unstable internal services (e.g., payment gateway). If the gateway returns 5xx repeatedly, the circuit opens and your service avoids calling it until it's likely recovered.

### Libraries

* **Resilience4j** (recommended): circuit-breaker module, works well with Spring Boot.
* **Spring Cloud Circuit Breaker**: abstraction layer over implementations (Resilience4j, Sentinel, Hystrix). ([Baeldung on Kotlin][1])

### Spring Boot example (Resilience4j — annotation style)

`pom.xml` (core deps)

```xml
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
  <version>2.0.0</version> <!-- check latest -->
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

`application.yml`

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        registerHealthIndicator: true
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

`PaymentClient.java` (calling downstream)

```java
@Service
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://payment-gateway").build();
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public Mono<String> charge(CardInfo card) {
        return webClient.post()
            .uri("/charge")
            .bodyValue(card)
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> paymentFallback(CardInfo card, Throwable t) {
        return Mono.just("Payment service unavailable; please try later");
    }
}
```

Notes: use metrics (Micrometer) to monitor circuit state and tune thresholds. ([Baeldung on Kotlin][1])

---

## Retry (with backoff)

### Problem

Some failures are transient (brief network blips, temporary throttling). A single retry — often with backoff — can succeed.

### Solution

Automatically re-invoke failed operations a limited number of times, optionally with delay/backoff and jitter to avoid thundering herds.

### When to use / Example use-case

Retry idempotent reads or safe operations (GETs). Be careful: retries on non-idempotent writes can cause duplicates — add idempotency where needed.

### Libraries

* **Spring Retry** (`@Retryable`, `RetryTemplate`) — integrates with Spring.
* **Resilience4j Retry** — module that integrates with circuit-breaker and time-limiter. ([Baeldung on Kotlin][2])

### Spring Boot example (`@Retryable` from Spring Retry)

`pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.retry</groupId>
  <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

`@Configuration`

```java
@EnableRetry
@Configuration
public class RetryConfig {}
```

`SomeService.java`

```java
@Service
public class SomeService {

  @Retryable(value = {IOException.class}, maxAttempts = 4, backoff = @Backoff(delay = 200, multiplier = 2))
  public String callExpensiveRemote() throws IOException {
    // remote call that might throw IOException
  }

  @Recover
  public String recover(IOException e) {
    return "Fallback after retries";
  }
}
```

Notes: combine retries with circuit breakers to avoid retrying while a circuit is open. Use exponential backoff and jitter to reduce synchronized retries. ([Baeldung on Kotlin][2])

---

## Timeout / TimeLimiter

### Problem

Downstream calls may hang or take too long — you need to fail fast and reclaim threads.

### Solution

Attach a timeout to remote calls (per-call). If the call overruns, cancel it and treat as failure (often combined with fallback).

### When to use / Example use-case

Setting a 2s timeout for an HTTP call to a user-profile service so slow responses don't exhaust request threads.

### Libraries

* **Resilience4j TimeLimiter**: integrates with circuit breaker and retry.
* For reactive stacks, use Reactor's `timeout()` operator. ([Baeldung on Kotlin][1])

### Spring Boot example (Resilience4j + CompletableFuture style)

`Service.java`

```java
@TimeLimiter(name = "profileService")
@CircuitBreaker(name = "profileService", fallbackMethod = "profileFallback")
public CompletableFuture<String> getProfile(String userId) {
    return CompletableFuture.supplyAsync(() ->
        restTemplate.getForObject("http://profile-service/users/" + userId, String.class)
    );
}

public CompletableFuture<String> profileFallback(String userId, Throwable t) {
    return CompletableFuture.completedFuture("default-profile");
}
```

`application.yml`

```yaml
resilience4j:
  timelimiter:
    instances:
      profileService:
        timeoutDuration: 2s
```

Important: ensure threads used for blocking calls are NOT the main request thread (use thread pool) or prefer non-blocking WebClient.

---

## Bulkhead

### Problem

A slow or misbehaving downstream operation can consume all threads/resources and take the whole service down.

### Solution

Isolate resources (threads, semaphores) for different categories of work so one failing part doesn't starve others — similar to ship bulkheads.

Two common types:

* **Semaphore Bulkhead**: limits concurrent calls on the same thread pool (lightweight).
* **Thread-Pool Bulkhead**: isolates blocking calls into a separate thread pool.

### When to use / Example use-case

Isolate heavy image-processing calls (thread-pool bulkhead) from normal API requests.

### Libraries

* **Resilience4j Bulkhead** provides both semaphore and thread-pool bulkheads. ([resilience4j][3])

### Spring Boot example (Resilience4j Bulkhead)

`application.yml`

```yaml
resilience4j:
  bulkhead:
    instances:
      imageService:
        maxConcurrentCalls: 10   # semaphore
      dbImportService:
        threadPoolBulkhead:
          maxThreadPoolSize: 5
          coreThreadPoolSize: 2
          queueCapacity: 50
```

`ImageService.java`

```java
@Bulkhead(name = "imageService", fallbackMethod = "imageFallback")
public String processImage(File f) {
  // CPU/IO heavy work
}
```

`DbImportService.java` (thread-pool bulkhead)

```java
@ThreadPoolBulkhead(name = "dbImportService", fallbackMethod = "importFallback")
public CompletableFuture<String> importData(...) { ... }
```

Notes: choose semaphore bulkhead for non-blocking/reactive services; thread-pool bulkheads for blocking calls.

---

## Rate Limiter

### Problem

A surging client or abusive actor can overwhelm your service or downstream services.

### Solution

Limit requests per client or globally over a time window (token-bucket or leaky-bucket). Throttle or reject excess requests gracefully.

### When to use / Example use-case

Public APIs often enforce per-API-key or per-IP limits (e.g., 1000 reqs/hour). Protects backend and downstream APIs.

### Libraries

* **Bucket4j** (token-bucket, supports Redis backing for distributed rate-limiting).
* **Resilience4j RateLimiter** (simple local token-bucket).
* **Guava RateLimiter** (client-side, simple). ([Baeldung on Kotlin][4])

### Spring Boot example (Bucket4j, per IP)

`pom.xml`

```xml
<dependency>
  <groupId>com.github.vladimir-bukhtoyarov</groupId>
  <artifactId>bucket4j-core</artifactId>
  <version>8.0.0</version>
</dependency>
```

`RateLimiterFilter.java`

```java
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

  private final Cache<String, Bucket> cache = Caffeine.newBuilder()
      .expireAfterAccess(1, TimeUnit.HOURS)
      .build();

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String ip = req.getRemoteAddr();
    Bucket bucket = cache.get(ip, k -> newBucket());
    if (bucket.tryConsume(1)) {
      chain.doFilter(req, res);
    } else {
      res.setStatus(429);
      res.getWriter().write("Too many requests");
    }
  }

  private Bucket newBucket() {
    Refill refill = Refill.intervally(10, Duration.ofMinutes(1));
    Bandwidth limit = Bandwidth.classic(10, refill);
    return Bucket4j.builder().addLimit(limit).build();
  }
}
```

For distributed systems, use Bucket4j + Redis as a shared backend. ([innoq.com][5])

---

## Fallback

### Problem

When a remote call fails, you still need to serve a response gracefully (possibly degraded) instead of propagating errors.

### Solution

Provide an alternative result (cached or default) when the main call fails. Fallbacks are usually quick and safe.

### When to use / Example use-case

If product recommendations service is down, return a "top-sellers" cached list instead of an error page.

### Libraries / Approach

* Implement in code as a fallback method (Resilience4j supports `fallbackMethod`), or use `@Recover` with Spring Retry. Use caching (e.g., Caffeine/Redis) for stale-but-useful responses.

### Example (Resilience4j fallback)

See `PaymentClient.paymentFallback()` in the Circuit Breaker example above — it returns a safe default.

---

## Backpressure (Reactive systems)

### Problem

A fast producer overwhelms a slower consumer (or downstream), causing memory growth or crashes.

### Solution

Use Reactive Streams (publisher/subscriber) with backpressure semantics: the consumer requests N items it can handle. Producers must honor demand, or you apply buffering/drop strategies, sampling, or rate-limiting.

### When to use / Example use-case

Streaming endpoints, event processing pipelines, or when integrating with systems that may lag.

### Libraries & Tech

* **Project Reactor** (used by Spring WebFlux) supports backpressure operators (`onBackpressureBuffer`, `onBackpressureDrop`, `limitRate`, `request()`), and `Flux`/`Mono` semantics. RxJava also supports backpressure. ([Baeldung on Kotlin][6])

### Spring WebFlux example (apply backpressure)

```java
@GetMapping("/events")
public Flux<ServerSentEvent<Event>> streamEvents() {
    return eventPublisher.getEvents() // returns Flux<Event>
        .onBackpressureBuffer(1000, // buffer up to 1000
            dropped -> log.warn("Dropped event: {}", dropped),
            BufferOverflowStrategy.DROP_OLDEST)
        .map(event -> ServerSentEvent.builder(event).build());
}
```

Or use `limitRate()` to control consumption:

```java
flux.limitRate(256);
```

Notes: prefer reactive for streaming/high-concurrency services. Use `timeout` + `retry` cautiously.

---

## Combining patterns — recommended approaches

* **Circuit Breaker + Retry + TimeLimiter**: retry + timeout are enclosed by circuit breaker so repeated retries don't overwhelm a failing service. Example order: TimeLimiter (fail fast) → Retry (with backoff) → CircuitBreaker (trip if many failures).
* **Bulkhead + RateLimiter**: bulkheads isolate resource usage, rate limiters control request rate externally.
* **Fallback + Cache**: Fallback often returns a cached/stale result from Redis or Caffeine for graceful degradation.
* **Monitoring**: capture metrics for attempts, failures, open/closed states, latency percentiles (Micrometer + Prometheus + Grafana). Resilience4j exposes metrics that integrate with Micrometer. ([Baeldung on Kotlin][1])

---

## Testing resiliency patterns

* **Unit tests**: mock downstream to simulate responses and verify fallback/circuit states.
* **Integration tests**: spin up a test double (WireMock) to simulate latency/failures.
* **Chaos engineering**: run fault injection (latency, kills) in staging to verify patterns behave as intended.

---

## Practical checklist for adopting resiliency

1. Choose libraries: **Resilience4j** (all-in-one) + **Bucket4j** for distributed rate-limiting. ([Baeldung on Kotlin][1])
2. For blocking I/O, use thread-pool bulkheads or offload to worker pools. For high concurrency, prefer reactive (WebFlux) with backpressure. ([reflectoring.io][7])
3. Prefer idempotency + deduplication when enabling retries for non-idempotent operations.
4. Monitor all patterns (circuit state, retry counts, queue sizes, dropped events).
5. Start conservative: simple timeouts + retries + rate limits, then add circuit breakers and bulkheads.

---

## Appendix — Useful libraries & links

* Resilience4j docs (circuit-breaker, retry, bulkhead, timelimiter): Resilience4j docs. ([resilience4j][3])
* Spring Retry guide (annotations, RetryTemplate): Baeldung Spring Retry. ([Baeldung on Kotlin][2])
* Bucket4j (token-bucket, Redis-backed for distributed limiting): Bucket4j guides. ([Baeldung on Kotlin][4])
* Backpressure & Reactor: Spring WebFlux backpressure guides. ([Baeldung on Kotlin][6])
* Hystrix history / migration guidance: Hystrix is deprecated; prefer Resilience4j or Spring Cloud Circuit Breaker. ([InfoQ][8])

---

## Example repo layout (suggested)

```
/resiliency-examples
  /circuit-breaker
    src/... (Resilience4j example)
  /retry
    src/... (Spring Retry example)
  /rate-limiter
    src/... (Bucket4j example)
  /reactive-backpressure
    src/... (WebFlux example)
  README.md  <-- this document
```

---

## Quick reference cheat-sheet

* **Circuit Breaker** — avoid cascading failures; use Resilience4j.
* **Retry** — handle transient failures; use Spring Retry or Resilience4j Retry.
* **Timeout** — fail-fast long calls; use TimeLimiter or Reactor `.timeout()`.
* **Bulkhead** — partition resources; Resilience4j bulkhead or thread pools.
* **Rate Limiter** — control request rate; use Bucket4j for distributed limits.
* **Fallback** — safe degraded responses; implement via `fallbackMethod` or `@Recover`.
* **Backpressure** — handle faster producers; use Project Reactor operators.

---