package com.aisupport.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aisupport.auth.dto.response.UserResponse;
import com.aisupport.auth.entity.User;
import com.aisupport.auth.mapper.UserMapper;
import com.aisupport.auth.repository.RefreshTokenRepository;
import com.aisupport.auth.repository.UserRepository;
import com.aisupport.common.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void updateUserRole_shouldUpdateAndReturnResponse() {

        User user = User.builder()
                .id(1L)
                .role(UserRole.CUSTOMER)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .role(UserRole.AGENT)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userManagementService.updateUserRole(1L, UserRole.AGENT);

        assertThat(result.getRole()).isEqualTo(UserRole.AGENT);

        assertThat(user.getRole()).isEqualTo(UserRole.AGENT);

        verify(userMapper).toResponse(user);
    }

    @Test
    void lockUser_shouldLockAndReturnResponse() {

        User user = User.builder()
                .id(1L)
                .locked(false)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .locked(true)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result = userManagementService.lockUser(1L);

        assertThat(result.getLocked()).isTrue();

        assertThat(user.isLocked()).isTrue();

        verify(userMapper).toResponse(user);
    }
}
