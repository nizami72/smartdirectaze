package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.shop.AiMode;
import az.nizami.smartdirectaze.shop.ConversationService;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.WhatsappChannelDto;
import az.nizami.smartdirectaze.whatsapp.WhatsappMessageReceivedEvent;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WhatsappAiResponderTest {

    private static final String CHAT = "994551112233@c.us";

    private final ProductService productService = mock(ProductService.class);
    private final AiService aiService = mock(AiService.class);
    private final WhatsappService whatsappService = mock(WhatsappService.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final WhatsappAiResponder responder = new WhatsappAiResponder(productService, aiService, whatsappService, conversationService);

    @BeforeEach
    void setUp() {
        when(productService.findWhatsappChannel("7107000000"))
                .thenReturn(Optional.of(new WhatsappChannelDto(4L, "7107000000", "token", AiMode.ON, Set.of())));
    }

    private WhatsappMessageReceivedEvent message(String text, String type) {
        return new WhatsappMessageReceivedEvent("7107000000", CHAT, "Leyla", text, type);
    }

    @Test
    void usualQuestion_ShouldBeAnsweredByAi() {
        when(aiService.answer(eq(4L), anyString(), eq(CHAT), eq("Çanta var?"))).thenReturn("Bəli, var");

        responder.onMessage(message("Çanta var?", "textMessage"));

        verify(whatsappService).sendMessage("7107000000", "token", CHAT, "Bəli, var");
        verify(conversationService, never()).handOverToSeller(any(), any(), any());
    }

    @Test
    void chatWithSeller_ShouldStaySilent() {
        when(conversationService.recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "Çanta var?")).thenReturn(true);

        responder.onMessage(message("Çanta var?", "textMessage"));

        verifyNoInteractions(aiService, whatsappService);
    }

    @Test
    void askingForPerson_ShouldHandOverWithoutAi() {
        responder.onMessage(message("Позовите менеджера", "textMessage"));

        verify(conversationService).handOverToSeller(4L, CHAT, "Клиент просит продавца");
        verify(whatsappService).sendMessage(eq("7107000000"), eq("token"), eq(CHAT), startsWith("Передал"));
        verifyNoInteractions(aiService);
    }

    @Test
    void voiceMessage_ShouldHandOverWithoutAi() {
        responder.onMessage(message(null, "audioMessage"));

        verify(conversationService).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "[голосовое сообщение]");
        verify(conversationService).handOverToSeller(4L, CHAT, "Клиент прислал голосовое сообщение");
        verifyNoInteractions(aiService);
    }

    @Test
    void sticker_ShouldBeIgnored() {
        responder.onMessage(message(null, "stickerMessage"));

        verifyNoInteractions(aiService, whatsappService, conversationService);
    }

    @Test
    void aiFailure_ShouldHandOverAndTellCustomer() {
        when(aiService.answer(eq(4L), anyString(), eq(CHAT), anyString())).thenThrow(new RuntimeException("timeout"));

        responder.onMessage(message("Çanta var?", "textMessage"));

        verify(conversationService).handOverToSeller(4L, CHAT, "AI не смог ответить (ошибка сервиса)");
        verify(whatsappService).sendMessage(eq("7107000000"), eq("token"), eq(CHAT), startsWith("Mesajınızı"));
    }
}
