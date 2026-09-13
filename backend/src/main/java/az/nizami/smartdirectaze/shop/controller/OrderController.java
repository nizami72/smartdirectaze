package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.OrderService;
import az.nizami.smartdirectaze.shop.dto.OrderStatusUpdateDto;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Управление заказами магазина владельцем.
 * Все операции изолированы: заказ доступен только владельцу магазина.
 */
@RestController
@RequestMapping("/api/v1/shops/{shopId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ShopRepository shopRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(
            @PathVariable Long shopId,
            @AuthenticationPrincipal UserDetails userDetails) {

        verifyOwnership(shopId, userDetails);
        return ResponseEntity.ok(orderService.getOrdersForShop(shopId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        verifyOwnership(shopId, userDetails);
        return ResponseEntity.ok(orderService.updateOrderStatus(shopId, orderId, dto.status()));
    }

    /**
     * Проверяет, что магазин существует и принадлежит текущему пользователю.
     * Иначе — 404, чтобы не раскрывать существование чужих магазинов.
     */
    private void verifyOwnership(Long shopId, UserDetails userDetails) {
        Long ownerId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        if (!shop.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found");
        }
    }
}
