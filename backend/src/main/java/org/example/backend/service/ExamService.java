package org.example.backend.service;

import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.CreateExamQuestionRequest;
import org.example.backend.web.dto.exam.ExamDto;
import org.example.backend.web.dto.exam.ExamQuestionDto;
import java.util.List;

public interface ExamService {
    ExamDto createExam(CreateExamRequest request, String adminEmail);
    ExamQuestionDto addQuestion(Long examId, CreateExamQuestionRequest request, String adminEmail);
    List<ExamDto> listAll();
    List<ExamDto> listActive();
    List<ExamQuestionDto> listQuestionsForExam(Long examId);
}
