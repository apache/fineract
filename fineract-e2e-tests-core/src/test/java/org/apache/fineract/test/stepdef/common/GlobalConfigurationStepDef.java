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

import com.google.gson.Gson;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.client.services.DefaultApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class GlobalConfigurationStepDef {

    @Autowired
    private GlobalConfigurationHelper globalConfigurationHelper;
    @Autowired
    private DefaultApi defaultApi;

    @Autowired
    private CurrencyApi currencyApi;

    private static final Gson GSON = new JSON().getGson();

    @Given("Global configuration {string} is disabled")
    public void disableGlobalConfiguration(String configKey) throws IOException {
        globalConfigurationHelper.disableGlobalConfiguration(configKey, 0L);
    }

    @Given("Global configuration {string} is enabled")
    public void enableGlobalConfiguration(String configKey) throws IOException {
        globalConfigurationHelper.enableGlobalConfiguration(configKey, 0L);
    }

    @When("Global config {string} value set to {string}")
    public void setGlobalConfigValueString(String configKey, String configValue) throws IOException {
        globalConfigurationHelper.setGlobalConfigValueString(configKey, configValue);
    }

    @When("Global config {string} value set to {string} through DefaultApi")
    public void setGlobalConfigValueStringDefaultApi(String configKey, String configValue) throws IOException {
        Long configValueLong = Long.valueOf(configValue);
        defaultApi.updateGlobalConfiguration(configKey, configValueLong);
    }

    @When("Update currency with incorrect empty value outcomes with an error")
    public void updateCurrencyEmptyValueFailure() throws IOException {
        var request = new CurrencyUpdateRequest();
        var currencyResponse = currencyApi.updateCurrencies(request.currencies(Collections.emptyList())).execute();
        final ErrorResponse errorDetails = ErrorResponse.from(currencyResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setCurrencyEmptyValueFailure()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.setCurrencyEmptyValueFailure());
    }

    @When("Update currency with incorrect null value outcomes with an error")
    public void updateCurrencyIncorrectNullValueFailure() throws IOException {
        var request = new CurrencyUpdateRequest();
        var currencyResponse = currencyApi.updateCurrencies(request.currencies(Collections.singletonList(null))).execute();
        final ErrorResponse errorDetails = ErrorResponse.from(currencyResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setCurrencyIncorrectValueFailure("null")).isEqualTo(404);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.setCurrencyIncorrectValueFailure("null"));
    }

    @When("Update currency as NULL value outcomes with an error")
    public void updateCurrencyNullValueFailure() throws IOException {
        var request = new CurrencyUpdateRequest();
        var currencyResponse = currencyApi.updateCurrencies(request.currencies(null)).execute();
        Integer httpStatusCodeExpected = 400;

        String errorBody = currencyResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        List<String> developerMessagesActual = errorResponse.getErrors().stream().map(ErrorResponse.Error::getDeveloperMessage).toList();

        List<String> developerMessagesExpected = asList(ErrorMessageHelper.setCurrencyEmptyValueFailure(),
                ErrorMessageHelper.setCurrencyNullValueMandatoryFailure());

        assertThat(httpStatusCodeActual)
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(developerMessagesActual)
                .as(ErrorMessageHelper.wrongErrorMessage(developerMessagesActual.toString(), developerMessagesExpected.toString()))
                .containsAll(developerMessagesExpected);
    }

    @When("Update currency as {string} value outcomes with an error")
    public void updateCurrencyIncorrectValueFailure(String currency) throws IOException {
        var request = new CurrencyUpdateRequest();
        var currencyResponse = currencyApi.updateCurrencies(request.currencies(Collections.singletonList(currency))).execute();
        final ErrorResponse errorDetails = ErrorResponse.from(currencyResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setCurrencyIncorrectValueFailure(currency)).isEqualTo(404);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.setCurrencyIncorrectValueFailure(currency));
    }
}
