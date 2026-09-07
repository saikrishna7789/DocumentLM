package com.documentlm.service;

import java.io.IOException;

public interface PdfService {

    String extractText(String filePath) throws IOException;

}