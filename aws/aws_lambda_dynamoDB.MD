Nice — below is a **complete, copy-pasteable guide** (README style) that shows:

* what **AWS Lambda** is and why you’d use it with Spring Boot,
* a **Spring Boot / Spring Cloud Function** implementation for both **HTTP (API Gateway)** and **DynamoDB-Stream** handlers,
* **Terraform** code to provision: Lambda, IAM, API Gateway (HTTP API v2), DynamoDB (with Streams enabled) and Event Source Mapping,
* build & deployment steps, and **scaling / production tips**.

I included citations to official docs and useful Terraform guides for the most important claims and patterns. Read to the end for **scaling recommendations** and **pitfalls**.

---

# Lambda + Spring Boot + DynamoDB — Full Implementation & Terraform

## Short summary (TL;DR)

* Use **Spring Cloud Function** to write Spring beans (`Function`, `Consumer`, `Supplier`) and run them as Lambda handlers — easiest way to run Spring code on Lambda with minimal wiring. ([Home][1])
* Provision with Terraform: `aws_lambda_function`, `aws_iam_role`, `aws_apigatewayv2_api` + integrations, `aws_dynamodb_table` with streams, `aws_lambda_event_source_mapping`. ([Terraform Registry][2])
* For DynamoDB → Lambda: enable **DynamoDB Streams** on the table and create an event source mapping linking the stream ARN to the Lambda. ([serverlessland.com][3])

---

## 1) Architecture (high level)

```
Client -> API Gateway (HTTP API) -> Lambda (Spring Cloud Function) -> DynamoDB
                                           ^
                                           | DynamoDB Stream (insert/update)
                                           v
                                   Lambda (Consumer) -> process events
```

---

## 2) Spring Boot application (Spring Cloud Function)

### Why Spring Cloud Function?

It abstracts function signatures and provides adapters for AWS Lambda so you can ship a standard Spring Boot app as Lambda without writing manual handler boilerplate. ([Home][1])

### `pom.xml` (important deps)

```xml
<project>
  ...
  <properties>
    <java.version>17</java.version>
    <spring.boot.version>3.2.0</spring.boot.version>
    <spring.cloud.function.version>4.1.0</spring.cloud.function.version>
  </properties>

  <dependencies>
    <!-- Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring Cloud Function and AWS Adapter -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-function-context</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-function-adapter-aws</artifactId>
    </dependency>

    <!-- AWS SDK v2 for direct DynamoDB access if needed -->
    <dependency>
      <groupId>software.amazon.awssdk</groupId>
      <artifactId>dynamodb</artifactId>
    </dependency>

    <!-- Optional: AWS Lambda Java Events for typed event classes (DynamoDBEvent etc.) -->
    <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-lambda-java-events</artifactId>
      <version>3.11.0</version>
    </dependency>

    <!-- Lombok (optional) -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- create an uber-jar -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <layers>false</layers>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

> Build a *fat jar* (Spring Boot executable jar) with `mvn -DskipTests package` and then zip it for Terraform's `aws_lambda_function` (or upload to S3 and reference it).

---

### Example application — HTTP handler + DynamoDB stream consumer

`src/main/java/com/example/lambda/DynamoLambdaApplication.java`

```java
package com.example.lambda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DynamoLambdaApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamoLambdaApplication.class, args);
    }
}
```

`src/main/java/com/example/lambda/beans/Handlers.java`

```java
package com.example.lambda.beans;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class Handlers {

    // HTTP handler: accepts a Map (parsed JSON) and returns a Map -> API Gateway integration
    @Bean
    public Function<String, String> echo() {
        return (input) -> {
            // simple echo / echo + write to DynamoDB if needed
            return "Echo: " + input;
        };
    }

    // DynamoDB Stream consumer: Spring Cloud Function can bind to AWS DynamoDBEvent
    @Bean
    public Consumer<DynamodbEvent> dynamoStreamConsumer() {
        return (dynamodbEvent) -> {
            dynamodbEvent.getRecords().forEach(record -> {
                // record processing example
                var eventName = record.getEventName();
                var newImage = record.getDynamodb().getNewImage();
                System.out.println("Record event: " + eventName + " newImage: " + newImage);
                // add your business logic here
            });
        };
    }
}
```

**Notes**

* The `spring-cloud-function-adapter-aws` automatically maps API Gateway requests and DynamoDB events to your beans when deployed to Lambda. ([Home][1])
* For HTTP APIs you can also use `Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>` if you want typed access.

---

## 3) Packaging & deploying the Lambda artifact

1. Build fat JAR:

   ```bash
   mvn -DskipTests package
   ```

   artifact: `target/myapp-0.0.1-SNAPSHOT.jar`

2. Create zip (Terraform expects a zip or S3 reference):

   ```bash
   cd target
   zip -r function.zip myapp-0.0.1-SNAPSHOT.jar
   ```

(Alternative: upload jar to S3 and reference `s3_key` in Terraform `aws_lambda_function`.)

---

## 4) Terraform: resources (example)

This example creates:

* IAM role for Lambda,
* Lambda function,
* API Gateway HTTP API (apigatewayv2),
* Integration and route,
* DynamoDB table with streams enabled,
* Event source mapping (DynamoDB stream → Lambda),
* Permissions to let API Gateway invoke Lambda.

> Put these files under `terraform/` folder.

### `terraform/provider.tf`

```hcl
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-south-1"
}
```

### `terraform/dynamodb.tf`

```hcl
resource "aws_dynamodb_table" "users" {
  name         = "Users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "UserId"

  attribute {
    name = "UserId"
    type = "S"
  }
  # enable streams
  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"

  tags = {
    Name = "users"
  }
}
```

### `terraform/iam_lambda.tf`

```hcl
resource "aws_iam_role" "lambda_exec" {
  name = "lambda_exec_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Principal = { Service = "lambda.amazonaws.com" },
      Effect    = "Allow",
      Sid       = ""
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Optional: DynamoDB access policy for the function
resource "aws_iam_policy" "dynamodb_access" {
  name = "lambda-dynamodb-access"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "dynamodb:PutItem",
          "dynamodb:GetItem",
          "dynamodb:Query",
          "dynamodb:Scan",
          "dynamodb:UpdateItem"
        ],
        Resource = [
          aws_dynamodb_table.users.arn,
          "${aws_dynamodb_table.users.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_policy_attachment" "attach_dynamo" {
  name       = "attach-dynamo"
  roles      = [aws_iam_role.lambda_exec.name]
  policy_arn = aws_iam_policy.dynamodb_access.arn
}
```

### `terraform/lambda.tf`

```hcl
# package must be the path to the zip/jar you created or upload to s3 and use s3_* attributes
resource "aws_lambda_function" "spring_lambda" {
  function_name = "spring-cloud-function-lambda"
  role          = aws_iam_role.lambda_exec.arn
  handler       = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"  # SCF adapter handler
  runtime       = "java11"
  filename      = "${path.module}/../springboot-app/target/function.zip"  # or S3 reference

  memory_size = 1024
  timeout     = 30

  # Environment variables if needed
  environment {
    variables = {
      SPRING_PROFILES_ACTIVE = "prod"
    }
  }
}
```

> `handler` for Spring Cloud Function when using the AWS adapter is `org.springframework.cloud.function.adapter.aws.FunctionInvoker`. That adapter bootstraps Spring and invokes the function bean.

### `terraform/apigw.tf`

```hcl
# Create an HTTP API (APIGateway v2)
resource "aws_apigatewayv2_api" "http_api" {
  name          = "spring-http-api"
  protocol_type = "HTTP"
}

# Integration with Lambda
resource "aws_apigatewayv2_integration" "lambda_integration" {
  api_id           = aws_apigatewayv2_api.http_api.id
  integration_type = "AWS_PROXY"
  integration_uri  = aws_lambda_function.spring_lambda.invoke_arn
  integration_method = "POST"
  payload_format_version = "2.0"
}

# Route (all paths -> Lambda)
resource "aws_apigatewayv2_route" "default_route" {
  api_id    = aws_apigatewayv2_api.http_api.id
  route_key = "ANY /{proxy+}"   # wildcard route
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

# Stage
resource "aws_apigatewayv2_stage" "default_stage" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true
}

# Allow API Gateway to invoke the Lambda
resource "aws_lambda_permission" "apigw_invoke" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.spring_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.http_api.execution_arn}/*/*"
}
```

### `terraform/dynamo_stream_event.tf`

```hcl
# Event source mapping from DynamoDB stream to Lambda
resource "aws_lambda_event_source_mapping" "dynamo_to_lambda" {
  event_source_arn = aws_dynamodb_table.users.stream_arn
  function_name    = aws_lambda_function.spring_lambda.arn
  starting_position = "TRIM_HORIZON"   # or LATEST
  batch_size = 100
  enabled = true
}
```

---

## 5) Deploy steps (local dev -> prod)

1. Build Spring Boot jar and zip it:

   ```bash
   cd springboot-app
   mvn -DskipTests package
   cd target
   zip -r function.zip myapp-0.0.1-SNAPSHOT.jar
   ```

2. Put the zip under `terraform/` path referenced in `aws_lambda_function.filename` (or upload to S3 and change Terraform to use `s3_bucket` + `s3_key`).

3. Init & apply Terraform:

   ```bash
   cd terraform
   terraform init
   terraform apply
   ```

4. After apply, test HTTP endpoint:

   * `curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/` with JSON body.

5. Insert items into DynamoDB table (via AWS Console or SDK) to trigger stream consumer Lambda. Confirm logs in CloudWatch.

---

## 6) How Lambda receives DynamoDB events (summary)

* Enable **Streams** for the DynamoDB table with `stream_view_type` set (`NEW_IMAGE`, `OLD_IMAGE`, or `NEW_AND_OLD_IMAGES`) and create `aws_lambda_event_source_mapping` with `event_source_arn` = table stream ARN. Lambda will receive **DynamodbEvent** objects containing records. ([serverlessland.com][3])

---

## 7) Scaling & Production considerations

### DynamoDB side

* Use **On-Demand** billing for unpredictable workloads, or **Provisioned + Auto-Scaling** for steady predictable loads. (Terraform examples earlier for autoscaling exist in docs.) ([Terraform Registry][2])
* Consider **DAX** for heavy read caching.

### Lambda side

* Tune **memory_size** and **timeout** — memory affects CPU: increasing memory may reduce cold-start time and execution time.
* Use **Provisioned Concurrency** (plus warmers or SnapStart for Java) if you need very low latency and consistent cold-starts (Java cold starts can be high). Terraform supports provisioned concurrency configuration (and aliases). (See Terraform `aws_lambda_provisioned_concurrency_config` and discussions). ([Stack Overflow][4])
* **Reserved concurrency** can limit concurrency for noisy functions to protect downstream resources.

### Cost & cold start

* Java + Spring Boot = higher cold start risk. Use Spring Cloud Function to keep the code small and consider:

  * smaller Spring context,
  * GraalVM native image or Spring Native (if feasible),
  * Provisioned Concurrency or AWS Lambda SnapStart (for Java 11+ with supported runtimes).

### Security

* Use least-privilege IAM roles (attached to Lambda) to allow only the necessary DynamoDB actions. Use environment variables for table names (not hard-coded).

---

## 8) Logging & Observability

* CloudWatch Logs: Lambda auto-logs `System.out` and stack traces.
* Use AWS X-Ray for distributed tracing (enable X-Ray for Lambda, instrument code as needed).
* Add structured logs (JSON) for easier downstream ingestion.

---

## 9) Useful reference docs (official / canonical)

* Spring Cloud Function — AWS adapter & docs. ([Home][1])
* Terraform `aws_lambda_function` docs. ([Terraform Registry][2])
* Terraform `aws_lambda_event_source_mapping` (DynamoDB, Kinesis, SQS). ([Terraform Registry][5])
* DynamoDB Streams → Lambda tutorial & docs. ([AWS Documentation][6])

---

## 10) Example: common pitfalls & debugging checklist

* Lambda `handler` mismatch: when using Spring Cloud Function use `org.springframework.cloud.function.adapter.aws.FunctionInvoker` as the handler. ([serverless.com][7])
* JAR too big: upload to S3, or trim dependencies.
* Cold starts: measure and mitigate (provisioned concurrency / SnapStart / smaller context). ([serverlessland.com][8])
* Permissions: make sure `aws_lambda_event_source_mapping` target function has correct role and the DynamoDB stream ARN is valid.
* API Gateway mapping: use `AWS_PROXY` integration (payload format v2) when using Spring Cloud Function adapter.

---

## Quick sample: add environment variable for table name (Spring/TF)

Terraform:

```hcl
environment {
  variables = {
    TABLE_NAME = aws_dynamodb_table.users.name
  }
}
```

Spring usage:

```java
@Value("${TABLE_NAME}")
private String tableName;
```

---


[1]: https://docs.spring.io/spring-cloud-function/reference/adapters/aws-intro.html?utm_source=chatgpt.com "AWS Lambda :: Spring Cloud Function"
[2]: https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function?utm_source=chatgpt.com "aws_lambda_function | Resources | hashicorp/aws | Terraform"
[3]: https://serverlessland.com/patterns/terraform-dynamodb-streams-lambda?utm_source=chatgpt.com "Amazon DynamoDB Streams to AWS Lambda"
[4]: https://stackoverflow.com/questions/66763336/aws-terraform-lambda-provisioned-concurrency?utm_source=chatgpt.com "AWS Terraform Lambda Provisioned Concurrency"
[5]: https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_event_source_mapping?utm_source=chatgpt.com "aws_lambda_event_source_ma..."
[6]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Streams.Lambda.Tutorial2.html?utm_source=chatgpt.com "Using filters to process some events with DynamoDB and ..."
[7]: https://www.serverless.com/examples/aws-java-spring-cloud-function-demo?utm_source=chatgpt.com "Examples AWS Demo Java Spring Cloud Function ..."
[8]: https://serverlessland.com/content/service/lambda/guides/effectively-running-java-on-serverless/terraform?utm_source=chatgpt.com "Effectively running Java on Serverless | Terraform"
