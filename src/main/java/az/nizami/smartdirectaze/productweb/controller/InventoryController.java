package az.nizami.smartdirectaze.productweb.controller;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@Log4j2
public class InventoryController {

    //<editor-fold desc="Fields">
    private final ProductService productService;
    private final TelegramAuthService telegramAuthService;
    private final String masterBotToken;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public InventoryController(ProductService productService,
                               TelegramAuthService telegramAuthService,
                               @Value("${telegram.token}")String masterBotToken) {
        this.productService = productService;
        this.telegramAuthService = telegramAuthService;
        this.masterBotToken = masterBotToken;
    }
    //</editor-fold>

    @GetMapping("/webhooks/inventory")
    public String getProducts(@RequestParam("shopId") String shopId) {
        log.debug("getProducts for shop [{}] called", shopId);
        return "auth";
    }

    @PostMapping("/webhooks/inventory/add")
    public String addProduct(@RequestParam("shopId") Long shopId,
                             @RequestParam("name") String name,
                             @RequestParam(value = "sku", required = false) String sku,
                             @RequestParam("salePrice") java.math.BigDecimal salePrice,
                             @RequestParam(value = "basePrice", required = false) java.math.BigDecimal basePrice,
                             @RequestParam(value = "currency", defaultValue = "AZN") String currency,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam(value = "brandName", required = false) String brandName,
                             @RequestParam(value = "barcode", required = false) String barcode,
                             @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
                             @RequestParam(value = "weight", required = false) Double weight,
                             @RequestParam(value = "size", required = false) String size,
                             @RequestParam(value = "mainImageUrl", required = false) String mainImageUrl,
                             @RequestParam(value = "unitOfMeasure", required = false) String unitOfMeasure,
                             @RequestParam(value = "isAvailable", defaultValue = "false") Boolean isAvailable) {
        ProductDTO productDto = new ProductDTO();
        productDto.getTitles().put("az", name);
        productDto.setSku(sku);
        productDto.setSalePrice(salePrice);
        productDto.setBasePrice(basePrice);
        productDto.setCurrency(currency);
        if (description != null && !description.isBlank()) {
            productDto.getDescriptions().put("ru", description);
        }
        productDto.setBrandName(brandName);
        productDto.setBarcode(barcode);
        productDto.setStockQuantity(stockQuantity);
        productDto.setWeight(weight);
        productDto.setSize(size);
        productDto.setMainImageUrl(mainImageUrl);
        if (unitOfMeasure != null && !unitOfMeasure.isBlank()) {
            productDto.getUnitOfMeasure().put("ru", unitOfMeasure);
        }
        productDto.setIsAvailable(isAvailable);

        productService.addProduct(shopId, productDto);
        return "redirect:https://qrfood.az/webhooks/inventory?shopId=" + shopId;
    }

    @PostMapping("/webhooks/api/auth-inventory")
    public String authenticateAndLoadInventory(
            @RequestParam("initData") String initData,
            @RequestParam("shopId") Long shopId,
            Model model) {

        // 1. Криптографическая проверка подписи Telegram (Алгоритм HMAC-SHA-256)
        if (!telegramAuthService.isValid(initData, masterBotToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Поддельные данные Telegram");
        }

        // 2. Извлекаем Telegram ID того, кто открыл страницу
        Long currentUserId = telegramAuthService.getUserIdFromInitData(initData);

        // 3. Проверяем права в базе данных
        if(!productService.isShopExist(shopId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Магазин не найден");
        if(!productService.isShopBelongToUser(shopId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не являетесь владельцем этого магазина");
        }

        // 4. Всё отлично! Достаем товары и рендерим фрагмент Thymeleaf
        List<ProductDTO> products = productService.findProductDtoForShop(shopId);
        model.addAttribute("products", products);
        model.addAttribute("shopId", shopId);

        // Возвращаем ТОЛЬКО кусок HTML с товарами (фрагмент), а не всю страницу целиком
        return "fragments/product-list :: inventory-content";
    }

}