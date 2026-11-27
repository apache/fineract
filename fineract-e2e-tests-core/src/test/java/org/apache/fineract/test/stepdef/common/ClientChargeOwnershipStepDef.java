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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.test.data.ChargeProductResolver;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;

@RequiredArgsConstructor
public class ClientChargeOwnershipStepDef extends AbstractStepDef {

    private static final String FIRST_CLIENT_ID = "clientChargeOwnershipFirstClientId";
    private static final String SECOND_CLIENT_ID = "clientChargeOwnershipSecondClientId";
    private static final String CLIENT_CHARGE_ID = "clientChargeOwnershipClientChargeId";

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";
    private static final String CHARGE_DATE = "01 January 2025";
    private static final String PAY_COMMAND = "paycharge";
    private static final String WAIVE_COMMAND = "waive";

    private final FineractFeignClient fineractClient;
    private final ClientRequestFactory clientRequestFactory;
    private final ChargeProductResolver chargeProductResolver;

    @When("Admin creates two clients and adds a charge to the second client")
    public void createTwoClientsAndChargeSecond() {
        final PostClientsResponse first = ok(
                () -> fineractClient.clients().createClient(clientRequestFactory.defaultClientCreationRequest()));
        final PostClientsResponse second = ok(
                () -> fineractClient.clients().createClient(clientRequestFactory.defaultClientCreationRequest()));
        final Long chargeId = chargeProductResolver.resolve(ChargeProductType.CLIENT_TEST_CHARGE_FEE);
        final PostClientsClientIdChargesRequest request = new PostClientsClientIdChargesRequest() //
                .chargeId(chargeId) //
                .amount(BigDecimal.TEN) //
                .dueDate(CHARGE_DATE) //
                .dateFormat(DATE_FORMAT) //
                .locale(LOCALE);
        final Long clientChargeId = ok(() -> fineractClient.clientCharges().createClientCharge(second.getClientId(), request))
                .getResourceId();
        testContext().set(FIRST_CLIENT_ID, first.getClientId());
        testContext().set(SECOND_CLIENT_ID, second.getClientId());
        testContext().set(CLIENT_CHARGE_ID, clientChargeId);
    }

    @Then("Paying the client charge through the first client is not found")
    public void payThroughFirstClientNotFound() {
        assertNotFound(() -> payOrWaive(FIRST_CLIENT_ID, PAY_COMMAND));
    }

    @Then("Waiving the client charge through the first client is not found")
    public void waiveThroughFirstClientNotFound() {
        assertNotFound(() -> payOrWaive(FIRST_CLIENT_ID, WAIVE_COMMAND));
    }

    @Then("Paying the client charge through the second client succeeds")
    public void payThroughSecondClientSucceeds() {
        ok(() -> payOrWaive(SECOND_CLIENT_ID, PAY_COMMAND));
    }

    private Object payOrWaive(final String clientKey, final String command) {
        final Long clientId = testContext().get(clientKey);
        final Long clientChargeId = testContext().get(CLIENT_CHARGE_ID);
        final PostClientsClientIdChargesChargeIdRequest request = new PostClientsClientIdChargesChargeIdRequest() //
                .transactionDate(CHARGE_DATE) //
                .amount(10) //
                .dateFormat(DATE_FORMAT) //
                .locale(LOCALE);
        return fineractClient.clientCharges().payOrWaiveClientCharge(clientId, clientChargeId, request, command);
    }

    private static void assertNotFound(final Supplier<?> call) {
        assertThat(fail(call).getStatus()).isEqualTo(404);
    }
}
