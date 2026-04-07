# Patient Management System - Microservices Architecture

A comprehensive **microservices-based patient management system** built with Spring Boot, gRPC, Kafka, and Protocol Buffers. This project demonstrates modern distributed system design patterns, inter-service communication, and event-driven architecture.

## 🎯 Project Overview

This is a production-ready microservices application showcasing:
- **REST API Design** - RESTful endpoints with validation
- **gRPC Communication** - High-performance synchronous inter-service calls
- **Event-Driven Architecture** - Asynchronous messaging with Kafka
- **Protocol Buffers** - Efficient serialization across services
- **Docker Containerization** - Easy deployment and scaling
- **Distributed System Design** - Service-to-service communication patterns

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Port 8000)                 │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   PATIENT    │  │   BILLING    │  │  ANALYTICS   │
│   SERVICE    │  │   SERVICE    │  │   SERVICE    │
│ (Port 4000)  │  │ (Port 9001)  │  │ (Port 8001)  │
└──────┬───────┘  └──────────────┘  └──────────────┘
       │                │                    ▲
       │         gRPC   │                    │
       │        (Proto) │                    │
       │                │                    │
       │                └────────────────────┤
       │                                     │
       │         ┌──────────────────────┐   │
       │         │  KAFKA MESSAGE BUS   │   │
       │         │  (Patient Events)    │───┘
       └────────▶│  (Async Events)      │
                 └──────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    DATABASE & PERSISTENCE                     │
│    (Each service has its own database - Microservices)       │
└──────────────────────────────────────────────────────────────┘
```

## 🏗️ System Components

### 1. **Patient Service** (Core Service)
- **Port**: 4000
- **Technology**: Spring Boot 3.4, JPA/Hibernate, MySQL
- **Responsibilities**:
  - Create, Read, Update, Delete patient records
  - Manage patient information
  - Produce patient events to Kafka
  - Call billing service via gRPC

**Key Endpoints**:
```
GET    /patients              - List all patients
POST   /patients              - Create new patient
PUT    /patients/{id}         - Update patient
DELETE /patients/{id}         - Delete patient
```

**Database**: MySQL
- Patient entity with UUID primary key
- Email uniqueness constraint
- Timestamps for audit trail

### 2. **Billing Service** (gRPC Service)
- **Port**: 9001 (gRPC)
- **Technology**: Spring Boot with gRPC framework
- **Responsibilities**:
  - Create billing accounts for patients
  - Handle billing-related operations
  - Expose gRPC services for inter-service communication

**gRPC Service**:
```proto
service BillingService {
  rpc CreateBillingAccount(BillingRequest) returns (BillingResponse);
}
```

### 3. **Analytics Service** (Event Subscriber)
- **Port**: 8001
- **Technology**: Spring Boot with Kafka consumer
- **Responsibilities**:
  - Consume patient events from Kafka
  - Process analytics and metrics
  - Real-time monitoring of patient creation events

**Event Consumption**:
- Topic: `patient`
- Consumer Group: `analytics-service-group`
- Message Format: Protobuf (PatientEvent)

### 4. **Auth Service** (Security)
- **Port**: 7000
- **Technology**: Spring Boot with Security
- **Responsibilities**:
  - User authentication and authorization
  - Token generation and validation
  - Role-based access control

### 5. **API Gateway** (Entry Point)
- **Port**: 8000
- **Technology**: Spring Cloud Gateway / Spring Boot
- **Responsibilities**:
  - Route requests to appropriate services
  - Handle cross-cutting concerns
  - API versioning and documentation

## 🔧 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Runtime** | Java | 21 |
| **Framework** | Spring Boot | 3.4.0 / 4.0.3 |
| **Build Tool** | Maven | 3.x |
| **API** | REST (Spring MVC) | OpenAPI 3.0 |
| **IPC** | gRPC | 1.69.0 |
| **Messaging** | Kafka | Latest |
| **Serialization** | Protocol Buffers | 4.29.1 |
| **Database** | MySQL / H2 | 8.x / 2.x |
| **ORM** | Hibernate/JPA | 6.x |
| **Container** | Docker | Latest |
| **Documentation** | Swagger/OpenAPI | 2.7.0 |

## 📦 Project Structure

```
Patient-management/
├── patient-service/              # Core microservice
│   ├── src/main/java/
│   │   └── com/example/patientservice/
│   │       ├── controller/        # REST controllers
│   │       ├── service/           # Business logic
│   │       ├── repository/        # Data access layer
│   │       ├── model/             # JPA entities
│   │       ├── dto/               # Data transfer objects
│   │       ├── mapper/            # DTO mappers
│   │       ├── exception/         # Custom exceptions
│   │       ├── kafka/             # Kafka producer
│   │       └── grpc/              # gRPC client
│   ├── src/main/proto/
│   │   ├── patient_event.proto    # Patient event message
│   │   └── billing_service.proto  # Billing service definition
│   └── pom.xml
│
├── billing-service/              # gRPC service
│   ├── src/main/java/
│   │   └── com/example/billingservice/
│   │       ├── grpc/              # gRPC service implementation
│   │       └── ...
│   ├── src/main/proto/
│   │   └── billing_service.proto
│   └── pom.xml
│
├── analytics-service/            # Event consumer service
│   ├── src/main/java/
│   │   └── com/example/analyticsservice/
│   │       └── kafka/             # Kafka consumer
│   ├── src/main/proto/
│   │   └── patient_event.proto
│   └── pom.xml
│
├── auth-service/                 # Authentication service
│   ├── src/main/java/
│   │   └── com/example/authservice/
│   │       └── ...
│   └── pom.xml
│
├── api-gateway/                  # API gateway
│   ├── src/main/java/
│   │   └── com/example/apigateway/
│   │       └── ...
│   └── pom.xml
│
├── api-Requests/                 # HTTP request files
│   └── Patient-Service/
│       ├── create-patient.http
│       ├── Get-Patients.http
│       ├── update-patient.http
│       └── delete-patient.http
│
├── grpc-request/                 # gRPC test files
│   └── billing-service/
│       └── create-billing-account.http
│
├── Dockerfile                    # Multi-stage Docker builds
├── docker-compose.yml            # Orchestration
├── pom.xml                       # Parent POM
└── README.md                     # This file
```

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- Docker & Docker Compose
- Git

### Installation & Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/patient-management.git
cd patient-management
```

#### 2. Build All Services
```bash
# Build entire project
mvn clean install -DskipTests

# Build specific service
mvn clean install -DskipTests -pl patient-service -am
```

#### 3. Database Setup (MySQL)
```bash
# Start MySQL via Docker
docker run --name patient-mysql -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=patient_db \
  -p 3306:3306 -d mysql:8.0

# Create tables (patient service will auto-create with JPA)
```

#### 4. Start Kafka
```bash
# Using Docker Compose
docker-compose up kafka zookeeper

# Or use Kafka locally:
# Download from: https://kafka.apache.org/downloads
```

#### 5. Run Services
```bash
# Terminal 1: Patient Service
cd patient-service
mvn spring-boot:run

# Terminal 2: Billing Service
cd billing-service
mvn spring-boot:run

# Terminal 3: Analytics Service
cd analytics-service
mvn spring-boot:run

# Terminal 4: Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 5: API Gateway
cd api-gateway
mvn spring-boot:run
```

### Using Docker Compose (Recommended)
```bash
docker-compose up --build
```

## 📡 API Documentation

### Patient Service Endpoints

#### 1. Get All Patients
```http
GET http://localhost:4000/patients

Response (200 OK):
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30,
    "medicalHistory": "Hypertension"
  }
]
```

#### 2. Create Patient
```http
POST http://localhost:4000/patients
Content-Type: application/json

Request:
{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "age": 25,
  "medicalHistory": "Diabetes"
}

Response (200 OK):
{
  "id": "123e4567-e89b-12d3-a456-426614174001",
  "name": "Jane Smith",
  "email": "jane@example.com",
  "age": 25,
  "medicalHistory": "Diabetes"
}
```

#### 3. Update Patient
```http
PUT http://localhost:4000/patients/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

Request:
{
  "name": "John Updated",
  "email": "john.updated@example.com",
  "age": 31,
  "medicalHistory": "Hypertension, Controlled"
}

Response (200 OK): Updated patient object
```

#### 4. Delete Patient
```http
DELETE http://localhost:4000/patients/123e4567-e89b-12d3-a456-426614174000

Response (204 No Content)
```

### Swagger Documentation
- **Patient Service**: http://localhost:4000/swagger-ui.html
- **API Gateway**: http://localhost:8000/swagger-ui.html

## 🔄 System Workflows

### Workflow 1: Create Patient & Trigger Events
```
1. Client sends POST /patients request
   ↓
2. PatientController receives request
   ↓
3. PatientService validates and saves to database
   ↓
4. Patient entity created with UUID
   ↓
5. KafkaProducer sends PatientCreated event (Protobuf)
   ↓
6. Event published to "patient" Kafka topic
   ↓
7. BillingGrpcClient calls BillingService synchronously
   ↓
8. BillingService creates billing account (gRPC)
   ↓
9. Analytics Service consumes PatientCreated event asynchronously
   ↓
10. Analytics updates metrics and logs
```

### Workflow 2: Synchronous gRPC Call
```
Patient Service (Client)
         ↓ (gRPC Request)
    Billing Service (Server)
         ↓ (Process)
    BillingServiceGrpcService.createBillingAccount()
         ↓
    Send response back
         ↓
Patient Service continues processing
```

### Workflow 3: Asynchronous Kafka Event
```
Patient Service (Producer)
         ↓ (Publish)
    KafkaProducer.sendEvent()
         ↓
    Kafka Topic: "patient"
         ↓ (Consume async)
    Analytics Service (Consumer)
         ↓
    KafkaConsumer.consumeEvent()
         ↓
    Process and log event
```

## 📝 Protocol Buffer Definitions

### PatientEvent (patient_event.proto)
```protobuf
syntax = "proto3";
package patient.events;

message PatientEvent {
    string id = 1;
    string name = 2;
    string email = 3;
    string event_type = 4;  // e.g., "PatientCreated"
}
```

### BillingService (billing_service.proto)
```protobuf
syntax = "proto3";
option java_package = "billing";

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
    string status = 2;  // "ACTIVE", "PENDING", etc.
}
```

## 🧪 Testing

### Unit Tests
```bash
# Run tests for patient-service
cd patient-service
mvn test

# Run tests for all services
mvn test -pl .
```

### Integration Tests with HTTP Files
Use IntelliJ's REST client or Postman:
- Located in: `api-Requests/Patient-Service/`
- Files: `create-patient.http`, `Get-Patients.http`, etc.

### gRPC Testing
- Use `grpcurl` or Postman
- Files in: `grpc-request/billing-service/`

## 🔐 Exception Handling

### Custom Exceptions
- `PatientNotFindException` - When patient not found
- `EmailAlreadyExistsException` - When email already registered
- `GlobalExceptionHandler` - Centralized error handling

### Response Format
```json
{
  "timestamp": "2024-04-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Patient with ID: xxx not found",
  "path": "/patients/xxx"
}
```

## 🏢 Enterprise Features

### 1. Input Validation
- Bean Validation (Jakarta)
- Custom validator groups for create vs update
- `CreatePatientValidatorGroup` for strict validation

### 2. Data Mapper
- `PatientMapper` for DTO conversions
- Separation of concerns
- Type safety

### 3. Logging
- SLF4J with Logback
- Structured logging in all services
- Log levels configurable via `application.properties`

### 4. Configuration Management
- Service discovery via Kafka broker addresses
- Environment-based properties
- gRPC client configuration

## 🐳 Docker & Deployment

### Build Docker Images
```bash
# Build patient-service image
docker build -f patient-service/Dockerfile -t patient-service:latest ./patient-service

# Build billing-service image
docker build -f billing-service/Dockerfile -t billing-service:latest ./billing-service

# Build analytics-service image
docker build -f analytics-service/Dockerfile -t analytics-service:latest ./analytics-service
```

### Docker Compose
```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

## 🎓 Key Learning Outcomes

This project demonstrates:

1. **Microservices Architecture**
   - Service isolation and independence
   - Database per service pattern
   - Service-to-service communication

2. **REST API Design**
   - RESTful principles
   - Proper HTTP status codes
   - DTO pattern for API contracts

3. **gRPC Communication**
   - Protocol Buffers
   - Unary RPC calls
   - Service interface definition

4. **Event-Driven Architecture**
   - Kafka topic publishing/subscribing
   - Asynchronous message processing
   - Event serialization with Protobuf

5. **Spring Framework Features**
   - Spring Boot auto-configuration
   - Dependency injection
   - Spring Data JPA
   - Spring Kafka integration
   - Global exception handling

6. **Java Best Practices**
   - Clean code principles
   - SOLID principles
   - Design patterns (DTO, Mapper, etc.)
   - Proper exception handling

7. **Testing & Validation**
   - Bean validation with groups
   - Integration testing
   - API testing with HTTP clients

## 📊 Metrics & Monitoring

### Logging
All services log:
- Request/response payloads
- Event production/consumption
- gRPC service calls
- Database operations

### Kafka Monitoring
```bash
# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Monitor topic messages
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic patient --from-beginning
```

## 🔄 Version Control & GitHub

### Push to GitHub
```bash
# Initialize git (if not done)
git init

# Add remote
git remote add origin https://github.com/yourusername/patient-management.git

# Create .gitignore
echo "target/" >> .gitignore
echo ".idea/" >> .gitignore
echo ".env" >> .gitignore

# Commit and push
git add .
git commit -m "Initial commit: Patient Management Microservices"
git branch -M main
git push -u origin main
```

## 📋 Git Workflow

```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes
git add .
git commit -m "feat: Add new feature description"

# Push feature branch
git push origin feature/new-feature

# Create Pull Request on GitHub
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

MIT License - feel free to use this project for learning and commercial purposes.

## 👤 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Spring Boot community
- Kafka documentation
- gRPC framework
- Protocol Buffers

## 📞 Support

For issues and questions:
1. Check existing GitHub issues
2. Create a new issue with detailed description
3. Include error logs and steps to reproduce

## 🎯 Future Enhancements

- [ ] Add authentication with JWT
- [ ] Implement circuit breaker pattern (Resilience4j)
- [ ] Add distributed tracing (Spring Cloud Sleuth)
- [ ] Implement metrics collection (Micrometer)
- [ ] Add API rate limiting
- [ ] Implement saga pattern for distributed transactions
- [ ] Add API versioning
- [ ] Implement caching with Redis
- [ ] Add GraphQL endpoint
- [ ] Kubernetes deployment manifests (Helm charts)

---

**Last Updated**: April 7, 2026  
**Status**: Production Ready 🚀

