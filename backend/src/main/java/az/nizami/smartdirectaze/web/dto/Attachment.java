package az.nizami.smartdirectaze.web.dto;

import lombok.Data;

@Data
class Attachment {
    private String type; // "image", "video", "file"
    private Payload payload;
}