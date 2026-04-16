package org.example.backend.service;

import org.example.backend.domain.entity.SmsLog;
import org.example.backend.repository.SmsLogRepository;
import org.example.backend.web.dto.communication.SendEmailRequest;
import org.example.backend.web.dto.communication.SendSmsRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CommunicationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SmsLogRepository smsLogRepository;
    private final BadWordsFilterService badWordsFilterService;

    public CommunicationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            SmsLogRepository smsLogRepository,
            BadWordsFilterService badWordsFilterService
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.smsLogRepository = smsLogRepository;
        this.badWordsFilterService = badWordsFilterService;
    }

    public String sendEmail(SendEmailRequest request) {
        String sanitizedSubject = badWordsFilterService.sanitize(request.subject());
        String sanitizedBody = badWordsFilterService.sanitize(request.body());
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            return "EMAIL_SIMULATED: " + request.to();
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.to());
            message.setSubject(sanitizedSubject);
            message.setText(sanitizedBody);
            mailSender.send(message);
            return "EMAIL_SENT: " + request.to();
        } catch (Exception ex) {
            return "EMAIL_SIMULATED_FALLBACK: " + request.to();
        }
    }

    public String sendSms(SendSmsRequest request) {
        SmsLog smsLog = new SmsLog();
        smsLog.setToNumber(request.to());
        smsLog.setMessage(badWordsFilterService.sanitize(request.message()));
        smsLog.setStatus("SIMULATED_SENT");
        smsLogRepository.save(smsLog);
        return "SMS_SIMULATED_SENT: " + request.to();
    }
}
