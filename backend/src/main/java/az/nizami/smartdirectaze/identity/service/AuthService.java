package az.nizami.smartdirectaze.identity.service;

import az.nizami.smartdirectaze.identity.dto.LoginRequest;
import az.nizami.smartdirectaze.identity.dto.LoginResponse;
import az.nizami.smartdirectaze.identity.entity.User;
import az.nizami.smartdirectaze.identity.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        String jwtToken = jwtService.generateToken(user.getEmail());

        return new LoginResponse(jwtToken, user.getRegistrationStep());
    }
}
