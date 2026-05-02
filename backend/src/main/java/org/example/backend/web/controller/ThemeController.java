package org.example.backend.web.controller;

import org.example.backend.service.FileStorageService;
import org.example.backend.service.ThemeSettingsService;
import org.example.backend.web.dto.theme.BackgroundImageDto;
import org.example.backend.web.dto.theme.ThemeSettingsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ThemeController {

    private final ThemeSettingsService themeService;
    private final FileStorageService fileStorageService;

    public ThemeController(ThemeSettingsService themeService, FileStorageService fileStorageService) {
        this.themeService = themeService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/public/theme")
    public ResponseEntity<ThemeSettingsDto> getSettings() {
        return ResponseEntity.ok(themeService.getSettings());
    }

    @GetMapping("/public/theme/backgrounds")
    public ResponseEntity<List<BackgroundImageDto>> getActiveBackgrounds() {
        return ResponseEntity.ok(themeService.getActiveBackgrounds());
    }

    @PutMapping("/admin/theme")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThemeSettingsDto> updateSettings(@RequestBody ThemeSettingsDto dto) {
        return ResponseEntity.ok(themeService.updateSettings(dto));
    }

    @PostMapping("/admin/theme/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/admin/theme/backgrounds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BackgroundImageDto>> getAllBackgrounds() {
        return ResponseEntity.ok(themeService.getAllBackgrounds());
    }

    @PostMapping("/admin/theme/backgrounds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackgroundImageDto> addBackground(@RequestParam("url") String url) {
        return ResponseEntity.ok(themeService.addBackgroundImage(url));
    }

    @DeleteMapping("/admin/theme/backgrounds/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBackground(@PathVariable Long id) {
        themeService.deleteBackgroundImage(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/theme/backgrounds/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackgroundImageDto> toggleBackground(@PathVariable Long id) {
        return ResponseEntity.ok(themeService.toggleBackgroundActive(id));
    }
}
