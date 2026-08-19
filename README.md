# DocMind AI - RAG System

A comprehensive Retrieval-Augmented Generation (RAG) system built with Spring Boot that enables intelligent document processing, semantic search, and AI-powered conversations.

## 📋 Overview

DocMind AI is a sophisticated backend system designed to:
- **Upload and Process Documents**: Support PDF document uploads with automatic text extraction
- **Semantic Search**: Store and search documents using vector embeddings via Qdrant
- **AI-Powered Chat**: Provide intelligent responses based on uploaded documents using Ollama LLM
- **Document Management**: Track document status, metadata, and processing information

## 🏗️ Architecture

### Technology Stack

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 4.1.0 |
| **Language** | Java 21 |
| **Database** | PostgreSQL |
| **Vector DB** | Qdrant |
| **LLM** | Ollama |
| **Build Tool** | Gradle |
| **ORM** | Spring Data JPA (Hibernate) |

### Dependencies

- **Spring Boot Starters**: Web, Data JPA, Validation, DevTools
- **PDF Processing**: Apache PDFBox 3.0.5
- **Utilities**: Lombok for boilerplate reduction
- **Database**: PostgreSQL Driver
- **Testing**: JUnit Platform with Spring Boot Test Starters

## 📁 Project Structure

```
docmind-ai/
├── src/
│   ├── main/
│   │   ├── java/com/docmind/
│   │   │   ├── controller/              # REST API Endpoints
│   │   │   │   ├── DocumentController   # Document upload endpoints
│   │   │   │   ├── ChatController       # Chat/Question endpoints
│   │   │   │   └── HealthController     # Health check endpoint
│   │   │   ├── service/                 # Business Logic Layer
│   │   │   │   ├── DocumentService      # Document management interface
│   │   │   │   ├── ChatService          # Chat service interface
│   │   │   │   ├── PdfService           # PDF processing interface
│   │   │   │   ├── EmbeddingService     # Vector embedding interface
│   │   │   │   ├── QdrantService        # Vector DB interface
│   │   │   │   ├── OllamaService        # LLM interface
│   │   │   │   ├── TextChunkService     # Text chunking interface
│   │   │   │   └── impl/                # Implementation classes
│   │   │   ├── repository/              # Data Access Layer
│   │   │   │   └── DocumentRepository   # JPA Repository for Document entity
│   │   │   ├── entity/                  # Domain Models
│   │   │   │   ├── Document             # Document entity with metadata
│   │   │   │   └── BaseEntity           # Base class with audit fields
│   │   │   ├── dto/                     # Data Transfer Objects
│   │   │   │   ├── EmbeddingRequest/Response
│   │   │   │   ├── SearchRequest/Response
│   │   │   │   ├── PointRequest
│   │   │   │   ├── request/ChatRequest, OllamaRequest
│   │   │   │   └── response/ChatResponse, OllamaResponse
│   │   │   ├── enums/                   # Enumerations
│   │   │   │   └── DocumentStatus       # Document processing status enum
│   │   │   ├── config/                  # Configuration Classes
│   │   │   │   └── JpaAuditConfig       # JPA auditing configuration
│   │   │   └── DocMindAIApplication.java  # Main application class
│   │   └── resources/
│   │       └── application.properties   # Application configuration
│   ├── test/
│   │   └── java/com/docmind/
│   │       └── DocMindAIApplicationTests.java
├── build.gradle                         # Gradle build configuration
├── settings.gradle                      # Gradle settings
├── gradlew / gradlew.bat               # Gradle wrapper scripts
└── uploads/                             # Directory for uploaded files

```

## 🔧 Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/docmind_ai
spring.datasource.username=postgres
spring.datasource.password=admin
```

### JPA/Hibernate Configuration
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Server Configuration
```properties
server.port=8080
file.upload-dir=uploads
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- PostgreSQL database
- Qdrant vector database instance
- Ollama LLM service
- Gradle (or use gradlew wrapper)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone XXXXX.git
   cd docmind-ai
   ```

2. **Configure environment variables**
   - Update `src/main/resources/application.properties` with your:
     - PostgreSQL connection details
     - Qdrant service endpoint
     - Ollama service endpoint

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`

## 📡 API Endpoints

### Swagger UI Documentation
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

### Health Check
**GET** `/health`
- Returns the health status of the DocMind AI service
- Response: `"DocMind AI is running successfully!"`

### Document Management

**POST** `/documents/upload`
- Upload a PDF document for processing
- Request: Multipart file upload (PDF)
- Response: Upload status message
- Features:
  - Automatic PDF text extraction
  - Text chunking for semantic search
  - Vector embedding generation
  - Document metadata storage in PostgreSQL
  - Vector storage in Qdrant

### Chat/Question Answering

**POST** `/chat`
- Submit a question to get AI-powered response based on uploaded documents
- Request Body:
  ```json
  {
    "question": "Your question here"
  }
  ```
- Response:
  ```json
  {
    "response": "AI generated answer"
  }
  ```
- Process:
  1. Generates embedding for the question
  2. Searches Qdrant for relevant document chunks
  3. Sends context + question to Ollama LLM
  4. Returns generated response

## 🏢 Core Components

### Controllers
- **DocumentController**: Handles document uploads and processing
- **ChatController**: Handles chat queries and responses
- **HealthController**: Service health monitoring

### Services
- **DocumentService**: Document lifecycle management (upload, storage, retrieval)
- **ChatService**: Chat query orchestration and response generation
- **PdfService**: PDF text extraction and processing
- **EmbeddingService**: Vector embedding generation for text chunks
- **QdrantService**: Vector database operations (store, search, delete)
- **OllamaService**: LLM integration for text generation
- **TextChunkService**: Text splitting and chunking strategies

### Data Models
- **Document Entity**: Stores document metadata with status tracking
  - ID, original filename, stored filename, file path
  - File size, MIME type, processing status
  - Timestamps for audit trail

### DTOs
- **ChatRequest/Response**: Chat interaction models
- **EmbeddingRequest/Response**: Embedding generation models
- **SearchRequest/Response**: Vector search models
- **OllamaRequest/Response**: LLM interaction models

## 📊 Workflow

```
Document Upload Flow:
1. User uploads PDF via /documents/upload
2. PDF is stored in local filesystem (uploads/)
3. Text extraction via Apache PDFBox
4. Text split into chunks (TextChunkService)
5. Embeddings generated for each chunk (EmbeddingService)
6. Document metadata saved to PostgreSQL
7. Embeddings and vectors stored in Qdrant
8. Return success status to user

Chat/Query Flow:
1. User submits question via /chat endpoint
2. Question embedding generated (EmbeddingService)
3. Vector search performed against Qdrant
4. Top-K relevant chunks retrieved
5. Context + question sent to Ollama LLM
6. AI response generated and returned
```

## 🔐 Document Status

The system tracks document processing status through the `DocumentStatus` enum:
- **PENDING**: Document uploaded, processing in queue
- **PROCESSING**: Currently extracting text and generating embeddings
- **COMPLETED**: Successfully processed and indexed
- **FAILED**: Processing encountered an error

## 📝 Entity Relationships

**Document Entity**:
- Extends `BaseEntity` for audit trail (created_at, updated_at, created_by, updated_by)
- Persisted in PostgreSQL `documents` table
- Tracked by `DocumentRepository`
- Integrates with vector storage in Qdrant

## 🛠️ Development

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Run with DevTools
DevTools is enabled for hot reload during development:
```bash
./gradlew bootRun
```

### Code Style
- Uses Lombok for reducing boilerplate code
- Follows Spring Boot best practices
- Organized into layers: Controller → Service → Repository

## 📦 Project Metadata

- **Group ID**: com.docmind
- **Artifact ID**: docmind-ai
- **Version**: 0.0.1-SNAPSHOT
- **Java Version**: 21
- **Spring Boot Version**: 4.1.0
- **Gradle Version**: Compatible with wrapper

## 🔄 Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT APPLICATION                       │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│             REST API (DocumentController, ChatController)    │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│         SERVICE LAYER (Business Logic & Orchestration)      │
└─────────────────────────────────────────────────────────────┘
                              ↕
         ┌────────────────────┼────────────────────┐
         ↕                    ↕                    ↕
    ┌─────────┐         ┌──────────┐         ┌──────────┐
    │PostgreSQL│         │ Qdrant   │         │  Ollama  │
    │Database  │         │ Vec DB   │         │   LLM    │
    └─────────┘         └──────────┘         └──────────┘
```

## 🚨 Error Handling

- Validation errors are managed through Spring Boot validation annotations
- File operations handle IO exceptions
- Database operations include transaction management
- REST endpoints return appropriate HTTP status codes

## 🎯 Future Enhancements

- Multi-format document support (DOCX, TXT, etc.)
- Advanced chunking strategies
- Prompt optimization
- Fine-tuning LLM responses
- Document versioning
- User authentication & authorization
- Rate limiting
- Caching layer for embeddings
- Monitoring and logging

## 📧 Support

For issues and questions, please refer to the repository or contact the development team.

## 📄 License

This project is part of the DocMind AI initiative.

---

**Last Updated**: 2026-07-20
**Version**: 0.0.1-SNAPSHOT
