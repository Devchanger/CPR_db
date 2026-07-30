package com.cpr_db.cpr_db.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Global Jackson config: serialize to snake_case.
 * Front-end table props and form fields use snake_case (e.g. scene_name, total_score, created_at),
 * matching database column names. With SNAKE_CASE enabled globally, Java camelCase fields are
 * automatically converted to snake_case in output.
 *
 * Spring Boot 4 uses Jackson 3.x (package tools.jackson.databind).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
