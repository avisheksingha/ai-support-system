package com.aisupport.auth.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.auth.dto.UserResponse;
import com.aisupport.auth.entity.User;
import com.aisupport.common.enums.UserRole;
import com.aisupport.auth.exception.AuthException;
import com.aisupport.auth.mapper.UserMapper;
import com.aisupport.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, UserRole newRole) {
        User user = getUser(userId);
        user.setRole(newRole);
        log.info("User {} role updated to {}", userId, newRole);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse lockUser(Long userId) {
        User user = getUser(userId);
        user.setLocked(true);
        log.info("User {} locked", userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse unlockUser(Long userId) {
        User user = getUser(userId);
        user.setLocked(false);
        log.info("User {} unlocked", userId);
        return userMapper.toResponse(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found with id: " + userId));
    }
}
