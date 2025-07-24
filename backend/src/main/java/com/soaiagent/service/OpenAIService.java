package com.soaiagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${OPENAI_API_KEY:${openai.api-key:}}")
    private String openAiApiKey;

    @Value("${OPENAI_URL:${openai.url:https://api.openai.com/v1/chat/completions}}")
    private String openAiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> generateEmbedding(String text) {
        String embeddingUrl = "https://api.openai.com/v1/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "text-embedding-ada-002",
                "input", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(embeddingUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map responseBody = response.getBody();
            if (responseBody != null) {
                Object dataObj = responseBody.get("data");
                if (dataObj instanceof List<?> dataList && !dataList.isEmpty()) {
                    Object firstObj = dataList.get(0);
                    if (firstObj instanceof Map<?,?> firstMap) {
                        Object embeddingObj = firstMap.get("embedding");
                        if (embeddingObj instanceof List<?> embeddingList) {
                            try {
                                return embeddingList.stream()
                                        .filter(e -> e instanceof Number)
                                        .map(e -> ((Number) e).doubleValue())
                                        .toList();
                            } catch (Exception ex) {
                                throw new RuntimeException("Embedding format error: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Failed to generate embedding from OpenAI.");
    }

    public String getAnswer(String question, List<String> contextChunks) {
    boolean hasContext = contextChunks != null && !contextChunks.isEmpty();

    List<String> topChunks = hasContext
            ? (contextChunks.size() > 5 ? contextChunks.subList(0, 5) : contextChunks)
            : List.of();

    StringBuilder context = new StringBuilder();
    String fallbackNote = "";

    if (hasContext) {
        context.append("You are an assistant answering questions about Solution Office documentation. ")
               .append("Use only the provided context. If the answer is not in the context, say so, and then give a general answer based on best practices, making clear that it was not found in the document.\n\n ");

        for (String chunk : topChunks) {
            context.append(chunk).append("\n---\n");
        }
    } else {
        context.append("The document does not contain any relevant information for this question. ")
               .append("Provide a general best-practice answer, but mention that the document did not have this content.\n\n");
        fallbackNote = "🧠 Note: This answer is based on general AI knowledge and not found in the uploaded document.\n\n";
    }

    String prompt = context + "\nQuestion: " + question;

    // 📝 Log prompt
    System.out.println("📝 GPT Prompt:\n" + prompt);

    String initialAnswer = callOpenAI(prompt);

    // ✨ Second pass: rewording
    String rewordPrompt = "You are an editor. Improve clarity and style of the following answer without changing its facts:\n\n" +
                          initialAnswer;
    String refinedAnswer = callOpenAI(rewordPrompt);

    // 📖 Append citations
    //String citations = extractCitations(topChunks);
    String citations = "\n\n📖 Sources: " + "\n" + "sources to be mapped. For Example:" + "\n" + "confulence-link-to-section1," + "\n" + "confulence-link-to-section2";

    return fallbackNote + refinedAnswer + citations;

    }

    private String callOpenAI(String prompt) {
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "You are an intelligent assistant helping users query Confluence documentation."
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4",
                "messages", List.of(systemMessage, userMessage)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(openAiUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object choicesObj = response.getBody().get("choices");
            if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                Object firstChoice = choicesList.get(0);
                if (firstChoice instanceof Map<?,?> choiceMap) {
                    Object messageObj = choiceMap.get("message");
                    if (messageObj instanceof Map<?,?> messageMap) {
                        Object contentObj = messageMap.get("content");
                        if (contentObj != null) {
                            return contentObj.toString();
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Failed to get response from OpenAI.");
    }

    private String extractCitations(List<String> chunks) {
        return chunks.stream()
                .map(this::extractSectionTitle)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("No specific sections");
    }

    private String extractSectionTitle(String chunk) {
        if (chunk.contains("\n")) {
            return chunk.substring(0, chunk.indexOf("\n")).trim();
        }
        return "Unknown Section";
    }
}