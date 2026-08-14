package com.cpr_db.cpr_db.contract;

import com.cpr_db.cpr_db.dto.ScoreDto;
import com.cpr_db.cpr_db.dto.ScoreStatsResponse;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BE-A-01 / BE-B-07 contract assertions:
 * - Global SNAKE_CASE serialization must keep stable snake_case keys (ScoreDto / Stats / envelope).
 * - POST /scores must accept both snake_case (backend default) and camelCase (VR) payloads
 *   via @JsonAlias, so the VR client stays unchanged.
 */
class JacksonContractTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    @Test
    @DisplayName("BE-B-07 snake_case payload deserializes into ScoreSubmitRequest")
    void snakePayload_deserializes() throws Exception {
        String json = """
                {
                  "scene": "Subway",
                  "skill": "CPR",
                  "total_score": 85.5,
                  "compression_depth_avg": 5.5,
                  "compression_rate_avg": 110,
                  "error_count": 3,
                  "step_details": "[{\\"stepName\\":\\"按压\\",\\"score\\":85,\\"comment\\":\\"ok\\"}]"
                }
                """;

        ScoreSubmitRequest request = mapper.readValue(json, ScoreSubmitRequest.class);

        assertEquals("Subway", request.getScene());
        assertEquals("CPR", request.getSkill());
        assertEquals(85.5f, request.getTotalScore());
        assertEquals(5.5f, request.getCompressionDepthAvg());
        assertEquals(110f, request.getCompressionRateAvg());
        assertEquals(3, request.getErrorCount());
        assertTrue(request.getStepDetails().contains("stepName"));
    }

    @Test
    @DisplayName("BE-B-07 VR camelCase payload deserializes into ScoreSubmitRequest")
    void camelPayload_deserializes() throws Exception {
        String json = """
                {
                  "scene": "Subway",
                  "skill": "CPR",
                  "totalScore": 92.0,
                  "compressionDepthAvg": 5.8,
                  "compressionRateAvg": 118,
                  "errorCount": 1,
                  "stepDetails": "[{\\"stepName\\":\\"按压\\",\\"score\\":92,\\"comment\\":\\"good\\"}]"
                }
                """;

        ScoreSubmitRequest request = mapper.readValue(json, ScoreSubmitRequest.class);

        assertEquals("Subway", request.getScene());
        assertEquals("CPR", request.getSkill());
        assertEquals(92.0f, request.getTotalScore());
        assertEquals(5.8f, request.getCompressionDepthAvg());
        assertEquals(118f, request.getCompressionRateAvg());
        assertEquals(1, request.getErrorCount());
        assertTrue(request.getStepDetails().contains("stepName"));
    }

    @Test
    @DisplayName("BE-A-01 ScoreDto serializes with stable snake_case keys")
    void scoreDto_serializes_snakeCase() throws Exception {
        ScoreDto dto = new ScoreDto();
        dto.setId(1L);
        dto.setUsername("bob");
        dto.setScene("地铁站");
        dto.setSkill("CPR");
        dto.setTotalScore(85.5f);
        dto.setCompressionDepthAvg(5.5f);
        dto.setCompressionRateAvg(110f);
        dto.setErrorCount(3);
        dto.setStepDetails("[{\"stepName\":\"按压\",\"score\":85}]");
        dto.setCreatedAt(LocalDateTime.of(2026, 8, 1, 10, 0, 0));

        JsonNode node = mapper.readTree(mapper.writeValueAsString(dto));

        assertEquals(1L, node.get("id").asLong());
        assertEquals("bob", node.get("username").asText());
        assertEquals("地铁站", node.get("scene_name").asText());
        assertEquals("CPR", node.get("skill_name").asText());
        assertEquals(85.5, node.get("total_score").asDouble());
        assertEquals(5.5, node.get("compression_depth_avg").asDouble());
        assertEquals(110.0, node.get("compression_rate_avg").asDouble());
        assertEquals(3, node.get("error_count").asInt());
        assertTrue(node.get("step_details").asText().contains("stepName"));
        assertEquals("2026-08-01T10:00:00", node.get("created_at").asText());
        assertTrue(node.get("total_score") != null && node.get("totalScore") == null);
    }

    @Test
    @DisplayName("BE-A-01 ScoreStatsResponse serializes with stable snake_case keys")
    void stats_serializes_snakeCase() throws Exception {
        ScoreStatsResponse stats = new ScoreStatsResponse();
        stats.setTotalAttempts(12);
        stats.setAverageScore(80.5);
        stats.setHighestScore(95.0);
        stats.setLowestScore(60.0);
        stats.setScenesTrained(3);
        stats.setSkillsTrained(2);
        ScoreDto dto = new ScoreDto();
        dto.setId(1L);
        dto.setUsername("bob");
        stats.setRecentScores(List.of(dto));

        JsonNode node = mapper.readTree(mapper.writeValueAsString(stats));

        assertEquals(12, node.get("total_attempts").asInt());
        assertEquals(80.5, node.get("average_score").asDouble());
        assertEquals(95.0, node.get("highest_score").asDouble());
        assertEquals(60.0, node.get("lowest_score").asDouble());
        assertEquals(3, node.get("scenes_trained").asInt());
        assertEquals(2, node.get("skills_trained").asInt());
        assertTrue(node.get("recent_scores").isArray());
        assertTrue(node.get("total_attempts") != null && node.get("totalAttempts") == null);
    }

    @Test
    @DisplayName("BE-A-01 pagination envelope keeps explicit {list,total,page,page_size} keys")
    void envelope_keeps_explicitKeys() throws Exception {
        Map<String, Object> envelope = Map.of(
                "list", List.of(),
                "total", 0L,
                "page", 1,
                "page_size", 10);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(envelope));

        assertTrue(node.has("list"));
        assertTrue(node.has("total"));
        assertTrue(node.has("page"));
        assertTrue(node.has("page_size"));
    }
}
