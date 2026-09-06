package az.nizami.smartdirectaze.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String password;
    private String username;
    private String googleId;
    private String name;
    private Set<String> phones;
    private String locale;
    private Integer emailSubscription;
    private Boolean isActive;
    private RegistrationStep registrationStep;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
