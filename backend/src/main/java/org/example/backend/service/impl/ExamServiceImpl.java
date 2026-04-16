package org.example.backend.service.impl;

import org.example.backend.domain.entity.Exam;
import org.example.backend.domain.entity.Question;
import org.example.backend.domain.entity.QuestionOption;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.QuestionOptionRepository;
import org.example.backend.repository.QuestionRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.AuditLogService;
import org.example.backend.service.ExamService;
import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.CreateExamQuestionRequest;
import org.example.backend.web.dto.exam.ExamDto;
import org.example.backend.web.dto.exam.ExamQuestionDto;
import org.example.backend.web.dto.exam.ExamQuestionOptionDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ExamServiceImpl(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public ExamDto createExam(CreateExamRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();

        Exam exam = new Exam();
        exam.setTitle(request.title());
        exam.setDescription(request.description());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setPassScore(request.passScore());
        exam.setIsActive(request.isActive() == null || request.isActive());
        exam.setCreatedBy(admin);

        Exam saved = examRepository.save(exam);
        auditLogService.log(admin.getId(), "CREATE_EXAM", "EXAM", saved.getId().toString(), "Exam created", null);
        return toDto(saved);
    }

    @Override
    public ExamQuestionDto addQuestion(Long examId, CreateExamQuestionRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        Exam exam = examRepository.findById(examId).orElseThrow();

        long correctCount = request.options().stream().filter(o -> Boolean.TRUE.equals(o.isCorrect())).count();
        if (request.options().size() < 2 || correctCount == 0) {
            throw new IllegalArgumentException("A question must have at least 2 options and one correct answer");
        }

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(request.questionText());
        question.setPoints(request.points() == null || request.points() <= 0 ? 1.0 : request.points());
        question.setOrderIndex(request.orderIndex() == null ? nextOrderIndex(examId) : request.orderIndex());
        Question savedQuestion = questionRepository.save(question);

        List<QuestionOption> options = request.options().stream().map(optionRequest -> {
            QuestionOption option = new QuestionOption();
            option.setQuestion(savedQuestion);
            option.setOptionText(optionRequest.optionText());
            option.setIsCorrect(Boolean.TRUE.equals(optionRequest.isCorrect()));
            return option;
        }).toList();

        List<QuestionOption> savedOptions = questionOptionRepository.saveAll(options);
        auditLogService.log(admin.getId(), "CREATE_EXAM_QUESTION", "EXAM", examId.toString(), "Question added", null);
        return toQuestionDto(savedQuestion, savedOptions);
    }

    @Override
    public List<ExamDto> listAll() {
        return examRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<ExamDto> listActive() {
        return examRepository.findByIsActiveTrue().stream().map(this::toDto).toList();
    }

    @Override
    public List<ExamQuestionDto> listQuestionsForExam(Long examId) {
        return questionRepository.findByExamIdOrderByOrderIndexAsc(examId).stream()
                .map(question -> toQuestionDto(question, questionOptionRepository.findByQuestionId(question.getId())))
                .toList();
    }

    private ExamDto toDto(Exam exam) {
        return new ExamDto(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getDurationMinutes(),
                exam.getPassScore(),
                exam.getIsActive()
        );
    }

    private ExamQuestionDto toQuestionDto(Question question, List<QuestionOption> options) {
        List<ExamQuestionOptionDto> optionDtos = options.stream()
                .map(option -> new ExamQuestionOptionDto(option.getId(), option.getOptionText()))
                .toList();
        return new ExamQuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getPoints(),
                question.getOrderIndex(),
                optionDtos
        );
    }

    private int nextOrderIndex(Long examId) {
        return questionRepository.findByExamIdOrderByOrderIndexAsc(examId).size() + 1;
    }
}
