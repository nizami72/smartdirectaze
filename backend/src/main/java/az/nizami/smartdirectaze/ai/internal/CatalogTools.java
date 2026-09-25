package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.shop.*;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogTools {

    private final ProductService productService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Tool("Search for products in the store catalog by name or SKU to get current prices and stock. An empty query returns the whole catalog.")
    public List<ProductDTO> searchProduct(@ToolMemoryId ConversationKey key,
                                          @P("Product name, part of the name or SKU; empty for the whole catalog") String query) {
        return productService.searchForAiAssistant(key.shopId(), query);
    }

    @Tool("Get the rules, prices, and delivery times, as well as 'Trying and Returns' policy for the current shop.")
    public String getShopPolicyInfo(@ToolMemoryId ConversationKey key) {
        ShopDto shop = productService.getShopById(key.shopId());
        StringBuilder sb = new StringBuilder();
        sb.append(describeDeliveryPrice(shop.deliveryPrice(), shop.freeDeliveryThreshold()));

        if (shop.zones() != null && !shop.zones().isEmpty()) {
            sb.append("Delivery zones: ");
            sb.append(shop.zones().stream()
                    .map(z -> z.name() + " (" + z.price() + " AZN)")
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append(". ");
        }

        sb.append(String.format("Fitting/Trying allowed: %s. ",
                shop.fittingAllowed() != null && shop.fittingAllowed() ? "Yes" : "No"));

        if (shop.refusalFee() != null) {
            sb.append(String.format("Refusal fee (if not buying after trying): %s AZN. ", shop.refusalFee()));
        }

        if (shop.tryingReturnsPolicy() != null && !shop.tryingReturnsPolicy().isBlank()) {
            sb.append(String.format("Trying and Returns Policy: %s. ", shop.tryingReturnsPolicy()));
        }

        // Добавь это в StringBuilder sb
        sb.append(String.format("Store Address: %s. ",
                shop.address() != null ? shop.address() : "Online-only store with delivery."));

        sb.append(String.format("Working Hours: %s. ",
                shop.workingHours() != null ? shop.workingHours() : "10:00 - 19:00"));

        if (shop.paymentMethods() != null && !shop.paymentMethods().isEmpty()) {
            sb.append("Payment methods: ");
            sb.append(shop.paymentMethods().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append(". ");
        } else {
            sb.append("Payment methods: m10, Credit Card, Cash on delivery. ");
        }

        return sb.toString().trim();
    }

    @Tool("Регистрация финального заказа в системе. Вызывай этот метод только после того, как клиент подтвердил имя, телефон, адрес и время доставки.")
    public String createFinalOrder(
            @ToolMemoryId ConversationKey key,
            @P("Имя клиента") String customerName,
            @P("Номер телефона клиента") String phoneNumber,
            @P("Полный адрес доставки и время") String deliveryAddress,
            @P("Список товаров и их количество") String itemsSummary,
            @P("Выбранный способ оплаты") String paymentMethod
    ) {
        // 1. Логика сохранения в твою БД (Table: Orders)
        Long shopId = key.shopId();
        OrderDTO order = orderService.createNewOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);

        // 2. Отправка уведомления владельцу (через твой Telegram Bot Service)
        notificationService.sendNewOrderAlertToOwner(shopId, order);

        return "Заказ успешно создан. Номер заказа: #" + order.getId();
    }

    /**
     * One unambiguous sentence for the model: a bare "free delivery threshold: 0" was read as "always free".
     */
    static String describeDeliveryPrice(BigDecimal price, BigDecimal freeThreshold) {
        if (price == null || price.signum() <= 0) {
            return "Delivery is free. ";
        }
        if (freeThreshold != null && freeThreshold.signum() > 0) {
            return String.format("Delivery price: %s AZN; delivery is free for orders from %s AZN. ", price, freeThreshold);
        }
        return String.format("Delivery price: %s AZN; there is no free delivery. ", price);
    }
}
