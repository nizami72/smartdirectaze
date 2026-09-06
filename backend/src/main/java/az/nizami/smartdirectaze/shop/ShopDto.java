package az.nizami.smartdirectaze.shop;

import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ShopDto(
        Long id,
        Long ownerId,
        String shopName,
        String knowledgeBase,
        Boolean isActive,
        java.math.BigDecimal deliveryPrice,
        java.math.BigDecimal freeDeliveryThreshold,
        List<DeliveryZoneDto> zones,
        String regionsDeliveryInfo,
        String processingTimeRules,
        String deliveryWorkingHours,
        Boolean collectPhone,
        Boolean collectAddress,
        Boolean collectLandmark,
        Boolean collectLocation,
        Integer courierWaitingTime,
        Boolean fittingAllowed,
        java.math.BigDecimal refusalFee,
        String tryingReturnsPolicy,
        String workingHours,
        String address,
        List<PaymentMethod> paymentMethods,

        String botUuid,

        // Telegram
        String telegramBotToken,
        String telegramBotUsername,
        ChannelStatus telegramStatus,

        // WhatsApp
        String whatsappInstanceId,
        String whatsappToken,
        ChannelStatus whatsappStatus,

        // Instagram
        String instagramPageId,
        String instagramAccessToken,
        ChannelStatus instagramStatus
) {
}
