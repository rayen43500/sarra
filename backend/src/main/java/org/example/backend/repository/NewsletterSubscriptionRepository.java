package org.example.backend.repository;

import org.example.backend.domain.entity.NewsletterSubscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    Optional<NewsletterSubscription> findByEmail(String email);
    long countByActiveTrue();
}
