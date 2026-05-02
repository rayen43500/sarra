package org.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.web.dto.exam.CreateExamQuestionOptionRequest;
import org.example.backend.web.dto.exam.CreateExamQuestionRequest;
import org.example.backend.web.dto.exam.GenerateQuizRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class AiQuizGenerationService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${app.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    public AiQuizGenerationService(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    public GeneratedQuizPayload generate(GenerateQuizRequest request) {
        if (!geminiEnabled || !StringUtils.hasText(geminiApiKey)) {
            throw new IllegalStateException("Gemini is required but not configured. Set app.gemini.enabled=true and app.gemini.api-key.");
        }

        try {
            List<CreateExamQuestionRequest> questions = generateWithGemini(request, false);
            if (questions.size() < Math.max(3, normalizedCount(request.questionCount()) / 2)) {
                questions = generateWithGemini(request, true);
            }
            if (!questions.isEmpty()) {
                return new GeneratedQuizPayload(questions, "GEMINI");
            }
        } catch (Exception ex) {
            String details = ex.getMessage();
            throw new IllegalStateException("Gemini generation failed: " + details, ex);
        }

        throw new IllegalStateException("Gemini returned empty or invalid questions.");
    }

    private List<CreateExamQuestionRequest> generateWithGemini(GenerateQuizRequest request, boolean strict) throws Exception {
        int count = normalizedCount(request.questionCount());
        String prompt = buildPrompt(request, count, strict);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.45,
                        "maxOutputTokens", 4096
                )
        );

        String response = restClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}", geminiModel, geminiApiKey)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        JsonNode quizJson = objectMapper.readTree(stripCodeFence(text));
        JsonNode questionsNode = quizJson.path("questions");
        if (!questionsNode.isArray()) {
            return List.of();
        }

        List<CreateExamQuestionRequest> questions = new ArrayList<>();
        int order = 1;
        int genericHits = 0;
        for (JsonNode questionNode : questionsNode) {
            String questionText = questionNode.path("questionText").asText("").trim();
            JsonNode optionsNode = questionNode.path("options");
            if (!StringUtils.hasText(questionText) || !optionsNode.isArray()) {
                continue;
            }

            List<CreateExamQuestionOptionRequest> options = new ArrayList<>();
            List<String> seen = new ArrayList<>();
            int correctCount = 0;
            for (JsonNode optionNode : optionsNode) {
                String optionText = optionNode.path("optionText").asText("").trim();
                boolean isCorrect = optionNode.path("isCorrect").asBoolean(false);
                if (StringUtils.hasText(optionText) && optionText.length() >= 6 && isUnique(optionText, seen)) {
                    options.add(new CreateExamQuestionOptionRequest(optionText, isCorrect));
                    if (isCorrect) {
                        correctCount++;
                    }
                    seen.add(normalize(optionText));
                    if (isGenericOption(optionText)) {
                        genericHits++;
                    }
                }
            }

            if (options.size() >= 2 && correctCount == 1) {
                questions.add(new CreateExamQuestionRequest(questionText, 1.0, order++, options));
            }
        }

        if (genericHits >= Math.max(3, count)) {
            return List.of();
        }

        return questions;
    }

    private String buildPrompt(GenerateQuizRequest request, int count, boolean strict) {
        String strictRules = strict
                ? """
                Evite les reponses generiques, abstraites ou repetitives.
                Chaque option doit etre specifique au sujet et differente des autres.
                Ne reutilise pas les memes formulations entre questions.
                """
                : "";

        return """
                Cree un QCM de revision pour CertifyHub.
                Sujet: %s
                Difficulte: %s
                Nombre de questions: %d
                %s
                Retourne uniquement du JSON valide, sans markdown, avec ce schema:
                {"questions":[{"questionText":"...","options":[{"optionText":"...","isCorrect":true},{"optionText":"...","isCorrect":false},{"optionText":"...","isCorrect":false},{"optionText":"...","isCorrect":false}]}]}
                Chaque question doit avoir exactement une bonne reponse.
                """.formatted(safeTopic(request.topic()), safeDifficulty(request.difficulty()), count, strictRules);
    }

    private boolean isGenericOption(String text) {
        String value = normalize(text);
        return value.contains("comprendre les notions cles")
                || value.contains("memoris")
                || value.contains("choisir les reponses au hasard")
                || value.contains("ignorer les exemples");
    }

    private boolean isUnique(String text, List<String> seen) {
        String value = normalize(text);
        return seen.stream().noneMatch(value::equals);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private List<CreateExamQuestionRequest> generateFallback(GenerateQuizRequest request) {
        int count = normalizedCount(request.questionCount());
        String topic = safeTopic(request.topic());
        String difficulty = safeDifficulty(request.difficulty());
        List<CreateExamQuestionRequest> questions = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String questionText = switch ((i - 1) % 5) {
                case 0 -> "Quel est l'objectif principal de " + topic + " ?";
                case 1 -> "Quelle pratique renforce le mieux la maitrise de " + topic + " ?";
                case 2 -> "Dans un contexte " + difficulty + ", quel element faut-il verifier en priorite pour " + topic + " ?";
                case 3 -> "Quel indicateur montre une bonne comprehension de " + topic + " ?";
                default -> "Quelle erreur doit etre evitee lors d'une revision sur " + topic + " ?";
            };

            List<CreateExamQuestionOptionRequest> options = List.of(
                    new CreateExamQuestionOptionRequest("Comprendre les notions cles et les appliquer dans un cas concret", true),
                    new CreateExamQuestionOptionRequest("Memoriser uniquement les titres sans pratiquer", false),
                    new CreateExamQuestionOptionRequest("Ignorer les exemples et passer directement a la conclusion", false),
                    new CreateExamQuestionOptionRequest("Choisir les reponses au hasard pour gagner du temps", false)
            );
            questions.add(new CreateExamQuestionRequest(questionText, 1.0, i, options));
        }

        return questions;
    }

    private String stripCodeFence(String text) {
        String value = text == null ? "" : text.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json)?", "").trim();
            if (value.endsWith("```")) {
                value = value.substring(0, value.length() - 3).trim();
            }
        }
        return value;
    }

    private int normalizedCount(Integer questionCount) {
        int count = questionCount == null ? 5 : questionCount;
        return Math.max(3, Math.min(30, count));
    }

    private String safeTopic(String topic) {
        return StringUtils.hasText(topic) ? topic.trim() : "certification numerique";
    }

    private String safeDifficulty(String difficulty) {
        return StringUtils.hasText(difficulty) ? difficulty.trim() : "intermediaire";
    }

    public record GeneratedQuizPayload(
            List<CreateExamQuestionRequest> questions,
            String source
    ) {
    }
}
