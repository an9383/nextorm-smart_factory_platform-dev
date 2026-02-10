package com.nextorm.processor.parametercontainer;

import com.nextorm.processor.model.ParameterModel;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

@AllArgsConstructor
public class Parameters implements Iterable<ParameterModel> {
	private final List<ParameterModel> parameterModels;

	public List<ParameterModel> parameters() {
		return parameterModels;
	}

	public List<ParameterModel> getParameters() {
		return parameterModels;
	}

	public List<ParameterModel> getCollectParameters() {
		return parameterModels.stream()
							  .filter(parameter -> !parameter.isVirtual())
							  .toList();
	}

	public List<ParameterModel> getVirtualParameters() {
		return parameterModels.stream()
							  .filter(ParameterModel::isVirtual)
							  .toList();
	}

	@Override
	public void forEach(Consumer<? super ParameterModel> action) {
		Iterable.super.forEach(action);
	}

	@Override
	public Spliterator<ParameterModel> spliterator() {
		return Iterable.super.spliterator();
	}

	@Override
	public Iterator<ParameterModel> iterator() {
		return parameterModels.iterator();
	}
}
