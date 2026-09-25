package az.nizami.smartdirectaze.whatsapp;

import az.nizami.smartdirectaze.whatsapp.controller.WhatsappWebhookController;
import az.nizami.smartdirectaze.whatsapp.dto.InstanceData;
import az.nizami.smartdirectaze.whatsapp.dto.MessageData;
import az.nizami.smartdirectaze.whatsapp.dto.SenderData;
import az.nizami.smartdirectaze.whatsapp.dto.TextMessageData;
import az.nizami.smartdirectaze.whatsapp.dto.WebhookRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class WhatsappWebhookControllerTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

    private WebhookRequest incomingText() {
        InstanceData instance = new InstanceData();
        instance.setIdInstance(7107000000L);
        SenderData sender = new SenderData();
        sender.setChatId("994551112233@c.us");
        TextMessageData text = new TextMessageData();
        text.setTextMessage("Salam");
        MessageData message = new MessageData();
        message.setTextMessageData(text);
        WebhookRequest request = new WebhookRequest();
        request.setTypeWebhook("incomingMessageReceived");
        request.setInstanceData(instance);
        request.setSenderData(sender);
        request.setMessageData(message);
        return request;
    }

    @Test
    void tokenSet_MissingOrWrongHeader_ShouldRejectWithoutProcessing() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "secret-token");

        assertEquals(HttpStatus.UNAUTHORIZED, controller.handleIncomingMessage(incomingText(), null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.handleIncomingMessage(incomingText(), "Bearer wrong").getStatusCode());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void tokenSet_RightHeader_ShouldAcceptWithAndWithoutBearer() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "secret-token");

        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), "Bearer secret-token").getStatusCode());
        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), "secret-token").getStatusCode());
        verify(publisher, org.mockito.Mockito.times(2)).publishEvent(any(WhatsappMessageReceivedEvent.class));
    }

    @Test
    void tokenNotSet_ShouldAccept() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "");

        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), null).getStatusCode());
        verify(publisher).publishEvent(any(WhatsappMessageReceivedEvent.class));
    }
}
