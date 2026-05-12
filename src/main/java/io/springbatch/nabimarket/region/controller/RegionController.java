package io.springbatch.nabimarket.region.controller;

import io.springbatch.nabimarket.region.RegionService;
import io.springbatch.nabimarket.region.dto.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<RegionResponse>> getSidoList() {
        return ResponseEntity.ok(regionService.getSidoList());
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<RegionResponse>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getChildren(id));
    }

}
