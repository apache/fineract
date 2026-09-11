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
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanAuditingIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanAuditingIntegrationTest.class);
    private static final String NEW_USER_PASSWORD = "A1b2c3d4e5f$";

    @Test
    public void checkAuditDates() throws InterruptedException {
        final Long staffId = new FeignStaffHelper(FineractFeignClientHelper.getFineractFeignClient()).createStaff().getResourceId();
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        final Long userId = FeignUserHelper.createUser(1L, staffId, username, NEW_USER_PASSWORD).getResourceId();

        LOG.info("-------------------------Creating Client---------------------------");

        final Long clientId = createClient();
        Assertions.assertNotNull(clientHelper.getClient(clientId));

        LOG.info("-------------------------Creating Loan---------------------------");
        final Long loanProductId = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, "2");
        OffsetDateTime now = Utils.getAuditDateTimeToCompare();

        final Long loanId = applyForLoanApplication(clientId, loanProductId, 10000.0, "10 July 2022", "11 July 2022");
        Assertions.assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LoanAuditFieldsData auditFieldsResponse = getAuditFields(loanId);

        OffsetDateTime createdDate = auditFieldsResponse.getCreatedDate();
        OffsetDateTime lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1L, auditFieldsResponse.getCreatedBy());
        assertEquals(1L, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now, createdDate, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(now, lastModifiedDate, ChronoUnit.MINUTES));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");

        // approve as the newly created user, so the audit fields record a different last modifier
        FineractFeignClient asNewUser = FineractFeignClientHelper.createNewFineractFeignClient(username, NEW_USER_PASSWORD);
        FeignLoanHelper newUserLoanHelper = new FeignLoanHelper(asNewUser);

        OffsetDateTime now2 = Utils.getAuditDateTimeToCompare();
        newUserLoanHelper.approveLoan(loanId, LoanRequestBuilders.approveLoan(10000.0, "11 July 2022"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        auditFieldsResponse = getAuditFields(loanId);

        OffsetDateTime createdDate2 = auditFieldsResponse.getCreatedDate();
        lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1L, auditFieldsResponse.getCreatedBy());
        assertTrue(DateUtils.isEqual(now, createdDate2, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(createdDate, createdDate2));

        assertEquals(userId, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now2, lastModifiedDate, ChronoUnit.MINUTES));
    }

    private LoanAuditFieldsData getAuditFields(Long loanId) {
        return ok(() -> fineractClient().defaultApi().getLoanAuditFields(loanId));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, Double principal, final String submittedOnDate,
            final String disbursementDate) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, submittedOnDate, principal, 6)//
                .expectedDisbursementDate(disbursementDate)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.FLAT);
        return applyForLoan(application);
    }

    private Long createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy,
            final String accountingRule) {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

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
