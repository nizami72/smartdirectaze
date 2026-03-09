package az.nizami.smartdirectaze.catalog.internal.sync;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.internal.sync.ProductSourceAdapter;
import az.nizami.smartdirectaze.catalog.internal.sync.SourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class GoogleSheetsAdapter implements ProductSourceAdapter {

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private static final String APPLICATION_NAME = "Smart-Direct AZE";
    private static final String RANGE = "Sheet1!A2:G"; // Начинаем со второй строки, чтобы пропустить заголовки

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

            return values.stream()
                    .map(this::mapToProductDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from Google Sheets", e);
        }
    }

    private ProductDTO mapToProductDTO(List<Object> row) {
        ProductDTO dto = new ProductDTO();
        // A: SKU, B: Name_AZ, C: Name_RU, D: Price, E: Category, F: Stock
        dto.setSku(row.get(0).toString());
        
        // Мапим мультиязычные названия
        dto.setTitles(Map.of(
            "az", row.size() > 1 ? row.get(1).toString() : "",
            "ru", row.size() > 2 ? row.get(2).toString() : ""
        ));

        // Цена (BigDecimal)
        String priceStr = row.size() > 3 ? row.get(3).toString().replace(",", ".") : "0";
        dto.setSalePrice(new BigDecimal(priceStr));
        dto.setCurrency("AZN");

        // Остаток
        if (row.size() > 5) {
            dto.setStockQuantity(Integer.parseInt(row.get(5).toString()));
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
}