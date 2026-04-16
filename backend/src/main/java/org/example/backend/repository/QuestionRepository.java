package org.example.backend.repository;

import org.example.backend.domain.entity.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByOrderIndexAsc(Long examId);
}
