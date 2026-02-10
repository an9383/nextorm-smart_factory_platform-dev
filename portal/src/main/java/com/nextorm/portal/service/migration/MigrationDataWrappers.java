package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MigrationDataWrappers implements Iterable<MigrationDataWrapper> {
	private final List<MigrationDataWrapper> wrappers;

	public MigrationDataWrappers(List<MigrationDataWrapper> migrationDataWrappers) {
		// immutable일 수 있으니, mutable한 객체로 변환
		this.wrappers = new ArrayList<>(migrationDataWrappers);
	}

	public LocalDateTime getMigrationStartAt() {
		return wrappers.get(0)
					   .getStartAt();
	}

	public LocalDateTime getMigrationEndAt() {
		return wrappers.get(0)
					   .getEndAt();
	}

	public List<LocalDateTime> getMigrationRequiredTicks() {
		return wrappers.get(0)
					   .getParameterDataList()
					   .stream()
					   .map(ParameterData::getTraceAt)
					   .toList();
	}

	public List<Parameter> getParameters() {
		return wrappers.stream()
					   .map(MigrationDataWrapper::getParameter)
					   .toList();
	}

	public List<ParameterData> getParameterDataList(Long parameterId) {
		Optional<MigrationDataWrapper> find = wrappers.stream()
													  .filter(it -> it.getParameterId()
																	  .equals(parameterId))
													  .findFirst();

		return find.map(MigrationDataWrapper::getParameterDataList)
				   .orElseThrow(() -> new IllegalArgumentException("parameterId not found: " + parameterId));
	}

	public void addAll(List<MigrationDataWrapper> migrationDataWrappers) {
		wrappers.addAll(migrationDataWrappers);
	}

	public boolean isNotEmpty() {
		return !wrappers.isEmpty();
	}

	@NotNull
	@Override
	public Iterator<MigrationDataWrapper> iterator() {
		return wrappers.iterator();
	}

	public List<MigrationBase> getMigrationBase() {
		return wrappers.get(0)
					   .getMigrationData()
					   .stream()
					   .map(MigrationData::toMigrationBase)
					   .toList();
	}
}
