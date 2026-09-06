package az.nizami.smartdirectaze.identity;
import az.nizami.smartdirectaze.identity.entity.Role;
import az.nizami.smartdirectaze.identity.entity.UserProfile;
import az.nizami.smartdirectaze.identity.entity.User;
import az.nizami.smartdirectaze.identity.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({UserServiceImpl.class, UserMapper.class, BCryptPasswordEncoder.class})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUser_ShouldCreateUserAndProfileWithEncryptedPassword() {
        // Arrange
        UserDto request = UserDto.builder()
        .email("i.sv@example.com")
        .name("Ilham Samad")
        .phones(Set.of("+994501234578"))
        .password("TxAk4VmDb8KJW")
        .build();

        // Act
        UserDto result = userService.registerUser(request);

        // Assert
        assertNotNull(result.getId());
        assertEquals("i.sv@example.com", result.getEmail());
        assertEquals("Ilham Samad", result.getName());
        assertNotNull(result.getPhones());
        assertFalse(result.getPhones().isEmpty());
        assertTrue(result.getPhones().contains("+994501234578"));
        assertNotEquals("TxAk4VmDb8KJW", result.getPassword());
        assertTrue(passwordEncoder.matches("TxAk4VmDb8KJW", result.getPassword()));

        // Verify in DB
        User user = userRepository.findById(result.getId()).orElseThrow();
        assertNotNull(user.getProfile());
        assertEquals("Ilham Samad", user.getProfile().getName());
        assertNotNull(user.getProfile().getPhones());
        assertFalse(user.getProfile().getPhones().isEmpty());
        assertTrue(user.getProfile().getPhones().contains("+994501234578"));
    }

    @Test
    void findAll_ShouldReturnListOfUserDtos() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .name("Test User")
                .build();
        user.setProfile(profile);
        userRepository.save(user);

        // Act
        List<UserDto> result = userService.findAll();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findById_ShouldReturnOptionalUserDto() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .name("Test User")
                .build();
        user.setProfile(profile);
        user = userRepository.save(user);

        // Act
        Optional<UserDto> result = userService.findById(user.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void update_ShouldThrowException_WhenUserDoesNotExist() {
        UserDto userDto = UserDto.builder()
                .id(999L)
                .email("test@example.com")
                .build();
        
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> userService.update(userDto));
    }
}
