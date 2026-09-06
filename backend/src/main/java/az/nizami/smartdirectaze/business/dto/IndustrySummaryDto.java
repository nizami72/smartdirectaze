package az.nizami.smartdirectaze.business.dto;

import az.nizami.smartdirectaze.business.domain.Industry;

public record IndustrySummaryDto(
        Industry industry,
        String displayName,
        long count
) {}
