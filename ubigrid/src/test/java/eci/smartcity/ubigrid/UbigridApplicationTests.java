package eci.smartcity.ubigrid;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"ubigrid.seed.vehicles=false",
		"ubigrid.traffic.feed.enabled=false"
})
class UbigridApplicationTests {

	@Test
	void contextLoads() {
	}

}
