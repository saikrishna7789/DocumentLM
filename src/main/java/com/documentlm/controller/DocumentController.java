package com.documentlm.controller;

import com.documentlm.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Document API", description = "Upload and manage documents")
public class DocumentController {

    private final DocumentService service;

    @Operation(summary = "Upload a PDF document")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.upload(file));
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