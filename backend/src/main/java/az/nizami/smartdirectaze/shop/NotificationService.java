package az.nizami.smartdirectaze.shop;

public interface NotificationService {
    void sendNewOrderAlertToOwner(Long shopId, OrderDTO order);

    /**
     * "The seller is needed" alert to the merchant's WhatsApp.
     */
    void sendHumanHelpAlert(Long shopId, String customerChatId, String customerName, String reason, String lastMessage);
}
