package az.nizami.smartdirectaze;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class SmartdirectazeApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void verifyModularity() {
		ApplicationModules.of(Application.class).verify();
	}

}
