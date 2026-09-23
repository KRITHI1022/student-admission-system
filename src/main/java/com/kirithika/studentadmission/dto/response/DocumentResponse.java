package com.kirithika.studentadmission.dto.response;

import com.kirithika.studentadmission.enums.DocumentType;
import com.kirithika.studentadmission.enums.DocumentVerificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
    private DocumentVerificationStatus verificationStatus;
    private String verificationRemarks;
}