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
                             @RequestParam("price") java.math.BigDecimal price) {
        ProductDTO productDto = new ProductDTO();
        productDto.getTitles().put("ru", name);
        productDto.setSalePrice(price);
        productService.addProduct(shopToken, productDto);
        return "redirect:/webhooks/inventory?token=" + shopToken;
    }
}