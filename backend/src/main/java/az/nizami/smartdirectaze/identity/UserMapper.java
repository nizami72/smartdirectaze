package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.entity.User;
import az.nizami.smartdirectaze.identity.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .registrationStep(user.getRegistrationStep())
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getProfile() != null ? user.getProfile().getName() : null)
                .googleId(user.getGoogleId())
                .name(user.getProfile() != null ? user.getProfile().getName() : null)
                .phones(user.getProfile() != null ? user.getProfile().getPhones() : null)
                .locale(user.getProfile() != null ? user.getProfile().getLocale() : null)
                .emailSubscription(user.getProfile() != null ? user.getProfile().getEmailSubscription() : null)
                .isActive(user.getProfile() != null ? user.getProfile().getIsActive() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .registrationStep(dto.getRegistrationStep())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .profile(UserProfile.builder().name(dto.getUsername()).build())
                .googleId(dto.getGoogleId())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    // --- НОВЫЕ МЕТОДЫ ДЛЯ ОБНОВЛЕНИЯ СУЩЕСТВУЮЩИХ ЭНТИТИ ---

    /**
     * Обновляет только те поля User, которые пришли в DTO (не null)
     */
    public void updateUserFromDto(UserDto dto, User user) {
        if (dto == null || user == null) return;

        if (dto.getRegistrationStep() != null) {
            user.setRegistrationStep(dto.getRegistrationStep());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }
        if (dto.getGoogleId() != null) {
            user.setGoogleId(dto.getGoogleId());
        }
        // Поля типа createdAt обычно не обновляются вручную, оставляем их
    }

    /**
     * Обновляет только те поля UserProfile, которые пришли в DTO (не null)
     */
    public void updateProfileFromDto(UserDto dto, UserProfile profile) {
        if (dto == null || profile == null) return;

        // В вашем toDto имя мапится и в username, и в name. Проверяем оба варианта
        if (dto.getName() != null) {
            profile.setName(dto.getName());
        } else if (dto.getUsername() != null) {
            profile.setName(dto.getUsername());
        }

        if (dto.getPhones() != null) {
            profile.setPhones(dto.getPhones());
        }
        if (dto.getLocale() != null) {
            profile.setLocale(dto.getLocale());
        }
        if (dto.getEmailSubscription() != null) {
            profile.setEmailSubscription(dto.getEmailSubscription());
        }
        if (dto.getIsActive() != null) {
            profile.setIsActive(dto.getIsActive());
        }
    }

}
