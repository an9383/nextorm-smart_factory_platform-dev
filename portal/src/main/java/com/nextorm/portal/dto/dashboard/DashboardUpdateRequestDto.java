package com.nextorm.portal.dto.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUpdateRequestDto {

    private Long id;
    private String name;
    private Integer sort;
    private Boolean isHide;
    private List<DashboardWidgetDto> widgets = new ArrayList<>();

    public Dashboard toEntity() {
        return Dashboard.builder()
                .id(id)
                .name(name)
                .isHide(isHide != null && isHide)
                .sort(sort)
                .widgets(widgets.stream()
                        .map(DashboardWidgetDto::toEntity)
                        .collect(Collectors.toList()))
                .build();
    }

}
