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
package org.apache.fineract.infrastructure.report.service;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.security.exception.InputValidationException;
import org.apache.fineract.infrastructure.security.service.InputValidator;

@Slf4j
public abstract class AbstractReportingProcessService implements ReportingProcessService {

    private static final String NUMERIC_FORMAT_TYPE = "number";
    private static final String DATE_FORMAT_TYPE = "date";

    private final InputValidator inputValidator;
    private final ReportParameterTypeResolver reportParameterTypeResolver;

    protected AbstractReportingProcessService(InputValidator inputValidator, ReportParameterTypeResolver reportParameterTypeResolver) {
        this.inputValidator = inputValidator;
        this.reportParameterTypeResolver = reportParameterTypeResolver;
    }

    @Override
    public Map<String, String> getReportParams(final String reportName, final MultivaluedMap<String, String> queryParams) {
        final Map<String, String> paramFormatTypes = this.reportParameterTypeResolver.loadParamFormatTypes(reportName);
        final boolean hasRegisteredParams = !paramFormatTypes.isEmpty();
        final Map<String, String> reportParams = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("R_")) {
                String paramVariable = entry.getKey().substring(2);
                String pKey = "${" + paramVariable + "}";
                String pValue = entry.getValue().get(0);

                if (hasRegisteredParams) {
                    String formatType = paramFormatTypes.get(paramVariable);
                    if (formatType == null) {
                        log.warn("Report '{}' received unknown parameter '{}' with no registered type — rejected", reportName,
                                paramVariable);
                        throw new InputValidationException(String.format("unknown report parameter '%s' is not registered for report '%s'",
                                paramVariable, reportName));
                    }

                    validateParamByType(paramVariable, pValue, formatType);
                }

                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }

    private void validateParamByType(final String paramName, final String value, final String formatType) {
        if (NUMERIC_FORMAT_TYPE.equalsIgnoreCase(formatType)) {
            inputValidator.validate(NUMERIC_FORMAT_TYPE, value);
        } else if (DATE_FORMAT_TYPE.equalsIgnoreCase(formatType)) {
            inputValidator.validate(DATE_FORMAT_TYPE, value);
        }
    }
}
