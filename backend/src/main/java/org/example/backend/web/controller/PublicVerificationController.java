package org.example.backend.web.controller;

import org.example.backend.service.CertificateService;
import org.example.backend.web.dto.verification.CertificateVerificationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/verify")
public class PublicVerificationController {

    private final CertificateService certificateService;

    public PublicVerificationController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<CertificateVerificationResponse> verifyByCode(
            @PathVariable String code,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(certificateService.verifyByCode(
                code,
                "CODE",
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        ));
    }
}
