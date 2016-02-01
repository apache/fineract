/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Component;

@Component
public class PaginationParametersDataValidator {

    public static Set<String> sortOrderValues = new HashSet<>(Arrays.asList("ASC", "DESC"));

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