package az.nizami.smartdirectaze.catalog.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
    Optional<ProductEntity> findBySku(String sku);

    List<ProductEntity> findByShopId(Long shopId);

    @Query(value = "SELECT DISTINCT p.* FROM products p " +
            "LEFT JOIN product_titles pt ON p.id = pt.product_id " +
            "WHERE pt.title_text ILIKE CONCAT('%', TRIM(:query), '%') " +
            "OR p.sku ILIKE CONCAT('%', TRIM(:query), '%')",
            nativeQuery = true)
    List<ProductEntity> searchByKeyword(@Param("query") String query);
}