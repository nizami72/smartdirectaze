package az.nizami.smartdirectaze.catalog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ProductDTO {
    // 1. Базовая информация (ID и Метаданные)
    private Long id;
    private String sku; // Артикул (Stock Keeping Unit) - критично для бизнеса
    private String barcode; // EAN/UPC код
    
    // 2. Локализация (Мультиязычность)
    // Используем Map (напр. {"az": "Köynək", "ru": "Рубашка"}) 
    // чтобы не переписывать базу при добавлении английского
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private String slug; // URL-friendly название для SEO

    // 3. Ценообразование и Налоги (Используем BigDecimal!)
    private BigDecimal basePrice; // Себестоимость или базовая цена
    private BigDecimal salePrice; // Цена со скидкой
    private String currency; // "AZN", "USD"
    private Double vatRate; // НДС (EDV) - важно для налогового ассистента в будущем
    
    // 4. Складской учет (Inventory)
    private Integer stockQuantity; // Остаток на складе
    private Boolean trackQuantity; // Нужно ли вообще отслеживать остаток
    private Boolean isAvailable; // Виден ли продукт клиенту
    private String unitOfMeasure; // "шт", "кг", "литр"

    // 5. Категории и Бренды
    private Long categoryId;
    private String brandName;

    // 6. Медиа
    private List<String> imageUrls;
    private String mainImageUrl;

    // 7. Гибкие характеристики (Атрибуты)
    // Позволяет хранить что угодно: размер, цвет, материал, вольтаж
    private List<ProductAttributeDTO> attributes;

    // 8. Физические параметры (для логистики/доставки)
    private Double weight; // в кг
    private DimensionsDTO dimensions; // Длина/Ширина/Высота

    // 9. Рейтинги и Аналитика
    private Double averageRating;
    private Integer reviewCount;

    @AllArgsConstructor
    @Getter
    public static class ProductAttributeDTO {
        private String key; // "Color", "Size", "Memory"
        private String value; // "Black", "XL", "256GB"
    }
}


