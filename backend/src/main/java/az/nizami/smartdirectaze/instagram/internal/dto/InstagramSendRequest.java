package az.nizami.smartdirectaze.instagram.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstagramSendRequest {
    private Recipient recipient;
    private MessageData message;

    public static InstagramSendRequest buildewr(String recipientId, String messageText) {
        return new InstagramSendRequest(new Recipient(recipientId), new MessageData(messageText));
    }

}

@Data
@AllArgsConstructor
class Recipient {
    private String id;
}

@Data
@AllArgsConstructor
class MessageData {
    private String text;
}
