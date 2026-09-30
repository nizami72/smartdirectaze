package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.controller.AuthController;
import az.nizami.smartdirectaze.identity.config.JwtAuthenticationFilter;
import az.nizami.smartdirectaze.identity.service.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;

class LogoutTest {
    @Test
    void clearsCookieEvenWithInvalidOrExpiredJwtAndCanRepeat() throws Exception {
        var jwt = mock(JwtService.class);
        var filter = new JwtAuthenticationFilter(jwt, mock(org.springframework.security.core.userdetails.UserDetailsService.class));
        ReflectionTestUtils.setField(filter, "urlRegister", "/api/v1/auth/register");
        filter.init();
        var controller = new AuthController(null, null, jwt, null);
        ReflectionTestUtils.setField(controller, "cookieSecure", true);
        var mvc = MockMvcBuilders.standaloneSetup(controller).addFilters(filter).build();
        for (String token : new String[]{"invalid", "expired", ""}) {
            mvc.perform(post("/api/v1/auth/logout").cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("jwt_token=; Path=/; Max-Age=0")))
                .andExpect(cookie().httpOnly("jwt_token", true))
                .andExpect(cookie().secure("jwt_token", true))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")));
        }
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isNoContent());
        verifyNoInteractions(jwt);
    }
}
