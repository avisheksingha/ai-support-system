package com.aisupport.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.aisupport.auth.entity.User;
import com.aisupport.auth.repository.UserRepository;
import com.aisupport.common.enums.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes default data for the local development environment.
 * Creates a default admin user if one does not already exist.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating default admin user for local environment...");
            
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .locked(false)
                    .build();
            
            userRepository.save(admin);
            log.info("Default admin user created successfully with email: {}", adminEmail); // Removed plain text password from logs for security
        } else {
            log.debug("Default admin user already exists. Skipping creation.");
        }
    }
}
