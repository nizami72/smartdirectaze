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
    private final ConversationService conversationService;

    @Tool("Search for products in the store catalog by name or SKU to get current prices and stock. An empty query returns the whole catalog.")
    public ProductSearchResult searchProduct(@ToolMemoryId ConversationKey key,
                                             @P("One key word of the product name or SKU, e.g. 'сумка', 'клатч'; empty for the whole catalog") String query) {
        List<ProductDTO> found = productService.searchForAiAssistant(key.shopId(), query);
        if (!found.isEmpty() || query == null || query.isBlank()) {
            return new ProductSearchResult(true, null, found);
        }
        // Catalog names are often in another language than the customer's words: let the model match by meaning
        return new ProductSearchResult(false,
                "No product name contains '" + query + "'. This is the shop's whole catalog: find the requested product "
                        + "by meaning in any language before saying it is not sold.",
                productService.searchForAiAssistant(key.shopId(), ""));
    }

    @Tool("Get the rules, prices, and delivery times, as well as 'Trying and Returns' policy for the current shop.")
    public String getShopPolicyInfo(@ToolMemoryId ConversationKey key) {
        ShopDto shop = productService.getShopById(key.shopId());
        StringBuilder sb = new StringBuilder();
        sb.append(describeDeliveryPrice(shop.deliveryPrice(), shop.freeDeliveryThreshold()));

        appendIfPresent(sb, "Delivery to other cities and regions", shop.regionsDeliveryInfo());
        appendIfPresent(sb, "When an order is delivered (processing time)", shop.processingTimeRules());
        appendIfPresent(sb, "Delivery hours", shop.deliveryWorkingHours());
        if (shop.courierWaitingTime() != null) {
            sb.append(String.format("The courier waits up to %d minutes. ", shop.courierWaitingTime()));
        }

        if (shop.zones() != null && !shop.zones().isEmpty()) {
            sb.append("Delivery zones: ");
            sb.append(shop.zones().stream()
                    .map(z -> z.name() + " (" + z.price() + " AZN)")
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append(". ");
        }

        sb.append(String.format("Fitting/Trying allowed: %s. ",
                shop.fittingAllowed() == null ? "Not specified; ask the seller" : shop.fittingAllowed() ? "Yes" : "No"));

        if (shop.refusalFee() != null) {
            sb.append(String.format("Refusal fee (if not buying after trying): %s AZN. ", shop.refusalFee()));
        }

        if (shop.tryingReturnsPolicy() != null && !shop.tryingReturnsPolicy().isBlank()) {
            sb.append(String.format("Trying and Returns Policy: %s. ", shop.tryingReturnsPolicy()));
        }

        // Добавь это в StringBuilder sb
        sb.append(String.format("Store Address: %s. ",
                specifiedOrUnknown(shop.address())));

        sb.append(String.format("Working Hours: %s. ",
                specifiedOrUnknown(shop.workingHours())));

        if (shop.paymentMethods() != null && !shop.paymentMethods().isEmpty()) {
            sb.append("Payment methods: ");
            sb.append(shop.paymentMethods().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append(". ");
        } else {
            sb.append("Payment methods: not specified; ask the seller. ");
        }

        return sb.toString().trim();
    }

    @Tool("Ask the human seller to answer one question. Call it when the shop data has no answer, the customer is unhappy "
            + "or complains, wants to change or cancel an order, or asks for something you cannot do.")
    public String requestHumanHelp(@ToolMemoryId ConversationKey key,
                                   @P("Short reason in Russian for the seller, e.g. 'Хочет скидку на 10 штук'") String reason) {
        if (key.customerChatId() != null) {
            conversationService.askSellerForHelp(key.shopId(), key.customerChatId(), reason);
        }
        return "The seller has been notified and will answer this question personally. Tell the customer briefly, in their "
                + "language, that the seller will answer it soon; do not answer it yourself. Keep helping with other questions.";
    }

    @Tool("Регистрация заявки на заказ, которую окончательно подтверждает продавец. Вызывай этот метод только после того, как клиент подтвердил имя, телефон, адрес и время доставки.")
    public String createFinalOrder(
            @ToolMemoryId ConversationKey key,
            @P("Имя клиента") String customerName,
            @P("Номер телефона клиента") String phoneNumber,
            @P("Полный адрес доставки и время") String deliveryAddress,
            @P("Список товаров и их количество, без цен и итоговых сумм") String itemsSummary,
            @P("Выбранный способ оплаты") String paymentMethod
    ) {
        // 1. Логика сохранения в твою БД (Table: Orders)
        Long shopId = key.shopId();
        OrderDTO order = orderService.createNewOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);

        // 2. Отправка уведомления владельцу (через твой Telegram Bot Service)
        notificationService.sendNewOrderAlertToOwner(shopId, order);

        return "Заявка на заказ #" + order.getId() + " принята. Окончательную стоимость и наличие подтверждает продавец.";
    }

    private static String specifiedOrUnknown(String value) {
        return value == null || value.isBlank() ? "Not specified; ask the seller" : value;
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value.trim()).append(". ");
        }
    }

    /**
     * One unambiguous sentence for the model: a bare "free delivery threshold: 0" was read as "always free".
     */
    static String describeDeliveryPrice(BigDecimal price, BigDecimal freeThreshold) {
        if (price == null || price.signum() < 0) {
            return "Delivery price: not specified; ask the seller. ";
        }
        if (price.signum() == 0) {
            return "Delivery is free. ";
        }
        if (freeThreshold != null && freeThreshold.signum() > 0) {
            return String.format("Delivery price: %s AZN; delivery is free for orders from %s AZN. ", price, freeThreshold);
        }
        return String.format("Delivery price: %s AZN; there is no free delivery. ", price);
    }
}
