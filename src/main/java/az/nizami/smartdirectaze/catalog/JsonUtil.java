package az.nizami.smartdirectaze.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    public static String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            System.out.println("[DEBUG_LOG] toJson input: " + obj + " | output: " + json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Error in toJson:  [{}]", e.getMessage());
            return "[]";
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error in fromJson: [{}]", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, com.fasterxml.jackson.databind.JavaType type) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Error in fromJson: [{}]", e.getMessage());
            return null;
        }
    }

    public static com.fasterxml.jackson.databind.JavaType getListType(Class<?> clazz) {
        return objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, clazz);
    }
}
