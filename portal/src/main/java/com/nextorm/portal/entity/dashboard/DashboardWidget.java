package com.nextorm.portal.entity.dashboard;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dashboard_widget")
@Getter
public class DashboardWidget extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dashboard_id")
	private Dashboard dashboard;

	@Column(name = "widget_id")
	private String widgetId;
	@Column(name = "title")
	private String title;
	@Column(name = "config", columnDefinition = "varchar(4000)")
	private String config;
	@Column(name = "x")
	private int x;
	@Column(name = "y")
	private int y;
	@Column(name = "w")
	private int w;
	@Column(name = "h")
	private int h;

	public void modifyConfig(String config) {
		this.config = config;
	}

	public void update(DashboardWidget widget) {
		this.widgetId = widget.getWidgetId();
		this.title = widget.getTitle();
		this.config = widget.getConfig();
		this.x = widget.getX();
		this.y = widget.getY();
		this.w = widget.getW();
		this.h = widget.getH();
	}
}
