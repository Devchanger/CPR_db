package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.ChatMessage;
import com.cpr_db.cpr_db.dto.QaRequest;
import com.cpr_db.cpr_db.dto.QaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QaService {

    private static final String SYSTEM_PROMPT = """
            你是"生息守护"急救培训系统的 AI 助手。你只能回答以下领域的问题：
            - 心肺复苏（CPR）操作规范（成人/儿童/婴儿）
            - AED（自动体外除颤器）使用方法
            - 常见急症处理（窒息、溺水、中暑、烫伤等）
            - 急救基础知识（止血、包扎、固定、搬运）

            回答时严格参考《2020 AHA 心肺复苏与心血管急救指南》。
            回答要求：简洁准确、分步骤说明、关键数字加粗。

            如果用户问题超出以上范围，礼貌回答："抱歉，我只能回答急救相关问题。如有医疗问题，请咨询专业医生。"
            """;

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public QaService(@Value("${qa.api.url:https://api.deepseek.com/v1/chat/completions}") String apiUrl,
                     @Value("${qa.api.key:}") String apiKey,
                     @Value("${qa.api.model:deepseek-chat}") String model) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public QaResponse ask(QaRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return new QaResponse("AI 问答服务暂未配置 API Key，请联系管理员。");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        if (request.getHistory() != null) {
            for (ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        messages.add(Map.of("role", "user", "content", request.getQuestion()));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return new QaResponse(content);
                }
            }
            throw new RuntimeException("Unexpected API response format");
        } catch (RestClientException e) {
            return new QaResponse("AI 服务暂时不可用，请稍后重试。");
        } catch (RuntimeException e) {
            return new QaResponse("AI 服务返回异常，请稍后重试。");
        }
    }

    public List<String> getPresets() {
        return List.of(
                "心肺复苏的正确步骤是什么？",
                "AED 如何使用？",
                "按压深度和频率应该是多少？",
                "如何判断患者是否需要心肺复苏？"
        );
    }
}
