package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.AdminAccess;
import az.nizami.smartdirectaze.shop.dto.channel.AdminWhatsappShopDto;
import az.nizami.smartdirectaze.shop.dto.channel.BindInstanceRequest;
import az.nizami.smartdirectaze.shop.service.AiChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Operator page: which shop uses which Green API instance.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminWhatsappController {

    private final AdminAccess adminAccess;
    private final AiChannelService aiChannelService;

    // Lets the frontend show the admin link; not an error for regular users
    @GetMapping("/me")
    public ResponseEntity<Map<String, Boolean>> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of("admin", userDetails != null && adminAccess.isAdmin(userDetails.getUsername())));
    }

    @GetMapping("/whatsapp/shops")
    public ResponseEntity<List<AdminWhatsappShopDto>> listShops(@AuthenticationPrincipal UserDetails userDetails) {
        requireAdmin(userDetails);
        return ResponseEntity.ok(aiChannelService.listShopsForAdmin());
    }

    @PutMapping("/whatsapp/shops/{shopId}")
    public ResponseEntity<AdminWhatsappShopDto> bindInstance(@PathVariable Long shopId,
                                                             @Valid @RequestBody BindInstanceRequest request,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        requireAdmin(userDetails);
        return ResponseEntity.ok(aiChannelService.bindInstance(shopId, request.instanceId().trim(), request.apiToken().trim()));
    }

    @DeleteMapping("/whatsapp/shops/{shopId}")
    public ResponseEntity<AdminWhatsappShopDto> unbindInstance(@PathVariable Long shopId,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        requireAdmin(userDetails);
        return ResponseEntity.ok(aiChannelService.unbindInstance(shopId));
    }

    private void requireAdmin(UserDetails userDetails) {
        if (userDetails == null || !adminAccess.isAdmin(userDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        }
    }
}
