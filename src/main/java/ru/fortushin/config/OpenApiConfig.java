package ru.fortushin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lmsOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("LMS — Learning Management System API")
                .version("1.0.0")
                .description("REST API for the ORM final project. "
                        + "Entities: User, Course, Module, Lesson, Assignment, Submission, "
                        + "Quiz, Question, AnswerOption, Enrollment, Review."));
    }
}