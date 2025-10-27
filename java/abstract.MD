Excellent question! Abstraction is the **backbone of design patterns**, and you're absolutely right - both interfaces and abstract classes can be used. Let me explain when and why to use each with practical examples.

# Abstraction in Design Patterns — Interfaces vs Abstract Classes

## Core Concept: Why Abstraction?

Abstraction allows us to:
- **Define contracts** without implementation details
- **Achieve loose coupling** between components
- **Enable polymorphism** for flexible runtime behavior
- **Follow DIP** (Dependency Inversion Principle)

---

## When to Use Interface vs Abstract Class

### Use **Interface** When:
- You need multiple inheritance
- No shared state/implementation needed
- Defining a contract/capability
- Maximum flexibility required

### Use **Abstract Class** When:
- Sharing common implementation
- Need to maintain state
- Want template methods
- Controlling inheritance hierarchy

---

# Strategy Pattern — Both Approaches

## Approach 1: Using Interface (Most Common)

```java
// Pure contract - no implementation
public interface PaymentStrategy {
    PaymentResult processPayment(double amount);
    boolean validatePaymentDetails();
}

public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cvv;
    
    @Override
    public PaymentResult processPayment(double amount) {
        // Implementation
        return new PaymentResult(true, "CC_12345");
    }
    
    @Override
    public boolean validatePaymentDetails() {
        return cardNumber != null && cvv != null;
    }
}
```

## Approach 2: Using Abstract Class (When Shared Logic Exists)

```java
// Abstract class with shared implementation
public abstract class PaymentStrategy {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected PaymentAuditor auditor;
    
    // Template method with common logic
    public final PaymentResult processPayment(double amount) {
        // Common pre-processing
        logger.info("Processing payment of ${}", amount);
        
        if (!validateAmount(amount)) {
            return PaymentResult.failed("Invalid amount");
        }
        
        // Delegate to specific implementation
        PaymentResult result = executePayment(amount);
        
        // Common post-processing
        auditor.recordTransaction(result);
        logger.info("Payment result: {}", result.isSuccess());
        
        return result;
    }
    
    // Common validation
    protected boolean validateAmount(double amount) {
        return amount > 0 && amount <= 1000000;
    }
    
    // Force subclasses to implement
    protected abstract PaymentResult executePayment(double amount);
    protected abstract boolean validatePaymentDetails();
}

public class CreditCardPayment extends PaymentStrategy {
    private String cardNumber;
    
    @Override
    protected PaymentResult executePayment(double amount) {
        // Credit card specific logic
        return chargeCard(cardNumber, amount);
    }
    
    @Override
    protected boolean validatePaymentDetails() {
        return cardNumber != null && cardNumber.length() == 16;
    }
}

public class PayPalPayment extends PaymentStrategy {
    private String email;
    
    @Override
    protected PaymentResult executePayment(double amount) {
        // PayPal specific logic
        return paypalApi.charge(email, amount);
    }
    
    @Override
    protected boolean validatePaymentDetails() {
        return email != null && email.contains("@");
    }
}
```

---

# Pattern-by-Pattern: Interface vs Abstract Class Usage

## 1. **Observer Pattern**

### Interface Approach (Java Standard)
```java
// Java's built-in approach
public interface Observer {
    void update(Observable o, Object arg);
}

// Custom approach
public interface EventListener {
    void onEvent(Event event);
}
```

### Abstract Class Approach (When Observers Share Logic)
```java
public abstract class BaseObserver {
    private String observerId;
    private LocalDateTime lastUpdate;
    
    public void update(Subject subject) {
        lastUpdate = LocalDateTime.now();
        logUpdate(subject);
        handleUpdate(subject);  // Delegate to subclass
    }
    
    protected void logUpdate(Subject subject) {
        System.out.println(observerId + " received update at " + lastUpdate);
    }
    
    // Subclasses must implement
    protected abstract void handleUpdate(Subject subject);
}
```

---

## 2. **Command Pattern**

### Interface for Simple Commands
```java
public interface Command {
    void execute();
}

public class SimpleCommand implements Command {
    @Override
    public void execute() {
        System.out.println("Executing simple command");
    }
}
```

### Abstract Class for Complex Commands with Undo
```java
public abstract class AbstractCommand {
    protected String id = UUID.randomUUID().toString();
    protected LocalDateTime executedAt;
    protected CommandReceiver receiver;
    protected boolean canUndo = true;
    
    // Template method
    public final void execute() {
        if (!canExecute()) {
            throw new IllegalStateException("Cannot execute command");
        }
        
        saveState();  // For undo
        doExecute();  // Actual execution
        executedAt = LocalDateTime.now();
        logExecution();
    }
    
    // Common implementation
    protected void saveState() {
        // Default implementation
    }
    
    protected void logExecution() {
        System.out.println("Command " + id + " executed at " + executedAt);
    }
    
    // Subclasses must implement
    protected abstract void doExecute();
    protected abstract void doUndo();
    protected abstract boolean canExecute();
}

public class DatabaseCommand extends AbstractCommand {
    private String query;
    private Object previousState;
    
    @Override
    protected void doExecute() {
        previousState = database.getState();
        database.executeQuery(query);
    }
    
    @Override
    protected void doUndo() {
        database.restoreState(previousState);
    }
    
    @Override
    protected boolean canExecute() {
        return database.isConnected();
    }
}
```

---

## 3. **Template Method Pattern** (Always Abstract Class)

```java
// Template Method REQUIRES abstract class
public abstract class DataProcessor {
    
    // Template method defining the algorithm
    public final ProcessResult process(DataSource source) {
        // Step 1: Extract (can be overridden)
        RawData raw = extract(source);
        
        // Step 2: Validate (common implementation)
        if (!validate(raw)) {
            return ProcessResult.invalid();
        }
        
        // Step 3: Transform (must be implemented)
        ProcessedData processed = transform(raw);
        
        // Step 4: Load (can be overridden)
        load(processed);
        
        // Step 5: Cleanup (hook - optional)
        cleanup();
        
        return ProcessResult.success(processed);
    }
    
    // Default implementation (can be overridden)
    protected RawData extract(DataSource source) {
        return source.getData();
    }
    
    // Common implementation (not overridden)
    private boolean validate(RawData data) {
        return data != null && !data.isEmpty();
    }
    
    // Hook method (optional override)
    protected void cleanup() {
        // Default: do nothing
    }
    
    // Abstract methods (must be implemented)
    protected abstract ProcessedData transform(RawData raw);
    protected abstract void load(ProcessedData data);
}
```

---

## 4. **Factory Pattern Variations**

### Factory Method with Interface
```java
public interface Product {
    void use();
}

public interface Factory {
    Product createProduct();
}
```

### Factory Method with Abstract Class
```java
public abstract class Factory {
    private List<Product> productCache = new ArrayList<>();
    
    // Template method with caching
    public Product getProduct() {
        // Check cache first
        Product cached = findInCache();
        if (cached != null) {
            return cached;
        }
        
        // Create new product
        Product product = createProduct();
        
        // Post-processing
        configureProduct(product);
        productCache.add(product);
        
        return product;
    }
    
    // Hook for configuration
    protected void configureProduct(Product product) {
        // Default: no configuration
    }
    
    // Abstract factory method
    protected abstract Product createProduct();
    
    private Product findInCache() {
        // Cache logic
        return null;
    }
}
```

---

## 5. **Decorator Pattern**

### Both Interface and Abstract Class Together
```java
// Component interface
public interface Coffee {
    double getCost();
    String getDescription();
}

// Concrete component
public class SimpleCoffee implements Coffee {
    @Override
    public double getCost() { return 2.0; }
    
    @Override
    public String getDescription() { return "Simple Coffee"; }
}

// Abstract decorator (provides base implementation)
public abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost();
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }
}

// Concrete decorators
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.5;
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", with Milk";
    }
}
```

---

# Decision Framework: Interface vs Abstract Class

## Choose **Interface** When:

```java
// 1. Multiple inheritance needed
public class PaymentService implements PaymentStrategy, Auditable, Cacheable {
    // Can implement multiple interfaces
}

// 2. Defining capabilities/contracts
public interface Flyable {
    void fly();
}

public interface Swimmable {
    void swim();
}

public class Duck implements Flyable, Swimmable {
    // Duck has multiple capabilities
}

// 3. Maximum flexibility for implementers
public interface Repository<T> {
    T findById(Long id);
    List<T> findAll();
    T save(T entity);
    void delete(T entity);
}
```

## Choose **Abstract Class** When:

```java
// 1. Sharing state and behavior
public abstract class Vehicle {
    protected String model;
    protected double speed;
    protected FuelType fuelType;
    
    public void start() {
        // Common implementation
        checkFuel();
        startEngine();
    }
    
    protected abstract void startEngine();
}

// 2. Controlling initialization
public abstract class Service {
    protected final Logger logger;
    protected final Config config;
    
    protected Service() {
        this.logger = LoggerFactory.getLogger(getClass());
        this.config = loadConfig();
        initialize();  // Force initialization sequence
    }
    
    protected abstract void initialize();
    protected abstract Config loadConfig();
}

// 3. Providing template methods
public abstract class ReportGenerator {
    public final void generateReport() {
        collectData();
        processData();
        formatReport();
        deliverReport();
    }
    
    protected abstract void collectData();
    protected abstract void processData();
    protected abstract void formatReport();
    
    // Optional hook
    protected void deliverReport() {
        // Default delivery
    }
}
```

---

# Real-World Hybrid Example: Payment Processing System

```java
// Interface for the contract
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
    boolean supports(PaymentType type);
}

// Abstract class for shared implementation
public abstract class AbstractPaymentProcessor implements PaymentProcessor {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected PaymentValidator validator;
    protected FraudDetector fraudDetector;
    protected MetricsCollector metrics;
    
    @Override
    public final PaymentResult process(PaymentRequest request) {
        // Common preprocessing
        logger.info("Processing payment: {}", request.getId());
        metrics.recordAttempt(request);
        
        // Validation
        ValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            return PaymentResult.failed(validation.getErrors());
        }
        
        // Fraud check
        if (fraudDetector.isSuspicious(request)) {
            return handleFraudulentRequest(request);
        }
        
        // Delegate to specific implementation
        PaymentResult result = doProcess(request);
        
        // Common postprocessing
        metrics.recordResult(result);
        auditTransaction(request, result);
        
        return result;
    }
    
    // Template methods
    protected abstract PaymentResult doProcess(PaymentRequest request);
    
    // Hook methods with default implementation
    protected PaymentResult handleFraudulentRequest(PaymentRequest request) {
        logger.warn("Fraudulent request detected: {}", request.getId());
        return PaymentResult.failed("Transaction declined");
    }
    
    protected void auditTransaction(PaymentRequest request, PaymentResult result) {
        // Default audit implementation
    }
}

// Concrete implementations
public class StripeProcessor extends AbstractPaymentProcessor {
    private StripeClient stripeClient;
    
    @Override
    protected PaymentResult doProcess(PaymentRequest request) {
        // Stripe-specific implementation
        return stripeClient.charge(request);
    }
    
    @Override
    public boolean supports(PaymentType type) {
        return type == PaymentType.CREDIT_CARD || 
               type == PaymentType.DEBIT_CARD;
    }
}

public class PayPalProcessor extends AbstractPaymentProcessor {
    private PayPalAPI paypalApi;
    
    @Override
    protected PaymentResult doProcess(PaymentRequest request) {
        // PayPal-specific implementation
        return paypalApi.processPayment(request);
    }
    
    @Override
    public boolean supports(PaymentType type) {
        return type == PaymentType.PAYPAL;
    }
    
    @Override
    protected void auditTransaction(PaymentRequest request, PaymentResult result) {
        // PayPal requires special audit trail
        super.auditTransaction(request, result);
        paypalApi.recordTransaction(request, result);
    }
}
```

---

# Key Takeaways

1. **Interfaces** = Pure contracts, multiple inheritance, maximum flexibility
2. **Abstract Classes** = Shared state/behavior, template methods, controlled extension
3. **Both Together** = Interface for contract, abstract class for partial implementation
4. **Pattern-Specific**:
   - Template Method → Always abstract class
   - Strategy → Usually interface, abstract class if shared logic
   - Observer → Interface for flexibility, abstract class for shared behavior
   - Command → Interface for simple, abstract class for complex with undo
5. **Modern Java** (8+): Interfaces with default methods blur the line but abstract classes still needed for state and constructors