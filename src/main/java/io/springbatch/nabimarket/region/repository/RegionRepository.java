package io.springbatch.nabimarket.region.repository;

import io.springbatch.nabimarket.region.domain.Region;
import io.springbatch.nabimarket.region.domain.RegionLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByLevelOrderByNameAsc(RegionLevel level);
    List<Region> findByParentIdOrderByNameAsc(Long parentId);

}
