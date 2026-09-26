package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.ConversationDto;
import az.nizami.smartdirectaze.shop.ConversationService;
import az.nizami.smartdirectaze.shop.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Chats handed over to the seller ("Нужен ответ" in the dashboard).
 */
@RestController
@RequestMapping("/api/v1/shops/{shopId}/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/waiting")
    public ResponseEntity<List<ConversationDto>> getWaitingForSeller(@PathVariable Long shopId,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        verifyOwnership(shopId, userDetails);
        return ResponseEntity.ok(conversationService.findWaitingForSeller(shopId));
    }

    @PostMapping("/{conversationId}/resume-ai")
    public ResponseEntity<Void> resumeAi(@PathVariable Long shopId,
                                         @PathVariable Long conversationId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        verifyOwnership(shopId, userDetails);
        conversationService.resumeAi(shopId, conversationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 404 for a foreign shop, so its existence is not revealed.
     */
    private void verifyOwnership(Long shopId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        Long userId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
        if (!productService.isShopBelongToUser(shopId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found");
        }
    }
}
