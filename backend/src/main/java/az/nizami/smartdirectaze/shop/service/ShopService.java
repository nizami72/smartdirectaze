package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.identity.RegistrationStep;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.dto.BaseShopDto;
import az.nizami.smartdirectaze.shop.dto.ShopCreateDto;
import az.nizami.smartdirectaze.shop.dto.ShopResponseDto;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.events.ShopRegistrationCompletedEvent;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final AiChannelService aiChannelService;

    /**
     * Создает магазин для владельца (Шаг 2 онбординга).
     */
    @Transactional
    public ShopResponseDto createShop(Long ownerId, ShopCreateDto dto) {
        log.info("Creating shop for owner {}: {}", ownerId, dto.shopName());

        ShopEntity shop = ShopEntity.builder()
                .ownerId(ownerId)
                .shopName(dto.shopName())
                .address(dto.address())
                .workingHours(dto.workingHours())
                .deliveryPrice(dto.deliveryPrice())
                .freeDeliveryThreshold(dto.freeDeliveryThreshold())
                .fittingAllowed(dto.fittingAllowed())
                .refusalFee(dto.refusalFee())
                .botUuid(UUID.randomUUID().toString())
                .isActive(true)
                .collectPhone(true)
                .collectAddress(true)
                .collectLandmark(true)
                .knowledgeBase("Вы — ИИ-ассистент магазина " + dto.shopName())
                .build();

        ShopEntity savedShop = shopRepository.save(shop);
        shop.setChannels(aiChannelService.createBaseChannels(savedShop));

        //todo decided to delete reg step "Обновляем шаг регистрации пользователя через UserService"
//        userService.updateRegistrationStep(ownerId, RegistrationStep.SHOP_CREATED);

        return new ShopResponseDto(
                savedShop.getId(),
                savedShop.getShopName(),
                RegistrationStep.SHOP_CREATED.name()
        );
    }

    /**
     * Создает базовую запись магазина на первом шаге онбординга.
     */
    @Transactional
    public ShopEntity createBaseShop(Long ownerId, BaseShopDto dto) {
        log.info("Creating base shop for owner {}: {}", ownerId, dto.getShopName());

        ShopEntity shop = ShopEntity.builder()
                .ownerId(ownerId)
                .shopName(dto.getShopName())
                .address(dto.getAddress())
                .workingHours(dto.getWorkingHours())
                .deliveryPrice(dto.getDeliveryPrice())
                .freeDeliveryThreshold(dto.getFreeDeliveryThreshold())
                .isActive(true)
                // Дефолтные настройки сбора данных
                .collectPhone(true)
                .collectAddress(true)
                .collectLandmark(false)
                .collectLocation(false)
                .build();

        return shopRepository.save(shop);
    }

    /**
     * Получает список магазинов для текущего пользователя.
     */
    public List<ShopResponseDto> getUserShops(Long ownerId) {
        log.info("Fetching shops for owner {}", ownerId);
        return shopRepository.findAllByOwnerId(ownerId).stream()
                .map(shop -> new ShopResponseDto(
                        shop.getId(),
                        shop.getShopName(),
                        null // Step not relevant here
                ))
                .toList();
    }


}
