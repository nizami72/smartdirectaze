package az.nizami.smartdirectaze.shop;

import az.nizami.smartdirectaze.shop.entities.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDTO createNewOrder(Long shopId, String customerName, String phoneNumber, String deliveryAddress, String itemsSummary, String paymentMethod);

    /**
     * Список заказов магазина (свежие сверху).
     */
    List<OrderDTO> getOrdersForShop(Long shopId);

    /**
     * Смена статуса заказа. Заказ обязан принадлежать переданному магазину.
     */
    OrderDTO updateOrderStatus(Long shopId, Long orderId, OrderStatus status);
}
