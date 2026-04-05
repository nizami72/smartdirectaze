package az.nizami.smartdirectaze.catalog;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    public void testProductDtoSerialization() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setSku("SKU123");
        
        Map<String, String> titles = new HashMap<>();
        titles.put("az", "Test Name");
        dto.setTitles(titles);
        
        dto.setSalePrice(new BigDecimal("10.50"));
        dto.setCurrency("AZN");
        dto.setIsAvailable(true);

        // Add attributes
        dto.getAttributes().add(new ProductDTO.ProductAttributeDTO("Color", "Red"));

        String json = JsonUtil.toJson(dto);
        System.out.println("[DEBUG_LOG] Serialized JSON: " + json);
        
        assertNotNull(json);
        assertNotEquals("{}", json);
        assertTrue(json.contains("SKU123"));
        assertTrue(json.contains("Test Name"));
        assertTrue(json.contains("Color"));
        assertTrue(json.contains("Red"));
    }
}
