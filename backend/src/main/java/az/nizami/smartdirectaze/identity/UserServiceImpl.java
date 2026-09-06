package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.exception.DuplicateEmailException;
import az.nizami.smartdirectaze.identity.entity.User;
import az.nizami.smartdirectaze.identity.entity.UserProfile;
import az.nizami.smartdirectaze.identity.repo.UserRepository;
import az.nizami.smartdirectaze.identity.repo.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    @Transactional // 🌟 Магия Hibernate сработает благодаря этой аннотации
    public UserDto update(UserDto dto) {

        User user = findByIdOrEmail(dto);
        // 2. Обновляем поля самого User
        userMapper.updateUserFromDto(dto, user);
        // 3. Обновляем поля связанного профиля, если он существует
        if (user.getProfile() != null) {
            userMapper.updateProfileFromDto(dto, user.getProfile());
        }
        // Вызывать .save() не нужно. Изменения автоматически применятся к базе.
        // Возвращаем обновленный DTO обратно клиенту
        return userMapper.toDto(user);
    }


    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto registerUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Пользователь с email %s уже зарегистрирован.".formatted(userDto.getEmail()));
        }

        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .registrationStep(RegistrationStep.ACCOUNT_CREATED)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .name(userDto.getName())
                .phones(userDto.getPhones() != null ? userDto.getPhones() : new HashSet<>())
                .build();

        user.setProfile(profile);

        // Assign default USER role
        roleRepository.findByName("USER").ifPresent(role -> user.getRoles().add(role));

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updateRegistrationStep(Long userId, RegistrationStep step) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRegistrationStep(step);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    private User findByIdOrEmail(UserDto dto) {
        if (dto.getId() != null) {
            Long id = dto.getId();
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
            return user;


        } else if (dto.getEmail() != null) {
            String email = dto.getEmail();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        }
        throw new EntityNotFoundException("User canot be found without id an email");
    }
}
