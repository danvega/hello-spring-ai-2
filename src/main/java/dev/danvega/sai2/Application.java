package dev.danvega.sai2;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner runner() {
		return args -> {
			String springBootVersion = SpringBootVersion.getVersion();
			String springAiVersion = ChatModel.class.getPackage().getImplementationVersion();

			System.out.println("Spring Boot Version: " + springBootVersion);
			System.out.println("Spring AI Version: " + springAiVersion);
		};
	}

}
