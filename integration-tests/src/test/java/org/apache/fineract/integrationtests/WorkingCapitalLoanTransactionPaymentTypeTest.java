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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanTransactionPaymentTypeTest {

    private static final int PAYMENT_TYPE_ID = 1;
    private static final int INVALID_PAYMENT_TYPE_ID = 999999;
    private static final LocalDate DEFAULT_DATE = LocalDate.of(2026, 1, 1);

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

    @AfterEach
    void cleanup() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdProductIds.clear();
    }

    // -------------------------------------------------------------------------
    // Repayment
    // -------------------------------------------------------------------------

    @Test
    public void testRepaymentWithPaymentTypeIsStoredAndReturnedInList() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE), () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, PAYMENT_TYPE_ID, null)));

        final GetWorkingCapitalLoanTransactionIdResponse repayment = findRepayment(loanId);
        assertNotNull(repayment.getPaymentDetailData(), "paymentDetailData must not be null after repayment with payment type");
        assertNotNull(repayment.getPaymentDetailData().getPaymentType(), "paymentType must be populated");
        assertEquals(Long.valueOf(PAYMENT_TYPE_ID), repayment.getPaymentDetailData().getPaymentType().getId());
    }

    @Test
    public void testRepaymentWithPaymentTypeIsReturnedOnPointGet() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE), () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, PAYMENT_TYPE_ID, null)));

        final GetWorkingCapitalLoanTransactionIdResponse repaymentFromList = findRepayment(loanId);
        final Long transactionId = repaymentFromList.getId();

        final GetWorkingCapitalLoanTransactionIdResponse repaymentFromGet = loanHelper.retrieveTransactionByLoanIdAndTransactionId(loanId,
                transactionId);

        assertNotNull(repaymentFromGet.getPaymentDetailData(), "paymentDetailData must be present on point GET");
        assertNotNull(repaymentFromGet.getPaymentDetailData().getPaymentType(), "paymentType must be present on point GET");
        assertEquals(Long.valueOf(PAYMENT_TYPE_ID), repaymentFromGet.getPaymentDetailData().getPaymentType().getId());
    }

    @Test
    public void testRepaymentWithoutPaymentTypeHasNullPaymentDetail() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE), () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, null, null)));

        final GetWorkingCapitalLoanTransactionIdResponse repayment = findRepayment(loanId);
        assertNull(repayment.getPaymentDetailData(), "paymentDetailData must be null when no payment type was provided");
    }

    @Test
    public void testRepaymentWithInvalidPaymentTypeIdFails() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));
        final CallFailedRuntimeException[] holder = new CallFailedRuntimeException[1];

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE),
                () -> holder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, INVALID_PAYMENT_TYPE_ID, null)));

        assertEquals(404, holder[0].getStatus(), "Repayment with non-existent paymentTypeId should return HTTP 404");
    }

    // -------------------------------------------------------------------------
    // Credit balance refund
    // -------------------------------------------------------------------------

    @Test
    public void testCreditBalanceRefundWithPaymentTypeIsStoredAndReturned() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        // Overpay to create credit balance
        BusinessDateHelper.runAt(fmt(DEFAULT_DATE), () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(5200), null, null, null, null)));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE),
                () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildCreditBalanceRefundRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, PAYMENT_TYPE_ID, null)));

        final GetWorkingCapitalLoanTransactionIdResponse refund = findCreditBalanceRefund(loanId);
        assertNotNull(refund, "Credit balance refund transaction must exist");
        assertNotNull(refund.getPaymentDetailData(), "paymentDetailData must be present on credit balance refund");
        assertNotNull(refund.getPaymentDetailData().getPaymentType());
        assertEquals(Long.valueOf(PAYMENT_TYPE_ID), refund.getPaymentDetailData().getPaymentType().getId());
    }

    @Test
    public void testCreditBalanceRefundWithoutPaymentTypeHasNullPaymentDetail() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        // Overpay to create credit balance
        BusinessDateHelper.runAt(fmt(DEFAULT_DATE), () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(5200), null, null, null, null)));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE),
                () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildCreditBalanceRefundRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, null, null)));

        final GetWorkingCapitalLoanTransactionIdResponse refund = findCreditBalanceRefund(loanId);
        assertNotNull(refund, "Credit balance refund transaction must exist");
        assertNull(refund.getPaymentDetailData(), "paymentDetailData must be null when no payment type was provided");
    }

    // -------------------------------------------------------------------------
    // Goodwill credit
    // -------------------------------------------------------------------------

    @Test
    public void testGoodwillCreditWithPaymentTypeIsStoredAndReturned() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE),
                () -> loanHelper.makeGoodwillCreditByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, PAYMENT_TYPE_ID, null)));

        final GetWorkingCapitalLoanTransactionIdResponse txn = findGoodwillCredit(loanId);
        assertNotNull(txn, "Goodwill credit transaction must exist");
        assertNotNull(txn.getPaymentDetailData(), "paymentDetailData must be present on goodwill credit");
        assertNotNull(txn.getPaymentDetailData().getPaymentType());
        assertEquals(Long.valueOf(PAYMENT_TYPE_ID), txn.getPaymentDetailData().getPaymentType().getId());
    }

    @Test
    public void testGoodwillCreditWithoutPaymentTypeHasNullPaymentDetail() {
        final Long loanId = createDisbursedLoan(BigDecimal.valueOf(5000));

        BusinessDateHelper.runAt(fmt(DEFAULT_DATE),
                () -> loanHelper.makeGoodwillCreditByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(DEFAULT_DATE, BigDecimal.valueOf(100), null, null, null, null)));

        final GetWorkingCapitalLoanTransactionIdResponse txn = findGoodwillCredit(loanId);
        assertNotNull(txn, "Goodwill credit transaction must exist");
        assertNull(txn.getPaymentDetailData(), "paymentDetailData must be null when no payment type was provided");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Long createDisbursedLoan(final BigDecimal principal) {
        return createDisbursedLoan(principal, DEFAULT_DATE);
    }

    private Long createDisbursedLoan(final BigDecimal principal, final LocalDate disbursementDate) {
        final Long productId = createProduct();
        final PostWorkingCapitalLoansRequest submitRequest = new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(principal).withSubmittedOnDate(disbursementDate)
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT).buildSubmitRequest();
        final Long loanId = loanHelper.submit(submitRequest);
        createdLoanIds.add(loanId);
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(disbursementDate, principal, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(disbursementDate, principal));
        return loanId;
    }

    private Long createProduct() {
        final String name = "WCL-PaymentType-" + UUID.randomUUID().toString().substring(0, 8);
        final String shortName = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(name).withShortName(shortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private GetWorkingCapitalLoanTransactionIdResponse findRepayment(final Long loanId) {
        return findTransactionByType(loanId, "loanTransactionType.repayment");
    }

    private GetWorkingCapitalLoanTransactionIdResponse findCreditBalanceRefund(final Long loanId) {
        return findTransactionByType(loanId, "loanTransactionType.creditBalanceRefund");
    }

    private GetWorkingCapitalLoanTransactionIdResponse findGoodwillCredit(final Long loanId) {
        return findTransactionByType(loanId, "loanTransactionType.goodwillCredit");
    }

    private GetWorkingCapitalLoanTransactionIdResponse findTransactionByType(final Long loanId, final String typeCode) {
        final GetWorkingCapitalLoanTransactionsResponse response = loanHelper.retrieveTransactionsByLoanId(loanId);
        assertNotNull(response.getContent(), "Transaction list must not be null");
        return response.getContent().stream()
                .filter(txn -> Boolean.FALSE.equals(txn.getReversed()) && txn.getType() != null && typeCode.equals(txn.getType().getCode()))
                .findFirst().orElse(null);
    }

    private String fmt(final LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }
}
