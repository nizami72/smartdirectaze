package az.nizami.smartdirectaze;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import az.nizami.smartdirectaze.config.AppConfig;

@SpringBootTest
class ContextLoadTest {

    @MockBean
    private AppConfig appConfig;

    @Test
    void contextLoads() {
    }
}
