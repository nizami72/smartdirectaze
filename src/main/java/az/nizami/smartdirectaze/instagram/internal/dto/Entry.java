package az.nizami.smartdirectaze.instagram.internal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Setter
@Getter
@ToString
public class Entry {
    private Long time;
    private String id;
    private List<Messaging> messaging;

    // Getters and Setters
}