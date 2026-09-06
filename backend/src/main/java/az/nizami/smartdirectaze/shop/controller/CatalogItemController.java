package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.RegistrationStep;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.ProductDTO;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.dto.CatalogItemRequestDto;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shops/catalog")
@RequiredArgsConstructor
public class CatalogItemController {

    private final ProductService productService;
    private final ShopRepository shopRepository;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProductDTO> addCatalogItem(
            @Valid @RequestBody CatalogItemRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        var user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ShopEntity shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new RuntimeException("Shop not found for current user"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setShopId(shop.getId());
        productDTO.setTitles(Map.of("ru", dto.getName())); // По умолчанию ru
        productDTO.setDescriptions(Map.of("ru", dto.getDescription() != null ? dto.getDescription() : ""));
        productDTO.setSalePrice(dto.getPrice());
        productDTO.setCurrency("AZN");
        productDTO.setIsAvailable(true);

        ProductDTO savedProduct = productService.addProduct(shop.getId(), productDTO, null);

        return ResponseEntity.ok(savedProduct);
    }
}
