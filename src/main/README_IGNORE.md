# KnowledgeHub AI

## Overview

KnowledgeHub AI is a Retrieval-Augmented Generation (RAG) application
built with Java and Spring Boot. It allows users to upload PDF
documents, generate embeddings, store vectors in Qdrant, and ask
questions about uploaded content using Ollama.

## Tech Stack

-   Java 21
-   Spring Boot
-   Gradle
-   PostgreSQL
-   Apache PDFBox
-   Ollama
-   Qdrant

## Prerequisites

-   Java 21
-   PostgreSQL
-   Ollama
-   Qdrant
-   Git
-   IntelliJ IDEA

## Installation

1.  Clone the repository.
2.  Create the PostgreSQL database.
3.  Configure application.properties.
4.  Start Qdrant.
5.  Start Ollama.
6.  Run the Spring Boot application.

## Ollama Models

``` bash
ollama pull nomic-embed-text
ollama pull qwen2.5-coder:7b
ollama list
```

## Start Services

``` bash
ollama serve
```

Run Qdrant executable:

    qdrant.exe


### Test Embedding API

```
curl http://localhost:11434/api/embeddings \

-d '{

"model":"nomic-embed-text",

"prompt":"Sai Krishna is 29 years old"

}/
```
## Qdrant Installation on Docker 

```

docker run -d --name qdrant -p 6333:6333 -v qdrant_storage:/qdrant/storage qdrant/qdrant

http://localhost:6333/dashboard

```



## Run Spring boot application


## Project Flow

1.  Upload PDF
2.  Extract Text
3.  Chunk Text
4.  Generate Embeddings
5.  Store in Qdrant
6.  Ask Question
7.  Retrieve Context
8.  Generate Answer

## API Endpoints

  Method   Endpoint            
  -------- ------------------- 
  POST     /documents/upload   

  POST     /chat 

## Troubleshooting

-   Ensure PostgreSQL is running.
-   Ensure Ollama is running.
-   Ensure Qdrant is running.
-   Verify required models are installed.
