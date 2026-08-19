package com.docmind.service.impl;

import com.docmind.entity.Document;
import com.docmind.enums.DocumentStatus;
import com.docmind.repository.DocumentRepository;
import com.docmind.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repository;

    private final PdfService pdfService;

    private final TextChunkService textChunkService;

    private final EmbeddingService embeddingService;

    private final QdrantService qdrantService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFileName =
                UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath);

        Document document = new Document();

        document.setOriginalFileName(file.getOriginalFilename());
        document.setStoredFileName(storedFileName);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setStatus(DocumentStatus.UPLOADED);

        repository.save(document);

        String extractedText = pdfService.extractText(filePath.toString());
        List<String> chunks = textChunkService.chunkText(extractedText);

        int chunkNumber = 1;

        for (String chunk : chunks) {
            List<Double> embedding =
                    embeddingService.generateEmbedding(chunk);

            qdrantService.storeEmbedding(
                    document.getId(),
                    chunkNumber++,
                    chunk,
                    embedding
            );
        }

        return "File uploaded successfully";
    }

    @Override
    @Transactional
    public String deleteDocument(Long documentId) {
        Document document = repository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete document file: " + document.getFilePath(), e);
        }

        qdrantService.deleteDocument(documentId);
        repository.delete(document);

        return "Document deleted successfully";
    }

    @Override
    @Transactional
    public String clearDocuments() {
        List<Document> documents = repository.findAll();

        for (Document document : documents) {
            try {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to delete document file: " + document.getFilePath(), e);
            }
        }

        qdrantService.clearCollection();
        repository.deleteAll();

        return "All documents cleared successfully";
    }
}