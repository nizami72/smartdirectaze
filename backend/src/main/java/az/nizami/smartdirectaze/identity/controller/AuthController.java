package az.nizami.smartdirectaze.identity.controller;

import az.nizami.smartdirectaze.identity.AdminAccess;
import az.nizami.smartdirectaze.identity.dto.AuthResponse;
import az.nizami.smartdirectaze.identity.dto.LoginRequest;
import az.nizami.smartdirectaze.identity.dto.LoginResponse;
import az.nizami.smartdirectaze.identity.service.AuthService;
import az.nizami.smartdirectaze.identity.service.JwtService;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final AdminAccess adminAccess;

    // Secret of deploy/create-admin.sh, readable only by root on the server; empty = no admin can register
    @Value("${app.admin.bootstrap-token:}")
    private String adminBootstrapToken;

    // HTTPS on the server: the cookie is marked Secure
    @Value("${app.security.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto userDto,
                                                 @RequestHeader(value = "X-Admin-Bootstrap", required = false) String bootstrapToken,
                                                 HttpServletResponse response) {
        // Admin accounts are created only by deploy/create-admin.sh (over SSH), never through the site
        boolean admin = adminAccess.isAdmin(userDto.getEmail());
        if (admin && !isValidBootstrapToken(bootstrapToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Эта почта зарезервирована");
        }
        // Merchants accept the pilot terms (/terms) in the form; the admin is created over SSH and has none
        if (!admin && (userDto.getTermsVersion() == null || userDto.getTermsVersion().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Примите условия пилота, чтобы зарегистрироваться");
        }
        UserDto registeredUser = userService.registerUser(userDto);
        String token = jwtService.generateToken(registeredUser.getEmail());
        setCookie(response, token);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(registeredUser)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.authenticate(request);
        String token = loginResponse.token();
        setCookie(response, token);
        return ResponseEntity.ok(loginResponse);
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    private boolean isValidBootstrapToken(String token) {
        return !adminBootstrapToken.isBlank() && token != null
                && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), adminBootstrapToken.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "jwt_token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
                + (cookieSecure ? "; Secure" : ""));
        return ResponseEntity.noContent().build();
    }

    private void setCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", "jwt_token=" + token + "; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax"
                + (cookieSecure ? "; Secure" : ""));
    }
}
