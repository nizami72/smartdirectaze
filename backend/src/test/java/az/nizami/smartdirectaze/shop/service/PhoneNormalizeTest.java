package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.PhoneUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhoneNormalizeTest {

    @Test
    void azerbaijanNumbersInAnyCommonForm() {
        assertEquals("994504679933", PhoneUtils.normalize("+994 50 467 99 33"));
        assertEquals("994504679933", PhoneUtils.normalize("050 467 99 33"));
        assertEquals("994504679933", PhoneUtils.normalize("50 467-99-33"));
        assertEquals("994504679933", PhoneUtils.normalize("00994504679933"));
    }

    @Test
    void incompleteAzerbaijanNumberIsRejected() {
        // The number that silenced the AI in the pilot test: "50" was missing
        assertNull(PhoneUtils.normalize("+9944679933"));
        assertNull(PhoneUtils.normalize("+994 50 467 99 3"));
        assertNull(PhoneUtils.normalize("12345"));
        assertNull(PhoneUtils.normalize(""));
    }

    @Test
    void otherCountriesKeepTheirCode() {
        assertEquals("79161234567", PhoneUtils.normalize("+7 916 123-45-67"));
        assertEquals("905321234567", PhoneUtils.normalize("+90 532 123 45 67"));
    }
}
