package com.kirithika.studentadmission.dto.request;

import com.kirithika.studentadmission.enums.DocumentVerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerificationRequest {

    private DocumentVerificationStatus verificationStatus;

    private String verificationRemarks;
}