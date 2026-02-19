package com.aisupport.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RoutingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingServiceApplication.class, args);
	}

}
