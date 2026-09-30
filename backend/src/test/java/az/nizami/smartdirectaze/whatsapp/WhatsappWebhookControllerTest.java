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

    private final IncomingWhatsappMessageService inbox = mock(IncomingWhatsappMessageService.class);
    private final org.springframework.mock.env.MockEnvironment env = new org.springframework.mock.env.MockEnvironment();

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
        request.setIdMessage("msg-1");
        request.setInstanceData(instance);
        request.setSenderData(sender);
        request.setMessageData(message);
        return request;
    }

    @Test
    void tokenSet_MissingOrWrongHeader_ShouldRejectWithoutProcessing() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "secret-token", inbox, env);

        assertEquals(HttpStatus.UNAUTHORIZED, controller.handleIncomingMessage(incomingText(), null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.handleIncomingMessage(incomingText(), "Bearer wrong").getStatusCode());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void tokenSet_RightHeader_ShouldAcceptWithAndWithoutBearer() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "secret-token", inbox, env);

        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), "Bearer secret-token").getStatusCode());
        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), "secret-token").getStatusCode());
        verify(inbox, org.mockito.Mockito.times(2)).accept(org.mockito.ArgumentMatchers.eq("msg-1"), any(WhatsappMessageReceivedEvent.class));
    }

    @Test
    void missingSecretFailsExceptExplicitLocalProfiles() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> new WhatsappWebhookController(publisher, " ", inbox, env));
        env.setActiveProfiles("prod", "test");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> new WhatsappWebhookController(publisher, "", inbox, env));
    }

    @Test
    void missingMessageIdIsRejectedAndDatabaseFailureNotAcknowledged() {
        var controller = new WhatsappWebhookController(publisher, "secret-token", inbox, env);
        var request = incomingText();
        request.setIdMessage(null);
        assertEquals(HttpStatus.BAD_REQUEST, controller.handleIncomingMessage(request, "secret-token").getStatusCode());
        org.mockito.Mockito.verifyNoInteractions(inbox);
        org.mockito.Mockito.doThrow(new RuntimeException("database down")).when(inbox)
                .accept(org.mockito.ArgumentMatchers.anyString(), any());
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> controller.handleIncomingMessage(incomingText(), "secret-token"));
    }

    @Test
    void tokenNotSet_ShouldAccept() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "", inbox, env.withProperty("spring.profiles.active", "test"));

        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(incomingText(), null).getStatusCode());
        verify(inbox).accept(org.mockito.ArgumentMatchers.eq("msg-1"), any(WhatsappMessageReceivedEvent.class));
    }

    @Test
    void stateChanged_ShouldPublishState() {
        WhatsappWebhookController controller = new WhatsappWebhookController(publisher, "", inbox, env.withProperty("spring.profiles.active", "test"));
        WebhookRequest request = new WebhookRequest();
        request.setTypeWebhook("stateInstanceChanged");
        InstanceData instance = new InstanceData();
        instance.setIdInstance(7107000000L);
        request.setInstanceData(instance);
        request.setStateInstance("notAuthorized");

        assertEquals(HttpStatus.OK, controller.handleIncomingMessage(request, null).getStatusCode());
        verify(publisher).publishEvent(new WhatsappStateChangedEvent("7107000000", "notAuthorized"));
    }
}
