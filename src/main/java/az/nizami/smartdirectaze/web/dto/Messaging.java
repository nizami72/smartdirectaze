package az.nizami.smartdirectaze.web.dto;

import lombok.Data;

@Data
 class Messaging {
    private Sender sender;
    private Recipient recipient;
    private Long timestamp;
    private Message message;
}

