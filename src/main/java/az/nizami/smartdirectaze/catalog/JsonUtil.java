package az.nizami.smartdirectaze.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    public String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            System.out.println("[DEBUG_LOG] toJson input: " + obj + " | output: " + json);
            return json;
        } catch (JsonProcessingException e) {
            System.err.println("[DEBUG_LOG] Error in toJson: " + e.getMessage());
            e.printStackTrace();
            return "{}";
        }
    }
}
