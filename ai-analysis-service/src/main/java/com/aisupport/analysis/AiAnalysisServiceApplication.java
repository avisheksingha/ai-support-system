package com.aisupport.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AiAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiAnalysisServiceApplication.class, args);
	}

}
