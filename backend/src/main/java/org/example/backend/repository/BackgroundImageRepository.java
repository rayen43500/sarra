package org.example.backend.repository;

import org.example.backend.domain.entity.BackgroundImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackgroundImageRepository extends JpaRepository<BackgroundImage, Long> {
    List<BackgroundImage> findByActiveTrue();
}
