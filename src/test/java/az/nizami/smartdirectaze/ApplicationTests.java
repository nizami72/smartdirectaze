package az.nizami.smartdirectaze;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class ApplicationTests {

	ApplicationModules modules = ApplicationModules.of(Application.class);

	@Test
	void contextLoads() {
	}

	@Test
	void verifyModularity() {
		ApplicationModules.of(Application.class).verify();
	}

	@Test
	void writeDocumentation() {
		new Documenter(modules).writeModulesAsPlantUml();
	}

}
