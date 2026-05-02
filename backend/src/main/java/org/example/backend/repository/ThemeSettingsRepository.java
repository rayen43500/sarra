package org.example.backend.repository;

import org.example.backend.domain.entity.ThemeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeSettingsRepository extends JpaRepository<ThemeSettings, Long> {
}
