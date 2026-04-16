package org.example.backend.service;

import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.ExamDto;
import java.util.List;

public interface ExamService {
    ExamDto createExam(CreateExamRequest request, String adminEmail);
    List<ExamDto> listAll();
    List<ExamDto> listActive();
}
