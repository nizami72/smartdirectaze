package az.nizami.smartdirectaze.business.dto;

import az.nizami.smartdirectaze.business.domain.Industry;

import java.util.UUID;

/**
 * Результат выбора типа бизнеса: id созданного/переиспользованного Business,
 * который фронт пробрасывает в шаг создания магазина/ивента.
 */
public record ChosenBusinessDto(
        UUID businessId,
        Industry industry
) {}
