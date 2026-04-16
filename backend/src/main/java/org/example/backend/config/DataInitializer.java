package org.example.backend.config;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.Exam;
import org.example.backend.domain.entity.Role;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.CertificateStatus;
import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ExamRepository examRepository,
            CertificateRepository certificateRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.ROLE_ADMIN);
            return roleRepository.save(role);
        });

        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.ROLE_CLIENT);
            return roleRepository.save(role);
        });

        User admin = userRepository.findByEmail("admin@cert.local").orElseGet(() -> {
            User adminUser = new User();
            adminUser.setFirstName("System");
            adminUser.setLastName("Admin");
            adminUser.setEmail("admin@cert.local");
            adminUser.setPasswordHash(passwordEncoder.encode("Admin@123"));
            adminUser.setStatus(UserStatus.ACTIVE);
            adminUser.setRoles(Set.of(adminRole, clientRole));
            return userRepository.save(adminUser);
        });

        User client = userRepository.findByEmail("client@cert.local").orElseGet(() -> {
            User user = new User();
            user.setFirstName("Client");
            user.setLastName("Demo");
            user.setEmail("client@cert.local");
            user.setPasswordHash(passwordEncoder.encode("Client@123"));
            user.setStatus(UserStatus.ACTIVE);
            user.setRoles(Set.of(clientRole));
            return userRepository.save(user);
        });

        User blockedClient = userRepository.findByEmail("blocked@cert.local").orElseGet(() -> {
            User user = new User();
            user.setFirstName("Blocked");
            user.setLastName("Client");
            user.setEmail("blocked@cert.local");
            user.setPasswordHash(passwordEncoder.encode("Blocked@123"));
            user.setStatus(UserStatus.BLOCKED);
            user.setRoles(Set.of(clientRole));
            return userRepository.save(user);
        });

        if (examRepository.count() == 0) {
            Exam exam1 = new Exam();
            exam1.setTitle("QCM Angular Secure Frontend");
            exam1.setDescription("Routing guards, interceptor JWT et UX securisee.");
            exam1.setDurationMinutes(20);
            exam1.setPassScore(70.0);
            exam1.setIsActive(true);
            exam1.setCreatedBy(admin);

            Exam exam2 = new Exam();
            exam2.setTitle("QCM Spring Boot Security API");
            exam2.setDescription("JWT, RBAC, logs et validation backend.");
            exam2.setDurationMinutes(25);
            exam2.setPassScore(75.0);
            exam2.setIsActive(true);
            exam2.setCreatedBy(admin);

            examRepository.save(exam1);
            examRepository.save(exam2);
        }

        seedCertificate(
                "CERT-DEMO-ACTIVE",
                "Certification Platform Security",
                "Certificat actif de demonstration",
                client,
                admin,
                CertificateStatus.ACTIVE,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusMonths(6),
                null,
                null
        );

        seedCertificate(
                "CERT-DEMO-EXPIRED",
                "Certification Legacy API",
                "Certificat expire de demonstration",
                client,
                admin,
                CertificateStatus.EXPIRED,
                LocalDate.now().minusMonths(12),
                LocalDate.now().minusDays(1),
                null,
                null
        );

        seedCertificate(
                "CERT-DEMO-REVOKED",
                "Certification Revoked Sample",
                "Certificat revoque de demonstration",
                blockedClient,
                admin,
                CertificateStatus.REVOKED,
                LocalDate.now().minusMonths(2),
                LocalDate.now().plusMonths(10),
                Instant.now().minusSeconds(3L * 24 * 60 * 60),
                "Policy violation"
        );
    }

    private void seedCertificate(
            String code,
            String title,
            String description,
            User issuedTo,
            User issuedBy,
            CertificateStatus status,
            LocalDate issueDate,
            LocalDate expiryDate,
            Instant revokedAt,
            String revokeReason
    ) {
        if (certificateRepository.findByCertificateCode(code).isPresent()) {
            return;
        }

        Certificate cert = new Certificate();
        cert.setCertificateCode(code);
        cert.setTitle(title);
        cert.setDescription(description);
        cert.setIssuedTo(issuedTo);
        cert.setIssuedBy(issuedBy);
        cert.setIssueDate(issueDate);
        cert.setExpiryDate(expiryDate);
        cert.setStatus(status);
        cert.setQrPayload("http://localhost:4200/verify?code=" + code);
        cert.setDigitalSignature("seed-signature-" + code);
        cert.setPdfPath("generated/pdfs/seed-" + code + ".pdf");
        cert.setRevokedAt(revokedAt);
        cert.setRevokeReason(revokeReason);
        certificateRepository.save(cert);
    }
}
