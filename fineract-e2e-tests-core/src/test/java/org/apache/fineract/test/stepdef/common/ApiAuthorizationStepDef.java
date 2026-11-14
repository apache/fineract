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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import feign.FeignException;
import io.cucumber.java.en.Then;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.models.HookCreateRequest;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.client.models.StaffCreateRequest;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class ApiAuthorizationStepDef extends AbstractStepDef {

    private static final Long ANY_ID = 1L;

    /**
     * API calls the scenarios refer to by name. Calls that the authorization rules refuse never reach the resource, so
     * the IDs and request bodies do not need to point to existing data.
     */
    private static final Map<String, Function<FineractFeignClient, ?>> CALLS = Map.ofEntries(
            named("list hooks", c -> c.hooks().retrieveAllHooks()),
            named("create a hook", c -> c.hooks().createHook(new HookCreateRequest())),
            named("list staff", c -> c.staff().retrieveAllStaff(null, null, null, null)),
            named("create a staff member", c -> c.staff().createStaff(new StaffCreateRequest())),
            named("list payment types", c -> c.paymentType().getAllPaymentTypes((Boolean) null)),
            named("update a payment type", c -> c.paymentType().updatePaymentType(ANY_ID, new PaymentTypeUpdateRequest())),
            named("delete a payment type", c -> c.paymentType().deleteCodePaymentType(ANY_ID)),
            named("list business dates", c -> c.businessDateManagement().getBusinessDates()),
            named("update currencies", c -> c.currency().updateCurrencies(new CurrencyUpdateRequest())),
            named("list meetings of a group", c -> c.meetings().retrieveAllMeetings("groups", ANY_ID, (Integer) null)),
            named("list templates", c -> c.templates().retrieveAllTemplates((Integer) null, (Integer) null)),
            named("list interest rate charts", c -> c.interestRateChart().retrieveAllInterestRateCharts((Long) null)),
            named("read the product mix of a loan product", c -> c.productMix().retrieveTemplateProductMix(ANY_ID)),
            named("read a code", c -> c.codes().retrieveOneCode(ANY_ID)),
            named("list tellers", c -> c.tellerCashManagement().retrieveAllTellers((Long) null)),
            named("list ad hoc queries", c -> c.adhocQuery().retrieveAll1()),
            named("list provisioning criteria", c -> c.provisioningCriteria().retrieveAllProvisioningCriteria()),
            named("list surveys", c -> c.spmSurveys().fetchAllSurveys((Boolean) null)));

    private final FineractClientConfiguration fineractClientConfiguration;

    @Then("Created user is refused when it calls the API to {string}")
    public void createdUserIsRefused(final String call) {
        final FineractFeignClient userClient = createdUserClient();
        // the security filter chain refuses the request before it reaches the resource
        assertThatThrownBy(() -> call(call).apply(userClient)).isInstanceOf(FeignException.Forbidden.class)
                .satisfies(e -> assertThat(((FeignException) e).status()).isEqualTo(403));
    }

    @Then("Created user can call the API to {string}")
    public void createdUserCanCall(final String call) {
        final FineractFeignClient userClient = createdUserClient();
        ok(() -> call(call).apply(userClient));
    }

    private FineractFeignClient createdUserClient() {
        final String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        final String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        return fineractClientConfiguration.fineractFeignClientForUser(username, password);
    }

    private static Map.Entry<String, Function<FineractFeignClient, ?>> named(final String name,
            final Function<FineractFeignClient, ?> call) {
        return Map.entry(name, call);
    }

    private static Function<FineractFeignClient, ?> call(final String name) {
        final Function<FineractFeignClient, ?> call = CALLS.get(name);
        assertThat(call).as("API call named '%s' in ApiAuthorizationStepDef", name).isNotNull();
        return call;
    }
}
