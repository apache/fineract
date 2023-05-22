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
package org.apache.fineract.portfolio.savings.data;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionDateParamName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.data.RangeOperator;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch.RangeFilter;
import org.springframework.stereotype.Component;

@Component
public class SavingsAccountTransactionSearchValidator {

    public void validateSearchFilters(SavingsTransactionSearch.Filters searchFilters) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME);
        if (searchFilters != null) {

            List<RangeFilter<LocalDate>> dateFilters = searchFilters.getTransactionDate();
            validateRangeFilters(baseDataValidator, dateFilters, transactionDateParamName);

            List<RangeFilter<BigDecimal>> amountFilters = searchFilters.getTransactionAmount();
            validateRangeFilters(baseDataValidator, amountFilters, transactionAmountParamName);
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private <T> void validateRangeFilters(DataValidatorBuilder baseDataValidator, List<RangeFilter<T>> rangeFilters, String paramName) {
        if (rangeFilters == null) {
            return;
        }

        if (rangeFilters.size() > 2) {
            baseDataValidator.parameter(paramName).value(rangeFilters).notExceedingListLengthOf(2);
        }

        if (!rangeFilters.isEmpty()) {
            RangeFilter<T> firstFilter = rangeFilters.get(0);
            RangeOperator firstOperator = firstFilter.getOperator();

            if (rangeFilters.size() == 2) {
                RangeFilter<T> secondFilter = rangeFilters.get(1);
                RangeOperator secondOperator = secondFilter.getOperator();

                if (((firstOperator == RangeOperator.GT || firstOperator == RangeOperator.GTE)
                        && !(secondOperator == RangeOperator.LT || secondOperator == RangeOperator.LTE))
                        || ((firstOperator == RangeOperator.LT || firstOperator == RangeOperator.LTE)
                                && !(secondOperator == RangeOperator.GT || secondOperator == RangeOperator.GTE))) {
                    baseDataValidator.parameter(paramName).failWithCode("invalid.range", firstOperator, secondOperator);
                }
            }
        }

        for (RangeFilter<T> filter : rangeFilters) {
            T value = filter.getValue();
            if (value instanceof BigDecimal) {
                baseDataValidator.parameter(paramName).value(value).zeroOrPositiveAmount();
            }
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
