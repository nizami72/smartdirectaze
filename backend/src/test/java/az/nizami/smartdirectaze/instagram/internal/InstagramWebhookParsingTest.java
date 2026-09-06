package az.nizami.smartdirectaze.instagram.internal;

import az.nizami.smartdirectaze.instagram.internal.dto.Entry;
import az.nizami.smartdirectaze.instagram.internal.dto.InstagramWebhookRequest;
import az.nizami.smartdirectaze.instagram.internal.dto.Messaging;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstagramWebhookParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testParsingNormalMessage() throws Exception {
        String payload = "[{\"object\":\"instagram\",\"entry\":[{\"time\":1772801951233,\"id\":\"17841405376618492\",\"messaging\":[{\"timestamp\":1772801951213,\"message\":{\"mid\":\"mid.123\",\"text\":\"Hello\"}}]}]}]";

        List<InstagramWebhookRequest> requests = objectMapper.readValue(payload, new TypeReference<List<InstagramWebhookRequest>>() {});

        assertNotNull(requests);
        assertEquals(1, requests.size());

        InstagramWebhookRequest request = requests.get(0);
        Entry entry = request.getEntry().get(0);
        Messaging messaging = entry.getMessaging().get(0);

        assertNotNull(messaging.getMessage());
        assertEquals("Hello", messaging.getMessage().getText());
    }

    @Test
    public void testParsingEditMessage() throws Exception {
        String payload = "[{\"object\":\"instagram\",\"entry\":[{\"time\":1772801951233,\"id\":\"17841405376618492\",\"messaging\":[{\"timestamp\":1772801951213,\"message_edit\":{\"mid\":\"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDA1Mzc2NjE4NDkyOjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI2MDE4MzI4MjYzNzY1NzA0NzozMjcwMjQxNjczMjM3MDMwODk1MTYzNTkxMjMwNDg4NTc2MAZDZD\",\"text\":\"\\u041e\\u043a\\u043f\\u043e\\u0440\\u043c\\u043f\",\"num_edit\":1}}]}]}]";

        List<InstagramWebhookRequest> requests = objectMapper.readValue(payload, new TypeReference<List<InstagramWebhookRequest>>() {});

        assertNotNull(requests);
        assertEquals(1, requests.size());

        InstagramWebhookRequest request = requests.get(0);
        Entry entry = request.getEntry().get(0);
        Messaging messaging = entry.getMessaging().get(0);

        assertNotNull(messaging.getMessageEdit());
        assertEquals("Окпормп", messaging.getMessageEdit().getText());
    }
}
