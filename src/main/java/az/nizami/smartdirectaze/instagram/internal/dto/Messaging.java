package az.nizami.smartdirectaze.instagram.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Messaging {
    private Long timestamp;
    private Message message;
    @JsonProperty("message_edit")
    private MessageEdit messageEdit;
}