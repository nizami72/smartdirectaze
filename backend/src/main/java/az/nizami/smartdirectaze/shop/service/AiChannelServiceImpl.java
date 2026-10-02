package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.AiMode;
import az.nizami.smartdirectaze.shop.PhoneUtils;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.dto.channel.AdminWhatsappShopDto;
import az.nizami.smartdirectaze.shop.dto.channel.AiSettingsDto;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppQrResponse;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import az.nizami.smartdirectaze.telegram.service.TelegramService;
import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import az.nizami.smartdirectaze.whatsapp.WhatsappStateChangedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChannelServiceImpl implements AiChannelService {

    private final AiChannelRepository aiChannelRepository;
    private final ShopRepository shopRepository;
    private final TelegramService telegramService;
    private final WhatsappService whatsappService;
    private final UserService userService;

    @Override
    @Transactional
    public void connectTelegram(Long shopId, String token) {
        String botUsername = telegramService.getBotUsername(token);
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        AiChannelEntity channel = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.TELEGRAM)
                .orElse(AiChannelEntity.builder()
                        .shop(shop)
                        .channelType(ChannelType.TELEGRAM)
                        .build());

        channel.setApiToken(token);
        channel.setInstanceExternalId(botUsername);
        channel.setChannelStatus(ChannelStatus.CONNECTED);

        aiChannelRepository.save(channel);
        log.info("Telegram bot @{} connected for shop {}", botUsername, shopId);
    }

    @Override
    @Transactional
    public void initWhatsApp(Long shopId, String instanceId, String token) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        AiChannelEntity channel = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .orElse(AiChannelEntity.builder()
                        .shop(shop)
                        .channelType(ChannelType.WHATSAPP)
                        .build());

        channel.setInstanceExternalId(instanceId);
        channel.setApiToken(token);
        channel.setChannelStatus(ChannelStatus.WAITING_QR);

        aiChannelRepository.save(channel);
        log.info("WhatsApp instance {} initialized for shop {}", instanceId, shopId);
    }

    @Override
    public WhatsAppQrResponse getWhatsAppQr(Long shopId) {
        log.debug("Get qr code for whatsapp channel activation [{}]", shopId);
        // 1. Быстро читаем данные из БД (транзакция откроется и закроется внутри репозитория)
        var channelOpt = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP);

        if (channelOpt.isEmpty()) {
            log.debug("Whatsapp channel still null, no instance_id no token [{}]", shopId);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        var aiChannelEntity = channelOpt.get();
        String instanceId = aiChannelEntity.getInstanceExternalId();
        String token = aiChannelEntity.getApiToken();

        if (instanceId == null || instanceId.isBlank() || token == null || token.isBlank()) {
            log.debug("Whatsapp channel not null, but some info still missing [{}]", shopId);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        // 2. ДЕЛАЕМ СЕТЕВЫЕ ВЫЗОВЫ (Соединение с БД СЕЙЧАС НЕ ИСПОЛЬЗУЕТСЯ)
        GreenApiResponseDto responseDto = whatsappService.getQrCode(instanceId, token);
        String type = responseDto.type();
        String message = responseDto.message();

        if ("alreadyLogged".equals(type)) {
            log.info("Already logged in");
            markConnected(aiChannelEntity);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.CONNECTED)
                    .connectedPhone(aiChannelEntity.getWid())
                    .build();
        } else if ("qrCode".equals(type)) {
            log.debug("Qr code are received, waiting for client phone to be logged in [{}]", shopId);
            aiChannelEntity.setChannelStatus(ChannelStatus.WAITING_QR);
            aiChannelRepository.save(aiChannelEntity);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.WAITING_QR)
                    .qrCode(message)
                    .build();
        } else if ("error".equals(type)) {
            log.error("AiChannelServiceImpl: operation failed");
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        return WhatsAppQrResponse.builder()
                .status(ChannelStatus.PENDING_ACTIVATION)
                .build();
    }

    @Override
    @Transactional
    public AiSettingsDto getWhatsAppAiSettings(Long shopId) {
        AiChannelEntity channel = findWhatsAppChannel(shopId);
        refreshStateIfUnknown(channel);
        return toAiSettings(channel);
    }

    @Override
    @Transactional
    public AiSettingsDto updateWhatsAppAiSettings(Long shopId, AiSettingsDto settings) {
        AiChannelEntity channel = findWhatsAppChannel(shopId);
        channel.setAiMode(settings.getAiMode());
        channel.getTestPhones().clear();
        settings.getTestPhones().stream()
                .map(PhoneUtils::digits)
                .filter(phone -> !phone.isEmpty())
                .forEach(channel.getTestPhones()::add);
        String notificationPhone = PhoneUtils.digits(settings.getNotificationPhone());
        channel.setNotificationPhone(notificationPhone.isEmpty() ? null : notificationPhone);
        log.info("WhatsApp AI settings of shop {} changed: mode {}, {} test phone(s)", shopId, channel.getAiMode(), channel.getTestPhones().size());
        return toAiSettings(channel);
    }

    private AiChannelEntity findWhatsAppChannel(Long shopId) {
        return aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .orElseThrow(() -> new RuntimeException("WhatsApp channel not found for shop: " + shopId));
    }

    private AiSettingsDto toAiSettings(AiChannelEntity channel) {
        return AiSettingsDto.builder()
                .aiMode(channel.getAiMode())
                .testPhones(new TreeSet<>(channel.getTestPhones()))
                .notificationPhone(channel.getNotificationPhone())
                .channelStatus(channel.getChannelStatus().name())
                .connectedPhone(channel.getWid())
                .build();
    }

    /**
     * Green API connection state of a shop's number: keeps the channel status and the connected number,
     * tells the operator in Telegram when a working number drops.
     */
    @EventListener
    @Transactional
    public void onStateChanged(WhatsappStateChangedEvent event) {
        aiChannelRepository.findByInstanceExternalIdAndChannelType(event.instanceId(), ChannelType.WHATSAPP)
                .ifPresent(channel -> {
                    ChannelStatus before = channel.getChannelStatus();
                    switch (String.valueOf(event.state())) {
                        case "authorized" -> markConnected(channel);
                        case "notAuthorized", "blocked", "yellowCard" -> {
                            channel.setChannelStatus(ChannelStatus.DISCONNECTED);
                            aiChannelRepository.save(channel);
                            if (before == ChannelStatus.CONNECTED || !"notAuthorized".equals(event.state())) {
                                telegramService.notifyAdmin(String.format(
                                        "⚠️ WhatsApp магазина «%s» (#%d) отключён: %s. Номер: %s",
                                        channel.getShop().getShopName(), channel.getShop().getId(), event.state(),
                                        channel.getWid() != null ? "+" + channel.getWid() : "—"));
                            }
                        }
                        default -> log.debug("WhatsApp instance {} state {}", event.instanceId(), event.state());
                    }
                    log.info("WhatsApp instance {} state {}: {} -> {}", event.instanceId(), event.state(), before, channel.getChannelStatus());
                });
    }

    private void markConnected(AiChannelEntity channel) {
        channel.setChannelStatus(ChannelStatus.CONNECTED);
        try {
            String wid = whatsappService.getSettings(channel.getInstanceExternalId(), channel.getApiToken());
            if (wid != null && !wid.isBlank()) {
                channel.setWid(PhoneUtils.digits(wid));
            }
        } catch (Exception e) {
            log.warn("AiChannelServiceImpl: operation failed");
        }
        aiChannelRepository.save(channel);
    }

    @Override
    @Transactional
    public List<AdminWhatsappShopDto> listShopsForAdmin() {
        aiChannelRepository.findAll().stream()
                .filter(channel -> channel.getChannelType() == ChannelType.WHATSAPP)
                .forEach(this::refreshStateIfUnknown);
        return shopRepository.findAll(Sort.by("id")).stream().map(this::toAdminDto).toList();
    }

    @Override
    @Transactional
    public AdminWhatsappShopDto bindInstance(Long shopId, String instanceId, String apiToken) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Магазин не найден"));
        aiChannelRepository.findByInstanceExternalIdAndChannelType(instanceId, ChannelType.WHATSAPP)
                .filter(other -> !other.getShop().getId().equals(shopId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
                            "Инстанс уже привязан к магазину #%d «%s». Сначала отвяжите его там.",
                            other.getShop().getId(), other.getShop().getShopName()));
                });

        // Also checks the pair: Green API rejects a wrong instance id or token
        try {
            whatsappService.configureWebhooks(instanceId, apiToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Green API не принял инстанс или токен. Проверьте idInstance и apiTokenInstance.");
        }

        initWhatsApp(shopId, instanceId, apiToken);
        AiChannelEntity channel = findWhatsAppChannel(shopId);
        if ("authorized".equals(safeState(instanceId, apiToken))) {
            markConnected(channel);
        }
        log.info("Admin bound WhatsApp instance {} to shop {}", instanceId, shopId);
        return toAdminDto(shop);
    }

    @Override
    @Transactional
    public AdminWhatsappShopDto unbindInstance(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Магазин не найден"));
        AiChannelEntity channel = findWhatsAppChannel(shopId);
        if (channel.getInstanceExternalId() != null && channel.getApiToken() != null) {
            whatsappService.logout(channel.getInstanceExternalId(), channel.getApiToken());
        }
        log.info("Admin unbound WhatsApp instance {} from shop {}", channel.getInstanceExternalId(), shopId);
        channel.setInstanceExternalId(null);
        channel.setApiToken(null);
        channel.setWid(null);
        channel.setChannelStatus(ChannelStatus.WAITING_FOR_INSTANCE);
        aiChannelRepository.save(channel);
        return toAdminDto(shop);
    }

    /**
     * The merchant scanned the QR with the wrong phone: log that phone out of the instance and show a fresh QR.
     * "For everyone" falls back to "Test", so a new number never answers all customers before it is checked.
     */
    @Override
    @Transactional
    public void disconnectWhatsApp(Long shopId) {
        AiChannelEntity channel = findWhatsAppChannel(shopId);
        if (channel.getInstanceExternalId() == null || channel.getApiToken() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "WhatsApp магазина ещё не подготовлен");
        }
        whatsappService.logout(channel.getInstanceExternalId(), channel.getApiToken());
        channel.setWid(null);
        channel.setChannelStatus(ChannelStatus.WAITING_QR);
        if (channel.getAiMode() == AiMode.ON) {
            channel.setAiMode(AiMode.TEST);
        }
        aiChannelRepository.save(channel);
        log.info("Merchant disconnected WhatsApp of shop {} to scan another phone", shopId);
    }

    /**
     * Channels bound before states were tracked (or while a webhook was missed) may show a stale status:
     * ask Green API once. Connected channels are kept up to date by the state webhook and are not asked.
     */
    private void refreshStateIfUnknown(AiChannelEntity channel) {
        if (channel.getChannelStatus() == ChannelStatus.CONNECTED
                || channel.getInstanceExternalId() == null || channel.getApiToken() == null) {
            return;
        }
        if ("authorized".equals(safeState(channel.getInstanceExternalId(), channel.getApiToken()))) {
            markConnected(channel);
        }
    }

    @Override
    public java.util.Map<Long, String> reconfigureAllWebhooks() {
        java.util.Map<Long, String> result = new java.util.TreeMap<>();
        aiChannelRepository.findAll().stream()
                .filter(c -> c.getChannelType() == ChannelType.WHATSAPP && c.getInstanceExternalId() != null && c.getApiToken() != null)
                .forEach(c -> {
                    try {
                        whatsappService.configureWebhooks(c.getInstanceExternalId(), c.getApiToken());
                        result.put(c.getShop().getId(), "ok");
                    } catch (Exception e) {
                        result.put(c.getShop().getId(), e.getMessage());
                    }
                });
        log.info("AiChannelServiceImpl: event processed");
        return result;
    }

    private String safeState(String instanceId, String apiToken) {
        try {
            return whatsappService.getStateInstance(instanceId, apiToken);
        } catch (Exception e) {
            return null;
        }
    }

    private AdminWhatsappShopDto toAdminDto(ShopEntity shop) {
        var channel = aiChannelRepository.findByShopIdAndChannelType(shop.getId(), ChannelType.WHATSAPP);
        String ownerEmail = userService.findById(shop.getOwnerId()).map(UserDto::getEmail).orElse(null);
        return new AdminWhatsappShopDto(shop.getId(), shop.getShopName(), ownerEmail,
                channel.map(c -> c.getChannelStatus().name()).orElse(null),
                channel.map(AiChannelEntity::getInstanceExternalId).orElse(null),
                channel.map(AiChannelEntity::getWid).orElse(null),
                channel.map(c -> c.getAiMode().name()).orElse(null));
    }

    @Override
    public List<AiChannelEntity> createBaseChannels(ShopEntity shopEntity) {
        return List.of(AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP)
                .channelStatus(ChannelStatus.WAITING_FOR_INSTANCE)
                .shop(shopEntity)
                .build());
    }


}
