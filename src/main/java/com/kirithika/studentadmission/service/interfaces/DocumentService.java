package com.kirithika.studentadmission.service.interfaces;

import com.kirithika.studentadmission.dto.response.DocumentResponse;
import com.kirithika.studentadmission.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(String studentEmail, Long applicationId,
                                    DocumentType documentType, MultipartFile file);

    List<DocumentResponse> getDocumentsByApplication(Long applicationId);
}