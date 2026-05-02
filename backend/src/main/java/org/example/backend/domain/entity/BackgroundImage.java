package org.example.backend.domain.entity;

import org.example.backend.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "background_images")
public class BackgroundImage extends BaseEntity {
    private String url;
    private boolean active = true;
}
