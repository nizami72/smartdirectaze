package az.nizami.smartdirectaze.ai.internal;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageHintTest {

    @Test
    void detect_ShouldRecognizeCustomerLanguages() {
        assertEquals(Optional.of("Russian"), LanguageHint.detect("Сколько стоит доставка?"));
        assertEquals(Optional.of("Azerbaijani"), LanguageHint.detect("Qırmızı klatç Nəsimiyə çatdırılma ilə cəmi neçə olacaq?"));
        assertEquals(Optional.of("Azerbaijani"), LanguageHint.detect("salam, qara canta var?"));
        assertEquals(Optional.of("English"), LanguageHint.detect("Hi! Do you have a black leather bag?"));
        assertEquals(Optional.empty(), LanguageHint.detect("👍"));
    }

    @Test
    void withHint_ShouldAppendLanguage() {
        assertEquals("Kəmər var?\n\n[Answer in Azerbaijani]", LanguageHint.withHint("Kəmər var?"));
        assertEquals("👍", LanguageHint.withHint("👍"));
    }
}
