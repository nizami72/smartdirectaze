package az.nizami.smartdirectaze.event.dto;

import az.nizami.smartdirectaze.event.domain.GuestInvitationStatus;
import az.nizami.smartdirectaze.event.domain.Salutation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestRequest {
    private Salutation salutation;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;
    private String whatsapp;

    @Email(message = "Invalid email format")
    private String email;

    private String language;

    private GuestInvitationStatus status;
}
