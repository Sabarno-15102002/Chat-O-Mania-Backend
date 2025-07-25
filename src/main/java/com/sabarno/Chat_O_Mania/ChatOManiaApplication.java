package com.sabarno.Chat_O_Mania;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class ChatOManiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatOManiaApplication.class, args);
	}

}
