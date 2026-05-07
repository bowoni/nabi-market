package io.springbatch.nabimarket.region.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RegionLevelConverter implements AttributeConverter<RegionLevel, Integer> {
    @Override
    public Integer convertToDatabaseColumn(RegionLevel attribute) {
        return attribute == null ? null : attribute.getValue();
    }
    @Override
    public RegionLevel convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : RegionLevel.of(dbData);
    }
}