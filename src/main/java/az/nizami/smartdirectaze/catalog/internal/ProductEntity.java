package az.nizami.smartdirectaze.catalog.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_shop_id", columnList = "shop_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_shop_sku", columnNames = {"shop_id", "sku"})
        }
)
@Getter
@Setter
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, updatable = false)
    private Long shopId;

    @Column(nullable = false)
    private String sku;

    private String barcode;
    private String slug;

    // --- Локализация ---
    @ElementCollection
    @CollectionTable(name = "product_titles", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "lang_code")
    @Column(name = "title_text")
    private Map<String, String> titles = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "product_descriptions", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "lang_code")
    @Column(name = "description_text", length = 2000)
    private Map<String, String> descriptions = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "product_unit_of_measure", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "lang_code")
    @Column(name = "unit_of_measure", length = 64)
    private Map<String, String> unitOfMeasure = new HashMap<>();

    // --- Финансы ---
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private String currency = "AZN";
    private Double vatRate;

    // --- Склад ---
    private Integer stockQuantity;
    private Boolean trackQuantity = true;
    private Boolean isAvailable = true;

    // --- Категории ---
    private Long categoryId;
    private String brandName;

    // --- Медиа ---
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private String mainImageUrl;

    // --- Гибкие атрибуты ---
    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductAttributeEmbeddable> attributes = new ArrayList<>();

    // --- Физические параметры ---
    private Double weight;
    private String size;

    // --- Аналитика ---
    private Double averageRating;
    private Integer reviewCount;

    @Column(name = "image_url")
    private String imageUrl;

    @PrePersist
    public void generateSkuIfMissing() {
        if (this.sku == null || this.sku.isBlank()) {
            // Простая генерация случайного 6-значного кода
            this.sku = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        }
    }

}

@Embeddable
@Getter
@Setter
class ProductAttributeEmbeddable {
    private String attrKey;
    private String attrValue;
}