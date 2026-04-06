package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.catalog.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogToolsTest {

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CatalogTools catalogTools;

    @Test
    void createFinalOrder_ShouldCreateOrderAndSendNotification() {
        // Arrange
        Long shopId = 1L;
        String customerName = "John Doe";
        String phoneNumber = "+994501234567";
        String deliveryAddress = "Baku, Main St 1";
        String itemsSummary = "Product A x 2";
        String paymentMethod = "CASH";

        OrderDTO mockOrder = OrderDTO.builder()
                .id(100L)
                .shopId(shopId)
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .deliveryAddress(deliveryAddress)
                .itemsSummary(itemsSummary)
                .paymentMethod(paymentMethod)
                .build();

        when(orderService.createNewOrder(eq(shopId), eq(customerName), eq(phoneNumber), eq(deliveryAddress), eq(itemsSummary), eq(paymentMethod)))
                .thenReturn(mockOrder);

        // Act
        String result = catalogTools.createFinalOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);

        // Assert
        assertEquals("Заказ успешно создан. Номер заказа: #100", result);
        verify(orderService).createNewOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);
        verify(notificationService).sendNewOrderAlertToOwner(eq(shopId), eq(mockOrder));
    }
}
