package az.nizami.smartdirectaze.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InstanceData {

    @JsonProperty("idInstance")
    private long idInstance;

    @JsonProperty("wid")
    private String wid;

    @JsonProperty("typeInstance")
    private String typeInstance;


}