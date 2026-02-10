package com.nextorm.portal.entity.processoptimization;

import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.portal.enums.ProcessOptimizationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@Table(name = "process_optimization")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOptimization extends BaseEntity {

	private String name;

	@Column(name = "tool_id")
	private Long toolId;

	private double targetValue;

	@Column(nullable = true)
	private double optimalValue;

	@Column(name = "optimization_parameters")
	@Convert(converter = ListOptimizationParametersToJsonStringConverter.class)
	private List<OptimizationParameters> optimizationParameters;

	@Column(name = "total_count")
	private double totalCount;

	@Column(name = "complete_count")
	private double completeCount;

	@Enumerated(EnumType.STRING)
	private ProcessOptimizationStatus status;

	@Comment("최적화 실패 사유")
	@Column(name = "failure_reason", columnDefinition = "varchar(1000)")
	private String failureReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ai_model_id")
	private AiModel aiModel;

	public long[] getParameterId() {
		long[] parameterIds = null;
		if (!this.optimizationParameters.isEmpty()) {
			parameterIds = this.optimizationParameters.stream()
													  .map(OptimizationParameters::getParameterId)
													  .mapToLong(Long::longValue)
													  .toArray();
		}
		return parameterIds;
	}

	public double[] getMinScaleX() {
		double[] minScaleXs = null;
		if (!this.optimizationParameters.isEmpty()) {
			minScaleXs = this.optimizationParameters.stream()
													.map(OptimizationParameters::getMinScaleX)
													.mapToDouble(Double::doubleValue)
													.toArray();
		}
		return minScaleXs;
	}

	public double[] getMaxScaleX() {
		double[] maxScaleXs = null;
		if (!this.optimizationParameters.isEmpty()) {
			maxScaleXs = this.optimizationParameters.stream()
													.map(OptimizationParameters::getMaxScaleX)
													.mapToDouble(Double::doubleValue)
													.toArray();
		}
		return maxScaleXs;
	}

	public int[] getStep() {
		int[] steps = null;
		if (!this.optimizationParameters.isEmpty()) {
			steps = this.optimizationParameters.stream()
											   .map(OptimizationParameters::getStep)
											   .mapToInt(Integer::intValue)
											   .toArray();
		}
		return steps;
	}

	public void updateFailure(String failureReason) {
		this.status = ProcessOptimizationStatus.FAIL;
		this.failureReason = failureReason;
	}
}

