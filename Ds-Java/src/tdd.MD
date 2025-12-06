# TDD vs BDD — short, practical guide (with a Java example in GitHub Markdown)

**TL;DR**

* **TDD (Test-Driven Development)**: developer workflow — **write a failing unit test → make it pass → refactor**. Tests drive the implementation details and design.
* **BDD (Behavior-Driven Development)**: stakeholder-focused — write **specifications of behavior** (often in Gherkin: `Given/When/Then`) that describe *what* the system should do. BDD tests often sit at a higher level (integration/acceptance), bridging product & dev.

---

## Key differences

| Aspect        |                         TDD | BDD                                                               |
| ------------- | --------------------------: | :---------------------------------------------------------------- |
| Focus         |  Developer & implementation | Behavior & business value                                         |
| Granularity   |    Unit tests (small units) | Scenarios / acceptance tests (feature-level)                      |
| Syntax        |   Code tests (JUnit, xUnit) | Natural language (Gherkin) or BDD frameworks (Cucumber, SpecFlow) |
| Who writes it |           Mainly developers | Product owners, testers, developers (collaboration)               |
| Goal          | Correct, well-designed code | Shared understanding, acceptance criteria                         |

---

## When to use which

* Use **TDD** to design clean, well-tested modules and get fast feedback during development.
* Use **BDD** to capture acceptance criteria, align with stakeholders, and drive integration/acceptance tests.
* They can complement: BDD specs define *what* to build; TDD drives *how* to build it.

---

## Practical TDD example (Java — GitHub Markdown)

Below is a minimal, real-world *TDD* walk-through for a tiny `BankAccount` feature: deposit, withdraw and guard against overdraft. The example uses **JUnit 5** and shows the Red → Green → Refactor cycle.

> Project structure (Maven)

```
bank-account-tdd/
├── pom.xml
└── src
    ├── main
    │   └── java
    │       └── com/example/bank
    │           └── BankAccount.java
    └── test
        └── java
            └── com/example/bank
                └── BankAccountTest.java
```

---

### 1) `pom.xml` (only the essentials)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>bank-account-tdd</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <junit.jupiter.version>5.10.0</junit.jupiter.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <version>${junit.jupiter.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <version>${junit.jupiter.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.1.2</version>
      </plugin>
    </plugins>
  </build>
</project>
```

---

### 2) TDD cycle — start with a failing test

Create `BankAccountTest.java` under `src/test/java/com/example/bank/`:

```java
package com.example.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void newAccount_hasZeroBalance() {
        BankAccount acc = new BankAccount();
        assertEquals(0.0, acc.getBalance(), 0.0001);
    }

    @Test
    void deposit_increasesBalance() {
        BankAccount acc = new BankAccount();
        acc.deposit(100.0);
        assertEquals(100.0, acc.getBalance(), 0.0001);
    }

    @Test
    void withdraw_decreasesBalance() {
        BankAccount acc = new BankAccount();
        acc.deposit(200.0);
        acc.withdraw(50.0);
        assertEquals(150.0, acc.getBalance(), 0.0001);
    }

    @Test
    void withdraw_moreThanBalance_throws() {
        BankAccount acc = new BankAccount();
        acc.deposit(30.0);
        assertThrows(IllegalArgumentException.class, () -> acc.withdraw(50.0));
    }
}
```

**Run tests now** (`mvn test`). All will fail because `BankAccount` doesn't exist. *Red* ✅

---

### 3) Make the minimal code to pass (Green)

Add `BankAccount.java` under `src/main/java/com/example/bank/`:

```java
package com.example.bank;

public class BankAccount {
    private double balance = 0.0;

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Cannot deposit negative amount");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Cannot withdraw negative amount");
        if (amount > balance) throw new IllegalArgumentException("Insufficient funds");
        balance -= amount;
    }
}
```

Run `mvn test` — tests should now pass. *Green* ✅

---

### 4) Refactor (if needed)

* Look for duplicate logic, naming improvements, or better exceptions.
* Keep tests passing after refactor.
* Add more tests for edge cases (e.g., deposit zero, rounding, concurrency if relevant).

Example new test — deposit negative amount throws:

```java
@Test
void deposit_negative_throws() {
    BankAccount acc = new BankAccount();
    assertThrows(IllegalArgumentException.class, () -> acc.deposit(-10.0));
}
```

Add implementation already handles that.

---

## Practical TDD tips (real-world)

1. **Small steps** — write a tiny test, make it pass, repeat.
2. **Fast tests** — unit tests should be quick to run (seconds); slow tests belong to integration/acceptance suites.
3. **One behavior per test** — each test should assert a single behavior or outcome (keeps failures clear).
4. **Name tests clearly** — methodName_state_expectedBehavior (e.g., `withdraw_moreThanBalance_throws`).
5. **Mock external dependencies** — when your class calls DB or network, isolate behavior with mocks (Mockito).
6. **Keep production code minimal** — implement only what tests require (YAGNI principle).
7. **Refactor often** — the tests give safety to clean design.
8. **Use property-based or parameterized tests** for repetitive inputs (JUnit Parameterized tests).
9. **Layered testing strategy**:

   * Unit tests (TDD) for logic,
   * Integration tests for wiring & DB,
   * Acceptance/BDD tests (Cucumber/Gherkin) for business flows.

---

## How TDD and BDD can work together in a project

1. **Start with BDD**: product owner and team define a few high-level scenarios in Gherkin (acceptance criteria).
2. **Break a BDD scenario into features** and implement features with TDD inside development tasks.
3. **Write unit tests (TDD)** for components. Use BDD scenarios for end-to-end acceptance tests.

Example BDD (Gherkin) for withdraw:

```gherkin
Feature: Withdraw money from account

  Scenario: Withdraw amount less than balance
    Given I have an account with balance 200.0
    When I withdraw 50.0
    Then the balance should be 150.0

  Scenario: Withdraw more than balance
    Given I have an account with balance 30.0
    When I withdraw 50.0
    Then I should see an "Insufficient funds" error
```

You can automate these with Cucumber (Java) or other BDD tools and map step definitions to tests that exercise the real system.

---

## Quick checklist to start TDD today

1. Choose a tiny feature to implement.
2. Write a unit test that captures the desired behavior (it should fail).
3. Implement the smallest amount of code to make it pass.
4. Run the test suite; make sure it passes.
5. Refactor both production and test code for clarity.
6. Repeat.

---