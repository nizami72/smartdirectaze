package az.nizami.smartdirectaze.event.dto;

import az.nizami.smartdirectaze.event.domain.GuestInvitationStatus;
import az.nizami.smartdirectaze.event.domain.Salutation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {
    private UUID id;
    private Salutation salutation;
    private String firstName;
    private String lastName;
    private String phone;
    private String whatsapp;
    private String email;
    private String language;
    private GuestInvitationStatus status;
}
