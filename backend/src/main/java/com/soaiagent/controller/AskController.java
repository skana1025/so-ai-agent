package com.soaiagent.controller;

import com.soaiagent.model.AskRequest;
import com.soaiagent.model.AskResponse;
import com.soaiagent.service.OpenAIService;
import com.soaiagent.service.QdrantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ask")
public class AskController {

    private final QdrantService qdrantService;
    private final OpenAIService openAIService;

    public AskController(QdrantService qdrantService, OpenAIService openAIService) {
        this.qdrantService = qdrantService;
        this.openAIService = openAIService;
    }

    @PostMapping
    public AskResponse askQuestion(@RequestBody AskRequest request) {
        System.out.println("📥 Received question: " + request.getQuestion());

        // Step 1: Generate embedding for user question
        List<Double> questionVector = openAIService.generateEmbedding(request.getQuestion());

        // Step 2: Search Qdrant for relevant chunks
        List<String> relevantChunks = qdrantService.searchRelevantChunks(questionVector, 10);
        System.out.println("🔍 Found " + relevantChunks.size() + " relevant chunks");
        relevantChunks.forEach(System.out::println);

        // Step 3: Send question + chunks to OpenAI Chat API
        String answer = openAIService.getAnswer(request.getQuestion(), relevantChunks);

        return new AskResponse(answer);
    }
}