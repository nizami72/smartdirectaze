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

}