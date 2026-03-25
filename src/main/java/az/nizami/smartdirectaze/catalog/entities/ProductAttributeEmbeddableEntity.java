package az.nizami.smartdirectaze.catalog.entities;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductAttributeEmbeddableEntity {
    private String attrKey;
    private String attrValue;
}
