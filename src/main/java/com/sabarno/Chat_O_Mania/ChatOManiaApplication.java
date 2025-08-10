package com.sabarno.Chat_O_Mania;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(
				info = @Info(
						title = "Chat-O-Mania API",
						version = "1.0",
						description = "API for Chat-O-Mania, a chat application built with Spring Boot"
						// http://localhost:8080/swagger-ui/index.html
				)
)
public class ChatOManiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatOManiaApplication.class, args);
	}

}
