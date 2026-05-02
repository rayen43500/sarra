package org.example.backend.service;

import org.example.backend.domain.entity.Certificate;
import org.example.backend.domain.entity.NewsletterSubscription;
import org.example.backend.repository.CertificateRepository;
import org.example.backend.repository.NewsletterSubscriptionRepository;
import org.example.backend.web.dto.newsletter.NewsletterSubscriptionRequest;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NewsletterService {

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final CertificateRepository certificateRepository;

    public NewsletterService(
            NewsletterSubscriptionRepository newsletterSubscriptionRepository,
            CertificateRepository certificateRepository
    ) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
        this.certificateRepository = certificateRepository;
    }

    public String subscribe(NewsletterSubscriptionRequest request) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(request.email())
                .orElseGet(NewsletterSubscription::new);
        subscription.setEmail(request.email());
        subscription.setActive(true);
        newsletterSubscriptionRepository.save(subscription);
        return "Newsletter subscription active for " + request.email();
    }

    public String unsubscribe(NewsletterSubscriptionRequest request) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(request.email())
                .orElseThrow();
        subscription.setActive(false);
        newsletterSubscriptionRepository.save(subscription);
        return "Newsletter subscription disabled for " + request.email();
    }

    public String buildCertificatesRss() {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<rss version=\"2.0\"><channel>");
        xml.append("<title>Certificates Feed</title>");
        xml.append("<description>Latest certificates</description>");
        xml.append("<link>http://localhost:8080/api/public/rss/certificates</link>");

        certificateRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().limit(20).forEach(cert -> {
            xml.append(itemXml(cert, fmt));
        });

        xml.append("</channel></rss>");
        return xml.toString();
    }

    private String itemXml(Certificate cert, DateTimeFormatter fmt) {
        return "<item>"
                + "<title>" + escapeXml(cert.getTitle()) + "</title>"
                + "<description>" + escapeXml(cert.getDescription() == null ? "No description" : cert.getDescription()) + "</description>"
                + "<link>http://localhost:4200/verify?code=" + escapeXml(cert.getCertificateCode()) + "</link>"
                + "<guid>" + escapeXml(cert.getCertificateCode()) + "</guid>"
                + "<pubDate>" + cert.getIssueDate().format(fmt) + "</pubDate>"
                + "</item>";
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
