package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.shop.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

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

    @Mock
    private ConversationService conversationService;

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
        String result = catalogTools.createFinalOrder(new ConversationKey(shopId, "wa:1:994500000000@c.us", "994500000000@c.us"), customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);

        // Assert
        assertEquals("Заказ успешно создан. Номер заказа: #100", result);
        verify(orderService).createNewOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);
        verify(notificationService).sendNewOrderAlertToOwner(eq(shopId), eq(mockOrder));
    }

    @Test
    void searchProduct_ShouldSearchOnlyInConversationShop() {
        // Arrange
        ConversationKey key = new ConversationKey(2L, "tg:bot:42", null);
        List<ProductDTO> products = List.of(new ProductDTO());
        when(productService.searchForAiAssistant(2L, "çanta")).thenReturn(products);

        // Act
        List<ProductDTO> result = catalogTools.searchProduct(key, "çanta");

        // Assert
        assertEquals(products, result);
        verify(productService).searchForAiAssistant(2L, "çanta");
    }

    @Test
    void describeDeliveryPrice_ShouldNeverPromiseFreeDeliveryWithoutThreshold() {
        assertEquals("Delivery price: 5 AZN; there is no free delivery. ",
                CatalogTools.describeDeliveryPrice(new BigDecimal("5"), BigDecimal.ZERO));
        assertEquals("Delivery price: 5 AZN; there is no free delivery. ",
                CatalogTools.describeDeliveryPrice(new BigDecimal("5"), null));
        assertEquals("Delivery price: 5 AZN; delivery is free for orders from 50 AZN. ",
                CatalogTools.describeDeliveryPrice(new BigDecimal("5"), new BigDecimal("50")));
        assertEquals("Delivery is free. ", CatalogTools.describeDeliveryPrice(BigDecimal.ZERO, null));
    }

    @Test
    void requestHumanHelp_WhatsappChat_ShouldHandOverToSeller() {
        catalogTools.requestHumanHelp(new ConversationKey(4L, "wa:1:994551112233@c.us", "994551112233@c.us"), "Хочет скидку");
        verify(conversationService).handOverToSeller(4L, "994551112233@c.us", "Хочет скидку");
    }

    @Test
    void requestHumanHelp_WebTestChat_ShouldNotHandOver() {
        catalogTools.requestHumanHelp(new ConversationKey(4L, "web:3:session", null), "Хочет скидку");
        verify(conversationService, org.mockito.Mockito.never()).handOverToSeller(any(), any(), any());
    }
}
