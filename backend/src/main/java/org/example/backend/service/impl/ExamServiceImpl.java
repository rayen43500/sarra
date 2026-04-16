package org.example.backend.service.impl;

import org.example.backend.domain.entity.Exam;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.ExamRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.AuditLogService;
import org.example.backend.service.ExamService;
import org.example.backend.web.dto.exam.CreateExamRequest;
import org.example.backend.web.dto.exam.ExamDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ExamServiceImpl(ExamRepository examRepository, UserRepository userRepository, AuditLogService auditLogService) {
        this.examRepository = examRepository;
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
    public List<ExamDto> listAll() {
        return examRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<ExamDto> listActive() {
        return examRepository.findByIsActiveTrue().stream().map(this::toDto).toList();
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
}
