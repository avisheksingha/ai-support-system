package com.aisupport.auth.dto;

import java.time.Instant;

import com.aisupport.common.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private Boolean locked;
    private Instant createdAt;
}
