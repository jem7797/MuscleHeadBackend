package com.MuscleHead.MuscleHead;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class})
public class MuscleHeadApplication {

	public static void main(String[] args) {
		SpringApplication.run(MuscleHeadApplication.class, args);
	}

}
