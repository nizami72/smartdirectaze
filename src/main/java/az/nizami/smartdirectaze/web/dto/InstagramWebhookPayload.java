package az.nizami.smartdirectaze.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class InstagramWebhookPayload {
    private String object;
    private List<Entry> entry;
}

