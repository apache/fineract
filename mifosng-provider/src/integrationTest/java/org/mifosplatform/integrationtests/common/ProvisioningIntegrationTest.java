/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.provisioning.ProvisioningHelper;
import org.mifosplatform.integrationtests.common.provisioning.ProvisioningTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProvisioningIntegrationTest {

    private static final String NONE = "1";
    private final static int LOANPRODUCTS_SIZE = 10;
    private final static int LOANS_SIZE = 5;

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private AccountHelper accountHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testCreateProvisioningCriteria() {/*
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper(requestSpec, responseSpec);
        ArrayList<Integer> loanProducts = new ArrayList<>(LOANPRODUCTS_SIZE);
        List<Integer> loans = new ArrayList<>();
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        for (int i = 0; i < LOANPRODUCTS_SIZE; i++) {
            final Integer loanProductID = createLoanProduct(false, NONE);
            loanProducts.add(loanProductID);
            Assert.assertNotNull(loanProductID);
            final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
            loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
            System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
            loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
            loans.add(loanID);
            Assert.assertNotNull(loanID);
        }

        ArrayList categories = transactionHelper.retrieveAllProvisioningCategories();
        Account liability = accountHelper.createLiabilityAccount() ;
        Account expense = accountHelper.createExpenseAccount() ;
        String json = ProvisioningHelper.createProvisioingCriteriaJson(loanProducts, categories, liability, expense);
        Integer criteriaId = transactionHelper.createProvisioningCriteria(json);
        Assert.assertNotNull(criteriaId);

        String provisioningEntryJson = ProvisioningHelper.createProvisioningEntryJson();
        Integer provisioningEntryId = transactionHelper.createProvisioningEntries(provisioningEntryJson);
        Assert.assertNotNull(provisioningEntryId);
    */}

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1,00,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
