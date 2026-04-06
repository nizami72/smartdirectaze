package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.ShopDto;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogTools {

    private final ProductService productService;

    @Tool("Search for products in the store catalog by name or SKU to get current prices and stock.")
    public List<ProductDTO> searchProduct(String query) {
        // Вызываем твой метод из модуля Catalog
        return productService.searchForAiAssistant(query);
    }

    @Tool("Get the rules, prices, and delivery times, as well as 'Trying and Returns' policy for the current shop.")
    public String getShopPolicyInfo(@P("The ID of the shop to get info for") Long shopId) {
        ShopDto shop = productService.getShopById(shopId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Delivery price: %s AZN. Free delivery threshold: %s AZN. ",
                shop.deliveryPrice() != null ? shop.deliveryPrice() : "0",
                shop.freeDeliveryThreshold() != null ? shop.freeDeliveryThreshold() : "0"));

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
            @P("ID магазина") Long shopId,
            @P("Имя клиента") String customerName,
            @P("Номер телефона клиента") String phoneNumber,
            @P("Полный адрес доставки и время") String deliveryAddress,
            @P("Список товаров и их количество") String itemsSummary,
            @P("Выбранный способ оплаты") String paymentMethod
    ) {
        // 1. Логика сохранения в твою БД (Table: Orders)
        OrderDTO order = orderService.createNewOrder(shopId, customerName, phoneNumber, deliveryAddress, itemsSummary, paymentMethod);

        // 2. Отправка уведомления владельцу (через твой Telegram Bot Service)
        notificationService.sendNewOrderAlertToOwner(shopId, order);

        return "Заказ успешно создан. Номер заказа: #" + order.getId();
    }

}

