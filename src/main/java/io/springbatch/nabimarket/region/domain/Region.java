package io.springbatch.nabimarket.region.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "region",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_region_parent_name", columnNames = {"parent_id", "name"})
        },
        indexes = {
                @Index(name = "idx_region_level", columnList = "level")
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Convert(converter = RegionLevelConverter.class)
    @Column(nullable = false)
    private RegionLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Region(String name, RegionLevel level, Region parent, String fullName) {
        this.name = name;
        this.level = level;
        this.parent = parent;
        this.fullName = fullName;
    }
}