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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.BackdatedTransactionValidationService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.junit.jupiter.api.Test;

class SavingsAccountTransactionDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final SavingsAccountTransactionDataValidator validator = new SavingsAccountTransactionDataValidator(fromJsonHelper,
            mock(ConfigurationDomainService.class), mock(BackdatedTransactionValidationService.class));

    @Test
    void validateAccumulatesInvalidTransactionTimeWithOtherErrors() {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("""
                {
                  "dateFormat": "dd MMMM yyyy",
                  "locale": "en",
                  "transactionDate": "01 March 2026",
                  "transactionTime": "invalid",
                  "paymentTypeId": 1
                }
                """), fromJsonHelper);

        assertThatThrownBy(() -> validator.validate(command)).isInstanceOfSatisfying(PlatformApiDataValidationException.class,
                exception -> {
                    assertThat(exception.getErrors())
                            .anyMatch(error -> SavingsApiConstants.transactionTimeParamName.equals(error.getParameterName()));
                    assertThat(exception.getErrors())
                            .anyMatch(error -> SavingsApiConstants.transactionAmountParamName.equals(error.getParameterName()));
                });
    }

    @Test
    void missingPaymentTypeWithPaymentDetailsIsReportedOnce() {
        assertThat(paymentTypeErrors("")).containsExactly("The parameter `paymentTypeId` is mandatory.");
    }

    @Test
    void paymentTypeNotGreaterThanZeroWithPaymentDetailsIsReportedOnce() {
        assertThat(paymentTypeErrors("\"paymentTypeId\": 0,")).containsExactly("The parameter `paymentTypeId` must be greater than 0.");
    }

    private List<String> paymentTypeErrors(String paymentType) {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("""
                {
                  "dateFormat": "dd MMMM yyyy",
                  "locale": "en",
                  "transactionDate": "01 March 2026",
                  "transactionAmount": 10,
                  %s
                  "receiptNumber": "RCPT-1"
                }
                """.formatted(paymentType)), fromJsonHelper);

        PlatformApiDataValidationException exception = catchThrowableOfType(PlatformApiDataValidationException.class,
                () -> validator.validate(command));
        return exception.getErrors().stream().filter(error -> SavingsApiConstants.paymentTypeIdParamName.equals(error.getParameterName()))
                .map(ApiParameterError::getDefaultUserMessage).toList();
    }
}
