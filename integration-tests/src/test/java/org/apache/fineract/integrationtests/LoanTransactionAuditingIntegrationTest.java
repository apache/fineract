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

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanTransactionAuditingIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionAuditingIntegrationTest.class);
    private static Long clientId;

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        clientId = createClient("10 July 2022");
        Assertions.assertNotNull(clientHelper.getClient(clientId));
    }

    @Test
    public void checkAuditDates() throws InterruptedException {
        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        final Integer userId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, 1, staffId, username, "A1b2c3d4e5f$",
                "resourceId");

        LOG.info("-------------------------Creating Loan---------------------------");

        final Long loanProductId = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, "2");
        final Long loanId = applyForLoanApplication(clientId, loanProductId, "10000", "10 July 2022", "12 July 2022");
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

        Map<String, Object> auditFieldsResponse = getAuditFields(loanId, transactionId);

        OffsetDateTime createdDate = OffsetDateTime.parse((String) auditFieldsResponse.get(CREATED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime lastModifiedDate = OffsetDateTime.parse((String) auditFieldsResponse.get(LAST_MODIFIED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1, ((Number) auditFieldsResponse.get(CREATED_BY)).intValue());
        assertEquals(1, ((Number) auditFieldsResponse.get(LAST_MODIFIED_BY)).intValue());
        assertTrue(DateUtils.isEqual(now, createdDate, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(now, lastModifiedDate, ChronoUnit.MINUTES));

        Thread.sleep(2000);

        // Reverse using a different user
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization",
                "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(username, "A1b2c3d4e5f$"));
        org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper restTxHelper = new org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper(
                this.requestSpec, this.responseSpec);

        OffsetDateTime now2 = Utils.getAuditDateTimeToCompare();
        restTxHelper.reverseRepayment(loanId.intValue(), transactionId.intValue(), "11 July 2022");

        auditFieldsResponse = getAuditFields(loanId, transactionId);

        OffsetDateTime createdDate2 = OffsetDateTime.parse((String) auditFieldsResponse.get(CREATED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        lastModifiedDate = OffsetDateTime.parse((String) auditFieldsResponse.get(LAST_MODIFIED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1, ((Number) auditFieldsResponse.get(CREATED_BY)).intValue());
        assertTrue(DateUtils.isEqual(now, createdDate2, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(createdDate, createdDate2));

        assertEquals(userId, ((Number) auditFieldsResponse.get(LAST_MODIFIED_BY)).intValue());
        assertTrue(DateUtils.isEqual(now2, lastModifiedDate, ChronoUnit.MINUTES));
    }

    private Map<String, Object> getAuditFields(Long loanId, Long transactionId) {
        String json = FeignRawHttpHelper.get("/internal/loan/" + loanId + "/transaction/" + transactionId + "/audit");
        return new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {}.getType());
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, String principal, final String submittedOnDate,
            final String disbursementDate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("6") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("6") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsFlatBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(submittedOnDate) //
                .withRepaymentStrategy(LoanApplicationTestBuilder.DEFAULT_STRATEGY) //
                .build(clientId.toString(), loanProductId.toString(), null);
        return applyForLoanFromJson(loanApplicationJSON);
    }

    private Long createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy,
            final String accountingRule) {
        final org.apache.fineract.integrationtests.common.accounting.Account assetAccount = getAccounts().getLoansReceivableAccount();
        final org.apache.fineract.integrationtests.common.accounting.Account incomeAccount = getAccounts().getInterestIncomeAccount();
        final org.apache.fineract.integrationtests.common.accounting.Account expenseAccount = getAccounts().getChargeOffExpenseAccount();
        final org.apache.fineract.integrationtests.common.accounting.Account overpaymentAccount = getAccounts().getOverpaymentAccount();

        final String loanProductJSON = new LoanProductTestBuilder() //
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
                .withAccounting(accountingRule, new org.apache.fineract.integrationtests.common.accounting.Account[] { assetAccount,
                        incomeAccount, expenseAccount, overpaymentAccount })
                .build(null);
        return createLoanProductFromJson(loanProductJSON);
    }
}
