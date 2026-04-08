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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CurrencyConfigurationData;
import org.apache.fineract.client.models.CurrencyData;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.models.CurrencyUpdateResponse;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class CurrencyStepDef extends AbstractStepDef {

    private static final List<String> DEFAULT_CURRENCIES = Arrays.asList("EUR", "USD");
    private final FineractFeignClient fineractClient;

    @When("Admin retrieves currency configuration")
    public void adminRetrievesCurrencyConfiguration() {

        CurrencyConfigurationData response = ok(() -> fineractClient.currency().retrieveCurrencies());
        testContext().set(TestContextKey.GET_CURRENCIES_RESPONSE, response);

    }

    @Then("Currency {string} has the following properties:")
    public void currencyHasFollowingProperties(String currencyCode, DataTable table) {

        CurrencyConfigurationData config = testContext().get(TestContextKey.GET_CURRENCIES_RESPONSE);

        List<CurrencyData> allCurrencies = new ArrayList<>();
        if (config.getSelectedCurrencyOptions() != null) {
            allCurrencies.addAll(config.getSelectedCurrencyOptions());
        }
        if (config.getCurrencyOptions() != null) {
            allCurrencies.addAll(config.getCurrencyOptions());
        }

        Map<String, String> expected = table.asMaps().get(0);
        String expectedName = expected.get("name");
        String expectedSymbol = expected.get("symbol");
        int expectedDecimalPlaces = Integer.parseInt(expected.get("decimalPlaces"));

        CurrencyData currency = allCurrencies.stream().filter(c -> currencyCode.equals(c.getCode())).findFirst().orElse(null);

        assertThat(currency).as(ErrorMessageHelper.currencyNotFound(currencyCode)).isNotNull();
        assertThat(currency.getName()).as(ErrorMessageHelper.wrongCurrencyField(currencyCode, "name", currency.getName(), expectedName))
                .isEqualTo(expectedName);
        assertThat(currency.getDisplaySymbol())
                .as(ErrorMessageHelper.wrongCurrencyField(currencyCode, "displaySymbol", currency.getDisplaySymbol(), expectedSymbol))
                .isEqualTo(expectedSymbol);
        assertThat(currency.getDecimalPlaces()).as(
                ErrorMessageHelper.wrongCurrencyField(currencyCode, "decimalPlaces", currency.getDecimalPlaces(), expectedDecimalPlaces))
                .isEqualTo(expectedDecimalPlaces);
        assertThat(currency.getNameCode())
                .as(ErrorMessageHelper.wrongCurrencyField(currencyCode, "nameCode", currency.getNameCode(), "currency." + currencyCode))
                .isEqualTo("currency." + currencyCode);
        assertThat(currency.getDisplayLabel()).as(ErrorMessageHelper.wrongCurrencyField(currencyCode, "displayLabel",
                currency.getDisplayLabel(), expectedName + " (" + expectedSymbol + ")"))
                .isEqualTo(expectedName + " (" + expectedSymbol + ")");

    }

    @When("Admin updates selected currencies to {string}")
    public void adminUpdatesSelectedCurrencies(String currencyCodes) {

        List<String> currencies = Arrays.asList(currencyCodes.split(","));
        CurrencyUpdateRequest request = new CurrencyUpdateRequest().currencies(currencies);
        CurrencyUpdateResponse response = ok(() -> fineractClient.currency().updateCurrencies(request, Map.of()));
        testContext().set(TestContextKey.PUT_CURRENCIES_RESPONSE, response);

    }

    @Then("The returned currency list matches {string}")
    public void returnedCurrencyListMatches(String expectedCodes) {

        List<String> expected = Arrays.asList(expectedCodes.split(","));
        CurrencyUpdateResponse response = testContext().get(TestContextKey.PUT_CURRENCIES_RESPONSE);

        assertThat(response).as(ErrorMessageHelper.idNull()).isNotNull();
        assertThat(response.getCurrencies()).as(ErrorMessageHelper.wrongSelectedCurrencies(response.getCurrencies(), expected))
                .isEqualTo(expected);

    }

    @Then("The selected currencies contain {string}")
    public void selectedCurrenciesContain(String expectedCodes) {

        List<String> expected = Arrays.asList(expectedCodes.split(","));
        CurrencyConfigurationData config = testContext().get(TestContextKey.GET_CURRENCIES_RESPONSE);

        assertThat(config.getSelectedCurrencyOptions()).as(ErrorMessageHelper.idNull()).isNotNull();

        List<String> selectedCodes = config.getSelectedCurrencyOptions().stream().map(CurrencyData::getCode).sorted().toList();
        List<String> expectedSorted = expected.stream().sorted().toList();

        assertThat(selectedCodes).as(ErrorMessageHelper.wrongSelectedCurrencies(selectedCodes, expectedSorted)).isEqualTo(expectedSorted);

    }

    @And("Admin resets selected currencies to default")
    public void adminResetsSelectedCurrenciesToDefault() {

        CurrencyUpdateRequest request = new CurrencyUpdateRequest().currencies(DEFAULT_CURRENCIES);
        ok(() -> fineractClient.currency().updateCurrencies(request, Map.of()));

    }
}
