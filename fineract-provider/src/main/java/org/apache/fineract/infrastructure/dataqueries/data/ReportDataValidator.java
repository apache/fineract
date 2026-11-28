/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;

/**
 * Checks the values of a {@link Report}.
 * <p>
 * The report is checked as it will be stored, not as the request sent it: an update only sends the fields that change,
 * so the write service applies the changes to the report first and then validates the result.
 */
public final class ReportDataValidator {

    private static final String RESOURCE = "report";

    private ReportDataValidator() {}

    public static void validate(final Report report, final Collection<String> reportTypes) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE);

        final String reportType = report.getReportType();
        final String reportSubType = report.getReportSubType();
        final String reportSql = report.getReportSql();

        baseDataValidator.reset().parameter("reportName").value(report.getReportName()).notBlank().notExceedingLengthOf(100);

        baseDataValidator.reset().parameter("reportType").value(reportType).notBlank().isOneOfTheseValues(reportTypes.toArray());

        baseDataValidator.reset().parameter("reportSubType").value(reportSubType).notExceedingLengthOf(20);

        if (StringUtils.isNotBlank(reportType)) {
            if (reportType.equals("Chart")) {
                baseDataValidator.reset().parameter("reportSubType").value(reportSubType)
                        .cantBeBlankWhenParameterProvidedIs("reportType", reportType).isOneOfTheseValues(new Object[] { "Bar", "Pie" });
            } else {
                baseDataValidator.reset().parameter("reportSubType").value(reportSubType).mustBeBlankWhenParameterProvidedIs("reportType",
                        reportType);
            }
        }

        baseDataValidator.reset().parameter("reportCategory").value(report.getReportCategory()).notExceedingLengthOf(45);

        if (StringUtils.isNotBlank(reportType)) {
            if (reportType.equals("Table") || reportType.equals("Chart")) {
                baseDataValidator.reset().parameter("reportSql").value(reportSql).cantBeBlankWhenParameterProvidedIs("reportType",
                        reportType);
            } else {
                baseDataValidator.reset().parameter("reportSql").value(reportSql).mustBeBlankWhenParameterProvidedIs("reportType",
                        reportType);
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
