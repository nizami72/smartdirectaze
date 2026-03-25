package az.nizami.smartdirectaze.fakedata.service;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.entities.ProductAttributeEmbeddableEntity;
import az.nizami.smartdirectaze.catalog.entities.ProductEntity;
import az.nizami.smartdirectaze.catalog.internal.ProductRepository;
import az.nizami.smartdirectaze.fakedata.FakeDataService;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Log4j2
public class DataSeeder implements FakeDataService {

    private final ProductService productService;

    @Value("${app.seeder.product-count:50}")
    private int productCount;

    public DataSeeder(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void generateFakeData(Long shopId) {
        log.debug("Processing fake data for [{}]", shopId);
        List< ProductDTO> products = new ArrayList<>(productCount);

        Faker fakerEn = new Faker(new Locale("en"));
        Faker fakerRu = new Faker(new Locale("ru"));
        Faker fakerAz = new Faker(new Locale("az"));

        log.debug(" ⏳ Начинаем генерацию [{}] тестовых товаров", productCount);

        // Используем переменную productCount вместо жестко заданного числа
        for (int i = 0; i < productCount; i++) {
            ProductDTO productDto = new ProductDTO();
            
//            product.setShopId(fakerEn.number().numberBetween(1L, 100L));
            productDto.setShopId(shopId);
            productDto.setBarcode(fakerEn.code().ean13());
            productDto.setSlug(fakerEn.internet().slug());
            productDto.setCategoryId(fakerEn.number().numberBetween(1L, 20L));
            productDto.setBrandName(fakerEn.company().name());
            productDto.setStockQuantity(fakerEn.number().numberBetween(0, 500));
            productDto.setVatRate(18.0);

            double randomPrice = fakerEn.number().randomDouble(2, 10, 1000);
            BigDecimal basePrice = BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP);
            productDto.setBasePrice(basePrice);
            
            BigDecimal discount = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.80, 0.95));
            productDto.setSalePrice(basePrice.multiply(discount).setScale(2, RoundingMode.HALF_UP));

            productDto.getTitles().put("en", fakerEn.commerce().productName());
            productDto.getTitles().put("ru", fakerRu.commerce().productName());
            productDto.getTitles().put("az", fakerAz.commerce().productName());

            productDto.getDescriptions().put("en", fakerEn.lorem().sentence(10));
            productDto.getDescriptions().put("ru", fakerRu.lorem().sentence(10));
            productDto.getDescriptions().put("az", fakerAz.lorem().sentence(10));

            productDto.getUnitOfMeasure().put("en", "pcs");
            productDto.getUnitOfMeasure().put("ru", "шт");
            productDto.getUnitOfMeasure().put("az", "ədəd");

            productDto.setMainImageUrl("https://picsum.photos/seed/" + fakerEn.internet().uuid() + "/800/800");

            ProductDTO.ProductAttributeDTO attr1 = new ProductDTO.ProductAttributeDTO("Material", fakerEn.commerce().material());
            productDto.getAttributes().add(attr1);

            products.add(productDto);
        }

        productService.loadProducts(products);
        log.debug(" ✅ Успешно сгенерировано и сохранено [{}] товаров!", productCount);
    }
}