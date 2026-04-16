package org.example.backend.web.controller;

import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.service.AuditLogService;
import org.example.backend.service.CertificateService;
import org.example.backend.service.DashboardService;
import org.example.backend.service.ExamService;
import org.example.backend.service.ResultService;
import org.example.backend.service.UserService;
import org.example.backend.web.dto.certificate.CertificateDto;
import org.example.backend.web.dto.certificate.CreateCertificateRequest;
import org.example.backend.web.dto.dashboard.DashboardStatsDto;
import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.ExamDto;
import org.example.backend.web.dto.log.AuditLogDto;
import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.user.AdminUpdateUserRequest;
import org.example.backend.web.dto.user.CreateUserRequest;
import org.example.backend.web.dto.user.UserDto;
import java.security.Principal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CertificateService certificateService;
    private final ExamService examService;
    private final ResultService resultService;
    private final DashboardService dashboardService;
    private final AuditLogService auditLogService;

    public AdminController(
            UserService userService,
            CertificateService certificateService,
            ExamService examService,
            ResultService resultService,
            DashboardService dashboardService,
            AuditLogService auditLogService
    ) {
        this.userService = userService;
        this.certificateService = certificateService;
        this.examService = examService;
        this.resultService = resultService;
        this.dashboardService = dashboardService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> users() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.adminUpdate(id, request));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return ResponseEntity.ok(userService.setStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> assignRole(@PathVariable Long id, @RequestParam RoleName role) {
        userService.assignRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateDto>> certificates() {
        return ResponseEntity.ok(certificateService.listAdminCertificates());
    }

    @GetMapping("/certificates/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {
        Path path = certificateService.getPdfPathForAdmin(id);
        byte[] content = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + id + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(content);
    }

    @PostMapping("/certificates")
    public ResponseEntity<CertificateDto> createCertificate(@RequestBody CreateCertificateRequest request, Principal principal) {
        return ResponseEntity.ok(certificateService.createCertificate(request, principal.getName()));
    }

    @PutMapping("/certificates/{id}/revoke")
    public ResponseEntity<CertificateDto> revokeCertificate(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id, body.getOrDefault("reason", "No reason"), principal.getName()));
    }

    @PostMapping("/exams")
    public ResponseEntity<ExamDto> createExam(@RequestBody CreateExamRequest request, Principal principal) {
        return ResponseEntity.ok(examService.createExam(request, principal.getName()));
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamDto>> exams() {
        return ResponseEntity.ok(examService.listAll());
    }

    @GetMapping("/results/{examId}")
    public ResponseEntity<List<ResultDto>> examResults(@PathVariable Long examId) {
        return ResponseEntity.ok(resultService.listByExam(examId));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> stats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLogDto>> logs() {
        return ResponseEntity.ok(auditLogService.listRecent());
    }
}
