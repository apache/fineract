/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ReportCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(
			Arrays.asList("reportName", "reportType", "reportSubType",
					"reportCategory", "reportSql", "useReport"));

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public ReportCommandFromApiJsonDeserializer(
			final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validate_for_create(final String json) {
		validate(json, "create");
	}

	public void validate_for_update(final String json) {
		validate(json, "update");
	}

	private void validate(final String json, final String commandType) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				supportedParameters);
		// wip jpw
		boolean isCreate = false;
		if (commandType.equals("create"))
			isCreate = true;

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("report");

		final JsonElement element = fromApiJsonHelper.parse(json);

		String paramName = "reportName";
		String paramValueStr = fromApiJsonHelper.extractStringNamed(paramName,
				element);

		baseDataValidator.reset().parameter(paramName).value(paramValueStr)
				.notBlank().notExceedingLengthOf(100);

		String paramReportType = fromApiJsonHelper.extractStringNamed(
				"reportType", element);
		paramName = "reportType";
		paramValueStr = fromApiJsonHelper
				.extractStringNamed(paramName, element);
		baseDataValidator
				.reset()
				.parameter(paramName)
				.value(paramValueStr)
				.notBlank()
				.notExceedingLengthOf(20)
				.isOneOfTheseValues(
						new Object[] { "Table", "Pentaho", "Chart" });

		paramName = "reportSubType";
		paramValueStr = fromApiJsonHelper
				.extractStringNamed(paramName, element);
		baseDataValidator.reset().parameter(paramName).value(paramValueStr)
				.notExceedingLengthOf(20);
		if (paramReportType != null) {
			if (paramReportType.equals("Chart")) {
				baseDataValidator
						.reset()
						.parameter(paramName)
						.value(paramValueStr)
						.cantBeBlankWhenParameterProvidedIs("reportType",
								paramReportType)
						.isOneOfTheseValues(new Object[] { "Bar", "Pie" });
			}
		}

		paramName = "reportCategory";
		paramValueStr = fromApiJsonHelper
				.extractStringNamed(paramName, element);
		baseDataValidator.reset().parameter(paramName).value(paramValueStr)
				.notExceedingLengthOf(45);

		paramName = "reportSql";
		paramValueStr = fromApiJsonHelper
				.extractStringNamed(paramName, element);
		if (paramReportType != null) {
			if ((paramReportType.equals("Table"))
					|| (paramReportType.equals("Chart"))) {
				baseDataValidator
						.reset()
						.parameter(paramName)
						.value(paramValueStr)
						.cantBeBlankWhenParameterProvidedIs("reportType",
								paramReportType);
			} else {
				baseDataValidator
						.reset()
						.parameter(paramName)
						.value(paramValueStr)
						.mustBeBlankWhenParameterProvidedIs("reportType",
								paramReportType);
			}
		}
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}