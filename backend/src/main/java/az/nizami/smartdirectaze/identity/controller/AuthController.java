package az.nizami.smartdirectaze.identity.controller;

import az.nizami.smartdirectaze.identity.dto.AuthResponse;
import az.nizami.smartdirectaze.identity.dto.LoginRequest;
import az.nizami.smartdirectaze.identity.dto.LoginResponse;
import az.nizami.smartdirectaze.identity.service.AuthService;
import az.nizami.smartdirectaze.identity.service.JwtService;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto userDto, HttpServletResponse response) {
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

    private void setCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addHeader("Set-Cookie", "jwt_token=" + token + "; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax");
    }
}
