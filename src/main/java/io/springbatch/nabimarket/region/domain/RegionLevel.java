package io.springbatch.nabimarket.region.domain;

public enum RegionLevel {
    SIDO(1),         // 도/광역시
    SIGUNGU(2),      // 시/군/구
    EUPMYEONDONG(3); // 읍/면/동

    private final int value;

    RegionLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RegionLevel of(int value) {
        for (RegionLevel level : values()) {
            if (level.value == value) return level;
        }
        throw new IllegalArgumentException("Unknown level: " + value);
    }
}
