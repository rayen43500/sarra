package org.example.backend.repository;

import org.example.backend.domain.entity.CertificateVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateVerificationRepository extends JpaRepository<CertificateVerification, Long> {
}
