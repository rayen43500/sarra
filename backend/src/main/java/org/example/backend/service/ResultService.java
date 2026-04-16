package org.example.backend.service;

import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.result.SubmitExamAnswersRequest;
import org.example.backend.web.dto.result.SubmitResultRequest;
import java.util.List;

public interface ResultService {
    ResultDto submit(SubmitResultRequest request, String clientEmail);
    ResultDto submitAnswers(SubmitExamAnswersRequest request, String clientEmail);
    List<ResultDto> listForClient(String clientEmail);
    List<ResultDto> listByExam(Long examId);
}
