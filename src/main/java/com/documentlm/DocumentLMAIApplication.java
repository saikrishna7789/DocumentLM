package com.documentlm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DocumentLMAIApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentLMAIApplication.class, args);
	}

}
