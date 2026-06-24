package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class QaRequest {

    @NotBlank(message = "question is required")
    private String question;

    private List<ChatMessage> history;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessage> history) {
        this.history = history;
    }
}
