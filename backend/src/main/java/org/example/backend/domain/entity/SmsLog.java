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
@Table(name = "sms_logs")
public class SmsLog extends BaseEntity {

    @Column(name = "to_number", nullable = false, length = 30)
    private String toNumber;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 50)
    private String status;
}
