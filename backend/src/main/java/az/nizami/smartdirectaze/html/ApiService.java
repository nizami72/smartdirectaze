package az.nizami.smartdirectaze.html;

import java.util.Map;

public interface ApiService {

    String sendPostRequest(String endpoint, Object body, Map<String, String> headers);
}
