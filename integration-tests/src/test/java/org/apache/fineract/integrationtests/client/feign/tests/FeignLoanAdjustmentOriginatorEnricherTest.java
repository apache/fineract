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
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ExternalEventTestValidators;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeignLoanAdjustmentOriginatorEnricherTest extends FeignLoanTestBase {

    private static final String ADJUST_EVENT = "LoanAdjustTransactionBusinessEvent";
    private static final String ACCRUAL_EVENT = "LoanAccrualTransactionCreatedBusinessEvent";

    private static FineractFeignClient fineractClient;
    private static FeignLoanOriginatorHelper originatorHelper;
    private static FeignExternalEventHelper externalEventHelper;

    @BeforeAll
    public static void setupOriginatorHelpers() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        originatorHelper = new FeignLoanOriginatorHelper(fineractClient);
        externalEventHelper = new FeignExternalEventHelper(fineractClient);
    }

    @Test
    public void testLoanAdjustTransactionEventContainsOriginators() {
        externalEventHelper.enableBusinessEvent(ADJUST_EVENT);
        try {
            final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator", "ACTIVE");
            final Long clientId = createClient();
            final Long productId = loanHelper.createSimpleLoanProduct();
            final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

            // Create loan, attach originator, approve, disburse
            final Long loanId = loanHelper.createSubmittedLoan(clientId, productId, today, 10000.0, 12);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId);
            approveLoan(loanId, LoanRequestBuilders.approveLoan(10000.0, today));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(10000.0, today));

            final Long repaymentId = addRepayment(loanId, repayment(500.0, today));
            externalEventHelper.deleteAllExternalEvents();

            // When: the repayment is undone
            undoRepayment(loanId, repaymentId, today);

            // Then
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADJUST_EVENT);
            final ExternalEventResponse event = ExternalEventTestValidators.findEventForLoan(events, loanId);
            ExternalEventTestValidators.assertOriginatorsInField(event, "transactionToAdjust", originatorExternalId);
        } finally {
            externalEventHelper.disableBusinessEvent(ADJUST_EVENT);
        }
    }

    @Test
    public void testLoanAdjustTransactionEventWithNoOriginators() {
        externalEventHelper.enableBusinessEvent(ADJUST_EVENT);
        try {
            final Long clientId = createClient();
            final Long productId = loanHelper.createSimpleLoanProduct();
            final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

            final Long loanId = createApproveAndDisburseLoan(clientId, productId, today, 10000.0, 12);
            final Long repaymentId = addRepayment(loanId, repayment(500.0, today));
            externalEventHelper.deleteAllExternalEvents();

            // When: the repayment is undone
            undoRepayment(loanId, repaymentId, today);

            // Then
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADJUST_EVENT);
            final ExternalEventResponse event = ExternalEventTestValidators.findEventForLoan(events, loanId);
            ExternalEventTestValidators.assertNoOriginatorsInField(event, "transactionToAdjust");
        } finally {
            externalEventHelper.disableBusinessEvent(ADJUST_EVENT);
        }
    }

    @Test
    public void testAccrualEventContainsOriginators() {
        externalEventHelper.enableBusinessEvent(ACCRUAL_EVENT);
        try {
            final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator", "ACTIVE");
            final Long clientId = createClient();
            final Long productId = createLoanProduct(fourInstallmentsCumulative());
            final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

            // Create loan with accrual accounting, attach originator, approve, disburse
            final Long loanId = loanHelper.createSubmittedLoan(clientId, productId, today, 10000.0, 4);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId);
            approveLoan(loanId, LoanRequestBuilders.approveLoan(10000.0, today));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(10000.0, today));

            // Add a fee charge to the loan
            final Long chargeId = createFlatFeeCharge(100.0, "EUR");
            ok(() -> fineractClient.loanCharges().executeLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId)
                    .amount(100.0).locale("en").dateFormat("dd MMMM yyyy").dueDate(today), (String) null));

            externalEventHelper.deleteAllExternalEvents();

            // When: COB runs and creates accrual transactions
            executeInlineCOB(loanId);

            // Then: the accrual event contains originator details
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ACCRUAL_EVENT);
            final ExternalEventResponse event = ExternalEventTestValidators.findEventForLoan(events, loanId);
            ExternalEventTestValidators.assertOriginators(event, originatorExternalId);
        } finally {
            externalEventHelper.disableBusinessEvent(ACCRUAL_EVENT);
        }
    }

    @Test
    public void testAdjustEventContainsOriginatorsAfterChargeWaiverReversal() {
        externalEventHelper.enableBusinessEvent(ADJUST_EVENT);
        try {
            final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator", "ACTIVE");
            final Long clientId = createClient();
            final Long productId = createLoanProduct(fourInstallmentsCumulative());
            final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

            // Create loan with accrual accounting, attach originator, approve, disburse
            final Long loanId = loanHelper.createSubmittedLoan(clientId, productId, today, 10000.0, 4);
            originatorHelper.attachOriginatorToLoan(loanId, originatorId);
            approveLoan(loanId, LoanRequestBuilders.approveLoan(10000.0, today));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(10000.0, today));

            // Add a fee charge and waive it
            final Long chargeId = createFlatFeeCharge(100.0, "EUR");
            final Long loanChargeId = ok(() -> fineractClient.loanCharges().executeLoanCharge(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeId).amount(100.0).locale("en").dateFormat("dd MMMM yyyy").dueDate(today), (String) null))
                    .getResourceId();

            ok(() -> fineractClient.loanCharges().executeLoanChargeOnExistingCharge(loanId, loanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest(), "waive"));

            // Find the waiver transaction from loan details
            final GetLoansLoanIdResponse loanAfterWaive = getLoanDetails(loanId);
            final Long waiveTransactionId = loanAfterWaive.getTransactions().stream()
                    .filter(t -> "loanTransactionType.waiveCharges".equals(t.getType().getCode())).map(GetLoansLoanIdTransactions::getId)
                    .findFirst().orElseThrow(() -> new AssertionError("Waiver transaction not found"));

            externalEventHelper.deleteAllExternalEvents();

            // When: the waiver is reversed
            undoRepayment(loanId, waiveTransactionId, today);

            // Then: the adjustment event contains originator details
            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(ADJUST_EVENT);
            final ExternalEventResponse event = ExternalEventTestValidators.findEventForLoan(events, loanId);
            ExternalEventTestValidators.assertOriginatorsInField(event, "transactionToAdjust", originatorExternalId);
        } finally {
            externalEventHelper.disableBusinessEvent(ADJUST_EVENT);
        }
    }

    private Long createFlatFeeCharge(double amount, String currencyCode) {
        return ok(() -> fineractClient.charges().createCharge(new ChargeRequest()//
                .name("Originator Test Fee " + System.currentTimeMillis())//
                .currencyCode(currencyCode)//
                .chargeAppliesTo(1)//
                .chargeTimeType(2)//
                .chargeCalculationType(1)//
                .chargePaymentMode(0)//
                .amount(amount)//
                .active(true)//
                .locale("en"))).getResourceId();
    }
}
