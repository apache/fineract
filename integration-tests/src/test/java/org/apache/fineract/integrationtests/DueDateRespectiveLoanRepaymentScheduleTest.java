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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class DueDateRespectiveLoanRepaymentScheduleTest extends FeignLoanTestBase {

    // Scenario1:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4 Adding a partial repayment
    // 4.1 Paying only principal portion
    // 4.2 Adding a partial repayment
    // 4.3 Paying only charge portion
    @Test
    public void scenario1() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateBusinessDate("01 February 2023");

            Long penalty = createSpecifiedDueDateCharge(50, true);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstRepaymentId = addRepaymentForLoan(loanId, 500.00, "10 January 2023");
            Long firstChargeId = addLoanCharge(loanId, penalty, "20 January 2023", 50.0).getResourceId();
            Long secondRepaymentId = addRepaymentForLoan(loanId, 450.00, "17 January 2023");

            Long thirdRepaymentId = addRepaymentForLoan(loanId, 50.00, "21 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(50.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(950.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(secondRepaymentId, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(450.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(450.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(thirdRepaymentId, response.getTransactions().get(3).getId());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(3).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario2:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4. Adding a partial repayment
    // 4.1 Paying only principal portion
    // 5. Adding a charge
    // 5.1 No any reverse-replay
    // 6. Adding a partial repayment
    // 6.1 Paying the 1st charge
    // 6.2 Paying secondly the in advance principal
    // 6.3 Not paying the 2nd charge (id: #5)
    @Test
    public void scenario2() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 February 2023");
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));

            Long penalty = createSpecifiedDueDateCharge(50, true);

            Long fee = createSpecifiedDueDateCharge(50, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstRepaymentId = addRepaymentForLoan(loanId, 500.00, "10 January 2023");
            Long firstChargeId = addLoanCharge(loanId, fee, "20 January 2023", 50.0).getResourceId();
            Long secondRepaymentId = addRepaymentForLoan(loanId, 100.00, "17 January 2023");

            Long secondChargeId = addLoanCharge(loanId, penalty, "23 January 2023", 10.0).getResourceId();

            Long thirdRepaymentId = addRepaymentForLoan(loanId, 100.00, "20 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(360.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(360.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(650.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(350.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(secondRepaymentId, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(100.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(400.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(thirdRepaymentId, response.getTransactions().get(3).getId());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(100.0, Utils.getDoubleValue(response.getTransactions().get(3).getAmount()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(350.0, Utils.getDoubleValue(response.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario3:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4. Adding a full repayment
    // 4.1 Paying first the in advance principal portion, and after the in advance charges
    @Test
    public void scenario3() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 February 2023");

            Long fee = createSpecifiedDueDateCharge(50, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstRepaymentId = addRepaymentForLoan(loanId, 500.00, "10 January 2023");
            Long firstChargeId = addLoanCharge(loanId, fee, "20 January 2023", 50.0).getResourceId();
            Long secondRepaymentId = addRepaymentForLoan(loanId, 550.00, "17 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(0.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getClosedObligationsMet());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));

            assertEquals(secondRepaymentId, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(550.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(2).getLoanChargePaidByList().size());
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    // Scenario4:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan with 3 installments
    // 2. Adding a charge but not due
    // 2. Adding a repayment which fully pays 1st installment and partially the next
    // 4.1 Paying first the in advance principal portion of #1 installment, and after the in advance charges of #1
    // installment and in advance principal in #2 installment
    @Test
    public void scenario4() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 February 2023");

            Long fee = createSpecifiedDueDateCharge(50, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 3, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "90", "30", "3", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstChargeId = addLoanCharge(loanId, fee, "20 January 2023", 50.0).getResourceId();

            Long firstRepaymentId = addRepaymentForLoan(loanId, 500.00, "10 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(550.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(550.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(116.67, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(216.66, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(450.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(550.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    // Scenario5:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan with 3 installments
    // 2. Adding a charge but not due
    // 2. Adding a repayment which fully pays 1st installment and partially the next
    // 4.1 Paying first the in advance principal portion of #1 installment, and after the in advance charges of #1
    // installment and in advance principal in #2 installment
    // 5 Overpay the loan
    @Test
    public void scenario5() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 February 2023");

            Long fee = createSpecifiedDueDateCharge(50, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 3, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "90", "30", "3", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstChargeId = addLoanCharge(loanId, fee, "20 January 2023", 50.0).getResourceId();

            Long firstRepaymentId = addRepaymentForLoan(loanId, 500.00, "10 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(550.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(550.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(116.67, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(216.66, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(450.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(50.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(550.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Long secondRepaymentId = addRepaymentForLoan(loanId, 650.00, "17 January 2023");

            response = getLoanDetails(loanId);

            int repaymentOrderNo;
            int accrualOrderNo;

            assertEquals(0.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(50.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(333.33, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesOutstanding()));
            assertEquals(333.34, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPrincipalDue()));
            assertEquals(333.34, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(3).getPrincipalOutstanding()));
            assertEquals(100.0, Utils.getDoubleValue(response.getTotalOverpaid()));
            assertTrue(response.getStatus().getOverpaid());

            int secondRepaymentIndex;
            // The repayment and accrual order is not consistent
            if (response.getTransactions().get(2).getType().getRepayment()) {
                secondRepaymentIndex = 2;
            } else {
                secondRepaymentIndex = 3;
            }

            assertEquals(secondRepaymentId, response.getTransactions().get(secondRepaymentIndex).getId());
            assertNull(response.getTransactions().get(secondRepaymentIndex).getReversedOnDate());
            assertTrue(response.getTransactions().get(secondRepaymentIndex).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(secondRepaymentIndex).getType().getRepayment());
            assertEquals(650.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getAmount()));
            assertEquals(550.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getPenaltyChargesPortion()));
            assertEquals(100.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(secondRepaymentIndex).getFeeChargesPortion()));

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    // Scenario6:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a snooze fee
    // 3. Do partial repayment
    // 3.1 Repay principal fully
    // 3.2 Repay fee partially
    @Test
    public void scenario6() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 March 2023");
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));

            Long fee = createSpecifiedDueDateCharge(20, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstChargeId = addLoanCharge(loanId, fee, "25 January 2023", 20.0).getResourceId();
            Long firstRepaymentId = addRepaymentForLoan(loanId, 1010.00, "01 March 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(10.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario7:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Full repayment
    // 3. Reverse repayment
    // 3.1 Add NSF Fee
    // 4. Partial repayment
    @Test
    public void scenario7() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("28 January 2023");
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));

            Long penalty = createSpecifiedDueDateCharge(15, true);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            Long firstRepaymentId = addRepaymentForLoan(loanId, 1000.00, "25 January 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(0.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getClosedObligationsMet());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(0, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = reverseLoanTransaction(loanId, firstRepaymentId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy").transactionDate("28 January 2023")
                            .transactionAmount(0.0).locale("en"));

            updateBusinessDate("31 January 2023");

            response = getLoanDetails(loanId);
            assertEquals(1000.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertEquals(LocalDate.of(2023, 1, 28), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(0, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Long firstChargeId = addLoanCharge(loanId, penalty, "28 January 2023", 15.0).getResourceId();
            Long secondRepayment = addRepaymentForLoan(loanId, 1010.00, "31 January 2023");

            response = getLoanDetails(loanId);
            assertEquals(5.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(995.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(995.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(5.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(2).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario8:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Partial repayment
    // 4. Reverse repayment
    // 4.1 NSF Fee added
    // 4. Partial repayment
    @Test
    public void scenario8() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateBusinessDate("15 February 2023");

            Long fee = createSpecifiedDueDateCharge(20, false);
            Long penalty = createSpecifiedDueDateCharge(15, true);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            createAndApproveReschedule(loanId, "25 January 2023", "31 January 2023", "01 March 2023");

            Long firstChargeId = addLoanCharge(loanId, fee, "01 March 2023", 20.0).getResourceId();
            Long firstRepaymentId = addRepaymentForLoan(loanId, 1010.00, "10 February 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(10.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = reverseLoanTransaction(loanId, firstRepaymentId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy").transactionDate("15 February 2023")
                            .transactionAmount(0.0).locale("en"));

            Long secondChargeId = addLoanCharge(loanId, penalty, "15 February 2023", 15.0).getResourceId();

            updateBusinessDate("01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(1035.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(1035.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertEquals(LocalDate.of(2023, 2, 15), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Long secondRepayment = addRepaymentForLoan(loanId, 15.00, "01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(1020.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(1020.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(2).getLoanChargePaidByList().size());

            Long thirdRepayment = addRepaymentForLoan(loanId, 1000.00, "01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(20.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(thirdRepayment, response.getTransactions().get(3).getId());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(3).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(0, response.getTransactions().get(3).getLoanChargePaidByList().size());

            Long forthRepayment = addRepaymentForLoan(loanId, 10.00, "01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(10.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(forthRepayment, response.getTransactions().get(4).getId());
            assertNull(response.getTransactions().get(4).getReversedOnDate());
            assertTrue(response.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(4).getType().getRepayment());
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(4).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(4).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(4).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(4).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario9:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Partial repayment
    // 4. Reverse repayment
    // 4.1 NSF Fee added
    // 4. Partial repayment
    // 5. Reverse repayment
    // 5.1 NSF Fee added
    // 6. Partial repayment
    @Test
    public void scenario9() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateBusinessDate("15 February 2023");

            Long fee = createSpecifiedDueDateCharge(20, false);
            Long penalty = createSpecifiedDueDateCharge(15, true);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2023"));
            disburseLoanWithAmount(loanId, "01 January 2023", 1000.0);

            createAndApproveReschedule(loanId, "25 January 2023", "31 January 2023", "01 March 2023");

            Long firstChargeId = addLoanCharge(loanId, fee, "01 March 2023", 20.0).getResourceId();
            Long firstRepaymentId = addRepaymentForLoan(loanId, 1010.00, "10 February 2023");

            GetLoansLoanIdResponse response = getLoanDetails(loanId);
            assertEquals(10.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = reverseLoanTransaction(loanId, firstRepaymentId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy").transactionDate("15 February 2023")
                            .transactionAmount(0.0).locale("en"));

            Long secondChargeId = addLoanCharge(loanId, penalty, "15 February 2023", 15.0).getResourceId();

            updateBusinessDate("01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(1035.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(1035.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId());
            assertEquals(LocalDate.of(2023, 2, 15), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, Utils.getDoubleValue(response.getTransactions().get(1).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getInterestPortion()));
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Long secondRepayment = addRepaymentForLoan(loanId, 1030.00, "01 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(5.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(1030.0, Utils.getDoubleValue(response.getTransactions().get(2).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getInterestPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(2).getOutstandingLoanBalance()));
            if (secondChargeId.equals(response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId())) {
                assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId());
                assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(1).getChargeId());
            } else {
                assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId());
                assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(1).getChargeId());
            }
            assertEquals(2, response.getTransactions().get(2).getLoanChargePaidByList().size());

            updateBusinessDate("07 March 2023");

            PostLoansLoanIdTransactionsResponse secondReverseRepayment = reverseLoanTransaction(loanId, secondRepayment,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy").transactionDate("07 March 2023")
                            .transactionAmount(0.0).locale("en"));

            Long thirdChargeId = addLoanCharge(loanId, penalty, "07 March 2023", 15.0).getResourceId();

            updateBusinessDate("08 March 2023");

            Long thirdRepayment = addRepaymentForLoan(loanId, 15.00, "08 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(1035.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(1035.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));

            assertEquals(thirdRepayment, response.getTransactions().get(3).getId());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(3).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(secondChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());

            Long forthRepayment = addRepaymentForLoan(loanId, 1015.00, "08 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(20.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));

            assertEquals(forthRepayment, response.getTransactions().get(4).getId());
            assertNull(response.getTransactions().get(4).getReversedOnDate());
            assertTrue(response.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(4).getType().getRepayment());
            assertEquals(1015.0, Utils.getDoubleValue(response.getTransactions().get(4).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getInterestPortion()));
            assertEquals(15.0, Utils.getDoubleValue(response.getTransactions().get(4).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(4).getOutstandingLoanBalance()));
            assertEquals(firstChargeId, response.getTransactions().get(4).getLoanChargePaidByList().get(0).getChargeId());
            assertEquals(1, response.getTransactions().get(4).getLoanChargePaidByList().size());

            Long fifthRepayment = addRepaymentForLoan(loanId, 10.00, "08 March 2023");

            response = getLoanDetails(loanId);
            assertEquals(10.0, Utils.getDoubleValue(response.getSummary().getTotalOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(20.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(15.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(5.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid()));
            assertEquals(10.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));

            assertEquals(fifthRepayment, response.getTransactions().get(5).getId());
            assertNull(response.getTransactions().get(5).getReversedOnDate());
            assertTrue(response.getTransactions().get(5).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(5).getType().getRepayment());
            assertEquals(10.0, Utils.getDoubleValue(response.getTransactions().get(5).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(5).getPrincipalPortion()));
            assertEquals(5.0, Utils.getDoubleValue(response.getTransactions().get(5).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(5).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(5).getInterestPortion()));
            assertEquals(5.0, Utils.getDoubleValue(response.getTransactions().get(5).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(response.getTransactions().get(5).getOutstandingLoanBalance()));
            if (firstChargeId.equals(response.getTransactions().get(5).getLoanChargePaidByList().get(0).getChargeId())) {
                assertEquals(thirdChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(1).getChargeId());
            } else {
                assertEquals(firstChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(1).getChargeId());
                assertEquals(thirdChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(0).getChargeId());
            }
            assertEquals(2, response.getTransactions().get(5).getLoanChargePaidByList().size());

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario10:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Merchant issued refund (partial)
    // 4. Charge adjustment (same day)
    // 5. Merchant issued refund (rest)
    @Test
    public void scenario10() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("14 May 2023");
            updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));

            Long fee = createSpecifiedDueDateCharge(3.65, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "14 May 2023", "14 May 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "14 May 2023"));
            disburseLoanWithAmount(loanId, "14 May 2023", 127.95);

            updateBusinessDate("11 June 2023");

            createAndApproveReschedule(loanId, "11 June 2023", "13 June 2023", "13 July 2023");
            Long penalty1LoanChargeId = addLoanCharge(loanId, fee, "13 July 2023", 3.65).getResourceId();
            updateBusinessDate("12 June 2023");
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(131.6, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(131.6, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(0).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(0).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getFeeChargesPortion()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getOutstandingLoanBalance()));

            updateBusinessDate("17 June 2023");
            PostLoansLoanIdTransactionsResponse merchantIssuedRefund1 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(125.0));

            loanDetails = getLoanDetails(loanId);
            assertEquals(6.6, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(6.6, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(1).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(1).getType().getMerchantIssuedRefund());
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOutstandingLoanBalance()));

            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = chargeAdjustment(loanId, penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(3.65));

            loanDetails = getLoanDetails(loanId);
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.70, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(2).getReversedOnDate());
            assertFalse(loanDetails.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertEquals((long) penalty1LoanChargeId,
                    loanDetails.getTransactions().get(2).getTransactionRelations().iterator().next().getToLoanCharge());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeAdjustment());
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.7, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund2 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(2.95));

            loanDetails = getLoanDetails(loanId);
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertNull(loanDetails.getTransactions().get(3).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(3).getType().getMerchantIssuedRefund());
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getInterestPortion()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));

        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    // Scenario11:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Merchant issued refund (partial)
    // 4. Charge adjustment (same day)
    // 5. Merchant issued refund (rest)
    @Test
    public void scenario11() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("14 May 2023");
            updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(false));

            Long fee = createSpecifiedDueDateCharge(3.65, false);
            final Long loanProductId = createLoanProduct(dueDateRespectiveNoAccountingNoInterestProduct(1000, 30, 1, 0,
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY));
            final Long clientId = createClient("01 January 2023");

            final Long loanId = applyForDueDateRespectiveLoan(clientId, loanProductId, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "14 May 2023", "14 May 2023");

            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "14 May 2023"));
            disburseLoanWithAmount(loanId, "14 May 2023", 127.95);

            updateBusinessDate("11 June 2023");

            createAndApproveReschedule(loanId, "11 June 2023", "13 June 2023", "13 July 2023");
            Long penalty1LoanChargeId = addLoanCharge(loanId, fee, "13 July 2023", 3.65).getResourceId();
            updateBusinessDate("12 June 2023");
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(131.6, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(131.6, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(0).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(0).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getFeeChargesPortion()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getTransactions().get(0).getOutstandingLoanBalance()));

            updateBusinessDate("17 June 2023");

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund1 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(125.0));

            loanDetails = getLoanDetails(loanId);
            assertEquals(6.6, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(6.6, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(1).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(1).getType().getMerchantIssuedRefund());
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(125.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getInterestPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getFeeChargesPortion()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOutstandingLoanBalance()));

            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = chargeAdjustment(loanId, penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(3.65));

            loanDetails = getLoanDetails(loanId);
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(0.70, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(2).getReversedOnDate());
            assertFalse(loanDetails.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertEquals((long) penalty1LoanChargeId,
                    loanDetails.getTransactions().get(2).getTransactionRelations().iterator().next().getToLoanCharge());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeAdjustment());
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getInterestPortion()));
            assertEquals(0.7, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund2 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(2.95));

            loanDetails = getLoanDetails(loanId);
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue()));
            assertEquals(3.65, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertEquals(127.95, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertNull(loanDetails.getTransactions().get(3).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(3).getType().getMerchantIssuedRefund());
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getInterestPortion()));
            assertEquals(2.95, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
            updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }

    private Long createSpecifiedDueDateCharge(double amount, boolean penalty) {
        ChargeRequest request = ChargeRequestBuilders.loanSpecifiedDueDateFee(amount);
        if (penalty) {
            request.penalty(true);
        }
        return chargesHelper.createCharge(request).getResourceId();
    }

    private Long applyForDueDateRespectiveLoan(Long clientId, Long productId, String principal, String loanTermFrequency,
            String repaymentAfterEvery, String numberOfRepayments, String interestRate, String repaymentStrategy,
            String expectedDisbursementDate, String submittedOnDate) {
        PostLoansRequest request = new PostLoansRequest().clientId(clientId).productId(productId).principal(new BigDecimal(principal))
                .loanTermFrequency(Integer.parseInt(loanTermFrequency)).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)
                .numberOfRepayments(Integer.parseInt(numberOfRepayments)).repaymentEvery(Integer.parseInt(repaymentAfterEvery))
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).interestRatePerPeriod(new BigDecimal(interestRate))
                .interestType(LoanTestData.InterestType.FLAT).amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                .transactionProcessingStrategyCode(repaymentStrategy)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .expectedDisbursementDate(expectedDisbursementDate).submittedOnDate(submittedOnDate)
                .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).loanType("individual");
        return applyForLoan(request);
    }

}
