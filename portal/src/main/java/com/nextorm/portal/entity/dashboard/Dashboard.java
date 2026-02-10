package com.nextorm.portal.entity.dashboard;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dashboard")
@Getter
public class Dashboard extends BaseEntity {
	@Column(name = "name")
	private String name;

	@Column(name = "sort")
	@Comment("대시보드 정렬 순서")
	private Integer sort;

	@Column(name = "is_hide", nullable = false)
	@Comment("대시보드 숨김 여부")
	private boolean isHide = false;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dashboard", cascade = {CascadeType.ALL}, orphanRemoval = true)
	private List<DashboardWidget> widgets;

	public void modify(Dashboard dashboard) {
		this.name = dashboard.getName();

		List<DashboardWidget> incomingWidgets = dashboard.getWidgets();
		java.util.Set<Long> incomingWidgetIds = incomingWidgets.stream()
														  .map(DashboardWidget::getId)
														  .collect(java.util.stream.Collectors.toSet());

		// 요청에 포함되지 않은 기존 위젯 삭제
		this.widgets.removeIf(existingWidget -> existingWidget.getId() != null && !incomingWidgetIds.contains(existingWidget.getId()));

		// 새로운 위젯 추가 또는 기존 위젯 업데이트
		for (DashboardWidget widget : incomingWidgets) {
			if (widget.getId() == null) {
				this.widgets.add(new DashboardWidget(this, widget.getWidgetId(), widget.getTitle(),
					widget.getConfig(), widget.getX(), widget.getY(), widget.getW(), widget.getH()));
			} else {
				this.widgets.stream()
							.filter(existingWidget -> existingWidget.getId() != null && existingWidget.getId().equals(widget.getId()))
							.findFirst()
							.ifPresent(existingWidget -> existingWidget.update(widget));
			}
		}
	}

	public void modify(
		Integer sort,
		boolean isHide
	) {
		this.sort = sort;
		this.isHide = isHide;
	}

	public void updateSort(Integer sort) {
		this.sort = sort;
	}

	public void toggleVisibility() {
		this.isHide = !this.isHide;
	}
}
