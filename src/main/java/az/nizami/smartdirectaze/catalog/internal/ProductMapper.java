package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.DimensionsDTO;
import az.nizami.smartdirectaze.catalog.ProductDTO;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

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
        dto.setSlug(entity.getSlug());

        // Глубокое копирование Map, чтобы избежать проблем с Hibernate Lazy Loading
        dto.setTitles(new HashMap<>(entity.getTitles()));
        dto.setDescriptions(new HashMap<>(entity.getDescriptions()));

        dto.setBasePrice(entity.getBasePrice());
        dto.setSalePrice(entity.getSalePrice());
        dto.setCurrency(entity.getCurrency());
        dto.setVatRate(entity.getVatRate());

        dto.setStockQuantity(entity.getStockQuantity());
        dto.setTrackQuantity(entity.getTrackQuantity());
        dto.setIsAvailable(entity.getIsAvailable());
        dto.setUnitOfMeasure(entity.getUnitOfMeasure());

        dto.setCategoryId(entity.getCategoryId());
        dto.setBrandName(entity.getBrandName());

        dto.setImageUrls(new ArrayList<>(entity.getImageUrls()));
        dto.setMainImageUrl(entity.getMainImageUrl());

        // Маппинг атрибутов
        if (entity.getAttributes() != null) {
            var attrs = entity.getAttributes().stream()
                    .map(a -> new ProductDTO.ProductAttributeDTO(a.getAttrKey(), a.getAttrValue()))
                    .collect(Collectors.toList());
            dto.setAttributes(attrs);
        }

        dto.setWeight(entity.getWeight());
        if (entity.getDimensions() != null) {
            DimensionsDTO dimDto = new DimensionsDTO();
            dimDto.setLength(entity.getDimensions().getLength());
            dimDto.setWidth(entity.getDimensions().getWidth());
            dimDto.setHeight(entity.getDimensions().getHeight());
            dto.setDimensions(dimDto);
        }

        dto.setAverageRating(entity.getAverageRating());
        dto.setReviewCount(entity.getReviewCount());

        return dto;
    }

    /**
     * Для сохранения или обновления данных
     */
    public void updateEntityFromDto(ProductDTO dto, ProductEntity entity) {
        if (dto == null || entity == null) return;

        entity.setSku(dto.getSku());
        entity.setBarcode(dto.getBarcode());
        entity.setSlug(dto.getSlug());

        // Очищаем и наполняем заново коллекции JPA
        entity.getTitles().clear();
        if (dto.getTitles() != null) entity.getTitles().putAll(dto.getTitles());

        entity.getDescriptions().clear();
        if (dto.getDescriptions() != null) entity.getDescriptions().putAll(dto.getDescriptions());

        entity.setBasePrice(dto.getBasePrice());
        entity.setSalePrice(dto.getSalePrice());
        entity.setCurrency(dto.getCurrency());
        entity.setVatRate(dto.getVatRate());

        entity.setStockQuantity(dto.getStockQuantity());
        entity.setTrackQuantity(dto.getTrackQuantity());
        entity.setIsAvailable(dto.getIsAvailable());
        entity.setUnitOfMeasure(dto.getUnitOfMeasure());

        entity.setCategoryId(dto.getCategoryId());
        entity.setBrandName(dto.getBrandName());

        entity.getImageUrls().clear();
        if (dto.getImageUrls() != null) entity.getImageUrls().addAll(dto.getImageUrls());
        entity.setMainImageUrl(dto.getMainImageUrl());

        // Маппинг атрибутов
        entity.getAttributes().clear();
        if (dto.getAttributes() != null) {
            dto.getAttributes().forEach(a -> {
                ProductAttributeEmbeddable attr = new ProductAttributeEmbeddable();
                attr.setAttrKey(a.getKey());
                attr.setAttrValue(a.getValue());
                entity.getAttributes().add(attr);
            });
        }

        entity.setWeight(dto.getWeight());
        if (dto.getDimensions() != null) {
            DimensionsEmbeddable dimensions = entity.getDimensions();
            if (dimensions == null) {
                dimensions = new DimensionsEmbeddable();
            }
            dimensions.setLength(dto.getDimensions().getLength());
            dimensions.setWidth(dto.getDimensions().getWidth());
            dimensions.setHeight(dto.getDimensions().getHeight());
            entity.setDimensions(dimensions);
        }

        entity.setAverageRating(dto.getAverageRating());
        entity.setReviewCount(dto.getReviewCount());
    }
}