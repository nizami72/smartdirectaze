package az.nizami.smartdirectaze.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WebhookRequest {

    @JsonProperty("typeWebhook")
    private String typeWebhook;

    @JsonProperty("idMessage")
    private String idMessage;

    @JsonProperty("instanceData")
    private InstanceData instanceData;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("senderData")
    private SenderData senderData;

    @JsonProperty("messageData")
    private MessageData messageData;


}