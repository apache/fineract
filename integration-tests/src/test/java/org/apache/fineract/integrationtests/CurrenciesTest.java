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
package org.apache.fineract.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.CurrencyCreateRequest;
import org.apache.fineract.client.models.CurrencyCreateResponse;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.integrationtests.common.CurrenciesHelper;
import org.apache.fineract.integrationtests.common.CurrencyDomain;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

@SuppressWarnings({ "unused" })
public class CurrenciesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCurrencyElements() {

        CurrencyDomain currency = CurrenciesHelper.getCurrencybyCode(requestSpec, responseSpec, "USD");
        CurrencyDomain usd = CurrencyDomain.create("USD", "US Dollar", 2, "$", "currency.USD", "US Dollar ($)").build();

        assertNotNull(currency);
        assertTrue(currency.getDecimalPlaces() >= 0);
        assertNotNull(currency.getName());
        assertNotNull(currency.getDisplaySymbol());
        assertNotNull(currency.getDisplayLabel());
        assertNotNull(currency.getNameCode());

        assertEquals(usd, currency);
    }

    @Test
    public void testUpdateCurrencySelection() {
        var currenciestoUpdate = List.of("KES", "BND", "LBP", "GHC", "USD", "INR");

        var currenciesOutput = CurrenciesHelper.updateSelectedCurrencies(this.requestSpec, this.responseSpec, currenciestoUpdate);

        assertNotNull(currenciesOutput);
        assertEquals(currenciestoUpdate, currenciesOutput, "Verifying returned currencies match after update");

        var currenciesBeforeUpdate = currenciestoUpdate.stream()
                .map(currency -> CurrenciesHelper.getCurrencybyCode(requestSpec, responseSpec, currency)).filter(Objects::nonNull).sorted()
                .toList();

        var currenciesAfterUpdate = CurrenciesHelper.getSelectedCurrencies(requestSpec, responseSpec);

        assertNotNull(currenciesAfterUpdate);
        assertEquals(currenciesBeforeUpdate, currenciesAfterUpdate, "Verifying selected currencies match after update");
    }

    @Test
    public void testCreateCurrencyPostApi_CurrencyCodeNotEqualToNull() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(1).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_CurrencyCodeNotEqualToRegex() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYz").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(1).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_CurrencyCodeNotEqualAlphabetsOnly() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("123").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(1).name("123 Token").nameCode("currency.123");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_CurrencyCodeDoesNotMatchNameCode() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(1).name("XYZ Token").nameCode("currency.ABC");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_DecimalPlacesLessThanZero() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(-1).displaySymbol("¤")
                .inMultiplesOf(1).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_DecimalPlacesMoreThanFive() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(6).displaySymbol("¤")
                .inMultiplesOf(1).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_InMultiplesOfLessThanZero() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(-1).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_InMultiplesOfMoreThanThousand() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("XYZ").decimalPlaces(2).displaySymbol("¤")
                .inMultiplesOf(1001).name("XYZ Token").nameCode("currency.XYZ");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    public void testCreateCurrencyPostApi_duplicateCurrencyOrCurrencyExists() throws IOException {
        CurrencyCreateRequest createCurrencyData = new CurrencyCreateRequest().code("USD").decimalPlaces(2).displaySymbol("$")
                .inMultiplesOf(1).name("US Dollar").nameCode("currency.USD");

        CurrencyApi currencyApi = FineractClientHelper.getFineractClient().currencies;
        Call<CurrencyCreateResponse> call = currencyApi.createCurrencies(createCurrencyData);
        Response<CurrencyCreateResponse> response = call.execute();

        assertThat(response.code()).isEqualTo(403);
    }
}
