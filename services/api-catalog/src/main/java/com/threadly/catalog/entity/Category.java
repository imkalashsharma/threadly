package com.threadly.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (
        name="categories",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_category_name", columnNames = "name")
        }
)
public class Category extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status;
}
