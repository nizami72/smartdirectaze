package az.nizami.smartdirectaze.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ExtendedTextMessageData {

    @JsonProperty("text")
    private String text;
}
