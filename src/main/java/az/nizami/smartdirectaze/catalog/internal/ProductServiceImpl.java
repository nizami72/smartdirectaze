package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;

@Component
class ProductMapper {

    /**
     * Превращаем Entity в DTO для отдачи другим модулям
     */
    public ProductDTO toDto(ProductEntity entity) {
        if (entity == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setSku(entity.getSku());
        dto.setBarcode(entity.getBarcode());

        // Глубокое копирование Map, чтобы избежать проблем с Hibernate Lazy Loading
        dto.setTitles(new HashMap<>(entity.getTitles()));
        dto.setDescriptions(new HashMap<>(entity.getDescriptions()));

        dto.setBasePrice(entity.getBasePrice());
        dto.setSalePrice(entity.getSalePrice());
        dto.setCurrency(entity.getCurrency());

        dto.setStockQuantity(entity.getStockQuantity());
        dto.setIsAvailable(entity.getIsAvailable());
        dto.setCategoryId(entity.getCategoryId());

        // Маппинг атрибутов (Map -> List<AttributeDTO>)
        if (entity.getAttributes() != null) {
            var attrs = new ArrayList<ProductDTO.ProductAttributeDTO>();
            entity.getAttributes().forEach((k, v) -> {
                attrs.add(new ProductDTO.ProductAttributeDTO(k, v));
            });
            dto.setAttributes(attrs);
        }

        return dto;
    }

    /**
     * Для сохранения или обновления данных
     */
    public void updateEntityFromDto(ProductDTO dto, ProductEntity entity) {
        if (dto == null || entity == null) return;

        entity.setSku(dto.getSku());
        entity.setBarcode(dto.getBarcode());

        // Очищаем и наполняем заново коллекции JPA
        entity.getTitles().clear();
        if (dto.getTitles() != null) entity.getTitles().putAll(dto.getTitles());

        entity.getDescriptions().clear();
        if (dto.getDescriptions() != null) entity.getDescriptions().putAll(dto.getDescriptions());

        entity.setBasePrice(dto.getBasePrice());
        entity.setSalePrice(dto.getSalePrice());
        entity.setCurrency(dto.getCurrency());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setIsAvailable(dto.getIsAvailable());

        // Маппинг атрибутов обратно в Map
        entity.getAttributes().clear();
        if (dto.getAttributes() != null) {
            dto.getAttributes().forEach(a -> entity.getAttributes().put(a.getKey(), a.getValue()));
        }
    }
}