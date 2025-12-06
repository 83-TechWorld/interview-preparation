# 📘 **Dependency Injection, Spring Bean Scopes & Spring Transaction Propagation — Full Documentation (GitHub Markdown)**

This document gives a **clear, production-level explanation** of:

* What is **Dependency Injection (DI)**
* Types of DI
* Spring **Bean Scopes**
* Spring **Transaction Propagation Levels**
* Real-world examples
* Sample code (Java + Spring Boot)

---

# ✨ **1. What is Dependency Injection (DI)?**

### **Definition**

**Dependency Injection** is a design pattern where **objects do not create their own dependencies**.
Instead, dependencies are **provided (injected)** by an external container (Spring IoC container).

### 🔥 Example (Without DI – bad)

```java
public class OrderService {
    private PaymentService paymentService = new PaymentService(); // Tightly coupled
}
```

### ✔️ Example (With DI – good)

```java
@Service
public class OrderService {
    private final PaymentService paymentService;

    @Autowired
    public OrderService(PaymentService paymentService) {  // Injected
        this.paymentService = paymentService;
    }
}
```

### Why DI?

* Removes **tight coupling**
* Improve **testability** (mocking becomes easy)
* Allows **loose coupling**
* Helps with **scalability**
* Spring manages lifecycle automatically

---

# 🧩 **2. Types of Dependency Injection**

### 1️⃣ **Constructor Injection** (Recommended)

```java
public class A {
   private final B b;

   @Autowired
   public A(B b) { this.b = b; }
}
```

* Immutable
* Required dependencies are clear
* Best for **production code**

---

### 2️⃣ **Setter Injection**

```java
public class A {
    private B b;

    @Autowired
    public void setB(B b) { this.b = b; }
}
```

Use when dependency is **optional or replaceable**.

---

### 3️⃣ **Field Injection** (Not recommended)

```java
@Autowired
private B b;
```

Problems:

* Difficult to test
* No immutability
* Hidden dependencies

---

# 🟦 **3. Spring Bean Scopes**

Spring beans have **lifecycles**. Scope defines **how long the bean lives**.

---

## **Singleton (Default)**

```
One instance per Spring container.
```

```java
@Scope("singleton")
@Service
public class MyService {}
```

✔ Best for stateless services
❌ Do **not** store request-specific data

---

## **Prototype**

```
New bean instance for every request.
```

```java
@Scope("prototype")
@Component
public class PrototypeBean {}
```

✔ Good for stateful or expensive objects
❌ Spring does NOT manage full lifecycle

---

## **Request Scope (Web only)**

```
One instance per HTTP request.
```

```java
@Scope("request")
@Component
public class RequestBean {}
```

✔ Holds data specific to single request (ex: tracking request ID)

---

## **Session Scope**

```
One instance per user session.
```

```java
@Scope("session")
@Component
public class SessionBean {}
```

✔ Good for login sessions, user carts

---

## **Application Scope**

```
One instance for entire web application.
```

```java
@Scope("application")
@Component
public class AppBean {}
```

---

## **WebSocket Scope**

```
One instance per WebSocket session.
```

```java
@Scope("websocket")
@Component
public class WebSocketBean {}
```

---

# 🟨 **4. Spring Transactions (@Transactional)**

## ✔ Why transactions?

To ensure **ACID**:

* **Atomicity** – all succeed or all fail
* **Consistency** – data remains valid
* **Isolation** – transactions don’t affect each other
* **Durability** – data persists

---

# 🟣 **5. Transaction Propagation Levels**

Propagation defines **how Spring handles existing transactions**.

---

## **1️⃣ REQUIRED (Default)**

```
If a transaction exists → join it  
If not → create a new one
```

```java
@Transactional(propagation = Propagation.REQUIRED)
public void processOrder() {}
```

✔ Most common
✔ Ensures everything runs inside one transaction

---

## **2️⃣ REQUIRES_NEW**

```
Always creates a new transaction.
Suspends the existing one.
```

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logTransaction() {}
```

✔ Perfect for logging or audit tables
✔ Even if main transaction rolls back, logs stay

---

## **3️⃣ SUPPORTS**

```
If transaction exists → join  
If none exists → run without transaction
```

Best for:

* Read-only operations
* Optional transaction use

---

## **4️⃣ NOT_SUPPORTED**

```
Always run without transaction.
Suspends existing transaction.
```

Used for:

* Long-running operations
* Non-transaction-safe calls

---

## **5️⃣ MANDATORY**

```
Must run inside an existing transaction.
If none exists → error
```

Used for strict business rules.

---

## **6️⃣ NEVER**

```
Must NOT run in a transaction.
Throws exception if one exists.
```

Rarely used.

---

## **7️⃣ NESTED**

```
Creates a nested transaction inside a parent.
Can roll back independently.
```

Works only with JDBC (not JPA).

Example:

```java
@Transactional(propagation = Propagation.NESTED)
public void childOperation() {}
```

---

# 🟩 **6. Transaction Isolation Levels**

Defines how concurrent transactions see database changes.

| Level                                     | Dirty Read | Non-Repeatable Read | Phantom Read |
| ----------------------------------------- | ---------- | ------------------- | ------------ |
| **READ_UNCOMMITTED**                      | Yes        | Yes                 | Yes          |
| **READ_COMMITTED** (default for most DBs) | No         | Yes                 | Yes          |
| **REPEATABLE_READ**                       | No         | No                  | Yes          |
| **SERIALIZABLE**                          | No         | No                  | No           |

Example:

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void loadData() {}
```

---

# 🛠 **7. Real Example — Spring Boot Service Using DI + Transactions**

```java
@Service
public class OrderService {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void placeOrder(Order order) {
        orderRepository.save(order);
        paymentService.process(order);
    }
}
```

---

# 🟧 **8. Example: Logging With REQUIRES_NEW**

```java
@Service
public class LoggingService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMessage(String message) {
        // saved even if parent rollback
        logRepository.save(new LogEntry(message));
    }
}
```

---

# 🟦 **9. DI + Bean Scope Example**

```java
@Component
@Scope("request")
public class RequestTracker {
    private final String requestId = UUID.randomUUID().toString();
}
```

Injected into services:

```java
@Service
public class UserService {

    private final RequestTracker tracker;

    public UserService(RequestTracker tracker) {
        this.tracker = tracker;
    }
}
```

Each HTTP call gets a fresh `RequestTracker`.

---

# 🧭 Summary Table

| Topic                       | Meaning                                                |
| --------------------------- | ------------------------------------------------------ |
| **Dependency Injection**    | Spring provides required objects automatically         |
| **Bean Scopes**             | Defines how long a Spring bean lives                   |
| **Transaction Propagation** | Defines how methods behave inside/outside transactions |
| **Isolation Levels**        | Defines how concurrent transactions read/write data    |

---
