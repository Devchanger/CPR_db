package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.QaRequest;
import com.cpr_db.cpr_db.dto.QaResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QaServiceTest {

    @Test
    void shouldReturnApiKeyMissingMessageWhenNoKey() {
        QaService service = new QaService("https://api.deepseek.com/v1/chat/completions", "", "deepseek-chat");
        QaRequest request = new QaRequest();
        request.setQuestion("成人心肺复苏按压深度是多少？");

        QaResponse response = service.ask(request);

        assertNotNull(response.getAnswer());
        assertTrue(response.getAnswer().contains("未配置"));
    }

    @Test
    void shouldReturnPresets() {
        QaService service = new QaService("https://api.deepseek.com/v1/chat/completions", "", "deepseek-chat");
        List<String> presets = service.getPresets();
        assertNotNull(presets);
        assertTrue(presets.size() >= 4);
    }
}
