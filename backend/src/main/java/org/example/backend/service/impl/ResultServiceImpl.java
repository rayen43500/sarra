package org.example.backend.service.impl;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.Exam;
import org.example.backend.domain.entity.Question;
import org.example.backend.domain.entity.QuestionOption;
import org.example.backend.domain.entity.Result;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.CertificateStatus;
import org.example.backend.domain.enums.NotificationType;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.QuestionOptionRepository;
import org.example.backend.repository.QuestionRepository;
import org.example.backend.repository.ResultRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.NotificationService;
import org.example.backend.service.PdfService;
import org.example.backend.service.QrCodeService;
import org.example.backend.service.ResultService;
import org.example.backend.web.dto.result.FraudSignalRequest;
import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.result.SubmitExamAnswersRequest;
import org.example.backend.web.dto.result.SubmitResultRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final CertificateRepository certificateRepository;
    private final PdfService pdfService;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;

    public ResultServiceImpl(
            ResultRepository resultRepository,
            UserRepository userRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            CertificateRepository certificateRepository,
            PdfService pdfService,
            QrCodeService qrCodeService,
            NotificationService notificationService
    ) {
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
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
        FraudAssessment fraud = assessFraud(null, 0);
        result.setFraudScore(fraud.score());
        result.setFraudSuspicious(fraud.suspicious());
        result.setFraudReason(fraud.reason());

        Result savedResult = resultRepository.save(result);
        if (passed) {
            createExamCertificate(user, exam);
        }
        return toDto(savedResult);
    }

    @Override
    public ResultDto submitAnswers(SubmitExamAnswersRequest request, String clientEmail) {
        User user = userRepository.findByEmail(clientEmail).orElseThrow();
        Exam exam = examRepository.findById(request.examId()).orElseThrow();

        List<Question> questions = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId());
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("This exam has no questions");
        }

        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        Map<Long, List<QuestionOption>> optionsByQuestion = questionOptionRepository.findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));

        Map<Long, Long> selectedOptionByQuestion = request.answers().stream()
                .collect(Collectors.toMap(
                        a -> a.questionId(),
                        a -> a.optionId(),
                        (first, second) -> second
                ));

        double maxScore = questions.stream().mapToDouble(q -> q.getPoints() == null ? 1.0 : q.getPoints()).sum();
        double score = 0.0;

        for (Question question : questions) {
            Long selectedOptionId = selectedOptionByQuestion.get(question.getId());
            if (selectedOptionId == null) {
                continue;
            }

            List<QuestionOption> options = optionsByQuestion.getOrDefault(question.getId(), List.of());
            boolean correct = options.stream().anyMatch(o -> o.getId().equals(selectedOptionId) && Boolean.TRUE.equals(o.getIsCorrect()));
            if (correct) {
                score += question.getPoints() == null ? 1.0 : question.getPoints();
            }
        }

        double percentage = maxScore <= 0 ? 0.0 : (score / maxScore) * 100.0;
        boolean passed = percentage >= exam.getPassScore();

        Result result = new Result();
        result.setExam(exam);
        result.setUser(user);
        result.setScore(score);
        result.setMaxScore(maxScore);
        result.setPercentage(percentage);
        result.setPassed(passed);
        result.setStartedAt(Instant.now());
        result.setSubmittedAt(Instant.now());
        result.setAttemptNumber(request.attemptNumber() == null ? 1 : request.attemptNumber());
        FraudAssessment fraud = assessFraud(request.fraud(), questions.size());
        result.setFraudScore(fraud.score());
        result.setFraudSuspicious(fraud.suspicious());
        result.setFraudReason(fraud.reason());

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
                result.getExam() != null ? result.getExam().getId() : 0L,
                result.getExam() != null ? result.getExam().getTitle() : "Examen inconnu",
                result.getScore(),
                result.getMaxScore(),
                result.getPercentage(),
                result.getPassed(),
                result.getSubmittedAt(),
                result.getFraudScore() == null ? 0 : result.getFraudScore(),
                Boolean.TRUE.equals(result.getFraudSuspicious()),
                result.getFraudReason() == null ? "Aucun signal suspect." : result.getFraudReason()
        );
    }

    private FraudAssessment assessFraud(FraudSignalRequest signal, int questionCount) {
        if (signal == null) {
            return new FraudAssessment(0, false, "Aucun signal suspect.");
        }

        int focusLoss = value(signal.focusLossCount());
        int copyPaste = value(signal.copyPasteCount());
        int fullscreenExit = value(signal.fullscreenExitCount());
        int totalSeconds = value(signal.totalSeconds());
        int remainingSeconds = value(signal.remainingSeconds());

        int score = Math.min(100, focusLoss * 15 + copyPaste * 25 + fullscreenExit * 20);
        List<String> reasons = new ArrayList<>();

        if (focusLoss >= 3) {
            reasons.add("plusieurs sorties de fenetre");
        }
        if (copyPaste > 0) {
            reasons.add("copier/coller ou menu contextuel pendant le quiz");
        }
        if (fullscreenExit > 0) {
            reasons.add("sortie du mode plein ecran");
        }
        if (totalSeconds > 0 && remainingSeconds > totalSeconds * 0.85 && questionCount >= 3) {
            score = Math.min(100, score + 25);
            reasons.add("soumission tres rapide");
        }

        boolean suspicious = score >= 40;
        String reason = reasons.isEmpty() ? "Aucun signal suspect." : String.join(", ", reasons);
        return new FraudAssessment(score, suspicious, reason);
    }

    private int value(Integer number) {
        return number == null ? 0 : Math.max(0, number);
    }

    private void createExamCertificate(User user, Exam exam) {
        Certificate certificate = new Certificate();
        certificate.setCertificateCode("CERT-EXAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        certificate.setTitle("Exam Success - " + exam.getTitle());
        certificate.setDescription("Certificate generated automatically after successful exam completion.");
        certificate.setIssuedTo(user);
        certificate.setIssuedBy(exam.getCreatedBy() == null ? user : exam.getCreatedBy());
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
            certificate.getDescription(),
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

    private record FraudAssessment(
            int score,
            boolean suspicious,
            String reason
    ) {
    }
}
