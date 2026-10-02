package az.nizami.smartdirectaze.shop;

public interface NotificationService {
    void sendNewOrderAlertToOwner(Long shopId, OrderDTO order);

    /**
     * "The seller is needed" alert to the merchant's WhatsApp.
     */
    /**
     * Test message to the notification phone (or the shop's own chat when it is empty), so the merchant
     * sees that alerts arrive. Returns the digits of the number it went to.
     */
    String sendTestNotification(Long shopId);

    void sendHumanHelpAlert(Long shopId, String customerChatId, String customerName, String reason, String lastMessage,
                            boolean aiPaused);
}
