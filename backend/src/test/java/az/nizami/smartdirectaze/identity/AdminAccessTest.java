package az.nizami.smartdirectaze.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAccessTest {

    @Test
    void onlyListedEmails_CaseInsensitive() {
        AdminAccess access = new AdminAccess(" Owner@Example.com , second@example.com");
        assertTrue(access.isAdmin("owner@example.com"));
        assertTrue(access.isAdmin("SECOND@example.com"));
        assertFalse(access.isAdmin("other@example.com"));
        assertFalse(access.isAdmin(null));
    }

    @Test
    void notConfigured_NobodyIsAdmin() {
        assertFalse(new AdminAccess("").isAdmin("owner@example.com"));
    }
}
