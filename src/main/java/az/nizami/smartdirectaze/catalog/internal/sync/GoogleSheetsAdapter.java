package az.nizami.smartdirectaze.catalog.internal.sync;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Log4j2
class GoogleSheetsAdapter implements ProductSourceAdapter {

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private static final String APPLICATION_NAME = "Smart-Direct AZE";
    private static final String RANGE = "Sheet1!A1:G"; // Начинаем со второй строки, чтобы пропустить заголовки
    private final Map<String, List<String>> productAliases;

    GoogleSheetsAdapter(Map<String, List<String>> productAliases) {
        this.productAliases = productAliases;
    }

    @Override
    public List<ProductDTO> fetchProducts() {
        try {
            Sheets sheetsService = createSheetsService();
            var response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, RANGE)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }

            List<Object> titles = values.removeFirst();
            return values.stream()
                    .map(o -> this.mapToProductDTO(titles, o))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from Google Sh eets", e);
        }
    }

    private ProductDTO mapToProductDTO(List<Object> titles, List<Object> row) {
        ProductDTO dto = new ProductDTO();
        titles
                .forEach(title -> {
                    String key = getIndexAndKey(title.toString());
                    if (key != null) {
                        callSetter(dto, key, row.get(titles.indexOf(title)));
                    } else {
                        log.debug("Unknown column header [{}]", title);
                    }
                });
        return dto;
    }

    private Sheets createSheetsService() throws Exception {
        var credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.GOOGLE_SHEETS;
    }

    public static void callSetter(Object obj, String fieldName, Object fieldValue) {
        if (obj == null || fieldName == null) return;

        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        try {
            // 1. Find the setter method by name
            Method setter = Arrays.stream(obj.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName))
                    .filter(m -> m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("Setter not found: " + setterName));
            // 2. Get the expected type from the setter
            Class<?> targetType = setter.getParameterTypes()[0];
            // 3. Convert the String fieldValue to the targetType
            Object convertedValue = convertValue(fieldValue, targetType);
            // 4. Invoke
            setter.invoke(obj, convertedValue);

        } catch (Exception e) {
            log.error("Failed to set field [{}]: {}", fieldName, e.getMessage());
        }
    }

    /**
     * Converts input (usually String from file) to the destination type
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) return null;

        // Convert to BigDecimal
        if (targetType == BigDecimal.class) {
            return new BigDecimal(strValue);
        }

        // Convert to Integer / int
        if (targetType == Integer.class || targetType == int.class) {
            // We use BigDecimal first to handle cases like "45.00" which Integer.parseInt fails on
            return (int) Double.parseDouble(strValue);
        }

        // Convert to Boolean / boolean
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(strValue) || strValue.equals("1") || strValue.equalsIgnoreCase("yes");
        }

        return value; // Return as-is if no conversion logic matches
    }

    private String getIndexAndKey(String titleVariant) {
        return productAliases.entrySet().stream()
                .filter(entry -> entry.getValue().contains(titleVariant))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null); // Or throw an exception if preferred
    }
}