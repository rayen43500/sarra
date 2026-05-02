package org.example.backend.web.dto.dashboard;

public record DashboardStatsDto(
        long totalCertificates,
        long activeCertificates,
        long expiredCertificates,
        long revokedCertificates,
        long totalUsers,
        long totalExams
) {
}
