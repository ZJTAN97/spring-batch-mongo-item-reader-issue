package com.batch.mongoitemreader.issue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class IssueApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(IssueApplication.class);

		ApplicationContext ctx = app.run(args);

		PetJobService petJobService = ctx.getBean(PetJobService.class);

		try {
			petJobService.runJob();
		} catch(Exception e) {
			System.out.println("error");
		}


	}

}
