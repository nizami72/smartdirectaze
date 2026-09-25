package az.nizami.smartdirectaze.business.dto;

import az.nizami.smartdirectaze.business.Industry;

public record IndustrySummaryDto(
        Industry industry,
        String displayName,
        long count
) {}
