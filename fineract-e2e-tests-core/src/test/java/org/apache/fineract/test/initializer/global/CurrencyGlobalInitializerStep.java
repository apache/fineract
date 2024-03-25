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
package org.apache.fineract.test.initializer.global;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PutCurrenciesRequest;
import org.apache.fineract.client.models.PutCurrenciesResponse;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CurrencyGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final List<String> CURRENCIES = Arrays.asList("EUR", "USD");

    private final CurrencyApi currencyApi;

    @Override
    public void initialize() throws Exception {
        PutCurrenciesRequest putCurrenciesRequest = new PutCurrenciesRequest();
        Response<PutCurrenciesResponse> putCurrenciesResponse = currencyApi.updateCurrencies(putCurrenciesRequest.currencies(CURRENCIES))
                .execute();
        TestContext.INSTANCE.set(TestContextKey.PUT_CURRENCIES_RESPONSE, putCurrenciesResponse);
    }
}
