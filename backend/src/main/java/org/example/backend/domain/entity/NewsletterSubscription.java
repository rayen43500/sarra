package org.example.backend.domain.entity;

import org.example.backend.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "newsletter_subscriptions")
public class NewsletterSubscription extends BaseEntity {

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;
}
