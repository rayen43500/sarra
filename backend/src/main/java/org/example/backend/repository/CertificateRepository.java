package org.example.backend.repository;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.CertificateStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateCode(String certificateCode);
    List<Certificate> findByIssuedTo(User user);
    long countByStatus(CertificateStatus status);
}
