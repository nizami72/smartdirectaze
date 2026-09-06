package az.nizami.smartdirectaze.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SenderData {

    @JsonProperty("chatId")
    private String chatId;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("senderName")
    private String senderName;

    @JsonProperty("senderContactName")
    private String senderContactName;

    @JsonProperty("chatName")
    private String chatName;

}