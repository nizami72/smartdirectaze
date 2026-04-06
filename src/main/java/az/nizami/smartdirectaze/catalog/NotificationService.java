package az.nizami.smartdirectaze.catalog;

public interface NotificationService {
    void sendNewOrderAlertToOwner(Long shopId, OrderDTO order);
}
