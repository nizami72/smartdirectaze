package az.nizami.smartdirectaze.productweb.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramAuthService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Главный метод проверки подлинности данных от Telegram
     */
    public boolean isValid(String initData, String botToken) {
        try {
            // 1. Разбиваем строку на параметры
            String[] pairs = initData.split("&");
            
            // Парсим в Map и сразу декодируем URL-кодировку (например, %22 в кавычки)
            Map<String, String> dataMap = Arrays.stream(pairs)
                    .map(pair -> pair.split("=", 2))
                    .collect(Collectors.toMap(
                            kv -> kv[0],
                            kv -> URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    ));

            // 2. Достаем и удаляем присланный hash (он не участвует в создании подписи)
            String receivedHash = dataMap.remove("hash");
            if (receivedHash == null) {
                return false;
            }

            // 3. Сортируем оставшиеся ключи по алфавиту и склеиваем в строку data_check_string
            String dataCheckString = dataMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));

            // 4. Генерируем секретный ключ: HMAC_SHA256(botToken, "WebAppData")
            // Внимание: ключ - "WebAppData", данные - токен бота
            byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken);

            // 5. Вычисляем финальный хэш от dataCheckString
            byte[] calculatedHashBytes = hmacSha256(secretKey, dataCheckString);
            String calculatedHash = bytesToHex(calculatedHashBytes);

            // 6. Сравниваем то, что посчитали мы, с тем, что прислал Telegram
            return calculatedHash.equals(receivedHash);

        } catch (Exception e) {
            // В случае любой ошибки парсинга или криптографии считаем данные поддельными
            return false;
        }
    }

    /**
     * Извлекает Telegram ID владельца из проверенной строки initData
     */
    public Long getUserIdFromInitData(String initData) {
        try {
            String[] pairs = initData.split("&");
            for (String pair : pairs) {
                if (pair.startsWith("user=")) {
                    // Декодируем JSON строку пользователя
                    String userJson = URLDecoder.decode(pair.substring(5), StandardCharsets.UTF_8);
                    JsonNode userNode = objectMapper.readTree(userJson);
                    return userNode.get("id").asLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Если не нашли ID
    }

    // --- Вспомогательные криптографические методы ---

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}