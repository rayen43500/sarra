package org.example.backend.service;

import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.CreateExamQuestionRequest;
import org.example.backend.web.dto.exam.ExamDto;
import org.example.backend.web.dto.exam.ExamQuestionDto;
import org.example.backend.web.dto.exam.GenerateQuizRequest;
import org.example.backend.web.dto.exam.GeneratedQuizDto;
import org.example.backend.web.dto.exam.RevisionDocumentDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ExamService {
    ExamDto createExam(CreateExamRequest request, String adminEmail);
    ExamQuestionDto addQuestion(Long examId, CreateExamQuestionRequest request, String adminEmail);
    GeneratedQuizDto generateQuiz(GenerateQuizRequest request, String adminEmail);
    RevisionDocumentDto uploadRevisionDocument(Long examId, String title, MultipartFile file, String adminEmail);
    List<ExamDto> listAll();
    List<ExamDto> listActive();
    List<ExamQuestionDto> listQuestionsForExam(Long examId);
}
