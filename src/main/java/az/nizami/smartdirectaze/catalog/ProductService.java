package az.nizami.smartdirectaze.catalog;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ProductService {

    @Async
    @Transactional
    void synchroniseProducts();

    List<ProductDTO> searchForAiAssistant(String message);
}