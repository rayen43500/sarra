package org.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.backend.domain.entity.SmsLog;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.repository.SmsLogRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.web.dto.communication.BulkEmailResponse;
import org.example.backend.web.dto.communication.SendEmailRequest;
import org.example.backend.web.dto.communication.SendSmsRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommunicationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SmsLogRepository smsLogRepository;
    private final BadWordsFilterService badWordsFilterService;
    private final UserRepository userRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public CommunicationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            SmsLogRepository smsLogRepository,
            BadWordsFilterService badWordsFilterService,
            UserRepository userRepository
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.smsLogRepository = smsLogRepository;
        this.badWordsFilterService = badWordsFilterService;
        this.userRepository = userRepository;
    }

    public String sendEmail(SendEmailRequest request) {
        String sanitizedSubject = badWordsFilterService.sanitize(request.subject());
        String sanitizedBody = badWordsFilterService.sanitize(request.body());
        String recipient = request.to().trim();

        if (!mailEnabled) {
            return "EMAIL_DISABLED: Configurez app.mail.enabled=true pour activer l envoi reel.";
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return "EMAIL_CONFIG_ERROR: JavaMailSender indisponible.";
        }

        String sender = StringUtils.hasText(mailFrom) ? mailFrom.trim() : mailUsername.trim();
        if (!StringUtils.hasText(sender)) {
            return "EMAIL_CONFIG_ERROR: Expediteur manquant.";
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(sender);
            helper.setTo(recipient);
            helper.setSubject(sanitizedSubject);
            
            // Build a premium HTML template
            String htmlContent = buildHtmlTemplate(sanitizedSubject, sanitizedBody);
            helper.setText(htmlContent, true); // true = isHtml
            
            mailSender.send(mimeMessage);
            return "EMAIL_SENT: " + recipient;
        } catch (MessagingException | MailException ex) {
            return "EMAIL_FAILED: " + ex.getMessage();
        }
    }

    private String buildHtmlTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #2563EB, #1E40AF); color: white; padding: 30px; text-align: center; }
                    .body { padding: 30px; background: #fff; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #777; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #2563EB; color: #fff; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 20px; }
                    h1 { margin: 0; font-size: 24px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>CertifyHub</h1>
                    </div>
                    <div class="body">
                        <h2>%s</h2>
                        <p>%s</p>
                        <p>Merci de faire confiance à CertifyHub pour vos besoins en certification numérique.</p>
                        <a href="http://localhost:4200" class="button">Accéder à la plateforme</a>
                    </div>
                    <div class="footer">
                        &copy; 2026 CertifyHub Platform. Tous droits réservés.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, content.replace("\n", "<br>"));
    }

    public String sendSms(SendSmsRequest request) {
        SmsLog smsLog = new SmsLog();
        smsLog.setToNumber(request.to());
        smsLog.setMessage(badWordsFilterService.sanitize(request.message()));
        smsLog.setStatus("SIMULATED_SENT");
        smsLogRepository.save(smsLog);
        return "SMS_SIMULATED_SENT: " + request.to();
    }

    public BulkEmailResponse sendEmailToAllUsers(String subject, String body) {
        List<User> recipients = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> StringUtils.hasText(user.getEmail()))
                .toList();

        List<String> details = new ArrayList<>();
        int sent = 0;
        int failed = 0;

        for (User recipient : recipients) {
            String status = sendEmail(new SendEmailRequest(recipient.getEmail(), subject, body));
            details.add(status);
            if (status.startsWith("EMAIL_SENT")) {
                sent++;
            } else {
                failed++;
            }
        }

        return new BulkEmailResponse(recipients.size(), sent, failed, details);
    }
}
