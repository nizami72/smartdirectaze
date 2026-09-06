package az.nizami.smartdirectaze.shop;

public interface NotificationService {
    void sendNewOrderAlertToOwner(Long shopId, OrderDTO order);
}
