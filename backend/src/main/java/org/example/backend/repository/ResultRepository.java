package org.example.backend.repository;

import org.example.backend.domain.entity.Result;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<Result> findByExamIdOrderBySubmittedAtDesc(Long examId);
}
