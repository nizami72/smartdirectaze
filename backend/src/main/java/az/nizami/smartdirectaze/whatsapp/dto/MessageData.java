package az.nizami.smartdirectaze.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MessageData {

    @JsonProperty("typeMessage")
    private String typeMessage;

    @JsonProperty("textMessageData")
    private TextMessageData textMessageData;

    // Sent instead of textMessageData for replies, messages with links, forwarded text
    @JsonProperty("extendedTextMessageData")
    private ExtendedTextMessageData extendedTextMessageData;

    public String extractText() {
        if (textMessageData != null) {
            return textMessageData.getTextMessage();
        }
        if (extendedTextMessageData != null) {
            return extendedTextMessageData.getText();
        }
        return null;
    }
}
