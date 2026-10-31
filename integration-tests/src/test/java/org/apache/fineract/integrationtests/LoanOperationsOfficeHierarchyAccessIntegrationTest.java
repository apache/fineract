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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.InterestPauseRequestDto;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2982: loan operations outside the FINERACT-2639 set (contract termination, buy-down fee, re-age and interest
 * pause) are only allowed for users whose office is the loan's office or one of its parents.
 */
public class LoanOperationsOfficeHierarchyAccessIntegrationTest extends FeignLoanTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String NOT_AUTHORIZED_ERROR_CODE = "error.msg.not.authorized";
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    private static final Map<String, Boolean> CONTRACT_TERMINATION_PERMISSIONS = Map.of("CONTRACT_TERMINATION_LOAN", true, "READ_LOAN",
            true);
    private static final Map<String, Boolean> BUY_DOWN_FEE_PERMISSIONS = Map.of("BUYDOWNFEE_LOAN", true, "READ_LOAN", true);
    private static final Map<String, Boolean> RE_AGE_PERMISSIONS = Map.of("REAGE_LOAN", true, "READ_LOAN", true);
    /** The interest pause API checks the read permission "UPDATE LOAN", which only ALL_FUNCTIONS_READ satisfies. */
    private static final Map<String, Boolean> INTEREST_PAUSE_PERMISSIONS = Map.of("CREATE_INTEREST_PAUSE", true, "ALL_FUNCTIONS_READ",
            true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testContractTerminationIsRestrictedToUserOfficeHierarchy() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        Long loanOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(loanOfficeId);
        Long productId = createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, "1 January 2024", 500.0, 7.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
        });

        runAt("2 February 2024", () -> executeInlineCOB(loanIdRef.get()));

        runAt("3 February 2024", () -> {
            Long loanId = loanIdRef.get();
            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, CONTRACT_TERMINATION_PERMISSIONS);
            FineractFeignClient loanOfficeUser = createUserInOffice(loanOfficeId, CONTRACT_TERMINATION_PERMISSIONS);

            assertRejected(() -> terminate(siblingOfficeUser, loanId));

            assertNotNull(ok(() -> terminate(loanOfficeUser, loanId)).getResourceId(),
                    "User from the loan's office must be able to terminate the contract");
        });
    }

    @Test
    public void testBuyDownFeeIsRestrictedToUserOfficeHierarchy() {
        Long loanOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(loanOfficeId);
        Long productId = createLoanProduct(create4IProgressive().enableBuyDownFee(true)
                .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue()).merchantBuyDownFee(true)
                .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue())
                .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue()));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, "1 January 2024", 500.0, 7.0, 3, null);
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, BUY_DOWN_FEE_PERMISSIONS);
            FineractFeignClient loanOfficeUser = createUserInOffice(loanOfficeId, BUY_DOWN_FEE_PERMISSIONS);
            PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("1 January 2024").locale(LoanTestData.LOCALE).transactionAmount(50.0);

            assertRejected(() -> loanTransactionCommand(siblingOfficeUser, loanId, request, "buyDownFee"));

            assertNotNull(ok(() -> loanTransactionCommand(loanOfficeUser, loanId, request, "buyDownFee")).getResourceId(),
                    "User from the loan's office must be able to add a buy-down fee");
        });
    }

    @Test
    public void testReAgeIsRestrictedToUserOfficeHierarchy() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        Long loanOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(loanOfficeId);
        Long productId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                .numberOfRepayments(3).repaymentEvery(1).installmentAmountInMultiplesOf(null).enableDownPayment(true)
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25)).enableAutoRepaymentForDownPayment(true)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()));

        runAt("01 January 2023", () -> {
            PostLoansRequest application = applyLoanRequest(clientId, productId, "01 January 2023", 1250.0, 3)
                    .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .repaymentEvery(1).loanTermFrequency(3).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS);
            Long loanId = applyForLoan(application);
            approveLoan(loanId, approveLoanRequest(1250.0, "01 January 2023"));
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");
            loanIdRef.set(loanId);
        });

        runAt("12 April 2023", () -> {
            Long loanId = loanIdRef.get();
            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, RE_AGE_PERMISSIONS);
            FineractFeignClient loanOfficeUser = createUserInOffice(loanOfficeId, RE_AGE_PERMISSIONS);
            PostLoansLoanIdTransactionsRequest request = LoanRequestBuilders.reAge("12 April 2023", RepaymentFrequencyType.MONTHS_STRING, 1,
                    4);

            assertRejected(() -> loanTransactionCommand(siblingOfficeUser, loanId, request, "reAge"));

            assertNotNull(ok(() -> loanTransactionCommand(loanOfficeUser, loanId, request, "reAge")).getResourceId(),
                    "User from the loan's office must be able to re-age the loan");
        });
    }

    @Test
    public void testInterestPauseIsRestrictedToUserOfficeHierarchy() {
        Long loanOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(loanOfficeId);
        Long productId = createLoanProduct(create4IProgressive().numberOfRepayments(12).interestRatePerPeriod(18.0).principal(10000.0));
        Long loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, productId, "01 January 2023", 10000.0, 18.0, 12,
                r -> r.repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.DAYS).loanTermFrequency(12)
                        .loanTermFrequencyType(RepaymentFrequencyType.DAYS)));
        approveLoan(loanId, approveLoanRequest(10000.0, "01 January 2023"));
        disburseLoan(loanId, BigDecimal.valueOf(10000.0), "01 January 2023");

        FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, INTEREST_PAUSE_PERMISSIONS);
        FineractFeignClient loanOfficeUser = createUserInOffice(loanOfficeId, INTEREST_PAUSE_PERMISSIONS);
        InterestPauseRequestDto pause = new InterestPauseRequestDto().startDate("2023-01-01").endDate("2023-01-12").dateFormat("yyyy-MM-dd")
                .locale("en");

        assertRejected(() -> siblingOfficeUser.loanInterestPause().createLoanInterestPause(loanId, pause));

        assertNotNull(ok(() -> loanOfficeUser.loanInterestPause().createLoanInterestPause(loanId, pause)).getResourceId(),
                "User from the loan's office must be able to create an interest pause");
    }

    private static void assertRejected(Runnable call) {
        CallFailedRuntimeException failure = FeignCalls.failVoid(call);
        assertEquals(403, failure.getStatus());
        assertEquals(NOT_AUTHORIZED_ERROR_CODE, failure.getUserMessageGlobalisationCode());
        assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                "Expected office hierarchy error but was: " + failure.getResponseBody());
    }

    private static PostLoansLoanIdResponse terminate(FineractFeignClient client, Long loanId) {
        return client.loans().handleCommandsLoan(loanId,
                new PostLoansLoanIdRequest().note("Contract Termination").externalId(Utils.randomStringGenerator("", 20)),
                Map.of("command", "contractTermination"));
    }

    private static PostLoansLoanIdTransactionsResponse loanTransactionCommand(FineractFeignClient client, Long loanId,
            PostLoansLoanIdTransactionsRequest request, String command) {
        return client.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", command));
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(new PostClientsRequest().officeId(officeId).legalFormId(1L)
                .firstname(Utils.randomFirstNameGenerator()).lastname(Utils.randomLastNameGenerator()).active(true)
                .activationDate(ClientHelper.DEFAULT_DATE).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE))
                .getClientId();
    }

    private FineractFeignClient createUserInOffice(Long officeId, Map<String, Boolean> permissions) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
