package com.aisupport.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.auth.entity.LoginAudit;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}
