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

    @Query("SELECT p FROM ProductEntity p JOIN p.titles t " +
            "WHERE LOWER(t) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ProductEntity> searchByKeyword(@Param("query") String query);
}