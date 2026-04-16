package org.example.backend.repository;

import org.example.backend.domain.entity.Exam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByIsActiveTrue();
}
