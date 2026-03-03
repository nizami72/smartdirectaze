package az.nizami.smartdirectaze.catalog.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter @Setter
class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    private String barcode;

    // Хранение мультиязычных названий: key - код языка (az, ru), value - текст
    @ElementCollection
    @CollectionTable(name = "product_titles", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "language_code")
    @Column(name = "title")
    private Map<String, String> titles = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "product_descriptions", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "language_code")
    @Column(name = "description", length = 2000)
    private Map<String, String> descriptions = new HashMap<>();

    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private String currency;

    private Integer stockQuantity;
    private Boolean isAvailable;

    // В реальном проекте здесь были бы ManyToOne для CategoryEntity
    private Long categoryId;

    // Характеристики (цвет, размер) можно хранить как JSON или также через ElementCollection
    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();
}