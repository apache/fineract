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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanNextPaymentDueAmountIntegrationTest extends BaseLoanIntegrationTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static final String LOAN_ACCOUNT_DATA_V_1 = "org.apache.fineract.avro.loan.v1.LoanAccountDataV1";

    @Test
    void test_progressive_interest_noRecalculation() {
        externalEventHelper.enableBusinessEvent("LoanApprovedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanDisbursalBusinessEvent");
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("15 January 2023", () -> {

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false))
                    .getResourceId();

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 January 2023", 100.0, 9.9, 4, null);
            loanIdRef.set(loanId);

            deleteAllExternalEvents();
            loanTransactionHelper.disburseLoan(loanId, "01 January 2023", 100.0);

            verifyRepaymentSchedule(loanId, //
                    installment(100.000000, null, "01 January 2023"), //
                    installment(24.700000, 0.820000, 25.520000, false, "01 February 2023"), //
                    installment(24.900000, 0.620000, 25.520000, false, "01 March 2023"), //
                    installment(25.100000, 0.420000, 25.520000, false, "01 April 2023"), //
                    installment(25.300000, 0.210000, 25.510000, false, "01 May 2023") //
            );
            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-02-01", 25.52d);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("31 January 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
    }

    @Test
    void test_progressive_interest_noRecalculation_prepay() {
        externalEventHelper.enableBusinessEvent("LoanApprovedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanDisbursalBusinessEvent");
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("15 January 2023", () -> {

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false))
                    .getResourceId();

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 January 2023", 100.0, 9.9, 4, null);
            loanIdRef.set(loanId);

            deleteAllExternalEvents();
            loanTransactionHelper.disburseLoan(loanId, "01 January 2023", 100.0);

            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-02-01", 25.52d);

            deleteAllExternalEvents();
            addRepaymentForLoan(loanId, 25.52d, "15 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(100.000000, null, "01 January 2023"), //
                    installment(24.700000, 0.820000, 0.0, true, "01 February 2023"), //
                    installment(24.900000, 0.620000, 25.520000, false, "01 March 2023"), //
                    installment(25.100000, 0.420000, 25.520000, false, "01 April 2023"), //
                    installment(25.300000, 0.210000, 25.510000, false, "01 May 2023") //
            );

            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-03-01", 25.52d);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("31 January 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
            deleteAllExternalEvents();
            addRepaymentForLoan(loanId, 25.52d, "02 March 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(100.000000, null, "01 January 2023"), //
                    installment(24.700000, 0.820000, 0.0, true, "01 February 2023"), //
                    installment(24.900000, 0.620000, 0.0, true, "01 March 2023"), //
                    installment(25.100000, 0.420000, 25.520000, false, "01 April 2023"), //
                    installment(25.300000, 0.210000, 25.510000, false, "01 May 2023") //
            );

            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-04-01", 25.52d);

        });
    }

    @Test
    void test_progressive_interest_recalculation_sameAsRepaymentPeriod() {
        externalEventHelper.enableBusinessEvent("LoanApprovedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanDisbursalBusinessEvent");
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("15 February 2023", () -> {

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long loanProductId = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true).recalculationRestFrequencyInterval(1)
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD))
                    .getResourceId();

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 January 2023", 100.0, 9.9, 4, null);
            loanIdRef.set(loanId);

            deleteAllExternalEvents();
            loanTransactionHelper.disburseLoan(loanId, "01 January 2023", 100.0);

            verifyRepaymentSchedule(loanId, //
                    installment(100.000000, null, "01 January 2023"), //
                    installment(24.700000, 0.820000, 25.520000, false, "01 February 2023"), //
                    installment(24.800000, 0.720000, 25.520000, false, "01 March 2023"), //
                    installment(25.100000, 0.420000, 25.520000, false, "01 April 2023"), //
                    installment(25.400000, 0.210000, 25.610000, false, "01 May 2023") //
            );

            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-02-01", 25.52d);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("31 January 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 2, 1), BigDecimal.valueOf(25.52d));
        });
    }

    @Test
    void test_progressive_interest_recalculation_daily() {
        externalEventHelper.enableBusinessEvent("LoanApprovedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanDisbursalBusinessEvent");
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("15 February 2023", () -> {

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).recalculationRestFrequencyInterval(1)
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)).getResourceId();

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 January 2023", 100.0, 9.9, 4, null);

            loanIdRef.set(loanId);

            deleteAllExternalEvents();
            loanTransactionHelper.disburseLoan(loanId, "01 January 2023", 100.0);

            verifyRepaymentSchedule(loanId, //
                    installment(100.000000, null, "01 January 2023"), //
                    installment(24.700000, 0.820000, 25.520000, false, "01 February 2023"), //
                    installment(24.800000, 0.720000, 25.520000, false, "01 March 2023"), //
                    installment(25.100000, 0.420000, 25.520000, false, "01 April 2023"), //
                    installment(25.400000, 0.210000, 25.610000, false, "01 May 2023") //
            );
            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-02-01", 25.52d);

            deleteAllExternalEvents();
            addRepaymentForLoan(loanId, 25.52d, "15 January 2023");

            verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount("2023-03-01", 25.52d);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));

        });
        runAt("31 January 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyNextPayment(loanDetails, LocalDate.of(2023, 3, 1), BigDecimal.valueOf(25.52d));
        });

    }

    /**
     * verifies that all the external events which has org.apache.fineract.avro.loan.v1.LoanAccountDataV1 types payload
     * has the correct nextPaymentDueDate and nextPaymentDueAmount
     *
     * @param dueDate
     *            expected due date formatted yyyy-MM-dd format (ie 2023-12-31 )
     * @param amount
     *            expected amount
     */
    private void verifyAllLoanAccountTypedExternalEventHasNextPaymentDueAmount(String dueDate, Double amount) {
        List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
        allExternalEvents.stream().filter(e -> LOAN_ACCOUNT_DATA_V_1.equals(e.getSchema()))
                .forEach(event -> verifyLoanEvent(event, data -> verifyNextPayment(data, dueDate, BigDecimal.valueOf(amount))));

    }

    // utility
    LoanAccountDataV1 convertLoan(ExternalEventResponse externalEventDTO) {
        if (LOAN_ACCOUNT_DATA_V_1.equals(externalEventDTO.getSchema())) {
            return objectMapper.convertValue(externalEventDTO.getPayLoad(), LoanAccountDataV1.class);
        } else {
            throw new RuntimeException("Unexpected schema: " + externalEventDTO.getSchema());
        }
    }

    void verifyLoanEvent(ExternalEventResponse externalEventDTO, Consumer<LoanAccountDataV1> validator) {
        validator.accept(convertLoan(externalEventDTO));
    }

    void verifyNextPayment(LoanAccountDataV1 loanAccountData, String nextPaymentDueDate, BigDecimal nextPaymentAmount) {
        Assertions.assertNotNull(loanAccountData, "loanDetails should not be null");
        Assertions.assertNotNull(loanAccountData.getDelinquent(), "loanDetails.delinquent should not be null");
        String nextPaymentDueDateActual = loanAccountData.getDelinquent().getNextPaymentDueDate();
        BigDecimal nextPaymentAmountActual = loanAccountData.getDelinquent().getNextPaymentAmount();
        log.info("Verify ExternalEventResponse nextPaymentDueDate. Expected: {}, Actual: {}", nextPaymentDueDate, nextPaymentDueDateActual);
        Assertions.assertEquals(nextPaymentDueDate, nextPaymentDueDateActual);
        log.info("Verify ExternalEventResponse nextPaymentAmount. Expected: {}, Actual: {}", nextPaymentAmount, nextPaymentAmountActual);
        Assertions.assertEquals(nextPaymentAmount, nextPaymentAmountActual);
    }

    void verifyNextPayment(GetLoansLoanIdResponse loanDetails, LocalDate nextPaymentDueDate, BigDecimal nextPaymentAmount) {
        Assertions.assertNotNull(loanDetails, "loanDetails should not be null");
        Assertions.assertNotNull(loanDetails.getDelinquent(), "loanDetails.delinquent should not be null");
        LocalDate nextPaymentDueDateActual = loanDetails.getDelinquent().getNextPaymentDueDate();
        BigDecimal nextPaymentAmountActual = loanDetails.getDelinquent().getNextPaymentAmount();
        log.info("Verify GetLoansLoanIdResponse nextPaymentDueDate. Expected: {}, Actual: {}", nextPaymentDueDate,
                nextPaymentDueDateActual);
        Assertions.assertEquals(nextPaymentDueDate, nextPaymentDueDateActual);
        log.info("Verify GetLoansLoanIdResponse nextPaymentAmount. Expected: {}, Actual: {}", nextPaymentAmount, nextPaymentAmountActual);
        Assertions.assertEquals(nextPaymentAmount, nextPaymentAmountActual);
    }
}
