package az.nizami.smartdirectaze.ai.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Cases decided in code before the model is called: the customer asks for a person,
 * or sends something the AI cannot read. Reliable, unlike relying on the model alone.
 */
final class HandoffRules {

    /** Media the AI cannot read: handed over to the seller */
    private static final Map<String, String> MEDIA = Map.of(
            "audioMessage", "голосовое сообщение",
            "imageMessage", "фото",
            "videoMessage", "видео",
            "documentMessage", "документ",
            "locationMessage", "геолокация",
            "contactMessage", "контакт");

    /** Asking for a person: az / ru / en; stems so that inflected forms match too */
    private static final List<String> ASK_FOR_PERSON = List.of(
            "operator", "menecer", "canlı insan", "insanla", "satıcı ilə", "satıcıyla",
            "оператор", "менеджер", "живой человек", "живым человеком", "с человеком", "позовите продавца",
            "позови продавца", "дайте продавца", "связаться с продавцом",
            "manager", "human", "real person");

    private static final Pattern CYRILLIC = Pattern.compile("\\p{IsCyrillic}");

    private HandoffRules() {
    }

    /**
     * @return the reason to hand the chat over, or empty to let the AI answer
     */
    static Optional<String> reasonBeforeAi(String text, String messageType) {
        if (text == null || text.isBlank()) {
            String media = MEDIA.get(messageType);
            return media == null ? Optional.empty() : Optional.of("Клиент прислал " + media);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return ASK_FOR_PERSON.stream().anyMatch(lower::contains)
                ? Optional.of("Клиент просит продавца")
                : Optional.empty();
    }

    /**
     * Stickers, reactions etc.: neither answered nor handed over.
     */
    static boolean isIgnored(String text, String messageType) {
        return (text == null || text.isBlank()) && !MEDIA.containsKey(messageType);
    }

    /**
     * What the customer is told on a handover decided in code; in their language when it is known.
     */
    static String customerNotice(String text) {
        String ru = "Передал ваше сообщение продавцу, он скоро вам ответит 🙏";
        String az = "Mesajınızı satıcıya ötürdüm, tezliklə sizə cavab verəcək 🙏";
        if (text == null || text.isBlank()) {
            return az + "\n" + ru;
        }
        return CYRILLIC.matcher(text).find() ? ru : az;
    }

    /**
     * Label stored as the last message for media, e.g. "[голосовое сообщение]".
     */
    static String messageLabel(String text, String messageType) {
        if (text != null && !text.isBlank()) {
            return text;
        }
        return "[" + MEDIA.getOrDefault(messageType, messageType) + "]";
    }
}
