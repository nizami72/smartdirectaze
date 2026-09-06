package az.nizami.smartdirectaze.event.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    private Salutation salutation;
    
    private String firstName;
    private String lastName;
    private String phone;
    private String whatsapp;
    private String email;
    private String language;
    
    @Enumerated(EnumType.STRING)
    private GuestInvitationStatus status;
}