package com.kirithika.studentadmission.dto.response;

import com.kirithika.studentadmission.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
}