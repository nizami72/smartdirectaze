package az.nizami.smartdirectaze.user.service;

import az.nizami.smartdirectaze.user.UserDto;
import az.nizami.smartdirectaze.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .build();
    }

    public UserEntity toEntity(UserDto dto) {
        return UserEntity.builder()
                .id(dto.getId())
                .build();
    }
}
