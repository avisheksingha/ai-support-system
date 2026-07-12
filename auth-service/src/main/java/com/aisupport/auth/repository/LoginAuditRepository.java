package com.aisupport.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.auth.entity.LoginAudit;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}
