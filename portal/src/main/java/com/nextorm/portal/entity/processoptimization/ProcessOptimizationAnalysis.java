package com.nextorm.portal.entity.processoptimization;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process_optimization_analysis")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOptimizationAnalysis extends BaseEntity {

	@OneToOne
	@JoinColumn(name = "process_optimization_id")
	private ProcessOptimization processOptimization;

	@Column(name = "analysis_data", columnDefinition = "LONGBLOB")
	private byte[] analysisData;
}

