package az.nizami.smartdirectaze.business.dto;

import az.nizami.smartdirectaze.business.domain.Industry;

import java.util.UUID;

public record BusinessSummaryDto(
        UUID id,
        String name,
        Industry industry
) {}
