package az.nizami.smartdirectaze.catalog;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public interface ProductService {

    @Async
    @Transactional
    void synchroniseProducts();

    Optional<ProductDTO> findForAiAssistant(String message);
}