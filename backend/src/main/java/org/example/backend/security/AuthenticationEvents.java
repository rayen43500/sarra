package org.example.backend.security;

import org.example.backend.domain.entity.LoginHistory;
import org.example.backend.repository.LoginHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Component
public class AuthenticationEvents {

    private final LoginHistoryRepository loginHistoryRepository;

    public AuthenticationEvents(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Object principal = success.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            recordLogin(userDetails.getUsername());
        }
    }

    private void recordLogin(String email) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            LoginHistory history = new LoginHistory();
            history.setEmail(email);
            history.setIpAddress(request.getRemoteAddr());
            history.setUserAgent(request.getHeader("User-Agent"));
            history.setLoginTime(Instant.now());
            loginHistoryRepository.save(history);
        }
    }
}
