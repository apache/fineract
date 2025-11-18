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
package org.apache.fineract.test.stepdef.common;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class GlobalConfigurationStepDef {

    @Autowired
    private GlobalConfigurationHelper globalConfigurationHelper;

    @Autowired
    private FineractFeignClient fineractClient;

    @Given("Global configuration {string} is disabled")
    public void disableGlobalConfiguration(String configKey) {
        globalConfigurationHelper.disableGlobalConfiguration(configKey, 0L);
    }

    @Given("Global configuration {string} is enabled")
    public void enableGlobalConfiguration(String configKey) {
        globalConfigurationHelper.enableGlobalConfiguration(configKey, 0L);
    }

    @When("Global config {string} value set to {string}")
    public void setGlobalConfigValueString(String configKey, String configValue) {
        globalConfigurationHelper.setGlobalConfigValueString(configKey, configValue);
    }

    @When("Global config {string} value set to {string} through DefaultApi")
    public void setGlobalConfigValueStringDefaultApi(String configKey, String configValue) {
        Long configValueLong = Long.valueOf(configValue);
        fineractClient.defaultApi().updateGlobalConfiguration(configKey, configValueLong);
    }

    @When("Update currency with incorrect empty value outcomes with an error")
    public void updateCurrencyEmptyValueFailure() {
        var request = new CurrencyUpdateRequest();
        try {
            fineractClient.currency().updateCurrencies(request.currencies(Collections.emptyList()), Map.of());
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            final ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setCurrencyEmptyValueFailure()).isEqualTo(400);

            if (errorDetails.getErrors() != null && !errorDetails.getErrors().isEmpty()) {
                boolean hasExpectedError = errorDetails.getErrors().stream().anyMatch(
                        error -> ErrorMessageHelper.setCurrencyEmptyValueFailure().equals(error.getDeveloperMessageWithoutPrefix()));
                assertThat(hasExpectedError).as("Expected error message: " + ErrorMessageHelper.setCurrencyEmptyValueFailure()
                        + " in errors: " + errorDetails.getErrors()).isTrue();
            } else {
                assertThat(errorDetails.getSingleError().getDeveloperMessageWithoutPrefix())
                        .isEqualTo(ErrorMessageHelper.setCurrencyEmptyValueFailure());
            }
        }
    }

    @When("Update currency as NULL value outcomes with an error")
    public void updateCurrencyNullValueFailure() {
        var request = new CurrencyUpdateRequest();
        Integer httpStatusCodeExpected = 400;

        try {
            fineractClient.currency().updateCurrencies(request.currencies(null));
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            ErrorResponse errorResponse = ErrorResponse.fromFeignException(e);
            Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
            List<String> developerMessagesActual = errorResponse.getErrors().stream()
                    .map(ErrorResponse.ErrorDetail::getDeveloperMessageWithoutPrefix).toList();

            List<String> developerMessagesExpected = asList(ErrorMessageHelper.setCurrencyEmptyValueFailure(),
                    ErrorMessageHelper.setCurrencyNullValueMandatoryFailure());

            assertThat(httpStatusCodeActual)
                    .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                    .isEqualTo(httpStatusCodeExpected);
            assertThat(developerMessagesActual)
                    .as(ErrorMessageHelper.wrongErrorMessage(developerMessagesActual.toString(), developerMessagesExpected.toString()))
                    .containsAll(developerMessagesExpected);
        }
    }

    @When("Update currency as {string} value outcomes with an error")
    public void updateCurrencyIncorrectValueFailure(String currency) {
        var request = new CurrencyUpdateRequest();
        try {
            fineractClient.currency().updateCurrencies(request.currencies(Collections.singletonList(currency)));
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            final ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setCurrencyIncorrectValueFailure(currency)).isEqualTo(404);
            assertThat(errorDetails.getSingleError().getDeveloperMessageWithoutPrefix())
                    .isEqualTo(ErrorMessageHelper.setCurrencyIncorrectValueFailure(currency));
        }
    }
}
