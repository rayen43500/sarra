package org.example.backend.service;

import org.example.backend.domain.entity.PaymentTransaction;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.PaymentStatus;
import org.example.backend.repository.PaymentTransactionRepository;
import org.example.backend.web.dto.payment.PaymentRequest;
import org.example.backend.web.dto.payment.PaymentResponseDto;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CurrentUserService currentUserService;

    public PaymentService(PaymentTransactionRepository paymentTransactionRepository, CurrentUserService currentUserService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.currentUserService = currentUserService;
    }

    public PaymentResponseDto pay(Principal principal, PaymentRequest request) {
        User user = currentUserService.require(principal);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setUser(user);
        tx.setAmount(request.amount());
        tx.setCurrency(request.currency() == null ? "EUR" : request.currency().toUpperCase());
        tx.setDescription(request.description());
        tx.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tx.setStatus(request.amount() > 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        PaymentTransaction saved = paymentTransactionRepository.save(tx);
        return new PaymentResponseDto(
                saved.getId(),
                saved.getReference(),
                saved.getStatus().name(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getProvider(),
                saved.getCreatedAt()
        );
    }
}
