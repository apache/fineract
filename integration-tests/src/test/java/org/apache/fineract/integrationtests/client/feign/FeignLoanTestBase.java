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
package org.apache.fineract.integrationtests.client.feign;

import java.time.LocalDate;
import java.util.function.Function;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanProductTemplates;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestAccounts;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;

public abstract class FeignLoanTestBase extends FeignIntegrationTest implements LoanProductTemplates {

    protected static FeignAccountHelper accountHelper;
    protected static FeignLoanHelper loanHelper;
    protected static FeignTransactionHelper transactionHelper;
    protected static FeignJournalEntryHelper journalHelper;
    protected static FeignBusinessDateHelper businessDateHelper;
    protected static FeignClientHelper clientHelper;
    protected static LoanTestAccounts accounts;

    @BeforeAll
    public static void setupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        accountHelper = new FeignAccountHelper(client);
        loanHelper = new FeignLoanHelper(client);
        transactionHelper = new FeignTransactionHelper(client);
        journalHelper = new FeignJournalEntryHelper(client);
        businessDateHelper = new FeignBusinessDateHelper(client);
        clientHelper = new FeignClientHelper(client);
    }

    protected LoanTestAccounts getAccounts() {
        if (accounts == null) {
            accounts = new LoanTestAccounts(accountHelper);
        }
        return accounts;
    }

    @Override
    public Long getAssetAccountId(String accountName) {
        return getAccounts().getAssetAccountId(accountName);
    }

    @Override
    public Long getLiabilityAccountId(String accountName) {
        return getAccounts().getLiabilityAccountId(accountName);
    }

    @Override
    public Long getIncomeAccountId(String accountName) {
        return getAccounts().getIncomeAccountId(accountName);
    }

    @Override
    public Long getExpenseAccountId(String accountName) {
        return getAccounts().getExpenseAccountId(accountName);
    }

    protected Long createClient(String firstName, String lastName) {
        return clientHelper.createClient(firstName, lastName);
    }

    protected Long createLoanProduct(PostLoanProductsRequest request) {
        return loanHelper.createLoanProduct(request);
    }

    protected Long applyForLoan(PostLoansRequest request) {
        return loanHelper.applyForLoan(request);
    }

    protected Long approveLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.approveLoan(loanId, request);
    }

    protected Long disburseLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.disburseLoan(loanId, request);
    }

    protected GetLoansLoanIdResponse getLoanDetails(Long loanId) {
        return loanHelper.getLoanDetails(loanId);
    }

    protected void undoApproval(Long loanId) {
        loanHelper.undoApproval(loanId);
    }

    protected void undoDisbursement(Long loanId) {
        loanHelper.undoDisbursement(loanId);
    }

    protected Long addRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addRepayment(loanId, request);
    }

    protected Long addInterestWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addInterestWaiver(loanId, request);
    }

    protected Long chargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.chargeOff(loanId, request);
    }

    protected Long addChargeback(Long loanId, Long transactionId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addChargeback(loanId, transactionId, request);
    }

    protected void undoRepayment(Long loanId, Long transactionId, String transactionDate) {
        transactionHelper.undoRepayment(loanId, transactionId, transactionDate);
    }

    protected void verifyJournalEntries(Long loanId, LoanTestData.Journal... expectedEntries) {
        journalHelper.verifyJournalEntries(loanId, expectedEntries);
    }

    protected void verifyJournalEntriesSequentially(Long loanId, LoanTestData.Journal... expectedEntries) {
        journalHelper.verifyJournalEntriesSequentially(loanId, expectedEntries);
    }

    protected void runAt(String date, Runnable action) {
        businessDateHelper.runAt(date, action);
    }

    protected void updateBusinessDate(String type, String date) {
        businessDateHelper.updateBusinessDate(type, date);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, principalPaid, principalOutstanding, 0.0,
                0.0);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double paidInAdvance, double paidLate) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, principalPaid, principalOutstanding,
                paidInAdvance, paidLate);
    }

    protected void verifyLoanStatus(GetLoansLoanIdResponse loanDetails, Function<GetLoansLoanIdStatus, Boolean> extractor) {
        LoanTestValidators.verifyLoanStatus(loanDetails, extractor);
    }

    protected Long createApproveAndDisburseLoan(Long clientId, Long productId, String date, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = LoanRequestBuilders.applyLoan(clientId, productId, date, principal, numberOfRepayments);
        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = LoanRequestBuilders.approveLoan(principal, date);
        approveLoan(loanId, approveRequest);

        PostLoansLoanIdRequest disburseRequest = LoanRequestBuilders.disburseLoan(principal, date);
        disburseLoan(loanId, disburseRequest);

        return loanId;
    }

    protected Long createApprovedLoan(Long clientId, Long productId, String date, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = LoanRequestBuilders.applyLoan(clientId, productId, date, principal, numberOfRepayments);
        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = LoanRequestBuilders.approveLoan(principal, date);
        approveLoan(loanId, approveRequest);

        return loanId;
    }

    protected LoanTestData.Journal debit(Long glAccountId, double amount) {
        return LoanTestData.Journal.debit(glAccountId, amount);
    }

    protected LoanTestData.Journal credit(Long glAccountId, double amount) {
        return LoanTestData.Journal.credit(glAccountId, amount);
    }

    protected PostLoansLoanIdTransactionsRequest repayment(double amount, String date) {
        return LoanRequestBuilders.repayLoan(amount, date);
    }

    protected PostLoansLoanIdTransactionsRequest waiveInterest(double amount, String date) {
        return LoanRequestBuilders.waiveInterest(amount, date);
    }

    protected PostLoansLoanIdTransactionsRequest chargeOff(String date) {
        return LoanRequestBuilders.chargeOff(date);
    }
}
