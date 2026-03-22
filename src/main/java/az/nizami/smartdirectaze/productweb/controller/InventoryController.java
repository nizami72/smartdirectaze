package az.nizami.smartdirectaze.productweb.controller;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@Log4j2
public class InventoryController {

    private final ProductService productService;

    @GetMapping("/webhooks/inventory")
    public String getProducts(@RequestParam("token") String shopToken, Model model) {
        log.debug("getProducts called");
        List<ProductDTO> products = productService.findProductDtoForShop(shopToken);
        model.addAttribute("products", products);
        model.addAttribute("token", shopToken);
        return "products";
    }

    @PostMapping("/webhooks/inventory/add")
    public String addProduct(@RequestParam("token") String shopToken,
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
        productDto.getTitles().put("ru", name);
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

        productService.addProduct(shopToken, productDto);
        return "redirect:/webhooks/inventory?token=" + shopToken;
    }
}