package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.entities.ProductAttributeEmbeddableEntity;
import az.nizami.smartdirectaze.catalog.entities.ProductEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

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
        if (entity.getUnitOfMeasure() != null) {
            dto.setUnitOfMeasure(new HashMap<>(entity.getUnitOfMeasure()));
        }

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


        dto.setAverageRating(entity.getAverageRating());
        dto.setReviewCount(entity.getReviewCount());

        return dto;
    }

    /**
     * Для сохранения или обновления данных
     */
    public void updateEntityFromDto(ProductDTO dto, ProductEntity entity) {
        if (dto == null || entity == null) return;

        if (dto.getSku() != null) entity.setSku(dto.getSku());
        entity.setShopId(dto.getShopId());
        entity.setBarcode(dto.getBarcode());
        entity.setSlug(dto.getSlug());

        // Очищаем и наполняем заново коллекции JPA
        if (dto.getTitles() != null) {
            entity.getTitles().clear();
            entity.getTitles().putAll(dto.getTitles());
        }

        if (dto.getDescriptions() != null) {
            entity.getDescriptions().clear();
            entity.getDescriptions().putAll(dto.getDescriptions());
        }

        entity.setBasePrice(dto.getBasePrice());
        entity.setSalePrice(dto.getSalePrice());
        if (dto.getCurrency() != null) entity.setCurrency(dto.getCurrency());
        entity.setVatRate(dto.getVatRate());

        entity.setStockQuantity(dto.getStockQuantity());
        if (dto.getTrackQuantity() != null) entity.setTrackQuantity(dto.getTrackQuantity());
        if (dto.getIsAvailable() != null) entity.setIsAvailable(dto.getIsAvailable());
        if (dto.getUnitOfMeasure() != null) {
            entity.getUnitOfMeasure().clear();
            entity.getUnitOfMeasure().putAll(dto.getUnitOfMeasure());
        }

        entity.setCategoryId(dto.getCategoryId());
        entity.setBrandName(dto.getBrandName());

        if (dto.getImageUrls() != null) {
            entity.getImageUrls().clear();
            entity.getImageUrls().addAll(dto.getImageUrls());
        }
        entity.setMainImageUrl(dto.getMainImageUrl());
        entity.setSize(dto.getSize());

        // Маппинг атрибутов
        if (dto.getAttributes() != null) {
            entity.getAttributes().clear();
            dto.getAttributes().forEach(a -> {
                ProductAttributeEmbeddableEntity attr = new ProductAttributeEmbeddableEntity();
                attr.setAttrKey(a.getKey());
                attr.setAttrValue(a.getValue());
                entity.getAttributes().add(attr);
            });
        }

        entity.setWeight(dto.getWeight());
        entity.setAverageRating(dto.getAverageRating());
        entity.setReviewCount(dto.getReviewCount());
    }
}