package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.entity.Role;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDto> findAll();

    Optional<UserDto> findById(Long id);

    UserDto update(UserDto userDto);

    void deleteById(Long id);

    UserDto registerUser(UserDto userDto);

    void updateRegistrationStep(Long userId, RegistrationStep step);

    Optional<UserDto> findByEmail(String email);
}
