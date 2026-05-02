package org.example.backend.service;

import org.example.backend.web.dto.certificate.CertificateDto;
import org.example.backend.web.dto.certificate.CreateCertificateRequest;
import org.example.backend.web.dto.verification.CertificateVerificationResponse;
import java.util.List;
import java.nio.file.Path;

public interface CertificateService {
    CertificateDto createCertificate(CreateCertificateRequest request, String adminEmail);
    List<CertificateDto> listAdminCertificates();
    List<CertificateDto> listClientCertificates(String clientEmail);
    CertificateDto revokeCertificate(Long id, String reason, String adminEmail);
    CertificateVerificationResponse verifyByCode(String code, String method, String ip, String userAgent);
    Path getPdfPathForAdmin(Long certificateId);
    Path getPdfPathForClient(Long certificateId, String clientEmail);
}
