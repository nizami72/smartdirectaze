package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.PhoneUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelpAlertTextTest {

    private final NotificationServiceImpl service = new NotificationServiceImpl(null, null, null, "https://smartdirect.qrfood.az");

    @Test
    void oneReplyLinkAndReadableNumber() {
        String text = service.formatHumanHelpMessage("994504679933@c.us", "Leyla", "скидка", "Endirim var?", false);

        assertTrue(text.contains("*Клиент:* +994 50 467 99 33 (Leyla)"));
        assertTrue(text.contains("👉 Ответить клиенту:\nhttps://wa.me/994504679933"));
        assertEquals(1, text.split("https://", -1).length - 1, "only one link");
    }

    @Test
    void prettyPhone() {
        assertEquals("+994 55 111 22 33", PhoneUtils.pretty("994551112233@c.us"));
        assertEquals("+79161234567", PhoneUtils.pretty("79161234567"));
        assertEquals("—", PhoneUtils.pretty(null));
    }
}
