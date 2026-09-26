package az.nizami.smartdirectaze.ai.internal;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandoffRulesTest {

    @Test
    void askingForPerson_ShouldHandOver_InAllLanguages() {
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.reasonBeforeAi("Позовите менеджера, пожалуйста", "textMessage"));
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.reasonBeforeAi("Canlı insanla danışmaq istəyirəm", "textMessage"));
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.reasonBeforeAi("Can I talk to a real person?", "extendedTextMessage"));
    }

    @Test
    void usualQuestion_ShouldGoToAi() {
        assertEquals(Optional.empty(), HandoffRules.reasonBeforeAi("Qara çanta var? Çatdırılma neçəyədir?", "textMessage"));
        assertEquals(Optional.empty(), HandoffRules.reasonBeforeAi("Сколько стоит доставка?", "textMessage"));
    }

    @Test
    void mediaAiCannotRead_ShouldHandOver() {
        assertEquals(Optional.of("Клиент прислал голосовое сообщение"), HandoffRules.reasonBeforeAi(null, "audioMessage"));
        assertEquals(Optional.of("Клиент прислал фото"), HandoffRules.reasonBeforeAi("", "imageMessage"));
    }

    @Test
    void stickersAndReactions_ShouldBeIgnored() {
        assertTrue(HandoffRules.isIgnored(null, "stickerMessage"));
        assertTrue(HandoffRules.isIgnored(null, "reactionMessage"));
        assertFalse(HandoffRules.isIgnored(null, "audioMessage"));
        assertFalse(HandoffRules.isIgnored("Salam", "textMessage"));
    }

    @Test
    void customerNotice_ShouldUseCustomerLanguage() {
        assertTrue(HandoffRules.customerNotice("Позовите менеджера").startsWith("Передал"));
        assertTrue(HandoffRules.customerNotice("Canlı insan lazımdır").startsWith("Mesajınızı"));
        // Voice: language unknown, both
        assertTrue(HandoffRules.customerNotice(null).contains("Mesajınızı") && HandoffRules.customerNotice(null).contains("Передал"));
    }
}
