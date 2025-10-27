# SOLID Design Principles vs Design Patterns

## Table of Contents
- [Introduction](#introduction)
- [SOLID Principles Overview](#solid-principles-overview)
- [Difference Between SOLID Principles and Design Patterns](#difference-between-solid-principles-and-design-patterns)
- [SOLID Principles with Java Examples](#solid-principles-with-java-examples)
  - [1. Single Responsibility Principle (SRP)](#1-single-responsibility-principle-srp)
  - [2. Open/Closed Principle (OCP)](#2-openclosed-principle-ocp)
  - [3. Liskov Substitution Principle (LSP)](#3-liskov-substitution-principle-lsp)
  - [4. Interface Segregation Principle (ISP)](#4-interface-segregation-principle-isp)
  - [5. Dependency Inversion Principle (DIP)](#5-dependency-inversion-principle-dip)
- [Real-World E-Commerce System Example](#real-world-e-commerce-system-example)
- [Benefits of SOLID Principles](#benefits-of-solid-principles)
- [Conclusion](#conclusion)

## Introduction

SOLID is an acronym for five design principles intended to make object-oriented designs more understandable, flexible, and maintainable. These principles were introduced by Robert C. Martin (Uncle Bob) and are fundamental to writing clean, maintainable code.

## SOLID Principles Overview

| Principle | Description |
|-----------|-------------|
| **S**ingle Responsibility | A class should have only one reason to change |
| **O**pen/Closed | Software entities should be open for extension but closed for modification |
| **L**iskov Substitution | Objects of a superclass should be replaceable with objects of its subclasses |
| **I**nterface Segregation | No client should be forced to depend on methods it does not use |
| **D**ependency Inversion | Depend on abstractions, not concretions |

## Difference Between SOLID Principles and Design Patterns

### SOLID Principles
- **What they are**: Guidelines for designing classes and relationships
- **Purpose**: Provide a foundation for good object-oriented design
- **Scope**: Low-level design principles
- **Application**: Applied during class and interface design
- **Nature**: Abstract concepts that guide design decisions

### Design Patterns
- **What they are**: Reusable solutions to recurring problems
- **Purpose**: Solve specific design problems
- **Scope**: Mid to high-level architectural solutions
- **Application**: Applied to solve specific problems (e.g., creating objects, structuring code)
- **Nature**: Concrete implementations with specific structures

### Key Differences

| Aspect | SOLID Principles | Design Patterns |
|--------|-----------------|-----------------|
| **Level** | Principles/Guidelines | Concrete Solutions |
| **Flexibility** | Always applicable | Context-specific |
| **Implementation** | Multiple ways to implement | Specific implementation structure |
| **Examples** | SRP, OCP, LSP, ISP, DIP | Singleton, Factory, Observer, Strategy |

## SOLID Principles with Java Examples

### 1. Single Responsibility Principle (SRP)
**"A class should have only one reason to change"**

#### ❌ Violating SRP
```java
// Bad: Employee class has multiple responsibilities
public class Employee {
    private String name;
    private String email;
    private double salary;
    
    public Employee(String name, String email, double salary) {
        this.name = name;
        this.email = email;
        this.salary = salary;
    }
    
    // Business logic responsibility
    public double calculateBonus() {
        return salary * 0.1;
    }
    
    // Database responsibility
    public void saveToDatabase() {
        // JDBC code to save employee to database
        System.out.println("Saving " + name + " to database");
    }
    
    // Email responsibility
    public void sendEmail(String message) {
        System.out.println("Sending email to " + email + ": " + message);
    }
}
```

#### ✅ Following SRP
```java
// Good: Separate classes for separate responsibilities

// Employee class only handles employee data
public class Employee {
    private String name;
    private String email;
    private double salary;
    
    public Employee(String name, String email, double salary) {
        this.name = name;
        this.email = email;
        this.salary = salary;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getSalary() { return salary; }
}

// Separate class for business logic
public class BonusCalculator {
    public double calculateBonus(Employee employee) {
        return employee.getSalary() * 0.1;
    }
}

// Separate class for database operations
public class EmployeeRepository {
    public void save(Employee employee) {
        // JDBC code to save employee
        System.out.println("Saving " + employee.getName() + " to database");
    }
    
    public Employee findById(int id) {
        // JDBC code to find employee
        return null;
    }
}

// Separate class for email operations
public class EmailService {
    public void sendEmail(String to, String message) {
        System.out.println("Sending email to " + to + ": " + message);
    }
}
```

### 2. Open/Closed Principle (OCP)
**"Software entities should be open for extension but closed for modification"**

#### ❌ Violating OCP
```java
// Bad: Need to modify class to add new discount types
public class DiscountCalculator {
    public double calculateDiscount(String customerType, double amount) {
        if (customerType.equals("REGULAR")) {
            return amount * 0.05;
        } else if (customerType.equals("PREMIUM")) {
            return amount * 0.10;
        } else if (customerType.equals("VIP")) {
            return amount * 0.20;
        }
        // Need to modify this method to add new customer types
        return 0;
    }
}
```

#### ✅ Following OCP
```java
// Good: Use abstraction to extend without modification

// Abstract discount strategy
public interface DiscountStrategy {
    double calculateDiscount(double amount);
}

// Concrete implementations
public class RegularCustomerDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.05;
    }
}

public class PremiumCustomerDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.10;
    }
}

public class VIPCustomerDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.20;
    }
}

// New discount type can be added without modifying existing code
public class StudentDiscount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.15;
    }
}

// Calculator using strategy
public class DiscountCalculator {
    public double calculateDiscount(DiscountStrategy strategy, double amount) {
        return strategy.calculateDiscount(amount);
    }
}
```

### 3. Liskov Substitution Principle (LSP)
**"Objects of a superclass should be replaceable with objects of its subclasses without breaking the application"**

#### ❌ Violating LSP
```java
// Bad: Square changes the behavior expected from Rectangle
public class Rectangle {
    protected int width;
    protected int height;
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getArea() {
        return width * height;
    }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width; // Violates LSP - changes expected behavior
    }
    
    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height; // Violates LSP - changes expected behavior
    }
}

// This test will fail for Square
public class TestLSP {
    public static void testRectangle(Rectangle rect) {
        rect.setWidth(5);
        rect.setHeight(4);
        assert rect.getArea() == 20; // Fails for Square (returns 16)
    }
}
```

#### ✅ Following LSP
```java
// Good: Use composition or separate hierarchies

public interface Shape {
    int getArea();
}

public class Rectangle implements Shape {
    private int width;
    private int height;
    
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public int getArea() {
        return width * height;
    }
}

public class Square implements Shape {
    private int side;
    
    public Square(int side) {
        this.side = side;
    }
    
    @Override
    public int getArea() {
        return side * side;
    }
}
```

### 4. Interface Segregation Principle (ISP)
**"No client should be forced to depend on methods it does not use"**

#### ❌ Violating ISP
```java
// Bad: Fat interface forcing implementations to have unnecessary methods
public interface Worker {
    void work();
    void eat();
    void sleep();
    void code();
    void attendMeeting();
    void fixBugs();
}

public class Developer implements Worker {
    @Override
    public void work() {
        System.out.println("Working...");
    }
    
    @Override
    public void eat() {
        System.out.println("Eating...");
    }
    
    @Override
    public void sleep() {
        System.out.println("Sleeping...");
    }
    
    @Override
    public void code() {
        System.out.println("Coding...");
    }
    
    @Override
    public void attendMeeting() {
        System.out.println("In meeting...");
    }
    
    @Override
    public void fixBugs() {
        System.out.println("Fixing bugs...");
    }
}

public class Robot implements Worker {
    @Override
    public void work() {
        System.out.println("Working...");
    }
    
    @Override
    public void eat() {
        // Robots don't eat - forced to implement unnecessary method
        throw new UnsupportedOperationException("Robots don't eat");
    }
    
    @Override
    public void sleep() {
        // Robots don't sleep - forced to implement unnecessary method
        throw new UnsupportedOperationException("Robots don't sleep");
    }
    
    @Override
    public void code() {
        System.out.println("Coding...");
    }
    
    @Override
    public void attendMeeting() {
        // Robots don't attend meetings
        throw new UnsupportedOperationException("Robots don't attend meetings");
    }
    
    @Override
    public void fixBugs() {
        System.out.println("Fixing bugs...");
    }
}
```

#### ✅ Following ISP
```java
// Good: Segregated interfaces

public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

public interface Codeable {
    void code();
    void fixBugs();
}

public interface Attendable {
    void attendMeeting();
}

// Human developer implements all interfaces they need
public class Developer implements Workable, Eatable, Sleepable, Codeable, Attendable {
    @Override
    public void work() {
        System.out.println("Working...");
    }
    
    @Override
    public void eat() {
        System.out.println("Eating...");
    }
    
    @Override
    public void sleep() {
        System.out.println("Sleeping...");
    }
    
    @Override
    public void code() {
        System.out.println("Coding...");
    }
    
    @Override
    public void fixBugs() {
        System.out.println("Fixing bugs...");
    }
    
    @Override
    public void attendMeeting() {
        System.out.println("In meeting...");
    }
}

// Robot only implements what it needs
public class Robot implements Workable, Codeable {
    @Override
    public void work() {
        System.out.println("Working 24/7...");
    }
    
    @Override
    public void code() {
        System.out.println("Generating code...");
    }
    
    @Override
    public void fixBugs() {
        System.out.println("Auto-fixing bugs...");
    }
}
```

### 5. Dependency Inversion Principle (DIP)
**"Depend on abstractions, not concretions"**

#### ❌ Violating DIP
```java
// Bad: High-level module depends on low-level module
public class MySQLDatabase {
    public void save(String data) {
        System.out.println("Saving to MySQL: " + data);
    }
    
    public String read(int id) {
        return "Data from MySQL with id: " + id;
    }
}

public class UserService {
    private MySQLDatabase database; // Direct dependency on concrete class
    
    public UserService() {
        this.database = new MySQLDatabase(); // Tight coupling
    }
    
    public void saveUser(String userData) {
        database.save(userData);
    }
    
    public String getUser(int id) {
        return database.read(id);
    }
}
```

#### ✅ Following DIP
```java
// Good: Depend on abstractions

// Abstraction
public interface Database {
    void save(String data);
    String read(int id);
}

// Concrete implementations
public class MySQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving to MySQL: " + data);
    }
    
    @Override
    public String read(int id) {
        return "Data from MySQL with id: " + id;
    }
}

public class MongoDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving to MongoDB: " + data);
    }
    
    @Override
    public String read(int id) {
        return "Data from MongoDB with id: " + id;
    }
}

// High-level module depends on abstraction
public class UserService {
    private Database database; // Dependency on abstraction
    
    // Dependency injection through constructor
    public UserService(Database database) {
        this.database = database;
    }
    
    public void saveUser(String userData) {
        database.save(userData);
    }
    
    public String getUser(int id) {
        return database.read(id);
    }
}

// Usage
public class Application {
    public static void main(String[] args) {
        // Can easily switch database implementation
        Database mysqlDb = new MySQLDatabase();
        UserService userService1 = new UserService(mysqlDb);
        
        Database mongoDb = new MongoDatabase();
        UserService userService2 = new UserService(mongoDb);
    }
}
```

## Real-World E-Commerce System Example

Here's a comprehensive example of an e-commerce order processing system following all SOLID principles:

```java
// ========== ABSTRACTIONS ==========

// ISP: Segregated interfaces
public interface PaymentProcessor {
    PaymentResult processPayment(double amount, String currency);
}

public interface OrderValidator {
    ValidationResult validate(Order order);
}

public interface InventoryService {
    boolean checkAvailability(String productId, int quantity);
    void reduceStock(String productId, int quantity);
}

public interface NotificationService {
    void sendNotification(String recipient, String message);
}

public interface ShippingService {
    ShippingInfo calculateShipping(Order order);
    String createShipment(Order order);
}

// ========== CONCRETE IMPLEMENTATIONS ==========

// SRP: Each class has a single responsibility
public class Order {
    private String orderId;
    private List<OrderItem> items;
    private Customer customer;
    private OrderStatus status;
    private double totalAmount;
    
    // Constructor, getters, setters
    public Order(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }
    
    private void calculateTotal() {
        this.totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    // Other getters and setters...
}

// OCP: Can extend with new payment methods without modifying existing code
public class CreditCardPayment implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(double amount, String currency) {
        // Credit card processing logic
        System.out.println("Processing credit card payment: " + amount + " " + currency);
        return new PaymentResult(true, "CC-" + UUID.randomUUID());
    }
}

public class PayPalPayment implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(double amount, String currency) {
        // PayPal processing logic
        System.out.println("Processing PayPal payment: " + amount + " " + currency);
        return new PaymentResult(true, "PP-" + UUID.randomUUID());
    }
}

// Can add new payment method without modifying existing code
public class CryptoPayment implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(double amount, String currency) {
        System.out.println("Processing crypto payment: " + amount + " " + currency);
        return new PaymentResult(true, "CRYPTO-" + UUID.randomUUID());
    }
}

// LSP: Implementations can be substituted without breaking functionality
public class StandardOrderValidator implements OrderValidator {
    @Override
    public ValidationResult validate(Order order) {
        List<String> errors = new ArrayList<>();
        
        if (order.getItems().isEmpty()) {
            errors.add("Order must contain at least one item");
        }
        
        if (order.getTotalAmount() <= 0) {
            errors.add("Order total must be greater than zero");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}

public class PremiumOrderValidator implements OrderValidator {
    @Override
    public ValidationResult validate(Order order) {
        // Premium orders might have different validation rules
        List<String> errors = new ArrayList<>();
        
        if (order.getTotalAmount() < 100) {
            errors.add("Premium orders must be at least $100");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}

// DIP: High-level module depends on abstractions
public class OrderService {
    private final OrderValidator validator;
    private final PaymentProcessor paymentProcessor;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ShippingService shippingService;
    private final OrderRepository orderRepository;
    
    // Dependency injection through constructor
    public OrderService(
            OrderValidator validator,
            PaymentProcessor paymentProcessor,
            InventoryService inventoryService,
            NotificationService notificationService,
            ShippingService shippingService,
            OrderRepository orderRepository) {
        this.validator = validator;
        this.paymentProcessor = paymentProcessor;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.shippingService = shippingService;
        this.orderRepository = orderRepository;
    }
    
    public OrderResult processOrder(Order order) {
        // 1. Validate order
        ValidationResult validation = validator.validate(order);
        if (!validation.isValid()) {
            return new OrderResult(false, validation.getErrors());
        }
        
        // 2. Check inventory
        for (OrderItem item : order.getItems()) {
            if (!inventoryService.checkAvailability(item.getProductId(), item.getQuantity())) {
                return new OrderResult(false, List.of("Product " + item.getProductId() + " is out of stock"));
            }
        }
        
        // 3. Process payment
        PaymentResult paymentResult = paymentProcessor.processPayment(
            order.getTotalAmount(), 
            order.getCurrency()
        );
        
        if (!paymentResult.isSuccess()) {
            return new OrderResult(false, List.of("Payment failed"));
        }
        
        // 4. Update inventory
        for (OrderItem item : order.getItems()) {
            inventoryService.reduceStock(item.getProductId(), item.getQuantity());
        }
        
        // 5. Create shipment
        String trackingNumber = shippingService.createShipment(order);
        order.setTrackingNumber(trackingNumber);
        
        // 6. Save order
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        // 7. Send notification
        notificationService.sendNotification(
            order.getCustomer().getEmail(),
            "Order " + order.getOrderId() + " confirmed. Tracking: " + trackingNumber
        );
        
        return new OrderResult(true, List.of());
    }
}

// ========== SUPPORTING CLASSES ==========

public class OrderItem {
    private String productId;
    private int quantity;
    private double price;
    
    // Constructor, getters, setters...
}

public class Customer {
    private String customerId;
    private String name;
    private String email;
    
    // Constructor, getters, setters...
}

public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

public class ValidationResult {
    private boolean valid;
    private List<String> errors;
    
    // Constructor, getters...
}

public class PaymentResult {
    private boolean success;
    private String transactionId;
    
    // Constructor, getters...
}

public class OrderResult {
    private boolean success;
    private List<String> errors;
    
    // Constructor, getters...
}

// ========== APPLICATION SETUP ==========

public class ECommerceApplication {
    public static void main(String[] args) {
        // Wire up dependencies (in real app, use DI framework like Spring)
        OrderValidator validator = new StandardOrderValidator();
        PaymentProcessor paymentProcessor = new CreditCardPayment();
        InventoryService inventoryService = new WarehouseInventoryService();
        NotificationService notificationService = new EmailNotificationService();
        ShippingService shippingService = new FedExShippingService();
        OrderRepository orderRepository = new MySQLOrderRepository();
        
        // Create order service with dependencies
        OrderService orderService = new OrderService(
            validator,
            paymentProcessor,
            inventoryService,
            notificationService,
            shippingService,
            orderRepository
        );
        
        // Process an order
        Customer customer = new Customer("C001", "John Doe", "john@example.com");
        Order order = new Order("ORD-001", customer);
        order.addItem(new OrderItem("PROD-001", 2, 29.99));
        order.addItem(new OrderItem("PROD-002", 1, 49.99));
        
        OrderResult result = orderService.processOrder(order);
        
        if (result.isSuccess()) {
            System.out.println("Order processed successfully!");
        } else {
            System.out.println("Order failed: " + result.getErrors());
        }
    }
}
```

## Benefits of SOLID Principles

### 1. **Maintainability**
- Code is easier to understand and modify
- Changes in one part don't cascade throughout the system
- Clear separation of concerns

### 2. **Testability**
- Each component can be tested in isolation
- Mock objects can easily replace dependencies
- Reduced test complexity

### 3. **Flexibility**
- New features can be added with minimal changes
- Easy to swap implementations
- Adaptable to changing requirements

### 4. **Reusability**
- Components can be reused in different contexts
- Interfaces enable polymorphic behavior
- Reduced code duplication

### 5. **Scalability**
- System can grow without becoming unmaintainable
- New team members can understand the code faster
- Parallel development is easier

## Conclusion

SOLID principles are fundamental guidelines that help create robust, maintainable, and scalable software systems. While design patterns provide specific solutions to recurring problems, SOLID principles provide the foundation for good object-oriented design.

By following SOLID principles:
- Your code becomes more modular and easier to test
- You can add new features without breaking existing functionality
- Your application becomes more resistant to bugs
- Team collaboration becomes more efficient

Remember that these principles should be applied pragmatically. Over-engineering simple solutions can be as harmful as under-engineering complex ones. Always consider the context and requirements of your specific project when applying these principles.

### Quick Reference

```java
// S - Single Responsibility: One class, one job
// O - Open/Closed: Extend, don't modify
// L - Liskov Substitution: Subclasses must be substitutable
// I - Interface Segregation: Many specific interfaces > one general interface
// D - Dependency Inversion: Depend on abstractions, not concretions
```

---

*This guide provides a comprehensive understanding of SOLID principles with practical Java examples. Use these principles as guidelines to write better, more maintainable code.*