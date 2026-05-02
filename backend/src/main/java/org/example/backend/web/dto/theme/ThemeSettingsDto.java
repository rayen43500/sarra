package org.example.backend.web.dto.theme;

import org.example.backend.domain.enums.BackgroundMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThemeSettingsDto {
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
    private BackgroundMode backgroundMode;
}
