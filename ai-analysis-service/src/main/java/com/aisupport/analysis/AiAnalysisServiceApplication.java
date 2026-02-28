package com.aisupport.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableKafka
public class AiAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiAnalysisServiceApplication.class, args);
	}

}
