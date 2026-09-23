package com.kirithika.studentadmission.controller;

import com.kirithika.studentadmission.dto.response.DocumentResponse;
import com.kirithika.studentadmission.enums.DocumentType;
import com.kirithika.studentadmission.service.interfaces.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaTypeFactory;
import com.kirithika.studentadmission.dto.request.DocumentVerificationRequest;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService) {

        this.documentService = documentService;
    }

    // ============================================================
    // STUDENT - UPLOAD DOCUMENT
    // ============================================================

    @PostMapping(
            value = "/upload/{applicationId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long applicationId,
            @RequestParam("documentType")
            DocumentType documentType,
            @RequestParam("file")
            MultipartFile file,
            Authentication authentication) {

        String studentEmail =
                authentication.getName();

        DocumentResponse response =
                documentService.uploadDocument(
                        studentEmail,
                        applicationId,
                        documentType,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ============================================================
    // VIEW APPLICATION DOCUMENTS
    // STUDENT -> OWN APPLICATIONS ONLY
    // ADMIN   -> ANY APPLICATION
    // ============================================================

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DocumentResponse>> getByApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority()
                                .replace("ROLE_", ""))
                .orElse("");

        List<DocumentResponse> documents =
                documentService.getDocumentsByApplication(
                        userEmail,
                        role,
                        applicationId
                );

        return ResponseEntity.ok(documents);
    }
    @GetMapping("/view/{documentId}")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId,
            Authentication authentication) {

        String userEmail =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority()
                                        .replace("ROLE_", ""))
                        .orElse("");

        Resource resource =
                documentService.getDocumentFile(
                        userEmail,
                        role,
                        documentId
                );

        MediaType mediaType =
                MediaTypeFactory
                        .getMediaType(resource.getFilename())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }

    @PutMapping("/admin/{documentId}/verify")
    public ResponseEntity<DocumentResponse> verifyDocument(
            @PathVariable Long documentId,
            @RequestBody DocumentVerificationRequest request) {

        return ResponseEntity.ok(
                documentService.verifyDocument(
                        documentId,
                        request
                )
        );
    }
}