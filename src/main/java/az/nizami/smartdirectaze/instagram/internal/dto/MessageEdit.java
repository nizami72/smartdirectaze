package az.nizami.smartdirectaze.instagram.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MessageEdit {
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("text")
    private String text;
    @JsonProperty("num_edit")
    private Integer numEdit;

    // Getters and Setters
}