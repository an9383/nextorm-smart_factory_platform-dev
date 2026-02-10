package com.nextorm.apcmodeling.common.exception.apcmodelsimulationdata;

import com.nextorm.apcmodeling.common.exception.BusinessException;

public class ApcSimulationDataNotFoundException extends BusinessException {
	public ApcSimulationDataNotFoundException() {
		super(ApcModelSimulationDataErrorCode.APC_MODEL_SIMULATION_DATA_NOT_FOUND);
	}
}
