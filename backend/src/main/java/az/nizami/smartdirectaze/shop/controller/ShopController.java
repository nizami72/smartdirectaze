package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.dto.ShopCreateDto;
import az.nizami.smartdirectaze.shop.dto.ShopResponseDto;
import az.nizami.smartdirectaze.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ShopResponseDto> createShop(
            @Valid @RequestBody ShopCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long ownerId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        
        return ResponseEntity.ok(shopService.createShop(ownerId, dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ShopResponseDto>> getMyShops(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long ownerId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return ResponseEntity.ok(shopService.getUserShops(ownerId));
    }
}
