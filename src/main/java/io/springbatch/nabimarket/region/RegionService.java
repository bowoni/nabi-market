package io.springbatch.nabimarket.region;

import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.region.domain.RegionLevel;
import io.springbatch.nabimarket.region.dto.RegionResponse;
import io.springbatch.nabimarket.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public List<RegionResponse> getSidoList() {
        return regionRepository.findByLevelOrderByNameAsc(RegionLevel.SIDO)
                .stream()
                .map(RegionResponse::from)
                .toList();
    }

    public List<RegionResponse> getChildren(Long parentId) {
        if (!regionRepository.existsById(parentId)) {
            throw new BusinessException(ErrorCode.REGION_NOT_FOUND);
        }
        return regionRepository.findByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(RegionResponse::from)
                .toList();
    }
}
