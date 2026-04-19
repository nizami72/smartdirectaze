package az.nizami.smartdirectaze.fakedata.service;

import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.ShopDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void generateFakeShopFromJson_ShouldLoadAndSaveShop() {
        // Act
        dataSeeder.generateFakeShopFromJson(1L);

        // Assert
        verify(productService, atLeastOnce()).createShop(any(ShopDto.class));
    }
}
