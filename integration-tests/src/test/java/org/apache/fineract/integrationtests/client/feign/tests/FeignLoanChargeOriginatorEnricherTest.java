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
package org.apache.fineract.integrationtests.client.feign.tests;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeignLoanChargeOriginatorEnricherTest extends FeignIntegrationTest {

    private static final String ADD_CHARGE_EVENT = "LoanAddChargeBusinessEvent";

    private static FineractFeignClient fineractClient;
    private static FeignLoanOriginatorHelper originatorHelper;
    private static FeignClientHelper clientHelper;
    private static FeignLoanHelper loanHelper;
    private static FeignExternalEventHelper externalEventHelper;

    @BeforeAll
    public static void setup() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        originatorHelper = new FeignLoanOriginatorHelper(fineractClient);
        clientHelper = new FeignClientHelper(fineractClient);
        loanHelper = new FeignLoanHelper(fineractClient);
        externalEventHelper = new FeignExternalEventHelper(fineractClient);
    }

    @Test
    public void testLoanAddChargeEventContainsOriginators() {
        externalEventHelper.enableBusinessEvent(ADD_CHARGE_EVENT);
        try {
            // Given: a loan with an originator attached
            final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator", "ACTIVE");
            final Long clientId = clientHelper.createClient();
            final Long loanId = loanHelper.createSubmittedLoan(clientId);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId);

            final Long chargeId = createFlatFeeCharge(50.0);

            externalEventHelper.deleteAllExternalEvents();

            // When: a charge is added to the loan
            ok(() -> fineractClient.loanCharges()
                    .executeLoanCharge(loanId,
                            new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(50.0).locale("en").dateFormat("dd MMMM yyyy")
                                    .dueDate(org.apache.fineract.integrationtests.common.Utils.dateFormatter
                                            .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant())),
                            (String) null));

            // Then: the external event payload contains originator details
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADD_CHARGE_EVENT);
            assertThat(events).isNotEmpty();

            final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(extractLoanId(e))).findFirst().orElse(null);
            assertThat(event).isNotNull();

            final Object originators = event.getPayLoad().get("originators");
            assertThat(originators).isNotNull().isInstanceOf(List.class);

            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> originatorList = (List<Map<String, Object>>) originators;
            assertThat(originatorList).hasSize(1);
            assertThat(originatorList.get(0).get("externalId")).isEqualTo(originatorExternalId);

            // Cleanup
            originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
            originatorHelper.deleteOriginator(originatorId);
        } finally {
            externalEventHelper.disableBusinessEvent(ADD_CHARGE_EVENT);
        }
    }

    @Test
    public void testLoanAddChargeEventContainsMultipleOriginators() {
        externalEventHelper.enableBusinessEvent(ADD_CHARGE_EVENT);
        try {
            // Given: a loan with two originators attached
            final String externalId1 = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final String externalId2 = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId1 = originatorHelper.createOriginator(externalId1, "Originator One", "ACTIVE");
            final Long originatorId2 = originatorHelper.createOriginator(externalId2, "Originator Two", "ACTIVE");
            final Long clientId = clientHelper.createClient();
            final Long loanId = loanHelper.createSubmittedLoan(clientId);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId1);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId2);

            final Long chargeId = createFlatFeeCharge(75.0);

            externalEventHelper.deleteAllExternalEvents();

            // When: a charge is added
            ok(() -> fineractClient.loanCharges()
                    .executeLoanCharge(loanId,
                            new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(75.0).locale("en").dateFormat("dd MMMM yyyy")
                                    .dueDate(org.apache.fineract.integrationtests.common.Utils.dateFormatter
                                            .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant())),
                            (String) null));

            // Then: both originators appear in the event
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADD_CHARGE_EVENT);
            final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(extractLoanId(e))).findFirst().orElse(null);
            assertThat(event).isNotNull();

            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> originatorList = (List<Map<String, Object>>) event.getPayLoad().get("originators");
            assertThat(originatorList).hasSize(2);

            // Cleanup
            originatorHelper.detachOriginatorFromLoan(loanId, originatorId1);
            originatorHelper.detachOriginatorFromLoan(loanId, originatorId2);
            originatorHelper.deleteOriginator(originatorId1);
            originatorHelper.deleteOriginator(originatorId2);
        } finally {
            externalEventHelper.disableBusinessEvent(ADD_CHARGE_EVENT);
        }
    }

    @Test
    public void testLoanAddChargeEventWithNoOriginators() {
        externalEventHelper.enableBusinessEvent(ADD_CHARGE_EVENT);
        try {
            // Given: a loan without originators
            final Long clientId = clientHelper.createClient();
            final Long loanId = loanHelper.createSubmittedLoan(clientId);

            final Long chargeId = createFlatFeeCharge(50.0);

            externalEventHelper.deleteAllExternalEvents();

            // When: a charge is added
            ok(() -> fineractClient.loanCharges()
                    .executeLoanCharge(loanId,
                            new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(50.0).locale("en").dateFormat("dd MMMM yyyy")
                                    .dueDate(org.apache.fineract.integrationtests.common.Utils.dateFormatter
                                            .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant())),
                            (String) null));

            // Then: originators field is null in the event (no enrichment when loan has no originators)
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADD_CHARGE_EVENT);
            final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(extractLoanId(e))).findFirst().orElse(null);
            assertThat(event).isNotNull();
            assertThat(event.getPayLoad().get("originators")).isNull();
        } finally {
            externalEventHelper.disableBusinessEvent(ADD_CHARGE_EVENT);
        }
    }

    private Long createFlatFeeCharge(double amount) {
        return ok(() -> fineractClient.charges().createCharge(new ChargeRequest()//
                .name("Originator Test Fee " + System.currentTimeMillis())//
                .currencyCode("USD")//
                .chargeAppliesTo(1)//
                .chargeTimeType(2)//
                .chargeCalculationType(1)//
                .chargePaymentMode(0)//
                .amount(amount)//
                .active(true)//
                .locale("en"))).getResourceId();
    }

    private Long extractLoanId(ExternalEventResponse event) {
        final Object loanId = event.getPayLoad().get("loanId");
        if (loanId instanceof Number) {
            return ((Number) loanId).longValue();
        }
        return null;
    }
}
