package az.nizami.smartdirectaze.identity.config;

import az.nizami.smartdirectaze.identity.Locales;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * Language of server texts (dashboard page, error messages): ?lang=, then the interface language the site sends
 * in Accept-Language, then the logged-in user's profile, else Azerbaijani.
 * The bean name "localeResolver" is the one Spring MVC looks up.
 */
@Component("localeResolver")
public class MerchantLocaleResolver implements LocaleResolver {

    private final UserService userService;

    // Lazy: the user service is not needed to build the web layer
    public MerchantLocaleResolver(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String param = request.getParameter("lang");
        if (isSupported(param)) {
            return Locale.forLanguageTag(Locales.supported(param));
        }
        String header = request.getHeader("Accept-Language");
        if (header != null && !header.isBlank()) {
            String first = header.split(",")[0].split(";")[0].split("-")[0].trim();
            if (isSupported(first)) {
                return Locale.forLanguageTag(Locales.supported(first));
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            String profile = userService.findByEmail(auth.getName()).map(UserDto::getLocale).orElse(null);
            if (isSupported(profile)) {
                return Locale.forLanguageTag(Locales.supported(profile));
            }
        }
        return Locale.forLanguageTag(Locales.DEFAULT);
    }

    private static boolean isSupported(String language) {
        return language != null && Locales.supported(language).equalsIgnoreCase(language.trim());
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("The language is chosen by the site and the user's profile");
    }
}
