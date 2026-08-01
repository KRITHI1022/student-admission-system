package com.kirithika.studentadmission.controller;

import com.kirithika.studentadmission.dto.response.DocumentResponse;
import com.kirithika.studentadmission.enums.DocumentType;
import com.kirithika.studentadmission.service.interfaces.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload/{applicationId}", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long applicationId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String studentEmail = authentication.getName();
        DocumentResponse response = documentService.uploadDocument(
                studentEmail, applicationId, documentType, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DocumentResponse>> getByApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(documentService.getDocumentsByApplication(applicationId));
    }
}