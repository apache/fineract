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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.LoanAuditFieldsData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanTransactionAuditingIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionAuditingIntegrationTest.class);
    private static final String NEW_USER_PASSWORD = "A1b2c3d4e5f$";
    private static Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient("10 July 2022");
        Assertions.assertNotNull(clientHelper.getClient(clientId));
    }

    @Test
    public void checkAuditDates() throws InterruptedException {
        final Long staffId = new FeignStaffHelper(FineractFeignClientHelper.getFineractFeignClient()).createStaff().getResourceId();
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        final Long userId = FeignUserHelper.createUser(1L, staffId, username, NEW_USER_PASSWORD).getResourceId();

        LOG.info("-------------------------Creating Loan---------------------------");

        final Long loanProductId = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, "2");
        final Long loanId = applyForLoanApplication(clientId, loanProductId, 10000.0, "10 July 2022", "12 July 2022");
        Assertions.assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        approveLoan(loanId, approveLoanRequest(10000.0, "11 July 2022"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        disburseLoanWithNetDisbursalAmount(loanId, "11 July 2022", "10000");
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);

        OffsetDateTime now = Utils.getAuditDateTimeToCompare();
        makeLoanRepayment(loanId, "Repayment", "11 July 2022", 100.0);

        // Get the transaction id from the loan details
        var loanDetails = getLoanDetails(loanId);
        Long transactionId = loanDetails.getTransactions().stream()
                .filter(t -> "Repayment".equalsIgnoreCase(t.getType().getValue()) && !Boolean.TRUE.equals(t.getManuallyReversed()))
                .findFirst().orElseThrow().getId();

        LoanAuditFieldsData auditFieldsResponse = getAuditFields(loanId, transactionId);

        OffsetDateTime createdDate = auditFieldsResponse.getCreatedDate();
        OffsetDateTime lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1L, auditFieldsResponse.getCreatedBy());
        assertEquals(1L, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now, createdDate, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(now, lastModifiedDate, ChronoUnit.MINUTES));

        Thread.sleep(2000);

        // Reverse using a different user
        FineractFeignClient asNewUser = FineractFeignClientHelper.createNewFineractFeignClient(username, NEW_USER_PASSWORD);
        FeignTransactionHelper newUserTransactionHelper = new FeignTransactionHelper(asNewUser);

        OffsetDateTime now2 = Utils.getAuditDateTimeToCompare();
        newUserTransactionHelper.reverseLoanTransaction(loanId, transactionId, "11 July 2022");

        auditFieldsResponse = getAuditFields(loanId, transactionId);

        OffsetDateTime createdDate2 = auditFieldsResponse.getCreatedDate();
        lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1L, auditFieldsResponse.getCreatedBy());
        assertTrue(DateUtils.isEqual(now, createdDate2, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(createdDate, createdDate2));

        assertEquals(userId, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now2, lastModifiedDate, ChronoUnit.MINUTES));
    }

    private LoanAuditFieldsData getAuditFields(Long loanId, Long transactionId) {
        return ok(() -> fineractClient().defaultApi().getLoanTransactionAuditFields(loanId, transactionId));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, Double principal, final String submittedOnDate,
            final String disbursementDate) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, submittedOnDate, principal, 6)//
                .expectedDisbursementDate(disbursementDate)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.FLAT);
        return loanHelper.applyForLoan(application).getLoanId();
    }

    private Long createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy,
            final String accountingRule) {
        final Account assetAccount = getAccounts().getLoansReceivableAccount();
        final Account incomeAccount = getAccounts().getInterestIncomeAccount();
        final Account expenseAccount = getAccounts().getChargeOffExpenseAccount();
        final Account overpaymentAccount = getAccounts().getOverpaymentAccount();

        return createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(repaymentStrategy) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails(digitsAfterDecimal, inMultiplesOf)
                .withAccounting(accountingRule, new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .buildRequest(null));
    }
}
