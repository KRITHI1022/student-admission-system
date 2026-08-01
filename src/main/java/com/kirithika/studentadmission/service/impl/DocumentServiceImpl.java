package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.response.DocumentResponse;
import com.kirithika.studentadmission.entity.*;
import com.kirithika.studentadmission.enums.DocumentType;
import com.kirithika.studentadmission.exception.ResourceNotFoundException;
import com.kirithika.studentadmission.repository.*;
import com.kirithika.studentadmission.service.interfaces.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               ApplicationRepository applicationRepository,
                               FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public DocumentResponse uploadDocument(String studentEmail, Long applicationId,
                                           DocumentType documentType, MultipartFile file) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getStudent().getUser().getEmail().equals(studentEmail)) {
            throw new ResourceNotFoundException("Application not found");
        }

        String storedFileName = fileStorageService.storeFile(file);

        Document document = Document.builder()
                .application(application)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileUrl(storedFileName)
                .build();

        Document saved = documentRepository.save(document);

        return mapToResponse(saved);
    }

    @Override
    public List<DocumentResponse> getDocumentsByApplication(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}