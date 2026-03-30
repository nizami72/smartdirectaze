package az.nizami.smartdirectaze.user.service;

import az.nizami.smartdirectaze.user.UserDto;
import az.nizami.smartdirectaze.user.UserService;
import az.nizami.smartdirectaze.user.Role;
import az.nizami.smartdirectaze.user.repo.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<UserDto> findByOwnerId(Long ownerId) {
        return userRepository.findByOwnerId(ownerId).map(userMapper::toDto);
    }

    @Override
    public Role findUserRole(Long ownerId) {
         Optional<UserDto> f = findByOwnerId(ownerId);
         if(f.isPresent()){
             return f.get().getRole();
         } else {
             return Role.NEW_USER;
         }
    }

    @Override
    public UserDto save(UserDto userDto) {
        return userMapper.toDto(userRepository.save(userMapper.toEntity(userDto)));
    }

    @Override
    public void deleteByOwnerId(Long ownerId) {
        userRepository.deleteById(ownerId);
    }

}
