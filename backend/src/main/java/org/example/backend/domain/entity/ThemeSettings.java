package org.example.backend.domain.entity;

import org.example.backend.domain.common.BaseEntity;
import org.example.backend.domain.enums.BackgroundMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "theme_settings")
public class ThemeSettings extends BaseEntity {

    private String appName;
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;
    private String loginBadgeText;
    private String loginHeroTitle;
    private String loginHeroSubtitle;
    private String loginAdminCardTitle;
    private String loginAdminCardDescription;
    private String loginAdminCardCredentials;
    private String loginClientCardTitle;
    private String loginClientCardDescription;
    private String loginClientCardCredentials;

    @Enumerated(EnumType.STRING)
    private BackgroundMode backgroundMode = BackgroundMode.STATIC;
}
