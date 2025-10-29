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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class FeignLoanCreationTest extends FeignLoanTestBase {

    @Test
    void testCreateAndDisburseLoan_OnePeriodNoInterest() {
        Long clientId = createClient("John", "Doe");
        assertNotNull(clientId);

        PostLoanProductsRequest productRequest = onePeriod30DaysNoInterest();
        Long productId = createLoanProduct(productRequest);
        assertNotNull(productId);

        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 1);
        assertNotNull(loanId);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertNotNull(loan);
        verifyLoanStatus(loan, status -> status.getActive());

        LocalDate expectedRepaymentDate = Utils.getLocalDateOfTenant().plusMonths(1);
        validateRepaymentPeriod(loan, 1, expectedRepaymentDate, 1000.0, 0.0, 1000.0);

        verifyJournalEntries(loanId, debit(getAccounts().getLoansReceivableAccount().getAccountID().longValue(), 1000.0),
                credit(getAccounts().getFundSource().getAccountID().longValue(), 1000.0));
    }

    @Test
    void testLoanRepayment_FullRepayment() {
        Long clientId = createClient("Jane", "Smith");
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 1);

        Long repaymentId = addRepayment(loanId, repayment(1000.0, todayDate));
        assertNotNull(repaymentId);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, status -> status.getClosedObligationsMet());

        assertEquals(0.0, Utils.getDoubleValue(loan.getSummary().getTotalOutstanding()));
        assertEquals(1000.0, Utils.getDoubleValue(loan.getSummary().getTotalRepayment()));
    }

    @Test
    void testLoanUndoApproval() {
        Long clientId = createClient("Bob", "Johnson");
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApprovedLoan(clientId, productId, todayDate, 1000.0, 1);

        GetLoansLoanIdResponse loanBeforeUndo = getLoanDetails(loanId);
        verifyLoanStatus(loanBeforeUndo, status -> status.getWaitingForDisbursal());

        undoApproval(loanId);

        GetLoansLoanIdResponse loanAfterUndo = getLoanDetails(loanId);
        verifyLoanStatus(loanAfterUndo, status -> status.getPendingApproval());
    }

    @Test
    void testLoanUndoDisbursement() {
        Long clientId = createClient("Alice", "Williams");
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 1);

        GetLoansLoanIdResponse loanBeforeUndo = getLoanDetails(loanId);
        verifyLoanStatus(loanBeforeUndo, status -> status.getActive());

        undoDisbursement(loanId);

        GetLoansLoanIdResponse loanAfterUndo = getLoanDetails(loanId);
        verifyLoanStatus(loanAfterUndo, status -> status.getWaitingForDisbursal());
    }

    @Test
    void testFourInstallmentLoan_CumulativeInterest() {
        Long clientId = createClient("Charlie", "Brown");
        Long productId = createLoanProduct(fourInstallmentsCumulative());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 4);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, status -> status.getActive());

        assertNotNull(loan.getRepaymentSchedule());
        assertNotNull(loan.getRepaymentSchedule().getPeriods());
        assertEquals(5, loan.getRepaymentSchedule().getPeriods().size());
    }

    @Test
    void testPartialRepayment() {
        Long clientId = createClient("David", "Miller");
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 1);

        Long repaymentId = addRepayment(loanId, repayment(500.0, todayDate));
        assertNotNull(repaymentId);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, status -> status.getActive());

        assertEquals(500.0, Utils.getDoubleValue(loan.getSummary().getTotalOutstanding()));
        assertEquals(500.0, Utils.getDoubleValue(loan.getSummary().getTotalRepayment()));
    }

    @Test
    void testUndoRepayment() {
        Long clientId = createClient("Eva", "Davis");
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = createApproveAndDisburseLoan(clientId, productId, todayDate, 1000.0, 1);

        Long repaymentId = addRepayment(loanId, repayment(1000.0, todayDate));
        assertNotNull(repaymentId);

        GetLoansLoanIdResponse loanAfterRepayment = getLoanDetails(loanId);
        verifyLoanStatus(loanAfterRepayment, status -> status.getClosedObligationsMet());

        undoRepayment(loanId, repaymentId, todayDate);

        GetLoansLoanIdResponse loanAfterUndo = getLoanDetails(loanId);
        verifyLoanStatus(loanAfterUndo, status -> status.getActive());

        LocalDate expectedRepaymentDate = Utils.getLocalDateOfTenant().plusMonths(1);
        validateRepaymentPeriod(loanAfterUndo, 1, expectedRepaymentDate, 1000.0, 0.0, 1000.0);
    }
}
