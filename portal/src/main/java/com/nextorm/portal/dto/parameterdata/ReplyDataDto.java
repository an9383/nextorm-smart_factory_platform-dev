package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDataDto {
	List<String> headers = new ArrayList<>();
	List<List<Object>> datas = new ArrayList<>();
}
