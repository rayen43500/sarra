package org.example.backend.web.controller;

import org.example.backend.service.NewsletterService;
import org.example.backend.web.dto.newsletter.NewsletterSubscriptionRequest;
import org.example.backend.web.dto.social.SocialProvidersDto;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicFeaturesController {

    private final NewsletterService newsletterService;

    public PublicFeaturesController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        return ResponseEntity.ok(Map.of("message", newsletterService.subscribe(request)));
    }

    @PostMapping("/newsletter/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        return ResponseEntity.ok(Map.of("message", newsletterService.unsubscribe(request)));
    }

    @GetMapping(value = "/rss/certificates", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> certificatesRss() {
        return ResponseEntity.ok(newsletterService.buildCertificatesRss());
    }

    @GetMapping("/social/providers")
    public ResponseEntity<SocialProvidersDto> socialProviders() {
        return ResponseEntity.ok(new SocialProvidersDto(
                "/oauth2/authorization/facebook",
                "/oauth2/authorization/linkedin",
                "Configurer les credentials OAuth2 Facebook/LinkedIn dans application.properties pour activer le SSO"
        ));
    }
}
