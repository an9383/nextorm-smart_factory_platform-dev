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
@Table(name = "process_config")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ProcessConfig extends BaseEntity {
	@Column(name = "name", unique = true)
	private String name;

	@Builder.Default
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
	private List<ProcessConfigToolMapping> toolMappings = new ArrayList<>();

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
						   .map(ProcessConfigToolMapping::getTool)
						   .toList();
	}

	public void addTools(List<Tool> tools) {
		toolMappings.addAll(tools.stream()
								 .map(tool -> ProcessConfigToolMapping.create(this, tool))
								 .toList());
	}

	public static ProcessConfig create(ProcessConfig createData) {
		return ProcessConfig.builder()
							.name(createData.getName())
							.isUseFailover(createData.getIsUseFailover())
							.systemIp(createData.getSystemIp())
							.connectionTimeout(createData.getConnectionTimeout())
							.hosts(createData.getHosts())
							.build();
	}

	public void modify(
		ProcessConfig modifyData,
		List<Tool> tools
	) {
		if (Objects.nonNull(modifyData)) {
			this.toolMappings.clear();    // 기존 매핑 데이터 삭제
			tools.stream()
				 .map(tool -> ProcessConfigToolMapping.create(this, tool))
				 .forEach(this.toolMappings::add);

			this.name = modifyData.getName();
			this.isUseFailover = modifyData.getIsUseFailover();
			this.systemIp = modifyData.getSystemIp();
			this.connectionTimeout = modifyData.getConnectionTimeout();
			this.hosts = modifyData.getHosts();
		}
	}
}
