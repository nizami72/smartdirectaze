package az.nizami.smartdirectaze.shop;

public interface OrderService {
    OrderDTO createNewOrder(Long shopId, String customerName, String phoneNumber, String deliveryAddress, String itemsSummary, String paymentMethod);
}
