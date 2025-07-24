package com.soaiagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class QdrantService {

    @Value("${qdrant.url}")
    private String qdrantUrl;

    @Value("${qdrant.api-key}")
    private String qdrantApiKey;

    @Value("${qdrant.collection}")
    private String collectionName;

    private final RestTemplate restTemplate = new RestTemplate();

   public List<String> searchRelevantChunks(List<Double> questionVector, int topK) {
    String endpoint = qdrantUrl + "/collections/" + collectionName + "/points/search";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("api-key", qdrantApiKey);

    Map<String, Object> body = Map.of(
        "vector", questionVector,
        "top", topK,
        "with_payload", true,
        "score_threshold", 0.05
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Map.class);

    System.out.println("🔥 Raw Qdrant Response: " + response.getBody());

    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Object resultObj = response.getBody().get("result");
        if (resultObj instanceof List<?> resultList) {
            return resultList.stream()
                    .map(pointObj -> {
                        if (pointObj instanceof Map<?,?> pointMap) {
                            Object payloadObj = pointMap.get("payload");
                            if (payloadObj instanceof Map<?,?> payloadMap) {
                                Object textObj = payloadMap.get("page_content");
                                if (textObj == null) {
                                    Map<?,?> metadataMap = (Map<?,?>) payloadMap.get("metadata");
                                    textObj = metadataMap != null ? metadataMap.get("text") : null;
                                }
                                return textObj != null ? textObj.toString() : "";
                            }
                        }
                        return "";
                    })
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    return List.of("❌ Qdrant returned no results.");
}

}
