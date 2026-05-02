package org.example.backend.service;

import org.example.backend.domain.entity.BackgroundImage;
import org.example.backend.domain.entity.ThemeSettings;
import org.example.backend.domain.enums.BackgroundMode;
import org.example.backend.repository.BackgroundImageRepository;
import org.example.backend.repository.ThemeSettingsRepository;
import org.example.backend.web.dto.theme.BackgroundImageDto;
import org.example.backend.web.dto.theme.ThemeSettingsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThemeSettingsService {
    
    private final ThemeSettingsRepository themeRepository;
    private final BackgroundImageRepository bgRepository;

    public ThemeSettingsService(ThemeSettingsRepository themeRepository, BackgroundImageRepository bgRepository) {
        this.themeRepository = themeRepository;
        this.bgRepository = bgRepository;
    }

    public ThemeSettingsDto getSettings() {
        ThemeSettings settings = themeRepository.findById(1L).orElseGet(this::createDefaultSettings);
        return mapToDto(settings);
    }

    public ThemeSettingsDto updateSettings(ThemeSettingsDto dto) {
        ThemeSettings settings = themeRepository.findById(1L).orElseGet(this::createDefaultSettings);
        settings.setAppName(dto.getAppName());
        settings.setPrimaryColor(dto.getPrimaryColor());
        settings.setSecondaryColor(dto.getSecondaryColor());
        settings.setLogoUrl(dto.getLogoUrl());
        settings.setLoginBadgeText(dto.getLoginBadgeText());
        settings.setLoginHeroTitle(dto.getLoginHeroTitle());
        settings.setLoginHeroSubtitle(dto.getLoginHeroSubtitle());
        settings.setLoginAdminCardTitle(dto.getLoginAdminCardTitle());
        settings.setLoginAdminCardDescription(dto.getLoginAdminCardDescription());
        settings.setLoginAdminCardCredentials(dto.getLoginAdminCardCredentials());
        settings.setLoginClientCardTitle(dto.getLoginClientCardTitle());
        settings.setLoginClientCardDescription(dto.getLoginClientCardDescription());
        settings.setLoginClientCardCredentials(dto.getLoginClientCardCredentials());
        settings.setBackgroundMode(dto.getBackgroundMode());
        return mapToDto(themeRepository.save(settings));
    }

    public BackgroundImageDto addBackgroundImage(String url) {
        BackgroundImage bg = new BackgroundImage();
        bg.setUrl(url);
        bg.setActive(true);
        return mapBgToDto(bgRepository.save(bg));
    }

    public void deleteBackgroundImage(Long id) {
        bgRepository.deleteById(id);
    }

    public List<BackgroundImageDto> getAllBackgrounds() {
        return bgRepository.findAll().stream().map(this::mapBgToDto).collect(Collectors.toList());
    }

    public List<BackgroundImageDto> getActiveBackgrounds() {
        return bgRepository.findByActiveTrue().stream().map(this::mapBgToDto).collect(Collectors.toList());
    }

    public BackgroundImageDto toggleBackgroundActive(Long id) {
        BackgroundImage bg = bgRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
        bg.setActive(!bg.isActive());
        return mapBgToDto(bgRepository.save(bg));
    }

    private ThemeSettings createDefaultSettings() {
        ThemeSettings settings = new ThemeSettings();
        settings.setId(1L);
        settings.setAppName("CertifyHub");
        settings.setPrimaryColor("#2563eb");
        settings.setSecondaryColor("#1e40af");
        settings.setLogoUrl("");
        settings.setLoginBadgeText("Certificats signes et verifiables");
        settings.setLoginHeroTitle("Votre espace de confiance pour la certification numerique.");
        settings.setLoginHeroSubtitle("Centralisez les preuves, validez l'authenticite en quelques secondes et partagez des documents inviolables avec vos partenaires.");
        settings.setLoginAdminCardTitle("Acces Admin");
        settings.setLoginAdminCardDescription("Publication, signature et suivi des certificats.");
        settings.setLoginAdminCardCredentials("admin@cert.local / Admin@123");
        settings.setLoginClientCardTitle("Acces Client");
        settings.setLoginClientCardDescription("Consultation, telechargement et partage securise.");
        settings.setLoginClientCardCredentials("client@cert.local / Client@123");
        settings.setBackgroundMode(BackgroundMode.STATIC);
        return themeRepository.save(settings);
    }

    private ThemeSettingsDto mapToDto(ThemeSettings entity) {
        return ThemeSettingsDto.builder()
                .appName(entity.getAppName())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .logoUrl(entity.getLogoUrl())
                .loginBadgeText(entity.getLoginBadgeText())
                .loginHeroTitle(entity.getLoginHeroTitle())
                .loginHeroSubtitle(entity.getLoginHeroSubtitle())
                .loginAdminCardTitle(entity.getLoginAdminCardTitle())
                .loginAdminCardDescription(entity.getLoginAdminCardDescription())
                .loginAdminCardCredentials(entity.getLoginAdminCardCredentials())
                .loginClientCardTitle(entity.getLoginClientCardTitle())
                .loginClientCardDescription(entity.getLoginClientCardDescription())
                .loginClientCardCredentials(entity.getLoginClientCardCredentials())
                .backgroundMode(entity.getBackgroundMode())
                .build();
    }

    private BackgroundImageDto mapBgToDto(BackgroundImage entity) {
        return BackgroundImageDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .active(entity.isActive())
                .build();
    }
}
