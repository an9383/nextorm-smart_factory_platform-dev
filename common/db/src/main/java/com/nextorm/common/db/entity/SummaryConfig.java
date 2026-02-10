package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "summary_config")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class SummaryConfig extends BaseEntity {
	@Column(name = "name", unique = true)
	private String name;

	@Builder.Default
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "summaryConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
	private List<SummaryConfigToolMapping> toolMappings = new ArrayList<>();

	@Column(name = "description")
	private String description;

	@Column(name = "is_use_failover", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("FailOver 기능 사용 여부")
	private Boolean isUseFailover;

	@Column(name = "system_ip")
	@Comment("Failover를 실행하는 시스템의 IP 정보")
	private String systemIp;

	@Column(name = "connection_timeout")
	@Comment("Zookeeper Host와 연결이 끊긴 것을 확인하기 위한 Timeout")
	private Integer connectionTimeout;

	@Column(name = "hosts")
	@Comment("Zookeeper Host 연결 정보 예) 192.168.0.55:2181,192.168.0.55:2182,192.168.0.55:2183")
	private String hosts;

	public List<Tool> getTools() {
		return toolMappings.stream()
						   .map(SummaryConfigToolMapping::getTool)
						   .toList();
	}

	public void addTools(List<Tool> tools) {
		toolMappings.addAll(tools.stream()
								 .map(tool -> SummaryConfigToolMapping.create(this, tool))
								 .toList());
	}

	public static SummaryConfig create(SummaryConfig createData) {
		return SummaryConfig.builder()
							.name(createData.getName())
							.isUseFailover(createData.getIsUseFailover())
							.systemIp(createData.getSystemIp())
							.connectionTimeout(createData.getConnectionTimeout())
							.hosts(createData.getHosts())
							.build();
	}

	public void modify(
		SummaryConfig modifyData,
		List<Tool> tools
	) {
		if (Objects.nonNull(modifyData)) {
			this.toolMappings.clear();    // 기존 매핑 데이터 삭제
			tools.stream()
				 .map(tool -> SummaryConfigToolMapping.create(this, tool))
				 .forEach(this.toolMappings::add);

			this.name = modifyData.getName();
			this.isUseFailover = modifyData.getIsUseFailover();
			this.systemIp = modifyData.getSystemIp();
			this.connectionTimeout = modifyData.getConnectionTimeout();
			this.hosts = modifyData.getHosts();
		}
	}
}
