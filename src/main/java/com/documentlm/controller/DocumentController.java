package com.documentlm.controller;

import com.documentlm.entity.Document;
import com.documentlm.repository.DocumentRepository;
import com.documentlm.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Document API", description = "Upload and manage documents")
public class DocumentController {

    private final DocumentService service;
    private final DocumentRepository documentRepository;

    @Operation(summary = "Upload a PDF document")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.upload(file));
    }

    @Operation(summary = "Open a previously uploaded document by stored file name")
    @GetMapping("/{storedFileName}")
    public ResponseEntity<Resource> openDocument(@PathVariable String storedFileName) {
        Document document = documentRepository.findAll().stream()
                .filter(item -> item.getStoredFileName().equals(storedFileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Document not found for file: " + storedFileName));

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        MediaType mediaType = document.getMimeType() != null && !document.getMimeType().isBlank()
                ? MediaType.parseMediaType(document.getMimeType())
                : MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete one document from the database and Qdrant")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(service.deleteDocument(documentId));
    }

    @Operation(summary = "Clear all documents from the database and Qdrant")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearDocuments() {
        return ResponseEntity.ok(service.clearDocuments());
    }
}