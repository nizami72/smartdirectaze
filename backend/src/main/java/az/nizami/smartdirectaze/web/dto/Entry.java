package az.nizami.smartdirectaze.web.dto;

import lombok.Data;
import java.util.List;

@Data
class Entry {
    private String id;
    private Long time;
    private List<Messaging> messaging;
}