package az.nizami.smartdirectaze.identity.dto;

import az.nizami.smartdirectaze.identity.RegistrationStep;

public record LoginResponse(
    String token,
    RegistrationStep registrationStep
) {}
