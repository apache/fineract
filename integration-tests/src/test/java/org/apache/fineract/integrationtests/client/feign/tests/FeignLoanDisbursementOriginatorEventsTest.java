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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdOriginatorData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ExternalEventTestValidators;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Every event raised while disbursing a down payment loan with originators on the disburse command must carry those
 * originators.
 *
 * <p>
 * This does not pin the order of the linking step inside the disbursement: external events raised inside the
 * disbursement transaction are serialized and enriched just before it commits, by which time the originators are linked
 * wherever the linking call sits. The ordering itself is covered by the unit tests of the loan write service.
 * </p>
 */
public class FeignLoanDisbursementOriginatorEventsTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "21 May 2026";
    private static final BigDecimal PRINCIPAL = new BigDecimal("600.00");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("9.99");
    private static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal("25.00");
    private static final int NUMBER_OF_REPAYMENTS = 3;

    private static final String DISBURSAL_EVENT = "LoanDisbursalBusinessEvent";
    private static final String STATUS_CHANGED_EVENT = "LoanStatusChangedBusinessEvent";
    private static final String DISBURSAL_TRANSACTION_EVENT = "LoanDisbursalTransactionBusinessEvent";
    private static final String DOWN_PAYMENT_POST_EVENT = "LoanTransactionDownPaymentPostBusinessEvent";
    private static final String BALANCE_CHANGED_EVENT = "LoanBalanceChangedBusinessEvent";
    private static final List<String> EVENTS = List.of(DISBURSAL_EVENT, STATUS_CHANGED_EVENT, DISBURSAL_TRANSACTION_EVENT,
            DOWN_PAYMENT_POST_EVENT, BALANCE_CHANGED_EVENT);

    private static FeignLoanOriginatorHelper originatorHelper;

    @BeforeAll
    public static void setupOriginatorHelper() {
        originatorHelper = new FeignLoanOriginatorHelper(FineractFeignClientHelper.getFineractFeignClient());
        EVENTS.forEach(externalEventHelper::enableBusinessEvent);
    }

    @AfterAll
    public static void disableEvents() {
        EVENTS.forEach(externalEventHelper::disableBusinessEvent);
    }

    @Test
    void disbursementEventsOfDownPaymentLoanContainOriginatorsFromDisburseCommand() {
        runAt("2026-05-21", () -> {
            final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Disbursement Originator", "ACTIVE");
            final Long loanId = createApprovedDownPaymentLoan();

            externalEventHelper.deleteAllExternalEvents();

            final PostLoansLoanIdRequest disburseRequest = LoanRequestBuilders.disburseLoan(PRINCIPAL.doubleValue(), DISBURSEMENT_DATE)
                    .originators(List.of(new PostLoansLoanIdOriginatorData().id(originatorId)));
            disburseLoan(loanId, disburseRequest);

            final List<ExternalEventResponse> loanDisbursalEvents = eventsOfLoan(DISBURSAL_EVENT, "id", loanId);
            assertThat(loanDisbursalEvents).as("loan disbursal events").isNotEmpty();
            loanDisbursalEvents.forEach(e -> ExternalEventTestValidators.assertOriginators(e, originatorExternalId));

            final List<ExternalEventResponse> statusChangedEvents = eventsOfLoan(STATUS_CHANGED_EVENT, "id", loanId);
            assertThat(statusChangedEvents).as("status changed events").isNotEmpty();
            statusChangedEvents.forEach(e -> ExternalEventTestValidators.assertOriginators(e, originatorExternalId));

            final List<ExternalEventResponse> disbursalEvents = eventsOfLoan(DISBURSAL_TRANSACTION_EVENT, "loanId", loanId);
            assertThat(disbursalEvents).as("disbursal transaction events").isNotEmpty();
            disbursalEvents.forEach(e -> ExternalEventTestValidators.assertOriginators(e, originatorExternalId));

            final List<ExternalEventResponse> downPaymentEvents = eventsOfLoan(DOWN_PAYMENT_POST_EVENT, "loanId", loanId);
            assertThat(downPaymentEvents).as("down payment events").isNotEmpty();
            downPaymentEvents.forEach(e -> ExternalEventTestValidators.assertOriginators(e, originatorExternalId));

            // Raised both by the disbursement and by the auto down payment that follows it; none may miss the
            // originator.
            final List<ExternalEventResponse> balanceChangedEvents = eventsOfLoan(BALANCE_CHANGED_EVENT, "id", loanId);
            assertThat(balanceChangedEvents).as("balance changed events").isNotEmpty();
            balanceChangedEvents.forEach(e -> ExternalEventTestValidators.assertOriginators(e, originatorExternalId));
        });
    }

    private Long createApprovedDownPaymentLoan() {
        final Long clientId = createClient(DISBURSEMENT_DATE);
        final Long productId = createLoanProduct(downPaymentProduct());
        return applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL.doubleValue(), INTEREST_RATE.doubleValue(),
                NUMBER_OF_REPAYMENTS, null);
    }

    private PostLoanProductsRequest downPaymentProduct() {
        return customizeProduct(create4IProgressive(), p -> p//
                .numberOfRepayments(NUMBER_OF_REPAYMENTS)//
                .interestRatePerPeriod(INTEREST_RATE.doubleValue())//
                .multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(null)//
                .outstandingLoanBalance(null)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE)//
                .enableAutoRepaymentForDownPayment(true)//
                .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "NEXT_INSTALLMENT"))));
    }

    private List<ExternalEventResponse> eventsOfLoan(final String eventType, final String loanIdField, final Long loanId) {
        final Predicate<ExternalEventResponse> belongsToLoan = e -> {
            final Map<String, Object> payload = e.getPayLoad();
            return payload.get(loanIdField) instanceof Number n && n.longValue() == loanId;
        };
        return externalEventHelper.getExternalEventsByType(eventType).stream().filter(belongsToLoan).toList();
    }
}
