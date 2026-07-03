package com.aisupport.auth.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.aisupport.auth.entity.User;
import com.aisupport.common.enums.UserRole;
import com.aisupport.auth.repository.UserRepository;

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

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@aisupport.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating default admin user for local environment...");
            
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .locked(false)
                    .build();
            
            userRepository.save(admin);
            log.info("Default admin user created successfully with email: {} and password: {}", adminEmail, "admin123");
        } else {
            log.debug("Default admin user already exists. Skipping creation.");
        }
    }
}
