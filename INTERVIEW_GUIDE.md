# Interview Preparation Guide - Patient Management Microservices

## 🎯 Project Summary (30 seconds - Elevator Pitch)

> *"I developed a microservices-based Patient Management System using Spring Boot, demonstrating distributed system design. The architecture includes 5 microservices communicating via REST APIs, gRPC, and Kafka messaging. Patient Service manages core CRUD operations, Billing Service handles billing via gRPC, and Analytics Service consumes events asynchronously. The project showcases modern Java development, microservices patterns, and enterprise-grade architecture."*

---

## 📚 Core Concepts & Interview Questions

### 1. **Microservices Architecture**

#### Q1: What are microservices and why did you choose this architecture?

**Answer Structure**:
```
Definition:
- Independent, loosely coupled services
- Each service has specific business responsibility
- Can be developed, deployed, and scaled independently

Why Microservices (for this project)?
✓ Scale individual services independently (Patient Service might need more capacity)
✓ Technology diversity (each service can use different tech stacks)
✓ Fault isolation (one service failure doesn't crash entire system)
✓ Easier to understand and modify individual services
✓ Deploy independently without coordinating releases

Tradeoffs Acknowledged:
✗ Complexity in distributed debugging
✗ Network latency between services
✗ Data consistency challenges
✗ Operational overhead
✗ Multiple databases increase complexity

How I addressed them:
- Centralized logging
- Proper error handling
- Kafka for async communication (decoupling)
- Protobuf for efficient serialization
```

#### Q2: How do your services communicate? Why different protocols?

**Answer Structure**:
```
Communication Methods:

1. REST API (Patient Service - Public Interface)
   - Synchronous, request-response
   - Client-facing endpoints
   - Easy to test and debug
   - HTTP status codes for error handling

2. gRPC (Patient → Billing Service)
   - Synchronous, high-performance
   - When immediate response needed
   - 10x faster than REST (binary protocol)
   - Use case: Create patient → immediately create billing account

3. Kafka (Event-driven)
   - Asynchronous, eventual consistency
   - Decouples Patient Service from Analytics
   - Prevents cascading failures
   - Use case: Log analytics events without blocking patient creation

Why different protocols?
- REST: Simple, human-readable, good for APIs
- gRPC: Fast, efficient, internal service-to-service
- Kafka: Loose coupling, high throughput, fault tolerance
```

**Code Reference**:
```java
// REST Controller - Patient Service
@RestController
@RequestMapping("/patients")
public class PatientController {
    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(...) { }
}

// gRPC Client - Patient Service calls Billing
BillingGrpcClient.createBillingAccount(patientId, name, email)

// Kafka Producer - Publish event
kafkaProducer.sendEvent(patient)

// Kafka Consumer - Analytics subscribes
@KafkaListener(topics="patient", groupId="analytics-service-group")
public void consumeEvent(byte[] event) { }
```

---

### 2. **REST API Design**

#### Q3: How do you design your REST endpoints? What principles do you follow?

**Answer Structure**:
```
RESTful Principles Applied:

1. Resource-Oriented (not action-oriented)
   ✓ /patients          (not /getAllPatients)
   ✓ /patients/{id}     (not /getPatient)
   
2. HTTP Methods for Intent
   GET    /patients       - Retrieve all (safe, idempotent)
   POST   /patients       - Create new (unsafe, not idempotent)
   PUT    /patients/{id}  - Replace (idempotent)
   DELETE /patients/{id}  - Remove (idempotent)

3. Proper HTTP Status Codes
   200 OK              - Request succeeded
   201 Created         - Resource created (could use)
   204 No Content      - Delete successful
   400 Bad Request     - Invalid input
   404 Not Found       - Patient not found
   409 Conflict        - Email already exists
   500 Internal Error  - Server error

4. Request/Response Format
   - Consistent JSON structure
   - Use DTOs (Data Transfer Objects)
   - Hide internal implementation
   - Validate input before processing

Error Response Example:
{
  "timestamp": "2024-04-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Patient with ID: xxx not found"
}
```

**Code Reference**:
```java
// DELETE request returns 204 No Content
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
}

// Custom exception handling
@ExceptionHandler(PatientNotFindException.class)
public ResponseEntity<ErrorResponse> handlePatientNotFound(...) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(errorResponse);
}
```

#### Q4: How do you validate input in your API?

**Answer Structure**:
```
Validation Strategy:

1. Bean Validation (Jakarta Validation)
   @Valid annotation on method parameters
   @NotBlank, @Email, @Size, etc. on fields
   
2. Custom Validator Groups
   CreatePatientValidatorGroup - strict rules for create
   Regular group - lenient rules for update
   
3. Global Exception Handler
   Catch ConstraintViolationException
   Return meaningful error messages
   Prevents invalid data in database

Validation Flow:
User Input → @Valid annotation → Validator Group → GlobalExceptionHandler → Error Response
```

**Code Reference**:
```java
@PostMapping
public ResponseEntity<PatientResponseDTO> createPatient(
    @Validated({Default.class, CreatePatientValidatorGroup.class})
    @RequestBody PatientRequestDTO patientDto) {
    // Validation happens before method execution
}

// Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(...) {
        // Return 400 Bad Request
    }
}
```

---

### 3. **gRPC & Protocol Buffers**

#### Q5: What is gRPC? Why use it instead of REST?

**Answer Structure**:
```
What is gRPC?
- Remote Procedure Call framework by Google
- Built on HTTP/2
- Uses Protocol Buffers for serialization
- Supports streaming (unary, server stream, client stream, bidirectional)

gRPC vs REST:

┌─────────────────┬──────────────┬──────────────┐
│ Feature         │ gRPC         │ REST         │
├─────────────────┼──────────────┼──────────────┤
│ Protocol        │ HTTP/2       │ HTTP/1.1     │
│ Serialization   │ Protobuf     │ JSON/XML     │
│ Speed           │ 10x faster   │ Slower       │
│ Size            │ Smaller      │ Larger       │
│ Human Readable  │ No           │ Yes          │
│ Browser Support │ No (need JS) │ Yes          │
│ Complexity      │ Higher       │ Lower        │
└─────────────────┴──────────────┴──────────────┘

Use REST for:
- Public APIs (clients use browsers)
- Simple, human-readable communication
- Legacy system integration

Use gRPC for:
- Internal service-to-service communication
- High-performance requirements
- Real-time applications
- When bandwidth/latency matters
```

**Code Reference**:
```protobuf
// .proto file - defines service contract
service BillingService {
    rpc CreateBillingAccount(BillingRequest) returns (BillingResponse);
}

message BillingRequest {
    string patientId = 1;
    string name = 2;
    string email = 3;
}

message BillingResponse {
    string billingAccountId = 1;
    string status = 2;
}
```

```java
// Client code - call gRPC service
BillingRequest request = BillingRequest.newBuilder()
    .setPatientId(patient.getId().toString())
    .setName(patient.getName())
    .setEmail(patient.getEmail())
    .build();

BillingResponse response = billingServiceStub.createBillingAccount(request);
```

#### Q6: What are Protocol Buffers? How do you use them?

**Answer Structure**:
```
What are Protocol Buffers?
- Serialization format (like JSON, XML)
- Developed by Google
- Language and platform neutral
- More compact and faster than JSON
- Schema-based (messages have defined structure)

Benefits:
✓ Backward/forward compatibility
✓ Smaller message size (~3-10x smaller than JSON)
✓ Faster serialization/deserialization
✓ Type safety
✓ Multiple language support (Java, Python, Go, etc.)

How I use them:

1. Define schema in .proto files
2. Compile with protoc compiler → generates Java classes
3. Create message instances
4. Serialize to bytes
5. Send over network
6. Deserialize on other end

Backward Compatibility Example:
Version 1: message PatientEvent { string id = 1; string name = 2; }
Version 2: message PatientEvent { string id = 1; string name = 2; string email = 3; }
Old service can ignore field 3, new service gets new field
```

**Code Reference**:
```proto
syntax = "proto3";

package patient.events;
option java_multiple_files = true;

message PatientEvent {
    string id = 1;
    string name = 2;
    string email = 3;
    string event_type = 4;
}
```

```java
// Create and serialize
PatientEvent event = PatientEvent.newBuilder()
    .setId(patient.getId().toString())
    .setName(patient.getName())
    .setEmail(patient.getEmail())
    .setEventType("PatientCreated")
    .build();

byte[] serialized = event.toByteArray();

// Send and deserialize
PatientEvent received = PatientEvent.parseFrom(bytes);
```

---

### 4. **Event-Driven Architecture & Kafka**

#### Q7: What is Kafka? How does it fit in your architecture?

**Answer Structure**:
```
What is Kafka?
- Distributed event streaming platform
- Pub-sub messaging system
- Persistence (topics store messages)
- High throughput, low latency
- Fault tolerant and scalable

Kafka Architecture:
┌─────────────┐
│ Producer    │  (Patient Service)
└──────┬──────┘
       │ (Publish)
       ▼
┌──────────────────┐
│ Kafka Cluster    │
│ Topics: patient  │
│ Partitions: 1-N  │
│ Replicas: 1-N    │
└──────┬───────────┘
       │ (Consume)
       ▼
┌─────────────┐
│ Consumer    │  (Analytics Service)
└─────────────┘

Why Kafka (vs alternatives)?
- High throughput (millions of messages)
- Persistent (replay events)
- Multiple consumers (each reads their own copy)
- Scales horizontally
- Fault tolerant (replicated partitions)

My Implementation:
Patient Service → Publishes PatientCreated event → patient topic
Analytics Service → Subscribes to patient topic → Processes asynchronously
Decouples services: Patient Service doesn't know about Analytics
Fault tolerance: If Analytics is down, messages queue up
```

#### Q8: Why use Kafka instead of direct REST call (synchronous)?

**Answer Structure**:
```
Synchronous (REST) Problems:
- Patient Service waits for Analytics to respond
- If Analytics is slow, patient creation is slow
- If Analytics is down, patient creation fails
- Tight coupling (Patient Service depends on Analytics)
- Cascading failures

Asynchronous (Kafka) Benefits:
✓ Patient Service publishes and continues immediately
✓ No wait for Analytics response
✓ Analytics processes at its own pace
✓ Loose coupling (Patient Service doesn't need to know about Analytics)
✓ Easy to add new consumers without changing Patient Service
✓ Built-in queue if Analytics falls behind
✓ Failure isolation (Analytics down ≠ patient creation fails)

Tradeoff - Eventual Consistency:
- Synchronous: Immediate consistency
- Asynchronous: Eventual consistency (small delay)
- My use case: Analytics is non-critical path, eventual consistency acceptable
```

#### Q9: How do you ensure message reliability in Kafka?

**Answer Structure**:
```
Reliability Aspects:

1. Producer Side (Patient Service)
   acks: Wait for broker acknowledgment
   retries: Retry on failure
   Error handling: Log and handle exceptions
   
2. Consumer Side (Analytics Service)
   auto.offset.reset: How to handle missing topic start
   enable.auto.commit: Manual or automatic offset management
   max.poll.records: Control batch size
   Session timeout: Detect consumer failures

3. My Implementation:
   - Try-catch in sendEvent() method
   - Log errors for monitoring
   - Future: Add dead letter queue for failed messages
   - Idempotent processing: Handle duplicate events gracefully

Code Flow:
try {
    kafkaTemplate.send("patient", event.toByteArray());
} catch (Exception e) {
    log.error("Error sending event", e);
    // Future: Send to dead letter queue
}
```

**Code Reference**:
```java
// Producer
@Service
public class kafkaProducer {
    public void sendEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder()
            .setId(patient.getId().toString())
            .setName(patient.getName())
            .setEmail(patient.getEmail())
            .setEventType("PatientCreated")
            .build();
        
        try {
            kafkaTemplate.send("patient", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending event", e);
        }
    }
}

// Consumer
@Service
public class KafkaConsumer {
    @KafkaListener(topics="patient", groupId="analytics-service-group")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            log.info("Received Patient Event: [PatientId={},Name={},Email={}]",
                patientEvent.getId(),
                patientEvent.getName(),
                patientEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event", e);
        }
    }
}
```

---

### 5. **Spring Framework & Dependency Injection**

#### Q10: How do you use Spring Boot in this project?

**Answer Structure**:
```
Spring Boot Key Features Used:

1. Auto-Configuration
   @SpringBootApplication
   - Automatically configures beans
   - Sets up embedded Tomcat
   - Configures DataSource
   - No XML configuration needed

2. Dependency Injection
   @Service, @Repository, @Component
   - Constructor injection (preferred)
   - Inversion of Control (IoC)
   - Loose coupling

3. Spring Web (MVC)
   @RestController, @RequestMapping
   - Handles HTTP requests
   - JSON serialization/deserialization
   - Exception handling

4. Spring Data JPA
   - ORM abstraction over Hibernate
   - Query methods (findByEmail, etc.)
   - Transaction management

5. Spring Kafka
   @KafkaListener
   - Simplified Kafka consumer setup
   - Automatic deserialization

Auto-Configuration Magic:
@SpringBootApplication automatically enables:
✓ Component scanning
✓ @EnableAutoConfiguration
✓ @ComponentScan
✓ Embedded Tomcat setup
✓ Property file loading
```

**Code Reference**:
```java
// Spring Boot Application Entry Point
@SpringBootApplication
public class PatientServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}

// Service with DI
@Service
public class PatientService {
    private final PatientRepository repository;
    private final kafkaProducer kafkaProducer;
    private final BillingGrpcClient billingClient;
    
    // Constructor injection (preferred over @Autowired)
    public PatientService(PatientRepository repository,
                         kafkaProducer kafkaProducer,
                         BillingGrpcClient billingClient) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.billingClient = billingClient;
    }
}

// Kafka Consumer with Spring
@Service
public class KafkaConsumer {
    @KafkaListener(topics="patient", groupId="analytics-service-group")
    public void consumeEvent(byte[] event) {
        // Automatically called when message arrives
    }
}
```

#### Q11: Why prefer constructor injection over field injection?

**Answer Structure**:
```
Constructor Injection Benefits:

1. Immutability
   - Fields can be final
   - Thread-safe
   - Can't be changed after construction

2. Testability
   - Easy to pass mock objects
   - No reflection needed
   - Clear dependencies

3. Fail-fast
   - Missing dependencies discovered at startup
   - Not at runtime when field is accessed

4. Null safety
   - Can't forget to initialize
   - No NullPointerException risks

5. Explicit Dependencies
   - Clear what class depends on

Field Injection (@Autowired):
✗ Can't be final (mutable)
✗ Harder to test (need reflection or setter)
✗ Late failure (runtime)
✗ Hidden dependencies

My Practice:
Constructor injection everywhere
All fields are private final
Dependencies are explicit
```

---

### 6. **Database Design & JPA/Hibernate**

#### Q12: How do you design your database schema?

**Answer Structure**:
```
Patient Entity Design:

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;  // Unique constraint
    
    @Column(nullable = false)
    private String name;
    
    private Integer age;
    private String medicalHistory;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

Design Decisions:

1. UUID as Primary Key
   ✓ Globally unique
   ✓ Can generate before persistence
   ✓ Privacy (not guessable sequential IDs)
   ✓ Good for distributed systems
   
2. Unique Email Constraint
   ✓ Business requirement
   ✓ Database level (prevents duplicates)
   ✓ Backed by custom exception handling

3. Timestamps (createdAt, updatedAt)
   ✓ Audit trail
   ✓ Track changes
   ✓ @CreationTimestamp, @UpdateTimestamp (Hibernate)

4. Not Null Constraints
   ✓ Data integrity
   ✓ Prevents invalid data
   ✓ Validated at database level too

Database per Microservice:
- Each service has own database
- No cross-service queries
- Data consistency via events (Kafka)
- Each service owns its domain data
```

#### Q13: How do you handle database transactions?

**Answer Structure**:
```
Transaction Management:

Spring's @Transactional:
@Service
public class PatientService {
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO dto) {
        // All operations here are in one transaction
        // Auto-rollback on exception
    }
}

Transaction Properties:
- Isolation: READ_COMMITTED (default)
- Propagation: REQUIRED (default)
- Rollback: On RuntimeException

In My Code:
1. Service methods marked @Transactional
2. Multiple operations (save, log, etc.) in single transaction
3. Automatic rollback on exception
4. Declarative transaction management (no try-catch for TX)

Kafka Publishing in Transaction:
⚠️ Issue: Kafka message sent, then DB fails
Problem: Message published but data not saved
Solution: 
  - Send event within transaction context
  - Or use transactional outbox pattern (advanced)
  - My approach: Log error and continue (eventual consistency)
```

---

### 7. **Exception Handling**

#### Q14: How do you handle exceptions in a distributed system?

**Answer Structure**:
```
Exception Handling Strategy:

1. Custom Exceptions
   - PatientNotFindException: When patient not found
   - EmailAlreadyExistsException: When email duplicate
   - Specific to business logic
   - Easier to handle specifically

2. Global Exception Handler
   @ControllerAdvice
   - Centralized exception handling
   - Converts to standardized response format
   - Consistent error responses across APIs

3. Graceful Degradation
   - Kafka error: Log and continue
   - gRPC error: Log and return error response
   - Prevent cascading failures

4. Logging
   - Log all exceptions with context
   - Enable monitoring and debugging
   - Track error patterns

Error Response Format:
{
  "timestamp": "2024-04-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Patient with ID: xxx not found",
  "path": "/patients/xxx"
}

Recovery Strategies:
- Retry with backoff (Kafka)
- Fallback responses
- Circuit breaker (future: Resilience4j)
- Dead letter queue (future: Kafka DLQ)
```

**Code Reference**:
```java
// Custom Exception
public class PatientNotFindException extends RuntimeException {
    public PatientNotFindException(String message) {
        super(message);
    }
}

// Global Exception Handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PatientNotFindException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(
        PatientNotFindException ex,
        HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}

// Service throws custom exception
@Service
public class PatientService {
    public PatientResponseDTO getPatient(UUID id) {
        return repository.findById(id)
            .map(patientMapper::toDTO)
            .orElseThrow(() -> new PatientNotFindException(
                "Patient with ID: " + id + " not found"
            ));
    }
}
```

---

### 8. **Data Transfer Objects (DTOs)**

#### Q15: Why use DTOs? How do you implement them?

**Answer Structure**:
```
What are DTOs?

DTOs (Data Transfer Objects) separate:
- Internal entity model (database representation)
- External API contract (what clients see)

Benefits:
✓ Hide internal implementation
✓ Version API independently from database
✓ Add/remove fields without DB changes
✓ Security (don't expose sensitive fields)
✓ Performance (only send needed fields)
✓ Clear API contract

Request DTO:
public class PatientRequestDTO {
    private String name;
    private String email;
    private Integer age;
    private String medicalHistory;
    // Validation annotations
}

Response DTO:
public class PatientResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private Integer age;
    private String medicalHistory;
    private LocalDateTime createdAt;
}

Mapper Pattern:
public class PatientMapper {
    public PatientResponseDTO toDTO(Patient patient) { }
    public Patient toEntity(PatientRequestDTO dto) { }
}

Separation of Concerns:
Request → Validation → Mapper → Service → Repository
Response ← Mapper ← Entity ← Repository ← Service
```

---

### 9. **Testing & Quality**

#### Q16: How do you test a microservices application?

**Answer Structure**:
```
Testing Strategy:

1. Unit Tests
   - Test individual methods
   - Mock dependencies
   - Fast execution
   - Example: PatientService.createPatient()

2. Integration Tests
   - Test service + database
   - Use test containers or H2
   - Example: REST endpoint → Service → Repository

3. API Testing (Manual)
   - HTTP files (IntelliJ REST client)
   - Test actual endpoints
   - Verify response format

Challenges in Microservices Testing:
- Mocking other services
- Network failures
- Distributed tracing
- Contract testing between services

My Approach:
@SpringBootTest
- Load Spring context
- Test with real or mocked services
- H2 in-memory database for testing

Consumer Contract Testing:
- Patient Service publishes events
- Analytics Service consumes them
- Test: Publish → Consume → Verify
```

---

### 10. **Deployment & DevOps**

#### Q17: How do you containerize and deploy your services?

**Answer Structure**:
```
Docker & Deployment:

1. Dockerfile (Multi-stage build)
   FROM maven:3.9-amazoncorretto-21 AS builder
   COPY . /app
   RUN mvn clean package -DskipTests
   
   FROM amazoncorretto:21
   COPY --from=builder /app/target/*.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]

2. Docker Compose Orchestration
   services:
     patient-service:
       build: ./patient-service
       ports:
         - "4000:4000"
       environment:
         - KAFKA_BROKER=kafka:9092
     
     billing-service:
       build: ./billing-service
       ports:
         - "9001:9001"
     
     analytics-service:
       build: ./analytics-service
       ports:
         - "8001:8001"
     
     kafka:
       image: confluentinc/cp-kafka:latest
       ports:
         - "9092:9092"

3. Service Discovery
   - Services communicate via service name (Docker DNS)
   - patient-service → http://billing-service:9001 (gRPC)
   
4. Environment Configuration
   - application.properties → application-docker.properties
   - Docker environment variables override properties

Benefits:
✓ Reproducible environments
✓ Easy scaling (scale patient-service to 3 instances)
✓ Isolation (each service separate container)
✓ Version control (Docker version per service)

Future:
- Kubernetes (K8s) deployment
- Helm charts for templating
- CI/CD pipelines (GitHub Actions, Jenkins)
```

---

## 🔗 System Interconnections

### Create Patient - Complete Flow

```
1. USER REQUEST
   POST /patients
   {
     "name": "John Doe",
     "email": "john@example.com",
     "age": 30,
     "medicalHistory": "Hypertension"
   }
   ↓

2. PATIENT CONTROLLER (REST)
   @PostMapping
   public ResponseEntity<PatientResponseDTO> createPatient(
       @Validated @RequestBody PatientRequestDTO dto)
   ↓

3. REQUEST VALIDATION
   - @NotBlank on name, email
   - @Email validation
   - CreatePatientValidatorGroup rules
   ↓ (If invalid, return 400 Bad Request)
   ↓

4. PATIENT SERVICE (Business Logic)
   - Check if email already exists
   - Create Patient entity
   - Save to database (JPA/Hibernate)
   ↓ (If email exists, throw EmailAlreadyExistsException)
   ↓

5. DATABASE PERSISTENCE
   Patient table:
   | id   | name     | email           | age | medical_history | created_at | updated_at |
   |------|----------|-----------------|-----|-----------------|------------|------------|
   | UUID | John Doe | john@email.com  | 30  | Hypertension    | TIMESTAMP  | TIMESTAMP  |
   ↓

6. KAFKA EVENT PUBLISHING (Asynchronous)
   kafkaProducer.sendEvent(patient)
   ↓
   - Convert Patient to PatientEvent (Protobuf)
   - Serialize to bytes
   - Publish to "patient" topic
   ↓

7. ANALYTICS SERVICE CONSUMES EVENT (Async)
   @KafkaListener(topics="patient")
   - Listen on topic
   - Deserialize PatientEvent
   - Log metrics
   - (No response back to patient service)
   ↓

8. gRPC BILLING SERVICE CALL (Synchronous)
   BillingGrpcClient.createBillingAccount(
       patientId, name, email)
   ↓
   - Create BillingRequest (Protobuf)
   - Call gRPC service (HTTP/2)
   - Wait for BillingResponse
   - Get billing account ID
   ↓

9. RETURN RESPONSE TO CLIENT
   PatientResponseDTO:
   {
     "id": "UUID",
     "name": "John Doe",
     "email": "john@example.com",
     "age": 30,
     "medicalHistory": "Hypertension",
     "createdAt": "2024-04-07T10:30:00Z"
   }
   HTTP 200 OK
   ↓

10. MONITORING & LOGGING
    - Patient Service logs: "Patient created: UUID"
    - Billing Service logs: "Billing account created"
    - Analytics Service logs: "Event received"
    - Centralized logging for debugging
```

---

## 💡 Technical Decision Justifications

### Why REST for Patient Service (not gRPC)?
- **Public API**: Clients are external (web, mobile)
- **Standards**: REST is industry standard
- **Debugging**: Human-readable JSON
- **Browser**: REST works from browser, gRPC needs special client

### Why gRPC for Billing (not REST)?
- **Internal communication**: Only called by Patient Service
- **Performance**: Need fast response (billing account creation)
- **Efficiency**: Binary protocol (less bandwidth)
- **Type safety**: Protobuf generates type-safe code

### Why Kafka for Analytics (not direct REST call)?
- **Decoupling**: Patient Service independent of Analytics
- **Resilience**: Analytics down ≠ patient creation fails
- **Scalability**: Can add multiple analytics consumers
- **History**: Kafka stores events (can replay if needed)

---

## 📊 Data Flow Diagrams

### Synchronous Request-Response (REST + gRPC)

```
Client              Patient Service         Billing Service
  │                      │                        │
  │ POST /patients       │                        │
  ├─────────────────────→│                        │
  │                      │ gRPC Request          │
  │                      ├──────────────────────→│
  │                      │                   Process
  │                      │ gRPC Response        │
  │                      │←──────────────────────┤
  │ 200 OK               │                        │
  │←─────────────────────┤                        │
  │                      │                        │
```

### Asynchronous Event-Driven (Kafka)

```
Patient Service         Kafka Topic         Analytics Service
     │                      │                       │
     │ Publish Event        │                       │
     ├─────────────────────→│                       │
     │                      │ Event in Queue        │
     │ Continue Processing  │                       │
     │                      │ Consume Event         │
     │                      ├──────────────────────→│
     │                      │                   Process
     │                      │                       │
     │                  (No Response)               │
```

---

## 🎓 Resume Bullet Points

```
✓ Designed and implemented 5-service microservices architecture
  using Spring Boot, demonstrating distributed system design
  patterns and service independence

✓ Implemented synchronous inter-service communication via gRPC
  with Protocol Buffers, achieving 10x performance improvement
  over REST for internal service calls

✓ Built event-driven architecture using Apache Kafka for
  asynchronous communication, ensuring fault isolation and
  eventual consistency across services

✓ Created RESTful APIs with proper HTTP semantics, status codes,
  request/response validation, and global exception handling

✓ Designed database schema with UUID primary keys, unique
  constraints, and audit timestamps; implemented JPA/Hibernate
  ORM with transaction management

✓ Implemented comprehensive input validation using Jakarta
  Validation with custom validator groups for create vs update
  operations

✓ Applied SOLID principles and design patterns including DTOs,
  mappers, repository pattern, and dependency injection

✓ Containerized microservices with Docker multi-stage builds and
  orchestrated using Docker Compose for simplified deployment

✓ Integrated Spring Kafka for event consumption with proper
  error handling and deserialization using Protocol Buffers

✓ Implemented centralized exception handling with custom
  exceptions and global exception handlers for consistent
  error responses
```

---

## 🚀 How to Discuss This in Interview

### Opening (2 minutes)
"I built a patient management microservices application with 5 services that communicate through REST, gRPC, and Kafka. The architecture demonstrates modern distributed system design patterns and addresses challenges like service decoupling and inter-service communication."

### Deep Dive (5-10 minutes)
Based on question, go deeper into:
- Architecture decisions and tradeoffs
- Technology choices and alternatives
- Problem-solving approach
- Real-world considerations

### Closing
"This project taught me how to design scalable systems, handle distributed communication challenges, and make informed technology choices based on specific use cases."

---

## 🔍 Common Follow-up Questions & Answers

**Q: How would you handle service failures?**
> With Kafka, if Analytics is down, events queue up and process when service recovers. For gRPC calls, I'd implement circuit breaker pattern with Resilience4j to fail fast. For REST, I'd add retry logic with exponential backoff.

**Q: How do you ensure data consistency?**
> Patient Service has strong consistency (ACID transactions). Analytics eventual consistency via Kafka (small delay). This is acceptable because analytics is non-critical path. For billing, gRPC ensures immediate consistency.

**Q: How would you scale this?**
> Horizontally: Run multiple instances of each service behind load balancer. Kafka partitions distribute events. Vertically: Increase CPU/memory. Use managed services like cloud Kafka, cloud databases.

**Q: What about security?**
> Auth Service handles JWT generation. API Gateway validates tokens. gRPC uses TLS. Database credentials in environment variables. Next: Implement API gateway authentication, rate limiting, OAuth2.

---

## 📚 Resources for Interview Prep

**Concepts to Study:**
1. CAP Theorem
2. Event Sourcing
3. SAGA Pattern for distributed transactions
4. Circuit Breaker Pattern
5. Service Discovery
6. API Versioning
7. Distributed Tracing
8. Message Queue vs Pub-Sub

**Practice:**
- Explain architecture without looking at code
- Draw system diagrams on whiteboard
- Discuss tradeoffs of each component
- Propose improvements/enhancements

---

**Last Updated**: April 7, 2026  
**Status**: Interview Ready 🎯

