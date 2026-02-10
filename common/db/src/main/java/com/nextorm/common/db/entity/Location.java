package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "location")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuperBuilder
public class Location extends BaseEntity {
	@Getter
	public enum Type {
		SITE(null), FAB(Type.SITE), LINE(Type.FAB);

		private final Type parent;

		Type(Type parent) {
			this.parent = parent;
		}

		public static List<Type> getAncestors(Type type) {
			List<Type> types = new ArrayList<>();
			if (type.getParent() != null) {
				types.add(type.getParent());
				types.addAll(getAncestors(type.getParent()));
			}
			Collections.reverse(types);
			return types;
		}
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "type", columnDefinition = "varchar(20)")
	@Comment("타입")
	private Type type;

	@Column(name = "name")
	@Comment("이름")
	private String name;

	@Column(name = "description")
	@Comment("설명")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent")
	@Comment("상위 로케이션 아이디")
	private Location parent;

	@Column(name = "seq")
	@Comment("순서")
	private Integer seq;

	@Column(name = "longitude")
	private Double longitude;
	@Column(name = "latitude")
	private Double latitude;

	public static Location create(
		Location location,
		Location parent
	) {
		return Location.builder()
					   .type(location.type)
					   .name(location.name)
					   .description(location.description)
					   .parent(parent)
					   .seq(location.seq)
					   .latitude(location.latitude)
					   .longitude(location.longitude)
					   .build();

	}

	public void modify(Location location) {
		this.name = location.name;
		this.description = location.description;
		this.seq = location.seq;
		this.latitude = location.latitude;
		this.longitude = location.longitude;
	}
}
