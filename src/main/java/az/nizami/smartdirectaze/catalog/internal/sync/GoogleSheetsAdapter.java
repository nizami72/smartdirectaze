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

    //<editor-fold desc="Fields">
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private static final String APPLICATION_NAME = "Smart-Direct AZE";
    private static final String RANGE = "Sheet1!A:Z";
    private final Map<String, List<String>> productAliases;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    GoogleSheetsAdapter(Map<String, List<String>> productAliases) {
        this.productAliases = productAliases;
    }
    //</editor-fold>

    @Override
    public List<ProductDTO> fetchProducts() {
        try {
            Sheets sheetsService = createSheetsService();
            var response = sheetsService
                    .spreadsheets()
                    .values()
                    .get(spreadsheetId, RANGE)
                    .execute();

            List<List<Object>> googleTable = response.getValues();
            if (googleTable == null || googleTable.isEmpty()) {
                return Collections.emptyList();
            }

            List<Object> googleTableTitles = googleTable.removeFirst();
            googleTable.removeFirst();
            return googleTable.stream()
                    .map(googleDataRow -> this.mapToProductDTO(googleTableTitles, googleDataRow))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from Google Sheets", e);
        }
    }

    private ProductDTO mapToProductDTO(List<Object> googleSheetTitles, List<Object> googleDataRow) {
        ProductDTO dto = new ProductDTO();
        int size = googleDataRow.size();
        for (int idx = 0; idx < size; idx++) {
            String title = googleSheetTitles.get(idx).toString();
            String value = googleDataRow.get(idx).toString();
            if (title == null) continue;
            if (value == null) value = "";
            if (title.startsWith("titles")) {
                dto.getTitles().put(title.split("_")[1], value);
            } else if (title.startsWith("descriptions")) {
                dto.getDescriptions().put(title.split("_")[1], value);
            } else if (title.startsWith("attributes")) {
                dto.getAttributes().add(new ProductDTO.ProductAttributeDTO(title.split("_")[1], value));
            } else if (title.startsWith("category")) {
                dto.getCategory().put(title.split("_")[1], value);
            } else if (title.startsWith("unitOfMeasure")) {
                dto.getUnitOfMeasure().put(title.split("_")[1], value);
            } else {
                callSetter(dto, title, googleDataRow.get(idx));
            }
        }
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
        Class<?> targetType = null;
        Object convertedValue = null;
        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        try {
            // 1. Find the setter method by name
            Method setter = Arrays.stream(obj.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName))
                    .filter(m -> m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("Setter not found: " + setterName));
            // 2. Get the expected type from the setter
            targetType = setter.getParameterTypes()[0];
            // 3. Convert the String fieldValue to the targetType
            convertedValue = convertValue(fieldValue, targetType);
            // 4. Invoke
            setter.invoke(obj, convertedValue);

        } catch (Exception e) {
            log.error("Failed to set field [{}], target type [{}], converted value [{}], exception [{}]", fieldName, targetType, convertedValue, e.getMessage());
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

        // Convert to Double
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue.replace(",", "."));
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