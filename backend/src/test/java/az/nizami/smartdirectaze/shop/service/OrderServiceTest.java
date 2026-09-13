package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.OrderService;
import az.nizami.smartdirectaze.shop.entities.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    private OrderDTO seedOrder(Long shopId, String customer) {
        return orderService.createNewOrder(shopId, customer, "+994500000000",
                "Baku, near the fountain", "1x Dress", "cash");
    }

    @Test
    @DisplayName("New order starts in NEW status")
    void createNewOrder_ShouldDefaultToNewStatus() {
        OrderDTO order = seedOrder(9001L, "Alice");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    @DisplayName("getOrdersForShop returns only that shop's orders, newest first")
    void getOrdersForShop_ShouldBeScopedAndOrdered() {
        Long shopId = 9002L;
        seedOrder(shopId, "First");
        seedOrder(shopId, "Second");
        seedOrder(9999L, "OtherShop");

        List<OrderDTO> orders = orderService.getOrdersForShop(shopId);

        assertThat(orders).isNotEmpty();
        assertThat(orders).allMatch(o -> o.getShopId().equals(shopId));
        // newest first: the last-created ("Second") should come before "First"
        assertThat(orders.get(0).getCreatedAt())
                .isAfterOrEqualTo(orders.get(orders.size() - 1).getCreatedAt());
    }

    @Test
    @DisplayName("updateOrderStatus changes the status")
    void updateOrderStatus_ShouldChangeStatus() {
        Long shopId = 9003L;
        OrderDTO order = seedOrder(shopId, "Bob");

        OrderDTO updated = orderService.updateOrderStatus(shopId, order.getId(), OrderStatus.CONFIRMED);

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateOrderStatus refuses an order belonging to another shop (isolation)")
    void updateOrderStatus_ShouldEnforceShopIsolation() {
        OrderDTO order = seedOrder(9004L, "Carol");

        assertThatThrownBy(() ->
                orderService.updateOrderStatus(8004L, order.getId(), OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
