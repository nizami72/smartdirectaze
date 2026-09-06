package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.dto.LoginRequest;
import az.nizami.smartdirectaze.identity.RegistrationStep;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.entity.User;
import az.nizami.smartdirectaze.identity.entity.UserProfile;
import az.nizami.smartdirectaze.identity.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Подгрузит application-test.yml ПОВЕРХ основного application.yml
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // <-- КРИТИЧЕСКИ ВАЖНАЯ СТРОКА
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    @Transactional
    void tearDown() {
        userRepository.deleteAll();
    }

    private final String userName = "Ilham Samad";
    private final String userEmail = "i.sv@example.com";
    private final String userPassword = "TxAk4VmDb8KJW";
    private final String userPhone = "+994501234578";

    @Test
    @Order(1)
    void registerUser_ShouldReturnTokenAndUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name(userName)
                .email(userEmail)
                .phones(java.util.Set.of(userPhone))
                .password(userPassword)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("jwt_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value(userEmail))
                .andExpect(jsonPath("$.user.name").value(userName))
                .andExpect(jsonPath("$.user.phones[0]").value(userPhone));
    }

    @Test
    @Order(2)
    void login_ShouldReturnTokenAndStep_WhenCredentialsValid() throws Exception {
        // Given
        String email = userEmail;
        String password = userPassword;
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .registrationStep(RegistrationStep.ACCOUNT_CREATED)
                .build();
        
        UserProfile profile = UserProfile.builder()
                .user(user)
                .name(userName)
                .phones(java.util.Set.of(userPhone))
                .build();
        user.setProfile(profile);
        
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(email, password);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("jwt_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.registrationStep").value("ACCOUNT_CREATED"));
    }

    @Test
    @Order(3)
    void login_ShouldReturn401_WhenPasswordInvalid() throws Exception {
        // Given
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("correct_password"))
                .registrationStep(RegistrationStep.ACCOUNT_CREATED)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .name("Test User")
                .build();
        user.setProfile(profile);

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(email, "wrong_password");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
