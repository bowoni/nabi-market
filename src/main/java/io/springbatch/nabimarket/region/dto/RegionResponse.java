package io.springbatch.nabimarket.region.dto;

import io.springbatch.nabimarket.region.domain.Region;
import io.springbatch.nabimarket.region.domain.RegionLevel;

public record RegionResponse(
        Long id,
        String name,
        String fullName,
        RegionLevel level
) {
    public static RegionResponse from(Region region) {
        return new RegionResponse(
                region.getId(),
                region.getName(),
                region.getFullName(),
                region.getLevel()
        );
    }
}
