package az.nizami.smartdirectaze.ai.internal;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects the customer's language, so the model is told explicitly: with a Russian catalog it tends to
 * answer an Azerbaijani question in Russian.
 */
final class LanguageHint {

    private static final Pattern CYRILLIC = Pattern.compile("\\p{IsCyrillic}");
    private static final Pattern AZ_LETTERS = Pattern.compile("[əıığşçöüƏİĞŞÇÖÜ]");
    // Azerbaijani typed without special letters is common: "salam, canta var?"
    private static final Set<String> AZ_WORDS = Set.of("salam", "var", "yoxdur", "nece", "neche", "necedir", "qiymet",
            "catdirilma", "sifaris", "isteyirem", "olar", "zehmet", "mene", "bu", "gun", "sabah", "hansi", "ne", "ve");
    private static final Set<String> EN_WORDS = Set.of("hi", "hello", "do", "you", "have", "how", "much", "is", "the",
            "price", "delivery", "please", "want", "order", "what", "can", "i", "bag", "thanks");

    private LanguageHint() {
    }

    static Optional<String> detect(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }
        if (CYRILLIC.matcher(message).find()) {
            return Optional.of("Russian");
        }
        if (AZ_LETTERS.matcher(message).find()) {
            return Optional.of("Azerbaijani");
        }
        String[] words = message.toLowerCase(Locale.ROOT).split("[^a-z]+");
        long az = java.util.Arrays.stream(words).filter(AZ_WORDS::contains).count();
        long en = java.util.Arrays.stream(words).filter(EN_WORDS::contains).count();
        if (az == 0 && en == 0) {
            return Optional.empty();
        }
        return Optional.of(az >= en ? "Azerbaijani" : "English");
    }

    /**
     * The message as the model sees it: with the answer language appended when it is known.
     */
    static String withHint(String message) {
        return detect(message)
                .map(language -> message + "\n\n[Answer in " + language + "]")
                .orElse(message);
    }
}
