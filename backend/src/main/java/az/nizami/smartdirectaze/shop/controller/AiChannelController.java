package az.nizami.smartdirectaze.shop.controller;

import az.nizami.smartdirectaze.identity.RegistrationStep;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.NotificationService;
import az.nizami.smartdirectaze.shop.dto.channel.AiSettingsDto;
import az.nizami.smartdirectaze.shop.dto.channel.TelegramChannelRequest;
import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppInitRequest;
import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppQrResponse;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import az.nizami.smartdirectaze.shop.service.AiChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops/channels")
@RequiredArgsConstructor
@Log4j2
public class AiChannelController {

    private final AiChannelService aiChannelService;
    private final ShopRepository shopRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/telegram")
    public ResponseEntity<Void> connectTelegram(
            @Valid @RequestBody TelegramChannelRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        validateOwner(request.getShopId(), userDetails);
        aiChannelService.connectTelegram(request.getShopId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/whatsapp/init")
    public ResponseEntity<Void> initWhatsApp(
            @Valid @RequestBody WhatsAppInitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Init [{}]", request.getShopId());
        
        validateOwner(request.getShopId(), userDetails);
        aiChannelService.initWhatsApp(request.getShopId(), request.getInstanceId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/whatsapp/qr")
    public ResponseEntity<WhatsAppQrResponse> getWhatsAppQr(@RequestParam Long shopId,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Get QR code [{}]", shopId);
        
        validateOwner(shopId, userDetails);
        return ResponseEntity.ok(aiChannelService.getWhatsAppQr(shopId));
    }

    // "I understand the risk" on the WhatsApp page: only after this the QR is given
    @PostMapping("/whatsapp/accept-risk")
    public ResponseEntity<Void> acceptWhatsAppRisk(@RequestParam Long shopId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        validateOwner(shopId, userDetails);
        aiChannelService.acceptWhatsAppRisk(shopId);
        return ResponseEntity.noContent().build();
    }

    // Wrong phone scanned: log it out, the page then shows a new QR
    @PostMapping("/whatsapp/disconnect")
    public ResponseEntity<Void> disconnectWhatsApp(@RequestParam Long shopId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        validateOwner(shopId, userDetails);
        aiChannelService.disconnectWhatsApp(shopId);
        return ResponseEntity.noContent().build();
    }

    // "Send a test notification": shows the merchant that order and help alerts reach the right phone
    @PostMapping("/whatsapp/test-notification")
    public ResponseEntity<java.util.Map<String, String>> sendTestNotification(@RequestParam Long shopId,
                                                                           @AuthenticationPrincipal UserDetails userDetails) {
        validateOwner(shopId, userDetails);
        return ResponseEntity.ok(java.util.Map.of("sentTo", notificationService.sendTestNotification(shopId)));
    }

    @GetMapping("/whatsapp/ai")
    public ResponseEntity<AiSettingsDto> getWhatsAppAiSettings(@RequestParam Long shopId,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        validateOwner(shopId, userDetails);
        return ResponseEntity.ok(aiChannelService.getWhatsAppAiSettings(shopId));
    }

    @PutMapping("/whatsapp/ai")
    public ResponseEntity<AiSettingsDto> updateWhatsAppAiSettings(@RequestParam Long shopId,
                                                                  @Valid @RequestBody AiSettingsDto settings,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        validateOwner(shopId, userDetails);
        return ResponseEntity.ok(aiChannelService.updateWhatsAppAiSettings(shopId, settings));
    }

    @PostMapping("/onboarding/complete")
    public ResponseEntity<Void> completeOnboarding(
            @RequestParam Long shopId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Complete onboarding [{}]", shopId);
        
        validateOwner(shopId, userDetails);

        return ResponseEntity.ok().build();
    }

    private void validateOwner(Long shopId, UserDetails userDetails) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        Long currentUserId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        
        if (!shop.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Access denied: You are not the owner of this shop");
        }
        log.debug("Validated user [{}] shop [{}]", userDetails.getUsername(), shopId);
    }
}
