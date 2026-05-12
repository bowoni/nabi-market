package io.springbatch.nabimarket.region.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRegionRequest(
        @NotNull(message = "동네 지역 ID는 필수 입니다.")
        Long regionId
) {
}
