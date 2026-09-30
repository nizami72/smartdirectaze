package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.shop.*;
import az.nizami.smartdirectaze.whatsapp.*;
import az.nizami.smartdirectaze.whatsapp.controller.WhatsappWebhookController;
import az.nizami.smartdirectaze.whatsapp.dto.*;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockEnvironment;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class MessageLoggingTest {
    @Test void webhookAndAiFailureDoNotLogContentOrProviderBody() {
        String secret = "PRIVATE_CUSTOMER_TEXT_937";
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start(); root.addAppender(appender);
        try {
            var inbox = mock(IncomingWhatsappMessageService.class);
            var controller = new WhatsappWebhookController(event -> {}, "token", inbox, new MockEnvironment());
            var request = new WebhookRequest();
            request.setTypeWebhook("incomingMessageReceived"); request.setIdMessage("msg");
            var instance = new InstanceData(); instance.setIdInstance(1L); request.setInstanceData(instance);
            var sender = new SenderData(); sender.setChatId("chat"); sender.setSenderName(secret); request.setSenderData(sender);
            var data = new MessageData(); var text = new TextMessageData(); text.setTextMessage(secret);
            data.setTextMessageData(text); data.setTypeMessage("textMessage"); request.setMessageData(data);
            controller.handleIncomingMessage(request, "token");
            var products = mock(ProductService.class); var ai = mock(AiService.class);
            when(products.findWhatsappChannel("1")).thenReturn(Optional.of(new WhatsappChannelDto(1L,"1","token",AiMode.ON,Set.of())));
            when(ai.answer(anyLong(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException(secret));
            var responder = new WhatsappAiResponder(products, ai, mock(WhatsappService.class), mock(ConversationService.class));
            responder.onMessage(new WhatsappMessageReceivedEvent("1","chat",secret,secret,"textMessage"));
            assertTrue(appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("AI processing failed")));
            for (var event : appender.list) {
                assertFalse(event.getFormattedMessage().contains(secret));
                assertNull(event.getThrowableProxy(), "Provider exception must not be attached");
            }
        } finally { root.detachAppender(appender); appender.stop(); }
    }
}
