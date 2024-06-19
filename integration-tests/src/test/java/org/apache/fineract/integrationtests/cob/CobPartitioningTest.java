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
package org.apache.fineract.integrationtests.cob;

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.CobHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("rawtypes")
@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class CobPartitioningTest {

    public static final int N = 10;
    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;
    private static Account ASSET_ACCOUNT;
    private static Account FEE_PENALTY_ACCOUNT;
    private static Account TRANSFER_ACCOUNT;
    private static Account EXPENSE_ACCOUNT;
    private static Account INCOME_ACCOUNT;
    private static Account OVERPAYMENT_ACCOUNT;
    private static FinancialActivityAccountHelper FINANCIAL_ACTIVITY_ACCOUNT_HELPER;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static LocalDate TODAYS_DATE;

    @BeforeAll
    public static void setupInvestorBusinessStep() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        AccountHelper accountHelper = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
        FINANCIAL_ACTIVITY_ACCOUNT_HELPER = new FinancialActivityAccountHelper(REQUEST_SPEC);
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);

        TODAYS_DATE = Utils.getLocalDateOfTenant();
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");

        ASSET_ACCOUNT = accountHelper.createAssetAccount();
        FEE_PENALTY_ACCOUNT = accountHelper.createAssetAccount();
        TRANSFER_ACCOUNT = accountHelper.createAssetAccount();
        EXPENSE_ACCOUNT = accountHelper.createExpenseAccount();
        INCOME_ACCOUNT = accountHelper.createIncomeAccount();
        OVERPAYMENT_ACCOUNT = accountHelper.createLiabilityAccount();

        setProperFinancialActivity(TRANSFER_ACCOUNT);
    }

    private static void setProperFinancialActivity(Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = FINANCIAL_ACTIVITY_ACCOUNT_HELPER.getAllFinancialActivityAccounts();
        financialMappings.forEach(mapping -> FINANCIAL_ACTIVITY_ACCOUNT_HELPER.deleteFinancialActivityAccount(mapping.getId()));
        FINANCIAL_ACTIVITY_ACCOUNT_HELPER.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                .glAccountId((long) transferAccount.getAccountID()));
    }

    @Test
    public void testLoanCOBPartitioningQuery() throws InterruptedException {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                    GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate("2020-03-02");

            List<Integer> loanIds = new CopyOnWriteArrayList<>();

            // Let's create 1, 2, ..., N-1, N loans
            final CountDownLatch createLatch = new CountDownLatch(N);
            Integer loanProductID = createLoanProduct();
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                futures.add(executorService.submit(() -> {
                    Integer clientID = createClient();
                    Integer loanID = createLoanForClient(clientID, loanProductID);
                    loanIds.add(loanID);
                    createLatch.countDown();
                }));
            }
            waitForFutures(futures, createLatch);
            futures.clear();

            // Force close loans 3, 4, ... , N-3, N-2
            Collections.sort(loanIds);
            final CountDownLatch closeLatch = new CountDownLatch(N - 4);
            for (int i = 2; i < N - 2; i++) {
                final int idx = i;
                futures.add(executorService.submit(() -> {
                    LOAN_TRANSACTION_HELPER.forecloseLoan("02 March 2020", loanIds.get(idx));
                    closeLatch.countDown();
                }));
            }
            futures.forEach(future -> {
                try {
                    future.get(); // turn any possible async failures into errors
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            closeLatch.await();

            // Let's retrieve the partitions
            List<Map<String, Object>> cobPartitions = CobHelper.getCobPartitions(REQUEST_SPEC, RESPONSE_SPEC, 3, "");
            log.info("\nLoans created: {},\nRetrieved partitions: {}", loanIds, cobPartitions);
            Assertions.assertEquals(2, cobPartitions.size());

            Assertions.assertEquals(0, cobPartitions.get(0).get("pageNo"));
            Assertions.assertEquals(loanIds.get(0), cobPartitions.get(0).get("minId"));
            Assertions.assertEquals(loanIds.get(8), cobPartitions.get(0).get("maxId"));
            Assertions.assertEquals(3, cobPartitions.get(0).get("count"));

            Assertions.assertEquals(1, cobPartitions.get(1).get("pageNo"));
            Assertions.assertEquals(loanIds.get(9), cobPartitions.get(1).get("minId"));
            Assertions.assertEquals(loanIds.get(9), cobPartitions.get(1).get("maxId"));
            Assertions.assertEquals(1, cobPartitions.get(1).get("count"));

            executorService.shutdown();
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    private static void waitForFutures(List<Future<?>> futures, CountDownLatch createLatch) throws InterruptedException {
        futures.forEach(future -> {
            try {
                future.get(); // turn any possible async failures into errors
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        createLatch.await();
    }

    private void setInitialBusinessDate(String date) {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.TRUE);
        BusinessDateHelper.updateBusinessDate(REQUEST_SPEC, RESPONSE_SPEC, BUSINESS_DATE, LocalDate.parse(date));
        GlobalConfigurationHelper.updateValueForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "10", "0");
    }

    private void cleanUpAndRestoreBusinessDate() {
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        REQUEST_SPEC.header("Fineract-Platform-TenantId", "default");
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        BusinessDateHelper.updateBusinessDate(REQUEST_SPEC, RESPONSE_SPEC, BUSINESS_DATE, TODAYS_DATE);
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.FALSE);
        GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, false);
    }

    @NotNull
    private Integer createClient() {
        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(clientID);
        return clientID;
    }

    private Integer createLoanProduct() {
        Integer overdueFeeChargeId = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
        Assertions.assertNotNull(overdueFeeChargeId);

        Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);
        return loanProductID;
    }

    @NotNull
    private Integer createLoanForClient(Integer clientID, Integer loanProductID) {

        HashMap loanStatusHashMap;

        Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), "1 March 2020");

        Assertions.assertNotNull(loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("01 March 2020", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        return loanID;
    }

    private Integer createLoanProduct(final String chargeId) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).build(chargeId);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String date) {
        List<HashMap> collaterals = new ArrayList<>();
        Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC, clientID, collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(date).withSubmittedOnDate(date).withCollaterals(collaterals)
                .build(clientID, loanProductID, null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

}
