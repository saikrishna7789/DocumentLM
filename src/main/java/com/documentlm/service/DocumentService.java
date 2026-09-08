package com.documentlm.service;

import com.documentlm.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    String upload(MultipartFile file) throws IOException;

    List<Document> listDocuments();

    String deleteDocument(Long documentId);

    String clearDocuments();
}