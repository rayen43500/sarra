package org.example.backend.web.controller;

import org.example.backend.service.AppointmentService;
import org.example.backend.service.CommunicationService;
import org.example.backend.service.MessageService;
import org.example.backend.service.PaymentService;
import org.example.backend.service.RatingService;
import org.example.backend.service.ReactionService;
import org.example.backend.web.dto.appointment.AppointmentDto;
import org.example.backend.web.dto.appointment.AppointmentRequest;
import org.example.backend.web.dto.communication.SendEmailRequest;
import org.example.backend.web.dto.communication.SendSmsRequest;
import org.example.backend.web.dto.message.MessageDto;
import org.example.backend.web.dto.message.SendMessageRequest;
import org.example.backend.web.dto.payment.PaymentRequest;
import org.example.backend.web.dto.payment.PaymentResponseDto;
import org.example.backend.web.dto.rating.RatingDto;
import org.example.backend.web.dto.rating.RatingRequest;
import org.example.backend.web.dto.rating.RatingSummaryDto;
import org.example.backend.web.dto.reaction.ReactionRequest;
import org.example.backend.web.dto.reaction.ReactionSummaryDto;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/features")
public class ClientFeaturesController {

    private final MessageService messageService;
    private final ReactionService reactionService;
    private final RatingService ratingService;
    private final CommunicationService communicationService;
    private final PaymentService paymentService;
    private final AppointmentService appointmentService;

    public ClientFeaturesController(
            MessageService messageService,
            ReactionService reactionService,
            RatingService ratingService,
            CommunicationService communicationService,
            PaymentService paymentService,
            AppointmentService appointmentService
    ) {
        this.messageService = messageService;
        this.reactionService = reactionService;
        this.ratingService = ratingService;
        this.communicationService = communicationService;
        this.paymentService = paymentService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody SendMessageRequest request, Principal principal) {
        return ResponseEntity.ok(messageService.send(principal, request));
    }

    @GetMapping("/messages/{otherUserId}")
    public ResponseEntity<List<MessageDto>> conversation(@PathVariable Long otherUserId, Principal principal) {
        return ResponseEntity.ok(messageService.conversation(principal, otherUserId));
    }

    @PostMapping("/reactions")
    public ResponseEntity<ReactionSummaryDto> react(@Valid @RequestBody ReactionRequest request, Principal principal) {
        return ResponseEntity.ok(reactionService.react(principal, request));
    }

    @GetMapping("/reactions")
    public ResponseEntity<ReactionSummaryDto> reactionSummary(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Principal principal
    ) {
        return ResponseEntity.ok(reactionService.summary(principal, targetType, targetId));
    }

    @PostMapping("/ratings")
    public ResponseEntity<RatingDto> rate(@Valid @RequestBody RatingRequest request, Principal principal) {
        return ResponseEntity.ok(ratingService.rate(principal, request));
    }

    @GetMapping("/ratings")
    public ResponseEntity<RatingSummaryDto> ratingSummary(@RequestParam String targetType, @RequestParam Long targetId) {
        return ResponseEntity.ok(ratingService.summary(targetType, targetId));
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return ResponseEntity.ok(Map.of("status", communicationService.sendEmail(request)));
    }

    @PostMapping("/sms")
    public ResponseEntity<Map<String, String>> sendSms(@Valid @RequestBody SendSmsRequest request) {
        return ResponseEntity.ok(Map.of("status", communicationService.sendSms(request)));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDto> pay(@Valid @RequestBody PaymentRequest request, Principal principal) {
        return ResponseEntity.ok(paymentService.pay(principal, request));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody AppointmentRequest request, Principal principal) {
        return ResponseEntity.ok(appointmentService.create(principal, request));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> myAppointments(Principal principal) {
        return ResponseEntity.ok(appointmentService.mine(principal));
    }
}
