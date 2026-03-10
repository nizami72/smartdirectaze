package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<ProductDTO> getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional
    public void saveProduct(ProductDTO productDTO) {
        ProductEntity entity = productRepository.findBySku(productDTO.getSku())
                .orElseGet(ProductEntity::new);
        
        productMapper.updateEntityFromDto(productDTO, entity);
        productRepository.save(entity);
    }
}