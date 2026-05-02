package org.example.backend.web.controller;

import org.example.backend.domain.entity.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.CertificateService;
import org.example.backend.service.ExamService;
import org.example.backend.service.FileStorageService;
import org.example.backend.service.NotificationService;
import org.example.backend.service.ResultService;
import org.example.backend.service.UserService;
import org.example.backend.web.dto.certificate.CertificateDto;
import org.example.backend.web.dto.exam.ExamDto;
import org.example.backend.web.dto.exam.ExamQuestionDto;
import org.example.backend.web.dto.notification.NotificationDto;
import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.result.SubmitExamAnswersRequest;
import org.example.backend.web.dto.result.SubmitResultRequest;
import org.example.backend.web.dto.user.UpdateProfileRequest;
import org.example.backend.web.dto.user.UserDto;
import java.security.Principal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final ExamService examService;
    private final ResultService resultService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public ClientController(
            UserService userService,
            UserRepository userRepository,
            CertificateService certificateService,
            ExamService examService,
            ResultService resultService,
            NotificationService notificationService,
            FileStorageService fileStorageService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.certificateService = certificateService;
        this.examService = examService;
        this.resultService = resultService;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> profile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UpdateProfileRequest request, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> uploadAvatar(@RequestPart("file") MultipartFile file, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        String avatarUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(userService.updateAvatar(user.getId(), avatarUrl));
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateDto>> myCertificates(Principal principal) {
        return ResponseEntity.ok(certificateService.listClientCertificates(principal.getName()));
    }

    @GetMapping("/certificates/{id}/pdf")
    public ResponseEntity<byte[]> downloadMyPdf(@PathVariable Long id, Principal principal) throws Exception {
        Path path = certificateService.getPdfPathForClient(id, principal.getName());
        byte[] content = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + id + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(content);
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamDto>> activeExams() {
        return ResponseEntity.ok(examService.listActive());
    }

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<List<ExamQuestionDto>> examQuestions(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.listQuestionsForExam(examId));
    }

    @PostMapping("/results")
    public ResponseEntity<ResultDto> submitResult(@RequestBody SubmitResultRequest request, Principal principal) {
        return ResponseEntity.ok(resultService.submit(request, principal.getName()));
    }

    @PostMapping("/results/answers")
    public ResponseEntity<ResultDto> submitAnswers(@Valid @RequestBody SubmitExamAnswersRequest request, Principal principal) {
        return ResponseEntity.ok(resultService.submitAnswers(request, principal.getName()));
    }

    @GetMapping("/results")
    public ResponseEntity<List<ResultDto>> myResults(Principal principal) {
        return ResponseEntity.ok(resultService.listForClient(principal.getName()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.ok(notificationService.listForUser(user.getId()));
    }
}
