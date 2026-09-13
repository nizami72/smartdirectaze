package az.nizami.smartdirectaze.shop.entities;

/**
 * Жизненный цикл заказа, созданного ИИ-продавцом.
 * Владелец магазина ведёт заказ по этим статусам из админки.
 */
public enum OrderStatus {
    NEW,
    CONFIRMED,
    IN_DELIVERY,
    COMPLETED,
    CANCELLED
}
