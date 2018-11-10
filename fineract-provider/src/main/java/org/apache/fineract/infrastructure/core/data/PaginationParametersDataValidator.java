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
package org.apache.fineract.infrastructure.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Component;

@Component
public class PaginationParametersDataValidator {

    private final Set<String> sortOrderValues = new HashSet<>(Arrays.asList("ASC", "DESC"));

    public void validateParameterValues(PaginationParameters parameters, final Set<String> supportedOrdeByValues, final String resourceName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            if (parameters.isOrderByRequested() && !supportedOrdeByValues.contains(parameters.getOrderBy())) {
                final String defaultUserMessage = "The orderBy value '" + parameters.getOrderBy()
                        + "' is not supported. The supported orderBy values are " + supportedOrdeByValues.toString();
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg." + resourceName
                                + ".orderBy.value.is.not.supported", defaultUserMessage, "orderBy", parameters.getOrderBy(),
                        supportedOrdeByValues.toString());
                dataValidationErrors.add(error);
            }

            if (parameters.isSortOrderProvided() && !sortOrderValues.contains(parameters.getSortOrder().toUpperCase())) {
                final String defaultUserMessage = "The sortOrder value '" + parameters.getSortOrder()
                        + "' is not supported. The supported sortOrder values are " + sortOrderValues.toString();
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg." + resourceName
                                + ".sortOrder.value.is.not.supported", defaultUserMessage, "sortOrder", parameters.getSortOrder(),
                        sortOrderValues.toString());
                dataValidationErrors.add(error);
            }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}