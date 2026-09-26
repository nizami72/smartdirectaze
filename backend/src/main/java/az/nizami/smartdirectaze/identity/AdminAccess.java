package az.nizami.smartdirectaze.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform operators: emails from ADMIN_EMAILS (comma separated). Empty = nobody is admin.
 */
@Component
public class AdminAccess {

    private final Set<String> adminEmails;

    public AdminAccess(@Value("${app.admin.emails:}") String adminEmails) {
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAdmin(String email) {
        return email != null && adminEmails.contains(email.toLowerCase(Locale.ROOT));
    }
}
