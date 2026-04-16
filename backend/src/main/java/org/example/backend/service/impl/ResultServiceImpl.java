package org.example.backend.service.impl;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.Exam;
import org.example.backend.domain.entity.Result;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.CertificateStatus;
import org.example.backend.domain.enums.NotificationType;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.ResultRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.NotificationService;
import org.example.backend.service.PdfService;
import org.example.backend.service.QrCodeService;
import org.example.backend.service.ResultService;
import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.result.SubmitResultRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final CertificateRepository certificateRepository;
    private final PdfService pdfService;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;

    public ResultServiceImpl(
            ResultRepository resultRepository,
            UserRepository userRepository,
            ExamRepository examRepository,
            CertificateRepository certificateRepository,
            PdfService pdfService,
            QrCodeService qrCodeService,
            NotificationService notificationService
    ) {
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.certificateRepository = certificateRepository;
        this.pdfService = pdfService;
        this.qrCodeService = qrCodeService;
        this.notificationService = notificationService;
    }

    @Override
    public ResultDto submit(SubmitResultRequest request, String clientEmail) {
        User user = userRepository.findByEmail(clientEmail).orElseThrow();
        Exam exam = examRepository.findById(request.examId()).orElseThrow();

        double max = request.maxScore() == null || request.maxScore() <= 0 ? 100.0 : request.maxScore();
        double score = request.score() == null ? 0.0 : request.score();
        double percentage = (score / max) * 100.0;

        Result result = new Result();
        result.setExam(exam);
        result.setUser(user);
        result.setScore(score);
        result.setMaxScore(max);
        result.setPercentage(percentage);
        boolean passed = percentage >= exam.getPassScore();
        result.setPassed(passed);
        result.setStartedAt(Instant.now());
        result.setSubmittedAt(Instant.now());
        result.setAttemptNumber(request.attemptNumber() == null ? 1 : request.attemptNumber());

        Result savedResult = resultRepository.save(result);
        if (passed) {
            createExamCertificate(user, exam);
        }
        return toDto(savedResult);
    }

    @Override
    public List<ResultDto> listForClient(String clientEmail) {
        User user = userRepository.findByEmail(clientEmail).orElseThrow();
        return resultRepository.findByUserIdOrderBySubmittedAtDesc(user.getId()).stream().map(this::toDto).toList();
    }

    @Override
    public List<ResultDto> listByExam(Long examId) {
        return resultRepository.findByExamIdOrderBySubmittedAtDesc(examId).stream().map(this::toDto).toList();
    }

    private ResultDto toDto(Result result) {
        return new ResultDto(
                result.getId(),
                result.getExam().getId(),
                result.getExam().getTitle(),
                result.getScore(),
                result.getMaxScore(),
                result.getPercentage(),
                result.getPassed(),
                result.getSubmittedAt()
        );
    }

    private void createExamCertificate(User user, Exam exam) {
        Certificate certificate = new Certificate();
        certificate.setCertificateCode("CERT-EXAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        certificate.setTitle("Exam Success - " + exam.getTitle());
        certificate.setDescription("Certificate generated automatically after successful exam completion.");
        certificate.setIssuedTo(user);
        certificate.setIssuedBy(user);
        certificate.setIssueDate(LocalDate.now());
        certificate.setExpiryDate(LocalDate.now().plusYears(1));
        certificate.setStatus(CertificateStatus.ACTIVE);

        String payload = "http://localhost:4200/verify?code=" + certificate.getCertificateCode();
        certificate.setQrPayload(payload);
        certificate.setDigitalSignature(sign(payload));

        qrCodeService.generateQrCode(payload, certificate.getCertificateCode());
        String pdfPath = pdfService.generateCertificatePdf(
                certificate.getCertificateCode(),
                certificate.getTitle(),
                user.getFirstName() + " " + user.getLastName(),
                certificate.getStatus().name(),
                Path.of("generated/qrcodes", certificate.getCertificateCode() + ".png").toString()
        );
        certificate.setPdfPath(pdfPath);
        certificateRepository.save(certificate);

        notificationService.notifyUser(
                user.getId(),
                "Certificat obtenu",
                "Felicitations, vous avez obtenu un nouveau certificat pour l examen " + exam.getTitle(),
                NotificationType.SUCCESS
        );
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
