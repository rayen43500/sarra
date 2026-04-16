package org.example.backend.service;

import org.example.backend.web.dto.result.ResultDto;
import org.example.backend.web.dto.result.SubmitResultRequest;
import java.util.List;

public interface ResultService {
    ResultDto submit(SubmitResultRequest request, String clientEmail);
    List<ResultDto> listForClient(String clientEmail);
    List<ResultDto> listByExam(Long examId);
}
