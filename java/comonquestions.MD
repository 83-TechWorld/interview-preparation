# Java & Spring Boot — Interview Q&A (GitHub Markdown)

> A concise but practical Q&A covering common Java, Spring Boot, Hibernate/JPA, Docker/Kubernetes and testing topics. Each question includes: What it is, why it matters, and a compact Java/Spring example where relevant.

---

## 1) Custom exception handling

**What & why:** Create application-specific exceptions to represent business errors (e.g., `OrderNotFoundException`). Improves readability, control, and maps to HTTP responses.

**Example:**

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) { super("Order not found: " + id); }
}
```

**Spring controller advice:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleOrderNotFound(OrderNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
```

---

## 2) How exceptions are handled in Spring Boot

Centralized handling via `@ControllerAdvice`/`@ExceptionHandler`. Spring translates some framework exceptions automatically. Use `ResponseEntityExceptionHandler` for REST-friendly responses.

**Tip:** Use `@ResponseStatus` on simple exceptions for concise mapping.

---

## 3) Compile-time vs Run-time exceptions

* **Compile-time (checked)**: must be declared or handled (e.g., `IOException`). Used for recoverable conditions.
* **Runtime (unchecked)**: `RuntimeException` subclasses (e.g., `NullPointerException`). Caller not forced to handle.

---

## 4) How `HashMap` / `HashSet` work internally

`HashMap` buckets by hashed key; resolves collisions via linked lists or balanced trees (since Java 8). `HashSet` uses `HashMap` internally.

**Key operations complexity:** average O(1) for get/put; worst-case O(n) if many collisions.

---

## 5) Java 8 features

Lambdas, Streams API, `Optional`, new Date/Time API (`java.time`), default/interface static methods, `Collectors`.

**Example:**

```java
List<String> filtered = names.stream()
    .filter(n -> n.startsWith("A"))
    .collect(Collectors.toList());
```

---

## 6) In-built functional interfaces

`Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`.

**Example:**

```java
Function<String,Integer> len = String::length;
Predicate<String> isEmpty = String::isEmpty;
Consumer<String> print = System.out::println;
Supplier<String> id = () -> UUID.randomUUID().toString();
```

---

## 7) Spring bean scopes

`singleton`, `prototype`, `request`, `session`, `application`, `websocket`.

**Example:** `@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)`

---

## 8) How `@Transactional` works in Spring

`@Transactional` uses AOP proxies to start/commit/rollback transactions through a `PlatformTransactionManager` (`JpaTransactionManager`, `DataSourceTransactionManager`). Rollback on unchecked exceptions by default.

**Caveat:** Self-invocation bypasses proxies — transaction won't be applied.

---

## 9) First-level vs Second-level cache (Hibernate/JPA)

* **L1**: per `EntityManager` (session) cache; always-on.
* **L2**: optional, shared across sessions (providers: Ehcache, Infinispan). Use for read-heavy data; manage invalidation carefully.

---

## 10) Handling complex queries in Hibernate/JPA

Options: JPQL, Criteria API (type-safe dynamic queries), native SQL, Spring Data `@Query`.

**Criteria example:**

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> q = cb.createQuery(User.class);
Root<User> r = q.from(User.class);
q.select(r).where(cb.like(r.get("name"), "%A%"));
List<User> u = em.createQuery(q).getResultList();
```

---

## 11) How `EntityManager` load or fetch works

* `find()` — immediate load (checks L1 first).
* `getReference()` — lazy proxy, may not hit DB until accessed.

Fetch types on associations: `EAGER` (immediate) vs `LAZY` (on access).

---

## 12) Transaction propagations in Spring (with examples)

* `REQUIRED` (default): join existing or create new.
* `REQUIRES_NEW`: suspend existing and create new.
* `SUPPORTS`: run in tx if present.
* `NOT_SUPPORTED`: run without tx.
* `MANDATORY`: must have existing tx.
* `NEVER`: must not run inside tx.
* `NESTED`: uses savepoints (when supported).

**Example:**

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void innerNew() { /* new transaction */ }
```

---

## 13) Fail-fast vs Fail-safe iterators

* **Fail-fast**: throw `ConcurrentModificationException` on structural concurrent modification (e.g., `ArrayList` iterator).
* **Fail-safe**: iterate over a snapshot or use concurrent collections (`CopyOnWriteArrayList`, `ConcurrentHashMap`), no exception but might not show latest changes.

---

## 14) Differences: `List` vs `Set` vs `Map` and complexity

* **List**: ordered, allows duplicates (ArrayList: get O(1), add amortized O(1), remove O(n)).
* **Set**: no duplicates (HashSet average O(1)).
* **Map**: key->value pairs (HashMap average O(1); TreeMap O(log n)).

---

## 15) Evolution: Java 8 → Java 25 (high level)

Highlights: Lambdas/Streams (8), Modules (9), `var` (10), Records/Text blocks (14/15), Sealed classes/Pattern matching (16–17), Virtual threads & structured concurrency (21), ongoing Panama/Project Loom/Panama improvements to 25.

---

## 16) `@Service` vs `@Component` vs `@Repository`

* `@Component`: generic stereotype.
* `@Service`: semantic service layer.
* `@Repository`: persistence layer + exception translation.

---

## 17) `Callable` vs `Runnable`

* `Runnable`: `void run()` — no return, no checked exceptions.
* `Callable<V>`: `V call()` — returns value, can throw checked exceptions. Used with `ExecutorService.submit()`.

---

## 18) JVM heap structure & Garbage Collection

Heap: Young (Eden + Survivors) and Old generation; Metaspace holds class metadata. Collectors: Serial, Parallel, CMS (older), G1 (common), ZGC/Shenandoah (low-pause). Minor GC for young gen; Full GC for whole heap.

---

## 19) Kubernetes Volumes

Provide persistent/shared storage for pods. Types include `emptyDir`, `hostPath`, `persistentVolumeClaim` (PVC), `configMap`, `secret` volumes.

**YAML snippet:**

```yaml
volumes:
- name: data
  persistentVolumeClaim:
    claimName: my-pvc
```

---

## 20) Using Secrets in Kubernetes

Store sensitive data, mount as files or env vars. Use `kubectl create secret` or declarative YAML. Enable encryption at rest and RBAC.

```bash
kubectl create secret generic db-creds --from-literal=username=admin --from-literal=password=s3cr3t
```

---

## 21) Nodes, Pods, Containers in Kubernetes

* **Node**: worker VM/host running kubelet.
* **Pod**: smallest deployable unit (one or more containers sharing network/volumes).
* **Container**: runtime instance (containerd, Docker shim earlier).

Controllers and scheduler place pods on nodes; kubelet ensures desired state.

---

## 22) How Docker works internally & image tagging

Images are layered read-only filesystems; each Dockerfile instruction adds a layer. `docker tag` assigns `repo:tag` names. Containers have a writable layer atop image layers.

**Dockerfile example:**

```dockerfile
FROM eclipse-temurin:17-jre
COPY target/app.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## 23) How scalability is achieved

Horizontal scaling (add instances/pods), vertical scaling (bigger machines), caching (Redis, CDN), DB replication/sharding, async processing (queues), autoscaling (HPA).

---

## 24) GitHub Actions — how it works + deploy steps (including Sonar & Cucumber)

Workflows (YAML) react to events. Typical pipeline: checkout → build → unit tests → static analysis (Sonar) → build image → push → deploy.

**Snippet for Sonar + build + deploy to k8s:**

```yaml
- name: SonarQube Scan
  uses: SonarSource/sonarcloud-github-action@v1
- name: Build Docker
  run: docker build -t ghcr.io/${{ github.repository }}/app:${{ github.sha }} .
- name: Push
  run: docker push ghcr.io/${{ github.repository }}/app:${{ github.sha }}
- name: Deploy to K8s
  uses: azure/k8s-deploy@v3
  with: {manifests: 'k8s/deployment.yaml'}
```

Run Cucumber tests in a separate integration job (start test environment, run `mvn -Dcucumber.options="..." test`).

---

## 25) JUnit & Mockito (and other assertions)

* **JUnit 5**: `@Test`, `@BeforeEach`, assertions via `Assertions`.
* **Mockito**: create mocks, `when(...).thenReturn(...)`, `verify(...)`.
* **Other assertion libraries**: AssertJ (fluent), Hamcrest.

**Example:**

```java
@ExtendWith(MockitoExtension.class)
class SvcTest{
  @Mock Repo repo; @InjectMocks Svc svc;
  @Test void t(){ when(repo.find("x")).thenReturn(new Item("x")); assertEquals("x", svc.getName("x")); }
}
```

---

## 26) Hibernate CriteriaBuilder & Pagination

Use CriteriaBuilder for dynamic queries; pagination via `setFirstResult()` and `setMaxResults()` or Spring Data `Pageable`.

---

## 27) Fetching: LAZY vs EAGER

`EAGER` loads associations immediately; `LAZY` loads on access via proxies. Prefer `LAZY` for collections and use `JOIN FETCH` or DTO projections to avoid N+1 selects.

---

## Final notes & fixes

* I fixed numbering, removed duplicate numbering, and ensured every topic you listed is present as its own section.
* If you'd like deeper code samples (full Spring Boot project), infra YAMLs, or a GitHub repo zip with examples (transactions, propagation demos, L2 cache config, criteriabuilder pagination), tell me which sections to expand and I'll add them directly into the canvas as separate files.

---

