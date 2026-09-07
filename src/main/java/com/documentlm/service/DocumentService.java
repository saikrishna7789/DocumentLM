package com.documentlm.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {

    String upload(MultipartFile file) throws IOException;

    String deleteDocument(Long documentId);

    String clearDocuments();
}