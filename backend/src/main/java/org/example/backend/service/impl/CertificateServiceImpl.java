package org.example.backend.service.impl;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.CertificateVerification;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.CertificateStatus;
import org.example.backend.domain.enums.NotificationType;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.CertificateVerificationRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.AuditLogService;
import org.example.backend.service.CertificateService;
import org.example.backend.service.NotificationService;
import org.example.backend.service.PdfService;
import org.example.backend.service.QrCodeService;
import org.example.backend.web.dto.certificate.CertificateDto;
import org.example.backend.web.dto.certificate.CreateCertificateRequest;
import org.example.backend.web.dto.verification.CertificateVerificationResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final QrCodeService qrCodeService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public CertificateServiceImpl(
            CertificateRepository certificateRepository,
            CertificateVerificationRepository verificationRepository,
            UserRepository userRepository,
            PdfService pdfService,
            QrCodeService qrCodeService,
            AuditLogService auditLogService,
            NotificationService notificationService
    ) {
        this.certificateRepository = certificateRepository;
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
        this.qrCodeService = qrCodeService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    public CertificateDto createCertificate(CreateCertificateRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        User client = userRepository.findById(request.issuedToUserId()).orElseThrow();

        Certificate certificate = new Certificate();
        certificate.setCertificateCode("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        certificate.setTitle(request.title());
        certificate.setDescription(request.description());
        certificate.setIssuedBy(admin);
        certificate.setIssuedTo(client);
        certificate.setIssueDate(request.issueDate() == null ? LocalDate.now() : request.issueDate());
        certificate.setExpiryDate(request.expiryDate());
        certificate.setStatus(CertificateStatus.ACTIVE);

        String payload = "http://localhost:4200/verify?code=" + certificate.getCertificateCode();
        certificate.setQrPayload(payload);
        certificate.setDigitalSignature(sign(payload));

        qrCodeService.generateQrCode(payload, certificate.getCertificateCode());

        String pdfPath = pdfService.generateCertificatePdf(
            certificate.getCertificateCode(),
            certificate.getTitle(),
            certificate.getDescription(),
            client.getFirstName() + " " + client.getLastName(),
            certificate.getStatus().name(),
            Path.of("generated/qrcodes", certificate.getCertificateCode() + ".png").toString()
        );
        certificate.setPdfPath(pdfPath);

        Certificate saved = certificateRepository.save(certificate);

        notificationService.notifyUser(
                client.getId(),
                "Nouveau certificat",
                "Un certificat a ete attribue: " + saved.getTitle(),
                NotificationType.SUCCESS
        );

        auditLogService.log(admin.getId(), "CREATE_CERTIFICATE", "CERTIFICATE", saved.getId().toString(), "Certificate created", null);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateDto> listAdminCertificates() {
        return certificateRepository.findAll().stream().map(this::toDtoWithDynamicStatus).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateDto> listClientCertificates(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail).orElseThrow();
        return certificateRepository.findByIssuedTo(client).stream().map(this::toDtoWithDynamicStatus).toList();
    }

    @Override
    public CertificateDto revokeCertificate(Long id, String reason, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        Certificate cert = certificateRepository.findById(id).orElseThrow();
        cert.setStatus(CertificateStatus.REVOKED);
        cert.setRevokedAt(Instant.now());
        cert.setRevokeReason(reason);
        Certificate saved = certificateRepository.save(cert);

        auditLogService.log(admin.getId(), "REVOKE_CERTIFICATE", "CERTIFICATE", saved.getId().toString(), reason, null);
        return toDto(saved);
    }

    @Override
    public CertificateVerificationResponse verifyByCode(String code, String method, String ip, String userAgent) {
        Certificate cert = certificateRepository.findByCertificateCode(code).orElse(null);
        String status;
        boolean valid;
        if (cert == null) {
            status = "NOT_FOUND";
            valid = false;
        } else {
            String expectedPayload = "http://localhost:4200/verify?code=" + cert.getCertificateCode();
            String expectedSignature = sign(expectedPayload);
            boolean signatureValid = expectedSignature.equals(cert.getDigitalSignature());
            boolean expired = cert.getExpiryDate() != null && cert.getExpiryDate().isBefore(LocalDate.now());

            if (!signatureValid) {
                status = "TAMPERED";
                valid = false;
            } else if (cert.getStatus() == CertificateStatus.REVOKED) {
                status = "REVOKED";
                valid = false;
            } else if (expired) {
                status = "EXPIRED";
                valid = false;
                cert.setStatus(CertificateStatus.EXPIRED);
                certificateRepository.save(cert);
            } else {
                status = "VALID";
                valid = true;
            }
        }

        CertificateVerification verification = new CertificateVerification();
        verification.setCertificate(cert);
        verification.setVerificationMethod(method);
        verification.setRequesterIp(ip);
        verification.setUserAgent(userAgent);
        verification.setResultStatus(status);
        verificationRepository.save(verification);

        if (cert == null) {
            return new CertificateVerificationResponse(code, "", null, null, status, false);
        }

        String holder = cert.getIssuedTo().getFirstName() + " " + cert.getIssuedTo().getLastName();
        return new CertificateVerificationResponse(
                cert.getCertificateCode(),
                holder,
                cert.getIssueDate(),
                cert.getExpiryDate(),
                status,
                valid
        );
    }

    private CertificateDto toDto(Certificate certificate) {
        String holder = certificate.getIssuedTo().getFirstName() + " " + certificate.getIssuedTo().getLastName();
        return new CertificateDto(
                certificate.getId(),
                certificate.getCertificateCode(),
                certificate.getTitle(),
                holder,
                certificate.getIssueDate(),
                certificate.getExpiryDate(),
                certificate.getStatus().name(),
                certificate.getPdfPath()
        );
    }

    private CertificateDto toDtoWithDynamicStatus(Certificate certificate) {
        if (certificate.getStatus() != CertificateStatus.REVOKED
                && certificate.getExpiryDate() != null
                && certificate.getExpiryDate().isBefore(LocalDate.now())) {
            certificate.setStatus(CertificateStatus.EXPIRED);
            certificateRepository.save(certificate);
        }
        return toDto(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public Path getPdfPathForAdmin(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId).orElseThrow();
        return Path.of(certificate.getPdfPath());
    }

    @Override
    @Transactional(readOnly = true)
    public Path getPdfPathForClient(Long certificateId, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail).orElseThrow();
        Certificate certificate = certificateRepository.findById(certificateId).orElseThrow();
        if (!certificate.getIssuedTo().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Forbidden certificate access");
        }
        return Path.of(certificate.getPdfPath());
    }

    private String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Signature generation failed", ex);
        }
    }
}
