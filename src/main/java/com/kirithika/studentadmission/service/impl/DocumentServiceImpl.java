package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.response.DocumentResponse;
import com.kirithika.studentadmission.entity.Application;
import com.kirithika.studentadmission.entity.Document;
import com.kirithika.studentadmission.enums.DocumentType;
import com.kirithika.studentadmission.exception.ResourceNotFoundException;
import com.kirithika.studentadmission.repository.ApplicationRepository;
import com.kirithika.studentadmission.repository.DocumentRepository;
import com.kirithika.studentadmission.service.interfaces.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import com.kirithika.studentadmission.dto.request.DocumentVerificationRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            ApplicationRepository applicationRepository,
            FileStorageService fileStorageService) {

        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.fileStorageService = fileStorageService;
    }

    // ============================================================
    // STUDENT - UPLOAD DOCUMENT
    // ============================================================

    @Override
    public DocumentResponse uploadDocument(
            String studentEmail,
            Long applicationId,
            DocumentType documentType,
            MultipartFile file) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        /*
         * Students can upload documents only to
         * their own application.
         */
        if (!application.getStudent()
                .getUser()
                .getEmail()
                .equals(studentEmail)) {

            throw new ResourceNotFoundException(
                    "Application not found");
        }

        String storedFileName =
                fileStorageService.storeFile(file);

        Document document = Document.builder()
                .application(application)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileUrl(storedFileName)
                .build();

        Document saved =
                documentRepository.save(document);

        return mapToResponse(saved);
    }

    // ============================================================
    // VIEW DOCUMENTS
    // STUDENT -> OWN APPLICATION ONLY
    // ADMIN   -> ANY APPLICATION
    // ============================================================

    @Override
    public List<DocumentResponse> getDocumentsByApplication(
            String userEmail,
            String role,
            Long applicationId) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        if ("ADMIN".equalsIgnoreCase(role)) {

            return documentRepository
                    .findByApplicationId(applicationId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (!application.getStudent()
                .getUser()
                .getEmail()
                .equals(userEmail)) {

            throw new ResourceNotFoundException(
                    "Application not found");
        }

        return documentRepository
                .findByApplicationId(applicationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public Resource getDocumentFile(
            String userEmail,
            String role,
            Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"));

        Application application =
                document.getApplication();

        if ("ADMIN".equalsIgnoreCase(role)) {

            return fileStorageService.loadFile(
                    document.getFileUrl()
            );
        }

        if (!application.getStudent()
                .getUser()
                .getEmail()
                .equals(userEmail)) {

            throw new ResourceNotFoundException(
                    "Document not found");
        }

        return fileStorageService.loadFile(
                document.getFileUrl()
        );
    }


    private DocumentResponse mapToResponse(
            Document document) {

        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .uploadedAt(document.getUploadedAt())
                .verificationStatus(document.getVerificationStatus())
                .verificationRemarks(document.getVerificationRemarks())
                .build();
    }

    @Override
    public DocumentResponse verifyDocument(
            Long documentId,
            DocumentVerificationRequest request) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"));

        document.setVerificationStatus(
                request.getVerificationStatus()
        );

        document.setVerificationRemarks(
                request.getVerificationRemarks()
        );

        Document updated =
                documentRepository.save(document);

        return mapToResponse(updated);
    }
}