package az.nizami.smartdirectaze.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class Message {
    private String mid;
    private String text;
    private List<Attachment> attachments;
}