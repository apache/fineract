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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetLoanAccountLockResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class ClientLoanAccountLockIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanAccountLockIntegrationTest.class);

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    private static final String NONE = "1";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanAccountLockHelper loanAccountLockHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());
    }

    @Test
    public void checkRetrieveLockedLoanAccountsList() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "12,000.00", collaterals);
        HashMap loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "12,000.00");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_INLINE_COB_PROCESSING", "Sample error");

        GetLoanAccountLockResponse getLoanAccountLockResponse = clientHelper.retrieveLockedAccounts(0, 1000);
        Assertions.assertTrue(getLoanAccountLockResponse.getContent().size() > 0);
        Assertions.assertTrue(getLoanAccountLockResponse.getContent().stream()
                .anyMatch(loanAccountLock -> loanAccountLock.getLoanId().equals(Long.valueOf(loanID))));
    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
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
                .withCollaterals(collaterals).withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
