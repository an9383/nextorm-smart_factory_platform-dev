package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.model.ParameterModel;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetHealthGradeByParameter implements VpCalculateExecutor {
	private final ParameterContainer parameterContainer;

	public GetHealthGradeByParameter(ParameterContainer parameterContainer) {
		this.parameterContainer = parameterContainer;
	}

	@Override
	public String getScript() {
		return """
			function getHealthGradeByParameter(parameterId) {
				const container = %s;
				
				return self_getHealthGradeByParameter.getHealthGrade(parseInt(parameterId), container[parameterId]);
			}""".formatted(ScriptEngine.CONTEXT_CONTAINER_NAME);
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getHealthGradeByParameter", this);
	}

	public Integer getHealthGrade(
		Long parameterId,
		Object object
	) {
		try {
			Double value = (Double)object;
			ParameterModel parameter = parameterContainer.getParameterById(parameterId);

			return calculateScore(value,
				parameter.getLsl(),
				parameter.getUsl(),
				parameter.getLcl(),
				parameter.getUcl());
		} catch (RuntimeException e) {
			return null;
		}
	}

	private int calculateScore(
		double value,
		Double lsl,
		Double usl,
		Double lcl,
		Double ucl
	) {
		// 모든 한계값이 null인 경우
		if (lsl == null && usl == null && lcl == null && ucl == null) {
			return 90;
		} else

			// 하나의 한계값만 있는 경우
			if (lsl != null && usl == null && lcl == null && ucl == null) {
				return value < lsl
					   ? 10
					   : 90;
			} else if (lsl == null && usl != null && lcl == null && ucl == null) {
				return value > usl
					   ? 10
					   : 90;
			} else if (lsl == null && usl == null && lcl != null && ucl == null) {
				return value < lcl
					   ? 10
					   : 90;
			} else if (lsl == null && usl == null && lcl == null && ucl != null) {
				return value > ucl
					   ? 10
					   : 90;
			} else

				// 두 개의 한계값만 있는 경우
				if (lsl != null && usl != null && lcl == null && ucl == null) {
					if (value < lsl || value > usl) {
						return 10;
					} else {
						double mid = (lsl + usl) / 2;
						if (value == mid) {
							return 100;
						}
						return value < mid
							   ? (int)(50 + 50 * (value - lsl) / (mid - lsl))
							   : (int)(50 + 50 * (usl - value) / (usl - mid));
					}
				} else if (lsl != null && lcl != null && usl == null && ucl == null) {
					if (value < lsl) {
						return 10;
					} else if (value > lcl) {
						return 90;
					} else {
						return (int)(50 + 40 * (value - lsl) / (lcl - lsl));
					}
				} else if (lsl != null && ucl != null && usl == null && lcl == null) {
					if (value < lsl || value > ucl) {
						return 10;
					} else {
						double mid = (lsl + ucl) / 2;
						if (value == mid) {
							return 100;
						}
						return value < mid
							   ? (int)(90 + 10 * (value - lsl) / (mid - lsl))
							   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
					}
				} else if (usl != null && lcl != null && lsl == null && ucl == null) {
					if (value < lcl || value > usl) {
						return 10;
					} else {
						double mid = (lcl + usl) / 2;
						if (value == mid) {
							return 100;
						}
						return value < mid
							   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
							   : (int)(90 + 10 * (usl - value) / (usl - mid));
					}
				} else if (usl != null && ucl != null && lsl == null && lcl == null) {
					if (value < ucl) {
						return 90;
					} else if (value > usl) {
						return 10;
					} else {
						return (int)(90 + 40 * (ucl - value) / (usl - ucl));
					}
				} else if (lcl != null && ucl != null && lsl == null && usl == null) {
					if (value < lcl || value > ucl) {
						return 10;
					} else {
						double mid = (lcl + ucl) / 2;
						return value < mid
							   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
							   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
					}
				} else

					// 세 개의 한계값만 있는 경우
					if (lsl != null && usl != null && lcl != null && ucl == null) {
						if (value < lsl || value > usl) {
							return 10;
						} else if (value < lcl) {
							return (int)(50 + 40 * (value - lsl) / (lcl - lsl));
						} else {
							double mid = (lcl + usl) / 2;
							if (value == mid) {
								return 100;
							}
							return value < mid
								   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
								   : (int)(90 + 10 * (usl - value) / (usl - mid));
						}
					} else if (lsl != null && usl != null && ucl != null && lcl == null) {
						if (value < lsl || value > usl) {
							return 10;
						} else if (value < ucl) {
							double mid = (lsl + ucl) / 2;
							if (value == mid) {
								return 100;
							}
							return value < mid
								   ? (int)(90 + 10 * (value - lsl) / (mid - lsl))
								   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
						} else {
							return (int)(90 - 40 * (value - ucl) / (usl - ucl));
						}
					} else if (lsl != null && lcl != null && ucl != null && usl == null) {
						if (value < lsl || value > ucl) {
							return 10;
						} else if (value < lcl) {
							return (int)(50 + 40 * (value - lsl) / (lcl - lsl));
						} else {
							double mid = (lcl + ucl) / 2;
							if (value == mid) {
								return 100;
							}
							return value < mid
								   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
								   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
						}
					} else if (usl != null && lcl != null && ucl != null && lsl == null) {
						if (value < lcl || value > usl) {
							return 10;
						} else if (value < ucl) {
							double mid = (lcl + ucl) / 2;
							if (value == mid) {
								return 100;
							}
							return value < mid
								   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
								   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
						} else {
							return (int)(90 - 40 * (value - ucl) / (usl - ucl));
						}
					} else {
						// 모든 한계값이 있는 경우
						if (value < lsl || value > usl) {
							return 10;
						} else if (value < lcl) {
							return (int)(50 + 40 * (value - lsl) / (lcl - lsl));
						} else if (value <= ucl) {
							double mid = (lcl + ucl) / 2;
							if (value == mid) {
								return 100;
							}
							return value < mid
								   ? (int)(90 + 10 * (value - lcl) / (mid - lcl))
								   : (int)(90 + 10 * (ucl - value) / (ucl - mid));
						} else {
							return (int)(90 - 40 * (value - ucl) / (usl - ucl));
						}
					}
	}
}
