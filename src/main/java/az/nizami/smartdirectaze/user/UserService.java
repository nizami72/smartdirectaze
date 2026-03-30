package az.nizami.smartdirectaze.user;

import java.util.Optional;

public interface UserService {

    Optional<UserDto> findByOwnerId(Long ownerId);

    Role findUserRole(Long ownerId);

    UserDto save(UserDto userDto);

    void deleteByOwnerId(Long ownerId);

}
