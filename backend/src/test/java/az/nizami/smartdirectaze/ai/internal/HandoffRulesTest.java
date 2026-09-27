package az.nizami.smartdirectaze.ai.internal;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandoffRulesTest {

    @Test
    void askingForPerson_ShouldHandOver_InAllLanguages() {
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.beforeAi("Позовите менеджера, пожалуйста", "textMessage").map(HandoffRules.Handoff::reason));
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.beforeAi("Canlı insanla danışmaq istəyirəm", "textMessage").map(HandoffRules.Handoff::reason));
        assertEquals(Optional.of("Клиент просит продавца"), HandoffRules.beforeAi("Can I talk to a real person?", "extendedTextMessage").map(HandoffRules.Handoff::reason));
    }

    @Test
    void usualQuestion_ShouldGoToAi() {
        assertEquals(Optional.empty(), HandoffRules.beforeAi("Qara çanta var? Çatdırılma neçəyədir?", "textMessage"));
        assertEquals(Optional.empty(), HandoffRules.beforeAi("Сколько стоит доставка?", "textMessage"));
    }

    @Test
    void mediaAiCannotRead_ShouldHandOver() {
        assertEquals(Optional.of("Клиент прислал голосовое сообщение"), HandoffRules.beforeAi(null, "audioMessage").map(HandoffRules.Handoff::reason));
        assertEquals(Optional.of("Клиент прислал фото"), HandoffRules.beforeAi("", "imageMessage").map(HandoffRules.Handoff::reason));
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

    @Test
    void onlyAskingForPerson_ShouldPauseAi() {
        assertTrue(HandoffRules.beforeAi("Позовите менеджера", "textMessage").orElseThrow().pauseAi());
        assertFalse(HandoffRules.beforeAi(null, "audioMessage").orElseThrow().pauseAi());
    }
}
