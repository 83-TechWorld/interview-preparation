Below is a **clear, real-world explanation of the most commonly used design patterns**, with **why we use them**, **where they are used in production systems**, and **one simple Java example for each**.
I’ve kept the examples **practical**, not textbook-heavy.

---

# 🎯 Most Used Design Patterns (Real-World + Java)

---

## 1️⃣ Factory Pattern

### 🔹 What problem it solves

You want to **create objects without exposing creation logic** and decide **which object to create at runtime**.

### 🔹 Real-world use cases

* Payment gateway selection (UPI / Card / NetBanking)
* Notification sender (Email / SMS / Push)
* Logger creation (File / DB / Cloud)

### 🔹 When to use

* Object creation depends on input/config
* You don’t want `new` scattered everywhere

### 🔹 Java Example: Payment Factory

```java
interface Payment {
    void pay();
}

class UpiPayment implements Payment {
    public void pay() {
        System.out.println("Paid using UPI");
    }
}

class CardPayment implements Payment {
    public void pay() {
        System.out.println("Paid using Card");
    }
}

class PaymentFactory {
    public static Payment getPayment(String type) {
        if ("UPI".equalsIgnoreCase(type)) return new UpiPayment();
        if ("CARD".equalsIgnoreCase(type)) return new CardPayment();
        throw new IllegalArgumentException("Invalid payment type");
    }
}

public class FactoryDemo {
    public static void main(String[] args) {
        Payment payment = PaymentFactory.getPayment("UPI");
        payment.pay();
    }
}
```

✔ Used heavily in **Spring**, **Hibernate**, **AWS SDKs**

---

## 2️⃣ Abstract Factory Pattern

### 🔹 What problem it solves

Create **families of related objects** without specifying concrete classes.

> Factory = one product
> Abstract Factory = **multiple related products**

### 🔹 Real-world use cases

* UI themes (Dark Button + Dark Checkbox)
* Cloud provider SDKs (AWS / Azure / GCP)
* Database drivers (MySQL + MySQLConnection)

### 🔹 Java Example: Cloud Resources Factory

```java
interface Compute {
    void createVM();
}

interface Storage {
    void createBucket();
}

class AwsCompute implements Compute {
    public void createVM() {
        System.out.println("AWS EC2 created");
    }
}

class AwsStorage implements Storage {
    public void createBucket() {
        System.out.println("AWS S3 bucket created");
    }
}

interface CloudFactory {
    Compute createCompute();
    Storage createStorage();
}

class AwsFactory implements CloudFactory {
    public Compute createCompute() {
        return new AwsCompute();
    }
    public Storage createStorage() {
        return new AwsStorage();
    }
}

public class AbstractFactoryDemo {
    public static void main(String[] args) {
        CloudFactory factory = new AwsFactory();
        factory.createCompute().createVM();
        factory.createStorage().createBucket();
    }
}
```

✔ Used in **UI frameworks**, **cross-platform SDKs**

---

## 3️⃣ Strategy Pattern

### 🔹 What problem it solves

You want to **change behavior at runtime** without `if-else` or modifying code.

### 🔹 Real-world use cases

* Discount calculation
* Tax calculation (Country-wise)
* Sorting algorithms
* Authentication methods

### 🔹 Java Example: Discount Strategy

```java
interface DiscountStrategy {
    double applyDiscount(double amount);
}

class NoDiscount implements DiscountStrategy {
    public double applyDiscount(double amount) {
        return amount;
    }
}

class FestivalDiscount implements DiscountStrategy {
    public double applyDiscount(double amount) {
        return amount * 0.8;
    }
}

class BillingService {
    private DiscountStrategy strategy;

    public BillingService(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double finalAmount(double amount) {
        return strategy.applyDiscount(amount);
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        BillingService billing =
            new BillingService(new FestivalDiscount());
        System.out.println(billing.finalAmount(1000));
    }
}
```

✔ Used in **Spring Security**, **pricing engines**, **rule engines**

---

## 4️⃣ Singleton Pattern

### 🔹 What problem it solves

Ensure **only one instance** exists.

### 🔹 Real-world use cases

* Configuration manager
* Connection pool
* Cache manager

### 🔹 Java Example (Thread-Safe)

```java
class ConfigManager {
    private static volatile ConfigManager instance;

    private ConfigManager() {}

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
}
```

✔ Used in **logging**, **caching**, **Spring beans (by default)**

---

## 5️⃣ Builder Pattern

### 🔹 What problem it solves

Create **complex objects step-by-step** without huge constructors.

### 🔹 Real-world use cases

* HTTP requests
* User profile creation
* Lombok `@Builder`

### 🔹 Java Example: User Builder

```java
class User {
    private String name;
    private int age;
    private String email;

    private User(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.email = builder.email;
    }

    static class Builder {
        private String name;
        private int age;
        private String email;

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder age(int age) {
            this.age = age;
            return this;
        }

        Builder email(String email) {
            this.email = email;
            return this;
        }

        User build() {
            return new User(this);
        }
    }
}

public class BuilderDemo {
    public static void main(String[] args) {
        User user = new User.Builder()
                .name("Satish")
                .age(35)
                .email("satish@gmail.com")
                .build();
    }
}
```

✔ Used in **Spring**, **Kafka configs**, **AWS SDK**

---

## 6️⃣ Observer Pattern

### 🔹 What problem it solves

Notify multiple objects **when state changes**.

### 🔹 Real-world use cases

* Event systems
* Notifications
* Kafka consumers
* UI updates

### 🔹 Java Example: Order Status Update

```java
interface Observer {
    void update(String status);
}

class EmailNotifier implements Observer {
    public void update(String status) {
        System.out.println("Email: Order is " + status);
    }
}

class Order {
    private List<Observer> observers = new ArrayList<>();

    void addObserver(Observer observer) {
        observers.add(observer);
    }

    void setStatus(String status) {
        observers.forEach(o -> o.update(status));
    }
}

public class ObserverDemo {
    public static void main(String[] args) {
        Order order = new Order();
        order.addObserver(new EmailNotifier());
        order.setStatus("SHIPPED");
    }
}
```

✔ Used in **Kafka**, **RxJava**, **Spring Events**

---

## 📌 Summary Table

| Pattern          | Main Purpose       | Real Usage           |
| ---------------- | ------------------ | -------------------- |
| Factory          | Object creation    | Spring Bean creation |
| Abstract Factory | Product families   | Cloud SDKs           |
| Strategy         | Dynamic behavior   | Pricing, Security    |
| Singleton        | Single instance    | Config, Cache        |
| Builder          | Complex objects    | Request builders     |
| Observer         | Event notification | Kafka, Events        |

---
