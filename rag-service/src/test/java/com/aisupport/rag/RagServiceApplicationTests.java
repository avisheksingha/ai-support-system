package com.aisupport.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires running infrastructure (PostgreSQL/Kafka/Eureka and AI config)")
class RagServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
