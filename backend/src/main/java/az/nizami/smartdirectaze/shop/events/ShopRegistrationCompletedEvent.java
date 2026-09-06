package az.nizami.smartdirectaze.shop.events;

/**
 * Событие, публикуемое при завершении регистрации магазина (настройки каналов связи).
 * @param shopId ID созданного магазина
 * @param ownerId ID владельца магазина
 */
public record ShopRegistrationCompletedEvent(Long shopId, Long ownerId) {
}
