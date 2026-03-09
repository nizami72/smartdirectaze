package az.nizami.smartdirectaze.instagram.internal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Setter
@Getter
@ToString
public class InstagramWebhookRequest {
    private String object;
    private List<Entry> entry;

    // Getters and Setters
}