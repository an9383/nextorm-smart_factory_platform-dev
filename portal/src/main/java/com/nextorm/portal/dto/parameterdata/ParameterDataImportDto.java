package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDataImportDto {
	private String parameterName;
	private List<Object> Datas; //[date,value]
}
