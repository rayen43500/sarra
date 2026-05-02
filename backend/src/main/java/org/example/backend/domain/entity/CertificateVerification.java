package org.example.backend.domain.entity;

import org.example.backend.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "certificate_verifications")
public class CertificateVerification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @Column(name = "verification_method", nullable = false, length = 20)
    private String verificationMethod;

    @Column(name = "requester_ip", length = 64)
    private String requesterIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "result_status", nullable = false, length = 20)
    private String resultStatus;
}
