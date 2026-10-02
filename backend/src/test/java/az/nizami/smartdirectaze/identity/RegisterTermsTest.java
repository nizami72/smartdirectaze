package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.controller.AuthController;
import az.nizami.smartdirectaze.identity.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Merchants register only after accepting the pilot terms; the version they accepted is stored
class RegisterTermsTest {

    private final UserService users = mock(UserService.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthController controller =
            new AuthController(users, null, jwt, new AdminAccess("admin@smartdirect.az"));

    @Test
    void refusedWithoutAcceptedTerms() {
        UserDto request = UserDto.builder().email("shop@mail.az").password("secret123").build();

        var e = assertThrows(ResponseStatusException.class,
                () -> controller.register(request, null, new MockHttpServletResponse()));

        assertEquals(400, e.getStatusCode().value());
        verifyNoInteractions(users);
    }

    @Test
    void registeredWithAcceptedTerms() {
        UserDto request = UserDto.builder().email("shop@mail.az").password("secret123").termsVersion("2026-10-02").build();
        when(users.registerUser(any())).thenReturn(UserDto.builder().email("shop@mail.az").build());
        when(jwt.generateToken("shop@mail.az")).thenReturn("token");

        assertEquals(200, controller.register(request, null, new MockHttpServletResponse()).getStatusCode().value());
        verify(users).registerUser(argThat(dto -> "2026-10-02".equals(dto.getTermsVersion())));
    }
}
