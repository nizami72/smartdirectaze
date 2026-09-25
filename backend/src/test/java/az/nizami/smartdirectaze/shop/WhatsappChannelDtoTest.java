package az.nizami.smartdirectaze.shop;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhatsappChannelDtoTest {

    private static final String TEST_CHAT = "994551112233@c.us";
    private static final String CUSTOMER_CHAT = "994709998877@c.us";

    private WhatsappChannelDto channel(AiMode mode) {
        return new WhatsappChannelDto(1L, "7107000000", "token", mode, Set.of("994551112233"));
    }

    @Test
    void aiAnswers_On_ShouldAnswerEveryone() {
        assertTrue(channel(AiMode.ON).aiAnswers(TEST_CHAT));
        assertTrue(channel(AiMode.ON).aiAnswers(CUSTOMER_CHAT));
    }

    @Test
    void aiAnswers_Off_ShouldAnswerNobody() {
        assertFalse(channel(AiMode.OFF).aiAnswers(TEST_CHAT));
        assertFalse(channel(AiMode.OFF).aiAnswers(CUSTOMER_CHAT));
    }

    @Test
    void aiAnswers_Test_ShouldAnswerOnlyTestPhones() {
        assertTrue(channel(AiMode.TEST).aiAnswers(TEST_CHAT));
        assertFalse(channel(AiMode.TEST).aiAnswers(CUSTOMER_CHAT));
    }

    @Test
    void digits_ShouldNormalizePhonesAndChatIds() {
        assertEquals("994551112233", PhoneUtils.digits("+994 (55) 111-22-33"));
        assertEquals("994551112233", PhoneUtils.digits(TEST_CHAT));
        assertEquals("", PhoneUtils.digits(null));
    }
}
