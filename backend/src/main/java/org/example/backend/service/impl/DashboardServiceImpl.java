package org.example.backend.service.impl;

import org.example.backend.domain.enums.CertificateStatus;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.DashboardService;
import org.example.backend.web.dto.dashboard.DashboardStatsDto;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    public DashboardServiceImpl(CertificateRepository certificateRepository, UserRepository userRepository, ExamRepository examRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    @Override
    public DashboardStatsDto getStats() {
        long active = certificateRepository.countByStatus(CertificateStatus.ACTIVE);
        long expired = certificateRepository.countByStatus(CertificateStatus.EXPIRED);
        long revoked = certificateRepository.countByStatus(CertificateStatus.REVOKED);

        return new DashboardStatsDto(
                certificateRepository.count(),
                active,
                expired,
                revoked,
                userRepository.count(),
                examRepository.count()
        );
    }
}
