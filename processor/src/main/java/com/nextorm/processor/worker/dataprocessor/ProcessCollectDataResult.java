package com.nextorm.processor.worker.dataprocessor;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.ParameterData;

import java.util.List;

/**
 * 수집한 데이터를 파싱하여 저장하는 레코드
 *
 * @param parameterDataList 수집한 파라미터 데이터 목록
 * @param faultHistoryList  수집한 파라미터 데이터의 Fault 검출 목록
 */
public record ProcessCollectDataResult(List<ParameterData> parameterDataList, List<FaultHistory> faultHistoryList) {
}