package com.nextorm.portal.dto.common;

import lombok.Data;

import java.time.Instant;


@Data
public class ConstraintViloationDto {

    private String path;
    private Long id;
    private String name;
    private String createBy;
    private Instant createAt;

    public ConstraintViloationDto(String path, Long id, String name, String createBy, Instant createAt) {
        this.path = path;
        this.id = id;
        this.name = name;
        this.createBy = createBy;
        this.createAt = createAt;
    }

}
