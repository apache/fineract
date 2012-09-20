package org.mifosng.platform.api.infrastructure;

import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ParameterListExclusionStrategy implements ExclusionStrategy {

	private final Set<String> parameterNamesToSkip;

	public ParameterListExclusionStrategy(final Set<String> parameterNamesToSkip) {
		this.parameterNamesToSkip = parameterNamesToSkip;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return parameterNamesToSkip.contains(f.getName());
	}

	@SuppressWarnings("unused")
	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}
