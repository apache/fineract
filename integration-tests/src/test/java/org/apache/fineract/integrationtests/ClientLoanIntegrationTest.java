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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdCollateralData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargeData;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdCollateral;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.SavingsAccountSummaryData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountTransferHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.LoanChargeCommandsApi;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Application Repayments Schedule, loan charges, penalties, loan
 * repayments and verifying accounting transactions
 */
@Order(2)
public class ClientLoanIntegrationTest extends FeignLoanTestBase {

    private static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanIntegrationTest.class);

    private static final Integer NONE = AccountingRuleType.NONE.getValue();
    private static final Integer CASH_BASED = AccountingRuleType.CASH_BASED.getValue();
    private static final Integer ACCRUAL_PERIODIC = AccountingRuleType.ACCRUAL_PERIODIC.getValue();
    private static final Integer ACCRUAL_UPFRONT = AccountingRuleType.ACCRUAL_UPFRONT.getValue();

    /** The loan/product JSON builders this test replaced used {@code en_GB}; keep it so number parsing is unchanged. */
    private static final String LOCALE = "en_GB";
    private static final String OVERRIDE_MESSAGE = "Loan overrode the product's %s";
    /** The interoperation repayment body was built with plain {@code en}, unlike the {@code en_GB} used elsewhere. */
    private static final String INTEROP_LOCALE = "en";

    /** Mirrors the legacy {@code SavingsAccountHelper} date constants. */
    private static final String SAVINGS_TRANSACTION_DATE = "01 March 2013";
    private static final String SAVINGS_CREATED_DATE = "08 January 2013";
    private static final String SAVINGS_CREATED_DATE_PLUS_ONE = "09 January 2013";

    private static final String DEFAULT_STRATEGY = "mifos-standard-strategy";
    private static final String RBI_INDIA_STRATEGY = "rbi-india-strategy";
    private static final String INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY = "interest-principal-penalties-fees-order-strategy";
    private static final String DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY = "due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy";
    private static final String DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY = "due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy";

    /**
     * GL accounts and helpers are created in {@link #setupClientLoanTest()} rather than in field initializers, because
     * the inherited Feign helpers are only assigned in the base class {@code @BeforeAll}, which runs after this class
     * is initialized.
     */
    // asset
    private static Account loansReceivableAccount;
    private static Account interestFeeReceivableAccount;
    private static Account suspenseAccount;
    // liability
    private static Account suspenseClearingAccount;
    private static Account overpaymentAccount;
    // income
    private static Account interestIncomeAccount;
    private static Account feeIncomeAccount;
    private static Account feeChargeOffAccount;
    private static Account recoveriesAccount;
    private static Account interestIncomeChargeOffAccount;
    // expense
    private static Account creditLossBadDebtAccount;
    private static Account creditLossBadDebtFraudAccount;
    private static Account writtenOffAccount;
    private static Account goodwillExpenseAccount;

    private static FeignCollateralHelper collateralHelper;
    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignAccountTransferHelper accountTransferHelper;

    @BeforeAll
    public static void setupClientLoanTest() {
        loansReceivableAccount = accountHelper.createAssetAccount();
        interestFeeReceivableAccount = accountHelper.createAssetAccount();
        suspenseAccount = accountHelper.createAssetAccount();

        suspenseClearingAccount = accountHelper.createLiabilityAccount();
        overpaymentAccount = accountHelper.createLiabilityAccount();

        interestIncomeAccount = accountHelper.createIncomeAccount();
        feeIncomeAccount = accountHelper.createIncomeAccount();
        feeChargeOffAccount = accountHelper.createIncomeAccount();
        recoveriesAccount = accountHelper.createIncomeAccount();
        interestIncomeChargeOffAccount = accountHelper.createIncomeAccount();

        creditLossBadDebtAccount = accountHelper.createExpenseAccount();
        creditLossBadDebtFraudAccount = accountHelper.createExpenseAccount();
        writtenOffAccount = accountHelper.createExpenseAccount();
        goodwillExpenseAccount = accountHelper.createExpenseAccount();

        collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());
        savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
        savingsProductHelper = new FeignSavingsProductHelper(FineractFeignClientHelper.getFineractFeignClient());
        accountTransferHelper = new FeignAccountTransferHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long clientID = createClient();

        verifyClientCreatedOnServer(clientID);

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, NONE);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, null, null, "12,000.00", collaterals);
        final List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);
    }

    @Test
    public void validateClientLoanWithUniqueExternalId() {
        // Given
        final Long clientID = createClient();
        final Long loanProductID = createLoanProduct(false, NONE);

        final String externalId = UUID.randomUUID().toString();

        // When
        final Long loanID = applyForLoanApplicationWithExternalId(clientID, loanProductID, "12,000.00", externalId);

        // Then
        assertNotNull(loanID);
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyForLoanApplicationWithExternalId(clientID, loanProductID, "12,000.00", externalId));
        assertEquals(403, exception.getStatus());
    }

    @Test
    public void testAddingLoanChargeIncludesLoanIdInTheResponse() {
        // given
        Long clientId = createClient();
        Long loanProductId = createLoanProduct(false, NONE);
        Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        List<PostLoansRequestCollateralData> collaterals = List.of(collateral(clientCollateralId, BigDecimal.ONE));

        Long chargeId = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0).getResourceId();
        List<PostLoansRequestChargeData> charges = List.of(charge(chargeId, 1.0, null));
        // when
        Long loanId = applyForLoanApplication(clientId, loanProductId, charges, null, "12,000.00", collaterals);
        // then
        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanId);
        Long loanChargeId = loanCharges.get(0).getId();
        GetLoansLoanIdChargesChargeIdResponse loanChargeDetail = getLoanCharge(loanId, loanChargeId);
        assertEquals(loanId, loanChargeDetail.getLoanId());
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE() {
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flatDisbursement = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.FLAT, 100.0).getResourceId();

        Long amountPercentage = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0).getResourceId();
        addCharges(charges, amountPercentage, 1.0, null);
        Long amountPlusInterestPercentage = chargesHelper
                .createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0).getResourceId();
        addCharges(charges, amountPlusInterestPercentage, 1.0, null);
        Long interestPercentage = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 1.0)
                .getResourceId();
        addCharges(charges, interestPercentage, 1.0, null);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.getFeeChargesDue()));

        addDisbursementCharge(loanID, flatDisbursement, 100.0);
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(disbursementDetail.getFeeChargesDue()));

        updateLoanCharge(loanID, getloanCharge(amountPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(interestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(amountPlusInterestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(flatDisbursement, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(150.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(disbursementDetail.getFeeChargesDue()));

        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(disbursementDetail.getFeeChargesDue()));

        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, flatDisbursement, 1.0), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.getFeeChargesDue()));

        charges.clear();
        addCharges(charges, flatDisbursement, 100.0, null);
        modifyLoanApplication(loanID, null, updateLoanRequest(clientID, loanProductID, toUpdateCharges(charges), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(disbursementDetail.getFeeChargesDue()));

        deleteLoanCharge(loanID, getloanCharge(flatDisbursement, loanCharges).getId());
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);
        Assertions.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0.0", String.valueOf(disbursementDetail.getFeeChargesDue()));

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE_WITH_AMOUNT_CHANGE() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long amountPercentage = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0).getResourceId();
        addCharges(charges, amountPercentage, 1.0, null);
        Long amountPlusInterestPercentage = chargesHelper
                .createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0).getResourceId();
        addCharges(charges, amountPlusInterestPercentage, 1.0, null);
        Long interestPercentage = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 1.0)
                .getResourceId();
        addCharges(charges, interestPercentage, 1.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");
        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        // DISBURSE
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000"));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "100.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "0.0", "5.05", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "0.0", "105.05", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.getFeeChargesDue()));

    }

    @Test
    public void testLoanDisbursedTodayIsRetrieved() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, 5, null);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String LOAN_DISBURSEMENT_DATE = "2 June 2014";

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        // DISBURSE on todays date so that loan can't be in arrears
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID, "10000"));
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        // Test added because loans created without arrears were failing to be retrieved (associations=all) due to inner
        // join on m_loan_arrears_aging (now left join)
        Assertions.assertNotNull(loanDetails, "Empty Loan Details");
        Assertions.assertNotNull(loanDetails.getId(), "No id Found");

    }

    @Test
    public void testLoanCharges_SPECIFIED_DUE_DATE_FEE() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flat = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false).getResourceId();
        Long flatAccTransfer = chargesHelper.createLoanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType.FLAT, 100.0, false)
                .getResourceId();

        Long amountPercentage = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false)
                .getResourceId();
        addCharges(charges, amountPercentage, 1.0, "29 September 2011");
        Long amountPlusInterestPercentage = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentage, 1.0, "29 September 2011");
        Long interestPercentage = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 1.0, false)
                .getResourceId();
        addCharges(charges, interestPercentage, 1.0, "29 September 2011");

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(firstInstallment.getFeeChargesDue()));

        addLoanCharge(loanID, flat, "29 September 2011", 100.0);
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);

        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(firstInstallment.getFeeChargesDue()));

        updateLoanCharge(loanID, getloanCharge(amountPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(interestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(amountPlusInterestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(flat, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(150.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(firstInstallment.getFeeChargesDue()));

        final Long savingsId = openSavingsAccountActivatedOnTransactionDate(clientID, MINIMUM_OPENING_BALANCE);
        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), savingsId, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(firstInstallment.getFeeChargesDue()));

        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, 1.0), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(firstInstallment.getFeeChargesDue()));

        charges.clear();
        addCharges(charges, flat, 100.0, "29 September 2011");
        modifyLoanApplication(loanID, null, updateLoanRequest(clientID, loanProductID, toUpdateCharges(charges), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesDue()));

        deleteLoanCharge(loanID, getloanCharge(flat, loanCharges).getId());
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        Assertions.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0", String.valueOf(firstInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        addLoanCharge(loanID, flatAccTransfer, "29 September 2011", 100.0);
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesDue()));

        // DISBURSE
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000"));

        addLoanCharge(loanID, amountPercentage, "29 September 2011", 1.0);
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.getFeeChargesDue()));

        waiveWholeLoanCharge(loanID, getloanCharge(amountPercentage, loanCharges).getId());
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.getFeeChargesDue()));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesOutstanding()));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesWaived()));

        payLoanCharge(loanID, getloanCharge(flatAccTransfer, loanCharges).getId(), new PostLoansLoanIdChargesChargeIdRequest()
                .transactionDate(SAVINGS_TRANSACTION_DATE).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "0.0", "100.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.getFeeChargesDue()));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesWaived()));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.getFeeChargesPaid()));
        validateNumberForEqual("0.0", String.valueOf(firstInstallment.getFeeChargesOutstanding()));
    }

    @Test
    public void testLoanCharges_INSTALMENT_FEE() {
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flat = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        Long flatAccTransfer = chargesHelper.createLoanInstallmentAccountTransferCharge(ChargeCalculationType.FLAT, 50.0, false)
                .getResourceId();

        Long amountPercentage = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false)
                .getResourceId();
        addCharges(charges, amountPercentage, 1.0, "29 September 2011");
        Long amountPlusInterestPercentage = chargesHelper
                .createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentage, 1.0, "29 September 2011");
        Long interestPercentage = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 1.0, false)
                .getResourceId();
        addCharges(charges, interestPercentage, 1.0, "29 September 2011");

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);

        Float totalPerOfAmout = 0F;
        Float totalPerOfAmoutPlusInt = 0F;
        Float totalPerOfint = 0F;
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            Float principalDue = installment.getPrincipalDue().floatValue();
            Float interestDue = installment.getInterestDue().floatValue();
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.getFeeChargesDue()));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        addLoanCharge(loanID, new PostLoansLoanIdChargesRequest().chargeId(flat).amount(50.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            Float principalDue = installment.getPrincipalDue().floatValue();
            Float interestDue = installment.getInterestDue().floatValue();
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 50;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.getFeeChargesDue()));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        updateLoanCharge(loanID, getloanCharge(amountPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(interestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(amountPlusInterestPercentage, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        updateLoanCharge(loanID, getloanCharge(flat, loanCharges).getId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(100.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            Float principalDue = installment.getPrincipalDue().floatValue();
            Float interestDue = installment.getInterestDue().floatValue();
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.getFeeChargesDue()));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        final Long savingsId = openSavingsAccountActivatedOnTransactionDate(clientID, MINIMUM_OPENING_BALANCE);
        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), savingsId, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            Float principalDue = installment.getPrincipalDue().floatValue();
            Float interestDue = installment.getInterestDue().floatValue();
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.getFeeChargesDue()));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        modifyLoanApplication(loanID, null,
                updateLoanRequest(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, 1.0), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            Float principalDue = installment.getPrincipalDue().floatValue();
            Float interestDue = installment.getInterestDue().floatValue();
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.getFeeChargesDue()));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        charges.clear();
        addCharges(charges, flat, 50.0, "29 September 2011");
        modifyLoanApplication(loanID, null, updateLoanRequest(clientID, loanProductID, toUpdateCharges(charges), null, collaterals));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("50", String.valueOf(installment.getFeeChargesDue()));
        }
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        deleteLoanCharge(loanID, getloanCharge(flat, loanCharges).getId());
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("0", String.valueOf(installment.getFeeChargesDue()));
        }

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        addLoanCharge(loanID,
                new PostLoansLoanIdChargesRequest().chargeId(flatAccTransfer).amount(100.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("100", String.valueOf(installment.getFeeChargesDue()));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");

        // DISBURSE
        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000");
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        verifyLoanIsActive(loanStatusHashMap);

        addLoanCharge(loanID, new PostLoansLoanIdChargesRequest().chargeId(flat).amount(50.0).locale(LOCALE).dateFormat(DATETIME_PATTERN));

        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.getFeeChargesDue()));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        Long waivePeriodnum = 1L;
        final Long waivedChargeId = waiveLoanCharge(loanID, getloanCharge(flat, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(waivePeriodnum).locale(LOCALE)).getResourceId();

        // Get loan transaction details
        List<GetLoansLoanIdTransactions> loanTransactions = getLoanDetails(loanID).getTransactions();
        Assertions.assertNotNull(loanTransactions, "Empty Loan Details");
        Long transId = null;
        Long chargeId = null;
        for (GetLoansLoanIdTransactions transaction : loanTransactions) {
            Assertions.assertNotNull(transaction.getType().getId());
            if (Boolean.TRUE.equals(transaction.getType().getWaiveCharges())) {
                transId = transaction.getId();
                Assertions.assertNotNull(transId);
                chargeId = undoWaiveLoanCharge(loanID, transId, new PutChargeTransactionChangesRequest().id(transId).loanId(loanID))
                        .getResourceId();
                break;
            }
        }

        Assertions.assertEquals(waivedChargeId, chargeId);

        // Validate the undo process
        List<GetLoansLoanIdTransactions> loanTransactionDetails = getLoanDetails(loanID).getTransactions();
        Assertions.assertNotNull(loanTransactionDetails, "Empty Loan Transaction Details");
        for (int i = 0; i < loanTransactionDetails.size(); i++) {
            final Boolean isReversed = loanTransactionDetails.get(i).getManuallyReversed();
            final Long id = loanTransactionDetails.get(i).getId();

            if (transId.compareTo(id) == 0) {
                BigDecimal waiveAmount = BigDecimal.valueOf(getLoanCharge(loanID, waivedChargeId).getAmountWaived());

                Assertions.assertEquals(true, isReversed);
                Assertions.assertEquals(Double.valueOf(0), waiveAmount.doubleValue());
                break;
            } else if (transId.compareTo(id) != 0 && i == loanTransactionDetails.size() - 1) {
                Assertions.assertEquals(transId, id);
            }
        }

        // Re-waive charge
        waiveLoanCharge(loanID, waivedChargeId,
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(waivePeriodnum).locale(LOCALE));
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.getFeeChargesDue()));
            if (isPeriod(installment, waivePeriodnum)) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.getFeeChargesOutstanding()));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.getFeeChargesWaived()));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.getFeeChargesOutstanding()));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.getFeeChargesWaived()));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

        Long payPeriodnum = 2L;
        payLoanCharge(loanID, getloanCharge(flatAccTransfer, loanCharges).getId(), new PostLoansLoanIdChargesChargeIdRequest()
                .transactionDate(SAVINGS_TRANSACTION_DATE).installmentNumber(payPeriodnum).locale(LOCALE).dateFormat(DATETIME_PATTERN));
        loanCharges = getLoanCharges(loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.getFeeChargesDue()));
            if (isPeriod(installment, payPeriodnum)) {
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.getFeeChargesOutstanding()));
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.getFeeChargesPaid()));
            } else if (isPeriod(installment, waivePeriodnum)) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.getFeeChargesOutstanding()));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.getFeeChargesWaived()));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.getFeeChargesOutstanding()));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.getFeeChargesPaid()));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "300", "100.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

        // Loan Charges with US Locale using the amount as a number in the JSON body
        addInstallmentChargeWithLocale(loanID, flat, 50.05, Locale.US);
        loanCharges = getLoanCharges(loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("200.05", String.valueOf(installment.getFeeChargesDue()));
        }

        // Loan Charges with other Locale using comma (,) as decimal delimiter
        addInstallmentChargeWithLocaleFormattedAmount(loanID, flat, "50,05", Locale.GERMAN);
        loanCharges = getLoanCharges(loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("250.10", String.valueOf(installment.getFeeChargesDue()));
        }

        // Loan Charges with German Locale (where the comma is the decimal delimiter) using the amount as a number in
        // the JSON body
        addInstallmentChargeWithLocale(loanID, flat, 50.05, Locale.GERMAN);
        loanCharges = getLoanCharges(loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        loanSchedule.remove(0);
        for (GetLoansLoanIdRepaymentPeriod installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("300.15", String.valueOf(installment.getFeeChargesDue()));
        }
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        final Long savingsId = openSavingsAccountActivatedOnTransactionDate(clientID, MINIMUM_OPENING_BALANCE);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplication(clientID, loanProductID, null, savingsId, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);
        float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

        // DISBURSE
        GetLoansLoanIdResponse loanStatusHashMap = disburseToSavingsWithNetDisbursalAmount(SAVINGS_TRANSACTION_DATE, loanID);
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        verifyLoanIsActive(loanStatusHashMap);

        summary = savingsHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("12000");
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

        undoDisbursement(loanID);
        loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanIsApprovedAndWaitingForDisbursal(loanStatusHashMap);

        summary = savingsHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_INTEREST() {

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);
        if (fourMonthsfromNowCalendar.get(Calendar.DAY_OF_MONTH) > 27) {
            fourMonthsfromNowCalendar.add(Calendar.DAY_OF_MONTH, 4);
        }

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long disbursementFee = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 5.0).getResourceId();
        addCharges(charges, disbursementFee, 5.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        final Long loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, null, "1000",
                DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);

        approveLoan(fourMonthsfromNow, loanID);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID);

        // check for disbursement fee: Principal 1,000 with 24% Annual Rate for 6 Months we have Total Interest of:
        // 120.00
        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        // Disbursement Fee: 5% of 120.00 = 6.00
        validateNumberForEqual("6.00", String.valueOf(disbursementDetail.getFeeChargesDue()));
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_TRANCHES() {
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(true, NONE);

        List<PostLoansDisbursementData> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, null, "45,000.00", tranches, collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("01 March 2014", loanID));

        // DISBURSE first Tranche
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("20 March 2014", loanID);
        LOG.info("DISBURSE {}", loanStatusHashMap);
        verifyLoanIsActive(loanStatusHashMap);

        // DISBURSE Second Tranche
        loanStatusHashMap = disburseLoanWithNetDisbursalAmount("23 April 2014", loanID);
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        verifyLoanIsActive(loanStatusHashMap);

        undoDisbursement(loanID);
        loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanIsApprovedAndWaitingForDisbursal(loanStatusHashMap);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS_WITH_TRANCHES() {
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(true, NONE);

        final Long savingsId = openSavingsAccountActivatedOnTransactionDate(clientID, MINIMUM_OPENING_BALANCE);

        List<PostLoansDisbursementData> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, savingsId, "45,000.00", tranches,
                collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("01 March 2014", loanID));

        SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);
        float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

        // DISBURSE first Tranche
        GetLoansLoanIdResponse loanStatusHashMap = disburseToSavingsWithNetDisbursalAmount("01 March 2014", loanID);
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        verifyLoanIsActive(loanStatusHashMap);

        summary = savingsHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("25000");
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

        // DISBURSE Second Tranche
        loanStatusHashMap = disburseToSavingsWithNetDisbursalAmount("23 April 2014", loanID);
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        verifyLoanIsActive(loanStatusHashMap);

        summary = savingsHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("25000") + Float.parseFloat("20000");
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

        undoDisbursement(loanID);
        loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanIsApprovedAndWaitingForDisbursal(loanStatusHashMap);

        summary = savingsHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.getAccountBalance().floatValue(), "Verifying opening Balance");

    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndCashBasedAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flatDisbursement = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.FLAT, 100.0).getResourceId();
        addCharges(charges, flatDisbursement, 100.0, null);
        Long flatSpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false)
                .getResourceId();
        addCharges(charges, flatSpecifiedDueDate, 100.0, "29 September 2011");
        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("100.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2911.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("150.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, flatSpecifiedDueDate, "29 October 2011", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.getFeeChargesDue()));
        waiveLoanCharge(loanID, getloanCharge(flatInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3251.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2969.72"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("181.77"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        Long flatPenaltySpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, true)
                .getResourceId();
        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "29 September 2011", 100.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2811.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("150.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3129.11"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("50.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("122.38"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "10 January 2012", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3139.68"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3139.68"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3089.68"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("50.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithChargesOfTypeAmountPercentageAndCashBasedAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long percentageDisbursementCharge = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0)
                .getResourceId();
        addCharges(charges, percentageDisbursementCharge, 1.0, null);

        Long percentageSpecifiedDueDateCharge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false).getResourceId();
        addCharges(charges, percentageSpecifiedDueDateCharge, 1.0, "29 September 2011");

        Long percentageInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false)
                .getResourceId();
        addCharges(charges, percentageInstallmentFee, 1.0, "29 September 2011");

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("120.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2911.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("149.11"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, percentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.getFeeChargesDue()));
        waiveLoanCharge(loanID, getloanCharge(percentageInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3271.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2969.72"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("181.77"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        Long percentagePenaltySpecifiedDueDate = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
        addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2791.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("149.11"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3301.78"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3149.11"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("30.29"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("122.38"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3120.58"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3120.58"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3089.68"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("30.90"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type percentage of
     * amount plus interest
     */
    @Test
    public void loanWithChargesOfTypeAmountPlusInterestPercentageAndCashBasedAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long amountPlusInterestPercentageDisbursementCharge = chargesHelper
                .createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0).getResourceId();
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, 1.0, null);

        Long amountPlusInterestPercentageSpecifiedDueDateCharge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, 1.0, "29 September 2011");

        Long amountPlusInterestPercentageInstallmentFee = chargesHelper
                .createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, 1.0, "29 September 2011");

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(collateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("126.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("126.06"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2911.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("157.57"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, amountPlusInterestPercentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.getFeeChargesDue()));
        waiveLoanCharge(loanID, getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3277.55"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2969.72"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("126.06"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("181.77"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        Long amountPlusInterestPercentagePenaltySpecifiedDueDate = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2791.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("157.57"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3303"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3149.11"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                journalEntry(Float.parseFloat("31.51"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("122.38"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3121.19"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3121.19"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3089.68"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                journalEntry(Float.parseFloat("31.51"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndUpfrontAccrualAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flatDisbursement = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.FLAT, 100.0).getResourceId();
        addCharges(charges, flatDisbursement, 100.0, null);
        Long flatSpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false)
                .getResourceId();

        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("50.00", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("605.94"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("100.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("200.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("605.94"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("200.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, flatSpecifiedDueDate, "29 September 2011", 100.0);

        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                journalEntry(Float.parseFloat("100.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, flatSpecifiedDueDate, "29 October 2011", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.getFeeChargesDue()));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        waiveLoanCharge(loanID, getloanCharge(flatInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("50.0"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                journalEntry(Float.parseFloat("50.0"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3251.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3251.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        Long flatPenaltySpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, true)
                .getResourceId();
        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "29 September 2011", 100.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // checking the journal entry as applied penalty has been collected
        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "10 January 2012", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("100"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make over payment for repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3220.60"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3220.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3139.68"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                journalEntry(Float.parseFloat("80.92"), overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
        GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getOverpaid);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithCahargesAndUpfrontAccrualAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long percentageDisbursementCharge = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0)
                .getResourceId();
        addCharges(charges, percentageDisbursementCharge, 1.0, null);

        Long percentageSpecifiedDueDateCharge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false).getResourceId();
        addCharges(charges, percentageSpecifiedDueDateCharge, 1.0, "29 September 2011");

        Long percentageInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false)
                .getResourceId();
        addCharges(charges, percentageInstallmentFee, 1.0, "29 September 2011");

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("605.94"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("605.94"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, percentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.getFeeChargesDue()));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        waiveLoanCharge(loanID, getloanCharge(percentageInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("29.7"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                journalEntry(Float.parseFloat("29.7"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3271.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3271.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        Long percentagePenaltySpecifiedDueDate = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
        addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // checking the journal entry as applied penalty has been collected
        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3301.78"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.78"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make over payment for repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3220.58"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3220.58"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3120.58"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100.00"), overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
        GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getOverpaid);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type percentage of
     * amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndUpfrontAccrualAccountingEnabled() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long amountPlusInterestPercentageDisbursementCharge = chargesHelper
                .createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0).getResourceId();
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, 1.0, null);

        Long amountPlusInterestPercentageSpecifiedDueDateCharge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();

        Long amountPlusInterestPercentageInstallmentFee = chargesHelper
                .createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, 1.0, "29 September 2011");

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("31.51", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("605.94"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("126.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("126.04"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("605.94"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("126.06"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("126.04"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, amountPlusInterestPercentageSpecifiedDueDateCharge, "29 September 2011", 1.0);

        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                journalEntry(Float.parseFloat("126.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                journalEntry(Float.parseFloat("126.06"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, amountPlusInterestPercentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.getFeeChargesDue()));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        waiveLoanCharge(loanID, getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("31.51"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                journalEntry(Float.parseFloat("31.51"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3277.55"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3277.55"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        Long amountPlusInterestPercentagePenaltySpecifiedDueDate = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // checking the journal entry as applied penalty has been collected
        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3303"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3303"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make over payment for repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3221.61"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3221.61"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3121.19"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100.42"), overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
        GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getOverpaid);
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatChargesAndPeriodicAccrualAccountingEnabled() throws InterruptedException {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long flatDisbursement = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.FLAT, 100.0).getResourceId();
        addCharges(charges, flatDisbursement, 100.0, null);
        Long flatSpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false)
                .getResourceId();
        addCharges(charges, flatSpecifiedDueDate, 100.0, "29 September 2011");
        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("100.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("100.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, flatSpecifiedDueDate, "29 October 2011", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.getFeeChargesDue()));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        waiveLoanCharge(loanID, getloanCharge(flatInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        /*
         * checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new
         * JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.CREDIT));
         * journalHelper.checkJournalEntryForExpenseAccount (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.DEBIT));
         */
        final String jobName = "Add Accrual Transactions";

        schedulerHelper.executeAndAwaitJob(jobName);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3251.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3251.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        Long flatPenaltySpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, true)
                .getResourceId();
        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "29 September 2011", 100.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // checking the journal entry as applied penalty has been collected
        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3301.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "10 January 2012", 100.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("100"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("100"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3139.68"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3139.68"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3139.68"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getClosed);
    }

    /**
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithChargesOfTypeAmountPercentageAndPeriodicAccrualAccountingEnabled() throws InterruptedException {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            final Long clientID = createClient();
            verifyClientCreatedOnServer(clientID);

            // Add charges with payment mode regular
            List<PostLoansRequestChargeData> charges = new ArrayList<>();
            Long percentageDisbursementCharge = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0)
                    .getResourceId();
            addCharges(charges, percentageDisbursementCharge, 1.0, null);

            Long percentageSpecifiedDueDateCharge = chargesHelper
                    .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false).getResourceId();
            addCharges(charges, percentageSpecifiedDueDateCharge, 1.0, "29 September 2011");

            Long percentageInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, false)
                    .getResourceId();
            addCharges(charges, percentageInstallmentFee, 1.0, "29 September 2011");

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

            final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

            final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
            collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

            final Long loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);
            final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
            Assertions.assertNotNull(loanID);
            verifyLoanIsPending(loanID);

            List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
            verifyLoanRepaymentSchedule(loanSchedule);

            List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
            validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
            validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

            // check for disbursement fee
            GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
            validateNumberForEqual("120.00", String.valueOf(disbursementDetail.getFeeChargesDue()));

            // check for charge at specified date and installment fee
            GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
            validateNumberForEqual("149.11", String.valueOf(firstInstallment.getFeeChargesDue()));

            // check for installment fee
            GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("29.70", String.valueOf(secondInstallment.getFeeChargesDue()));

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

            List<GetLoansLoanIdTransactions> loanTransactionDetails = getLoanDetails(loanID).getTransactions();
            final LoanTestData.Journal[] assetAccountInitialEntry = {
                    journalEntry(Float.parseFloat("120.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                    journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
            checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
            checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                    journalEntry(Float.parseFloat("120.00"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            loanCharges.clear();
            loanCharges = getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

            LOG.info("-------------Make repayment 1-----------");
            makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
            loanCharges.clear();
            loanCharges = getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
            validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
            validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

            checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                    journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

            addLoanCharge(loanID, percentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);

            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("149.70", String.valueOf(secondInstallment.getFeeChargesDue()));
            LOG.info("----------- Waive installment charge for 2nd installment ---------");
            waiveLoanCharge(loanID, getloanCharge(percentageInstallmentFee, loanCharges).getId(),
                    new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
            loanCharges.clear();
            loanCharges = getLoanCharges(loanID);
            validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

            /*
             * checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new
             * JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.CREDIT));
             * journalHelper.checkJournalEntryForExpenseAccount (expenseAccount, "20 September 2011", new
             * JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.DEBIT));
             */

            final String jobName = "Add Accrual Transactions";

            schedulerHelper.executeAndAwaitJob(jobName);

            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            checkAccrualTransactions(loanSchedule, loanID);

            LOG.info("----------Make repayment 2------------");
            makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
            checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                    journalEntry(Float.parseFloat("3271.49"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("3271.49"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

            LOG.info("--------------Waive interest---------------");
            waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
            validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

            checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                    journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                    journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

            Long percentagePenaltySpecifiedDueDate = chargesHelper
                    .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
            addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
            loanCharges.clear();
            loanCharges = getLoanCharges(loanID);
            validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

            // checking the journal entry as applied penalty has been collected
            checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                    journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("3300.60"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

            LOG.info("----------Make repayment 3 advance------------");
            makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
            checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                    journalEntry(Float.parseFloat("3301.78"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("3301.78"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

            addLoanCharge(loanID, percentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
            validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
            validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

            LOG.info("----------Pay applied penalty ------------");
            makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
            checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                    journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            loanSchedule.clear();
            loanSchedule = getLoanRepaymentSchedule(loanID);
            fourthInstallment = loanSchedule.get(4);
            validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
            validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

            LOG.info("----------Make repayment 4 ------------");
            makeRepayment("20 January 2012", Float.parseFloat("3120.58"), loanID);
            checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                    journalEntry(Float.parseFloat("3120.58"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                    journalEntry(Float.parseFloat("3120.58"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
            verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getClosed);
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type percentage of
     * amount and interest
     */
    @Test
    public void loanWithChargesOfTypeAmountPlusInterestPercentageAndPeriodicAccrualAccountingEnabled() throws InterruptedException {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long amountPlusInterestPercentageDisbursementCharge = chargesHelper
                .createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0).getResourceId();
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, 1.0, null);

        Long amountPlusInterestPercentageSpecifiedDueDateCharge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, 1.0, "29 September 2011");

        Long amountPlusInterestPercentageInstallmentFee = chargesHelper
                .createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).getResourceId();
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, 1.0, "29 September 2011");

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<GetLoansLoanIdChargesChargeIdResponse> loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.getFeeChargesDue()));

        // check for charge at specified date and installment fee
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.getFeeChargesDue()));

        // check for installment fee
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.getFeeChargesDue()));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("126.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                journalEntry(Float.parseFloat("126.06"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, amountPlusInterestPercentageSpecifiedDueDateCharge, "29 October 2011", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.getFeeChargesDue()));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        waiveLoanCharge(loanID, getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).getId(),
                new PostLoansLoanIdChargesChargeIdRequest().installmentNumber(2L).locale(LOCALE));
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        /*
         * checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", journalEntry(* Float.parseFloat("31.51"),
         * assetAccount, JournalEntry.TransactionType.CREDIT.name())); journalHelper.checkJournalEntryForExpenseAccount
         * (expenseAccount, "20 September 2011", new JournalEntry(Float.parseFloat("31.51"),
         * JournalEntry.TransactionType.DEBIT));
         */

        final String jobName = "Add Accrual Transactions";

        schedulerHelper.executeAndAwaitJob(jobName);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        LOG.info("----------Make repayment 2------------");
        makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3277.55"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3277.55"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        LOG.info("--------------Waive interest---------------");
        waiveInterestOnLoan(loanID, "20 December 2011", 61.79);

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.getInterestOutstanding()));

        checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                journalEntry(Float.parseFloat("61.79"), expenseAccount, JournalEntry.TransactionType.DEBIT.name()));

        Long amountPlusInterestPercentagePenaltySpecifiedDueDate = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 1.0, true).getResourceId();
        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "29 September 2011", 1.0);
        loanCharges.clear();
        loanCharges = getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // checking the journal entry as applied penalty has been collected
        checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3309.06"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        LOG.info("----------Make repayment 3 advance------------");
        makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                journalEntry(Float.parseFloat("3303"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3303"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        addLoanCharge(loanID, amountPlusInterestPercentagePenaltySpecifiedDueDate, "10 January 2012", 1.0);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Pay applied penalty ------------");
        makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("120"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.getPenaltyChargesOutstanding()));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));

        LOG.info("----------Make repayment 4 ------------");
        makeRepayment("20 January 2012", Float.parseFloat("3121.19"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                journalEntry(Float.parseFloat("3121.19"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("3121.19"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        GetLoansLoanIdResponse loanStatusHashMap = getLoanDetails(loanID);
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getClosed);
    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(100, 0, DEFAULT_STRATEGY);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, null, collaterals);
        final List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentScheduleForEqualPrincipal(loanSchedule);

    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails_with_grace() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanProductID = createLoanProduct(100, 0, DEFAULT_STRATEGY);
        final Long loanID = applyForLoanApplication(clientID, loanProductID, 5, collaterals);
        final List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(loanSchedule);

    }

    /***
     * Test case to verify RBI payment strategy
     */
    @Test
    public void testRBIPaymentStrategy() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        /***
         * Create loan product with RBI strategy
         */
        final Long loanProductID = createLoanProduct(100, 0, RBI_INDIA_STRATEGY);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final Long savingsId = null;
        final String principal = "12,000.00";

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithPaymentStrategy(clientID, loanProductID, null, savingsId, principal,
                RBI_INDIA_STRATEGY, collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount("20 September 2011", loanID));

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("3200", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        /***
         * Make payment for installment #1
         */
        makeRepayment("20 October 2011", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        /***
         * Verify 2nd and 3rd repayments dues before making excess payment for installment no 2
         */
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);

        validateNumberForEqual("3200", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));

        validateNumberForEqual("3000", String.valueOf(secondInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.getPrincipalOutstanding()));

        /***
         * Make payment for installment #2
         */
        makeRepayment("20 November 2011", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        /***
         * Verify 2nd and 3rd repayments after making excess payment for installment no 2
         */
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        /***
         * According to RBI Excess payment should go to principal portion of next installment, but as interest
         * recalculation is not implemented, it wont make any difference to schedule even though if we made excess
         * payment, so excess payments will behave the same as regular payment with the excess amount
         */
        thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.getPrincipalPaid()));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.getInterestPaid()));
        validateNumberForEqual("100.00", String.valueOf(thirdInstallment.getInterestOutstanding()));

        /***
         * Make payment with due amount of 3rd installment on 4th installment date
         */
        makeRepayment("20 January 2012", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);

        /***
         * Verify overdue interests are deducted first and then remaining amount for interest portion of due installment
         */
        thirdInstallment = loanSchedule.get(3);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);

        validateNumberForEqual("100", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("100", String.valueOf(thirdInstallment.getPrincipalOutstanding()));

        validateNumberForEqual("2900", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("100", String.valueOf(fourthInstallment.getInterestPaid()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getInterestOutstanding()));

        makeRepayment("20 January 2012", Float.parseFloat("3000"), loanID);

        /***
         * verify loan is closed as we paid full amount
         */
        verifyLoanAccountIsClosed(loanID);

    }

    @Test
    public void testLoanPrePaymentWithMultiplePayments() {
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        // Create a loan product
        Long loanProductId = createLoanProduct(false, NONE);
        Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        List<PostLoansRequestCollateralData> collaterals = List.of(collateral(clientCollateralId, BigDecimal.ONE));

        // Apply for a loan
        final String disbursementDate = "1 May 2023";
        final String approvalDate = "1 April 2023";
        final String submissionDate = "1 March 2023";
        final String interestRate = "7";
        final Long loanID = applyForLoanApplication(clientID, loanProductId, disbursementDate, submissionDate, interestRate, null, null,
                "1000", collaterals);
        Assertions.assertNotNull(loanID);

        // Check loan status
        verifyLoanIsPending(loanID);

        // Approve the loan
        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(approvalDate, loanID));

        // Disburse the loan
        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(disbursementDate, loanID));

        // Make the first partial repayment
        LOG.info("------------------------MAKE FIRST PARTIAL REPAYMENT-----------------------------------");
        Float firstRepaymentAmount = 500.0f; // First partial repayment
        String firstRepaymentDate = "1 June 2023";
        makeRepayment(firstRepaymentDate, firstRepaymentAmount, loanID);

        // Verify the prepayment amount after the first partial repayment
        LOG.info("------------------------GET PREPAYMENT AMOUNT AFTER FIRST PAYMENT-----------------------");
        GetLoansLoanIdTransactionsTemplateResponse prepayAmount = getPrepayAmount(loanID);
        Assertions.assertNotNull(prepayAmount);

        // Extract the principal and interest portions
        Float totalPrepayAmount = prepayAmount.getAmount().floatValue();
        Float principalAmount = prepayAmount.getPrincipalPortion().floatValue();
        Float interestAmount = prepayAmount.getInterestPortion().floatValue();

        // Expected values after the first partial repayment
        Float expectedTotalPrepayAmount = 606.18f;
        Float expectedPrincipal = 570.0f;
        Float expectedInterest = 36.18f;

        // Validate calculations
        validateNumberForEqual(String.valueOf(expectedTotalPrepayAmount), String.valueOf(totalPrepayAmount));
        validateNumberForEqual(String.valueOf(expectedPrincipal), String.valueOf(principalAmount));
        validateNumberForEqual(String.valueOf(expectedInterest), String.valueOf(interestAmount));

        // Make the second partial repayment
        LOG.info("------------------------MAKE SECOND PARTIAL REPAYMENT----------------------------------");
        Float secondRepaymentAmount = 606.18f;
        String secondRepaymentDate = "1 July 2023";
        makeRepayment(secondRepaymentDate, secondRepaymentAmount, loanID);

        // Recheck the prepayment amount
        LOG.info("------------------------RECHECK PREPAYMENT AMOUNT AFTER FULL REPAYMENT------------------");
        GetLoansLoanIdTransactionsTemplateResponse postPrepayAmount = getPrepayAmount(loanID);
        Assertions.assertNotNull(postPrepayAmount);

        // Verify that the principal and interest portions are zero
        Float postPrincipalAmount = postPrepayAmount.getPrincipalPortion().floatValue();
        Float postInterestAmount = postPrepayAmount.getInterestPortion().floatValue();

        validateNumberForEqual("0.0", String.valueOf(postPrincipalAmount));
        validateNumberForEqual("0.0", String.valueOf(postInterestAmount));

        // Check the loan status after repayment
        LOG.info("------------------------CHECK LOAN STATUS---------------------------------------------");
        verifyLoanAccountIsClosed(loanID);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.NONE, LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 0,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, null);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                DEFAULT_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = Float.parseFloat("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3965.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1771.88", "16.39", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1780.05", "8.22", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        validateNumberForEqualWithMsg("verify pre-close amount", "3551.93", prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        Integer preCloseInterestStrategy = LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE;
        String preCloseAmount = "7561.84";
        testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_REST_DATE() {
        Integer preCloseInterestStrategy = LoanTestData.PreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE;
        String preCloseAmount = "7586.62";
        testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_WITH_INSTALLMENT_CHARGE() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.NONE, LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 0,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, null);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long installmentCharge = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.PERCENT_OF_INTEREST, 10.0, false)
                .getResourceId();
        addCharges(charges, installmentCharge, 10.0, null);
        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                DEFAULT_STRATEGY, charges);

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "1.16", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "1.17", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "1.16", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = Float.parseFloat("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3961.84", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1773.61", "16.41", "1.64", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1781.79", "8.22", "0.82", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(RBI_INDIA_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST,
                LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = getLoanFutureRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = Float.parseFloat("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        ExpectedInstallment paymentday = new ExpectedInstallment(addDays(today, -5), "3990.09", "9.91", "0", "0");
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        expectedvalues.add(paymentday);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.31", "11.6", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1009.84", "4.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);

    }

    @Test
    public void testInteroperationLoanRepaymentAPI() {
        try {
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(true));
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Long clientID = createClient();
            verifyClientCreatedOnServer(clientID);
            final Long loanProductID = createLoanProductWithInterestRecalculationAndInstallmentAmount(
                    INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, LoanTestData.InterestRecalculationCompoundingMethod.NONE,
                    LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, 12);

            final Long loanID = applyForLoanApplicationForInterestRecalculationWithFirstRepaymentDate(clientID, loanProductID,
                    loanDisbursementDate, INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            verifyLoanIsPending(loanID);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanID));

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID));

            List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
            Assertions.assertNotNull(loanSchedule);
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());

            Float earlyPayment = Float.parseFloat("3000");
            String accountNo = getLoanDetails(loanID).getAccountNo();

            String loanRepayment = makeRepaymentByAccountNo(accountNo, loanFirstRepaymentDate, earlyPayment);
            assertNotNull(loanRepayment);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        Integer compoundingDayOfMonth = getDayOfMonth(todaysDate);
        Integer compoundingDayOfWeek = getDayOfWeek(todaysDate);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -2);
        Integer restDayOfMonth = getDayOfMonth(todaysDate);
        Integer restDayOfWeek = getDayOfWeek(todaysDate);
        final String REST_START_DATE = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        todaysDate.add(Calendar.DAY_OF_MONTH, 2);
        final String LOAN_FLAT_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, 14);
        final String LOAN_INTEREST_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        List<PostLoansRequestChargeData> charges = new ArrayList<>(2);
        Long flat = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false).getResourceId();
        Long principalPercentage = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 2.0, false)
                .getResourceId();

        addCharges(charges, flat, 100.0, LOAN_FLAT_CHARGE_DATE);
        addCharges(charges, principalPercentage, 2.0, LOAN_INTEREST_CHARGE_DATE);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST_AND_FEE,
                LoanTestData.RescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS, LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, compoundingDayOfMonth, compoundingDayOfWeek,
                restDayOfMonth, restDayOfWeek);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                DEFAULT_STRATEGY, charges);

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.08", "46.83", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.49", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = Float.parseFloat("5100");
        repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "5065.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "0", "11.32", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2451.93", "11.32", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        Integer preCloseInterestStrategy = LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE;
        String preCloseAmount = "7761.89";
        testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_REST_DATE() {
        Integer preCloseInterestStrategy = LoanTestData.PreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE;
        String preCloseAmount = "7786.79";
        testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_DAILY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_WITH_OVERDUE_CHARGE()
            throws InterruptedException {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -2);
        final String REST_START_DATE = dateFormat.format(todaysDate.getTime());

        Long overdueFeeChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(10.0))
                .getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Integer recalculationCompoundingFrequencyInterval = null;
        final Long loanProductID = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST_AND_FEE,
                LoanTestData.RescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, overdueFeeChargeId, false, null, null, null,
                null);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                DEFAULT_STRATEGY, null);

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());

        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.33", "46.58", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2552.37", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        String JobName = "Apply penalty to overdue loans";
        schedulerHelper.executeAndAwaitJob(JobName);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2481.38", "47.53", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2479.99", "48.92", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2555.87", "11.8", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        totalDueForCurrentPeriod = totalDueForCurrentPeriod - Float.parseFloat("252.89");
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2493.05", "35.86", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2491.72", "37.19", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2532.47", "11.69", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -3);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        totalDueForCurrentPeriod = loanSchedule.get(2).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2493.05", "35.86", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2497.22", "31.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2526.97", "11.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_PERIODIC_ACCOUNTING() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("Disbursal Date Calendar {}", todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        Account[] accounts = { assetAccount, incomeAccount, expenseAccount, overpaymentAccount };
        final Long loanProductID = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.NONE, LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 0,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, accounts, null, null);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                DEFAULT_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("Date during repayment schedule {}", todaysDate.getTime());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(10000.0f, assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(10000.0f, assetAccount, JournalEntry.TransactionType.DEBIT.name()), };
        checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String runOndate = dateFormat.format(todaysDate.getTime());
        LOG.info("runOndate : {}", runOndate);
        runPeriodicAccrualAccounting(runOndate);
        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 46.15f, 0f, 0f, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        runPeriodicAccrualAccounting(runOndate);
        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);

        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_CURRENT_REPAYMENT_BASED_ARREARS_AGEING() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(RBI_INDIA_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST,
                LoanTestData.RescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 1,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, getDayOfMonth(todaysDate),
                getDayOfWeek(todaysDate), getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        GetLoansLoanIdSummary loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(toLocalDate(todaysDate), loanSummary.getOverdueSinceDate());

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(toLocalDate(todaysDate), loanSummary.getOverdueSinceDate());

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_ORIGINAL_REPAYMENT_BASED_ARREARS_AGEING() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("----timeeeeeeeeeeeeee------> {}", dateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Integer recalculationCompoundingFrequencyInterval = null;
        final Long loanProductID = createLoanProductWithInterestRecalculation(RBI_INDIA_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST,
                LoanTestData.RescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, true, null, null,
                getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        GetLoansLoanIdSummary loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(toLocalDate(todaysDate), loanSummary.getOverdueSinceDate());

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = getLoanDetails(loanID).getSummary();
        Assertions.assertNull(loanSummary.getOverdueSinceDate());

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, "10006.59");
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_REST_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE, "10046.15");
    }

    /***
     * Test case to verify default Style payment strategy
     */
    @Test
    public void testLoanRefundByCashCashBasedAccounting() {

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        // FINERACT-885: If the loan starts on day 27-31th of month and not all months have that
        // many days, then loan payment will get reset to a day of month less than today's day
        // and 4th payment will be in the past. In such case, start the loan a few days later,
        // so that 4th payment is guaranteed to be in the future.
        if (fourMonthsfromNowCalendar.get(Calendar.DAY_OF_MONTH) > 27) {
            fourMonthsfromNowCalendar.add(Calendar.DAY_OF_MONTH, 4);
        }

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProduct(0, 0, DEFAULT_STRATEGY, CASH_BASED, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final Long savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();

        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId, principal,
                DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(fourMonthsfromNow, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                journalEntry(Float.parseFloat("2290"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow,
                journalEntry(Float.parseFloat("50"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                journalEntry(Float.parseFloat("4580"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("4000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow,
                journalEntry(Float.parseFloat("100"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("480"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        // FINERACT-885: As loan may not have started exactly four months ago,
        // make final payment today and not four months from start (as that may be in the future)
        fourMonthsfromNowCalendar.setTime(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRefundByCash(now, Float.parseFloat("20"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        makeRefundByCash(now, Float.parseFloat("2000"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("2000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("1980"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        checkJournalEntryForIncomeAccount(incomeAccount, now,
                journalEntry(Float.parseFloat("20"), incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

    }

    /***
     * Test case to verify Default style payment strategy
     */
    @Test
    public void testLoanRefundByCashAccrualBasedAccounting() {
        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        // FINERACT-885: If the loan starts on day 27-31th of month and not all months have that
        // many days, then loan payment will get reset to a day of month less than today's day
        // and 4th payment will be in the past. In such case, start the loan a few days later,
        // so that 4th payment is guaranteed to be in the future.
        if (fourMonthsfromNowCalendar.get(Calendar.DAY_OF_MONTH) > 27) {
            fourMonthsfromNowCalendar.add(Calendar.DAY_OF_MONTH, 4);
        }

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProduct(0, 0, DEFAULT_STRATEGY, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);// ,
        // LoanProductTestBuilder.EQUAL_INSTALLMENTS,
        // LoanProductTestBuilder.FLAT_BALANCE);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final Long savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();

        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId, principal,
                DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(fourMonthsfromNow, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("1440"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("300.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                journalEntry(Float.parseFloat("2290"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2290"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                journalEntry(Float.parseFloat("4580"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("4580"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        // FINERACT-885: As loan may not have started exactly four months ago,
        // make final payment today and not four months from start (as that may be in the future)
        fourMonthsfromNowCalendar.setTime(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRefundByCash(now, Float.parseFloat("20"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        makeRefundByCash(now, Float.parseFloat("2000"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("2000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("1980"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

    }

    @Test
    public void testLoanRefundByTransferCashBasedAccounting() {

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        // FINERACT-885: If the loan starts on day 27-31th of month and not all months have that
        // many days, then loan payment will get reset to a day of month less than today's day
        // and 4th payment will be in the past. In such case, start the loan a few days later,
        // so that 4th payment is guaranteed to be in the future.
        if (fourMonthsfromNowCalendar.get(Calendar.DAY_OF_MONTH) > 27) {
            fourMonthsfromNowCalendar.add(Calendar.DAY_OF_MONTH, 4);
        }

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        final Long savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Long savingsId = savingsHelper.submitApplication(clientID, savingsProductID, SAVINGS_CREATED_DATE).getSavingsId();
        Assertions.assertNotNull(savingsId);

        assertTrue(updateSavingsSubmittedOnDate(savingsId, clientID, savingsProductID, SAVINGS_CREATED_DATE_PLUS_ONE),
                "Expected submittedOnDate to be reported as modified");

        assertTrue(savingsHelper.getSavingsDetails(savingsId).getStatus().getSubmittedAndPendingApproval());

        savingsHelper.approveSavings(savingsId, SAVINGS_TRANSACTION_DATE);
        assertTrue(savingsHelper.getSavingsDetails(savingsId).getStatus().getApproved());

        savingsHelper.activateSavings(savingsId, SAVINGS_TRANSACTION_DATE);
        assertTrue(savingsHelper.getSavingsDetails(savingsId).getStatus().getActive());

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProduct(0, 0, DEFAULT_STRATEGY, CASH_BASED, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */

        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<PostLoansRequestChargeData> charges = new ArrayList<>();

        Long flatInstallmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatInstallmentFee, 50.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, null, principal,
                DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(fourMonthsfromNow, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID));

        final LoanTestData.Journal[] assetAccountInitialEntry = {
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("12000.00"), assetAccount, JournalEntry.TransactionType.DEBIT.name()) };
        checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                journalEntry(Float.parseFloat("2290"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("2000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow,
                journalEntry(Float.parseFloat("50"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("240"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.getTotalOutstandingForPeriod()));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                journalEntry(Float.parseFloat("4580"), assetAccount, JournalEntry.TransactionType.DEBIT.name()),
                journalEntry(Float.parseFloat("4000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow,
                journalEntry(Float.parseFloat("100"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("480"), incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.getTotalOutstandingForPeriod()));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        Float transferAmountValue = 20f;

        // FINERACT-885: As loan may not have started exactly four months ago,
        // make final payment today and not four months from start (as that may be in the future)
        fourMonthsfromNowCalendar.setTime(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        final String FROM_LOAN_ACCOUNT_TYPE = "1";
        final String TO_SAVINGS_ACCOUNT_TYPE = "2";

        accountTransferHelper.refundLoanByTransfer(now, clientID, loanID, savingsId, transferAmountValue.toString());

        Float toSavingsBalance = Float.parseFloat(MINIMUM_OPENING_BALANCE);

        SavingsAccountSummaryData toSavingsSummaryAfter = savingsHelper.getSavingsSummary(savingsId);

        toSavingsBalance += transferAmountValue;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals(toSavingsBalance, toSavingsSummaryAfter.getAccountBalance().floatValue(),
                "Verifying From Savings Account Balance after Account Transfer");

        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("20"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0
        // final String now = Utils.convertDate(fourMonthsfromNowCalendar);

        transferAmountValue = 2000f;

        accountTransferHelper.refundLoanByTransfer(now, clientID, loanID, savingsId, transferAmountValue.toString());

        toSavingsSummaryAfter = savingsHelper.getSavingsSummary(savingsId);

        toSavingsBalance += transferAmountValue;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals(toSavingsBalance, toSavingsSummaryAfter.getAccountBalance().floatValue(),
                "Verifying From Savings Account Balance after Account Transfer");

        checkJournalEntryForAssetAccount(assetAccount, now,
                journalEntry(Float.parseFloat("2000"), assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                journalEntry(Float.parseFloat("1980"), assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        checkJournalEntryForIncomeAccount(incomeAccount, now,
                journalEntry(Float.parseFloat("20"), incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

        loanSchedule.clear();
        loanSchedule = getLoanRepaymentSchedule(loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.getTotalOutstandingForPeriod()));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.getPrincipalOutstanding()));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.getInterestOutstanding()));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.getFeeChargesOutstanding()));

    }

    @Test
    public void testLoanProductConfiguration() {
        final String proposedAmount = "5000";

        final Long clientID = createClient("01 January 2012");
        Long loanProductID = createLoanProductWithAttributeOverrides(true);
        LOG.info("-----------------------LOAN PRODUCT CREATED WITH ATTRIBUTE CONFIGURATION AS TRUE-------------------------- {}",
                loanProductID);
        Long loanID = applyForLoanApplicationWithProductConfigurationAsTrue(clientID, loanProductID, proposedAmount);
        LOG.info("------------------------LOAN CREATED WITH ID------------------------------{}", loanID);

        loanProductID = createLoanProductWithAttributeOverrides(false);
        LOG.info("-------------------LOAN PRODUCT CREATED WITH ATTRIBUTE CONFIGURATION AS FALSE---------------------- {}", loanProductID);
        /*
         * Try to override attribute values in loan account when attribute configurations are set to false at product
         * level
         */
        loanID = applyForLoanApplicationWithProductConfigurationAsFalse(clientID, loanProductID, proposedAmount);
        LOG.info("--------------------------LOAN CREATED WITH ID------------------------- {}", loanID);
        validateIfValuesAreNotOverridden(loanID, loanProductID);
    }

    /**
     * Test case to verify Loan Foreclosure.
     */
    @Test
    public void testLoanForeclosure() {

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();

        Long flatAmountChargeOne = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 50.0, false).getResourceId();
        addCharges(charges, flatAmountChargeOne, 50.0, "01 October 2011");
        Long flatAmountChargeTwo = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, true).getResourceId();
        addCharges(charges, flatAmountChargeTwo, 100.0, "15 December 2011");

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        final Long loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "10,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        verifyLoanIsPending(loanID);

        LOG.info("----------------------------------- APPROVE LOAN -----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("20 September 2011", loanID));

        LOG.info("----------------------------------- DISBURSE LOAN ----------------------------------------");
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10,000.00");
        LOG.info("DISBURSE {}", loanStatusHashMap);
        verifyLoanIsActive(loanStatusHashMap);

        LOG.info("---------------------------------- Make repayment 1 --------------------------------------");
        makeRepayment("20 October 2011", Float.parseFloat("2676.24"), loanID);

        LOG.info("---------------------------------- FORECLOSE LOAN ----------------------------------------");
        forecloseLoan(loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("08 November 2011").dateFormat(DATETIME_PATTERN)
                .locale(LOCALE).note("Foreclosure"));

        // retrieving the loan status
        loanStatusHashMap = getLoanDetails(loanID);
        // verifying the loan status is closed
        verifyLoanStatus(loanStatusHashMap, GetLoansLoanIdStatus::getClosed);
        // verifying the loan sub-status is foreclosed
        assertEquals("Foreclosed", loanStatusHashMap.getSubStatus().getValue());

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_INTEREST_FIRST_STRATEGY_AND_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, LoanTestData.InterestRecalculationCompoundingMethod.INTEREST,
                LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, Collections.emptyList());

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = getLoanFutureRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = Float.parseFloat("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        ExpectedInstallment paymentday = new ExpectedInstallment(addDays(today, -5), "3990.09", "9.91", "0", "0");
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        expectedvalues.add(paymentday);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.31", "11.6", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1009.84", "4.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_INTEREST_FIRST_STRATEGY_AND_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS_EARLY_REPAYMENT() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, LoanTestData.InterestRecalculationCompoundingMethod.INTEREST,
                LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS, LoanTestData.RecalculationRestFrequencyType.DAILY, 1,
                LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Long loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, Collections.emptyList());

        Assertions.assertNotNull(loanID);
        verifyLoanIsPending(loanID);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(LOAN_DISBURSEMENT_DATE, loanID));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = getLoanFutureRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        // early repayment - pay exact due amount 2 days before due date
        Float earlyPayment = Float.parseFloat("2528.91");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -2);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = getLoanRepaymentSchedule(loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        // early-repayment
        addRepaymentValues(expectedvalues, todaysDate, 5, true, "2504.13", "24.78", "0.0", "0.0");

        addRepaymentValues(expectedvalues, todaysDate, 2, true, "2522.33", "6.58", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2490.78", "11.5", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
        String prepayAmount = String.valueOf(prepayDetail.getAmount());
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        verifyLoanAccountIsClosed(loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculationMakePrepaymentAfterRepayment() {
        try {
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(true));
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            Calendar currentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Long clientID = createClient();
            verifyClientCreatedOnServer(clientID);
            final Long loanProductID = createLoanProductWithInterestRecalculationAndInstallmentAmount(
                    INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, LoanTestData.InterestRecalculationCompoundingMethod.NONE,
                    LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, 12);

            final Long loanID = applyForLoanApplicationForInterestRecalculationWithFirstRepaymentDate(clientID, loanProductID,
                    loanDisbursementDate, INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            verifyLoanIsPending(loanID);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanID));

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID));

            List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
            Assertions.assertNotNull(loanSchedule);
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());
            //
            Float earlyPayment = Float.parseFloat("3000");
            makeRepayment(loanFirstRepaymentDate, earlyPayment, loanID);

            GetLoansLoanIdTransactionsTemplateResponse prepayDetail = getPrepayAmount(loanID);
            String prepayAmount = String.valueOf(prepayDetail.getAmount());
            String loanPrepaymentDate = dateFormat.format(currentDate.getTime());
            makeRepayment(loanPrepaymentDate, Float.parseFloat(prepayAmount), loanID);
            verifyLoanAccountIsClosed(loanID);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanScheduleWithInterestRecalculationMakeAdvancePaymentTillSettlement() {
        try {
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(true));
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            Calendar currentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Long clientID = createClient();
            verifyClientCreatedOnServer(clientID);
            final Long loanProductID = createLoanProductWithInterestRecalculationAndInstallmentAmount(
                    INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, LoanTestData.InterestRecalculationCompoundingMethod.NONE,
                    LoanTestData.RescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanTestData.PreClosureInterestCalculationStrategy.TILL_PRE_CLOSE_DATE, null, 12);

            final Long loanID = applyForLoanApplicationForInterestRecalculationWithFirstRepaymentDate(clientID, loanProductID,
                    loanDisbursementDate, INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            verifyLoanIsPending(loanID);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanID));

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID));

            List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
            Assertions.assertNotNull(loanSchedule);
            Calendar repaymentDate = (Calendar) firstRepaymentDate.clone();
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());
            //
            Float earlyPayment = Float.parseFloat("3000");
            String retrieveDueDate = null;
            Float amount = null;
            makeRepayment(loanFirstRepaymentDate, earlyPayment, loanID);
            for (int i = 1; i < loanSchedule.size(); i++) {

                retrieveDueDate = dateFormat.format(repaymentDate.getTime());
                amount = ((Number) loanSchedule.get(i).getPrincipalOriginalDue()).floatValue()
                        + ((Number) loanSchedule.get(i).getInterestOriginalDue()).floatValue();
                if (currentDate.after(repaymentDate)) {
                    makeRepayment(retrieveDueDate, amount, loanID);
                } else {
                    break;
                }
                repaymentDate.add(Calendar.MONTH, 1);
            }
            final String futureRepaymentDate = retrieveDueDate;
            final Float futureRepaymentAmount = amount;
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeRepayment(futureRepaymentDate, futureRepaymentAmount, loanID));
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.cannot.be.a.future.date"));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testCollateralDataIsAvailableWhenRequested() {
        // given

        Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        Long clientId = createClient();
        verifyClientCreatedOnServer(clientId);

        Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        collaterals.add(collateral(clientCollateralId, BigDecimal.ONE));

        Long loanProductId = createLoanProduct(false, NONE);

        // when
        Long loanId = applyForLoanApplication(clientId, loanProductId, null, null, "12,000.00", collaterals);

        // then
        List<GetLoansLoanIdCollateralData> loanCollateral = getLoanDetails(loanId).getCollateral();
        assertEquals(clientCollateralId, loanCollateral.get(0).getClientCollateralId());
    }

    @Test
    public void undoWaivedChargeTransactionDoesNotExist() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> undoWaiveLoanCharge(-1L, -2L, new PutChargeTransactionChangesRequest().id(-2L).loanId(-1L)));
        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.loan.transaction.id.invalid"));
        assertTrue(exception.getMessage().contains("Transaction with identifier -2 does not exist for loan with identifier -1."));
    }

    @Test
    public void chargeAdjustmentChargeWrongParams() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> chargeAdjustment(0L, 0L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanId.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanChargeId.not.greater.than.zero"));
        exception = assertThrows(CallFailedRuntimeException.class,
                () -> chargeAdjustment(1L, 0L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanChargeId.not.greater.than.zero"));
        exception = assertThrows(CallFailedRuntimeException.class,
                () -> chargeAdjustment(1L, 1L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
    }

    @Test
    public void chargeAdjustmentChargeDoesNotExist() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient("01 January 2011");

        final Long loanID = applyForLoanApplication(clientID, loanProductID);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> chargeAdjustment((long) loanID, 1L, new PostLoansLoanIdChargesChargeIdRequest().amount(1.0)));
        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid"));
    }

    @Test
    public void chargeAdjustmentChargeDoesNotExistForLoan() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient("01 January 2011");

        final Long loanID = applyForLoanApplication(clientID, loanProductID);

        verifyLoanIsPending(loanID);

        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        verifyLoanIsActive(loanStatusHashMap);

        Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0, true).getResourceId();
        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
        Long penalty1LoanChargeId = addLoanCharge(loanID, penalty, penaltyCharge1AddedDate, 10.0).getResourceId();

        final Long loanID2 = applyForLoanApplication(clientID, loanProductID);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> chargeAdjustment(loanID2, penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(1.0)));
        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));
    }

    @Test
    public void chargeAdjustmentForUnpaidCharge() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 November 2022");
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0, true).getResourceId();
            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
            assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
            Long penalty1LoanChargeId = addLoanCharge(loanID, penalty, penaltyCharge1AddedDate, 10.0).getResourceId();

            assertNoAccrualTransactions(loanID);

            loanSchedule = getLoanRepaymentSchedule(loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
            assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
            assertEquals(1010.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
            assertEquals(0, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

            GetLoansLoanIdSummary loanSummary = getLoanDetails(loanID).getSummary();
            assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
            assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
            assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
            assertEquals(1010.0f, loanSummary.getTotalOutstanding().floatValue());
            assertEquals(0.0f, loanSummary.getTotalWaived().floatValue());

            String externalId = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).externalId(externalId).paymentTypeId(1L));

            loanSchedule = getLoanRepaymentSchedule(loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesPaid().floatValue());
            assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
            assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
            assertEquals(10.0f, loanSchedule.get(1).getTotalPaidForPeriod().floatValue());

            loanSummary = getLoanDetails(loanID).getSummary();
            assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
            assertEquals(0.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
            assertEquals(10.0f, loanSummary.getPenaltyChargesPaid().floatValue());
            assertEquals(1000.0f, loanSummary.getTotalOutstanding().floatValue());

            GetLoansLoanIdTransactionsTransactionIdResponse chargeAdjustmentTransaction = getLoanTransactionDetails(loanID,
                    chargeAdjustmentResponse.getSubResourceId());
            assertEquals(10.0, chargeAdjustmentTransaction.getAmount());
            assertEquals(10.0, chargeAdjustmentTransaction.getPenaltyChargesPortion());
            assertEquals("loanTransactionType.chargeAdjustment", chargeAdjustmentTransaction.getType().getCode());
            assertEquals(externalId, chargeAdjustmentTransaction.getExternalId());
            GetLoanTransactionRelation transactionRelation = chargeAdjustmentTransaction.getTransactionRelations().iterator().next();
            assertEquals(chargeAdjustmentResponse.getSubResourceId(), transactionRelation.getFromLoanTransaction());
            assertEquals((long) penalty1LoanChargeId, transactionRelation.getToLoanCharge());
            assertEquals("CHARGE_ADJUSTMENT", transactionRelation.getRelationType());
            assertEquals(1L, chargeAdjustmentTransaction.getPaymentDetailData().getPaymentType().getId());

            PostLoansLoanIdTransactionsResponse repaymentResult = makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("06 September 2022").locale("en").transactionAmount(5.0));

            loanSchedule = getLoanRepaymentSchedule(loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesPaid().floatValue());
            assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getPrincipalDue().floatValue());
            assertEquals(5.0f, loanSchedule.get(1).getPrincipalPaid().floatValue());
            assertEquals(995.0f, loanSchedule.get(1).getPrincipalOutstanding().floatValue());
            assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
            assertEquals(995.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
            assertEquals(15.0f, loanSchedule.get(1).getTotalPaidForPeriod().floatValue());

            loanSummary = getLoanDetails(loanID).getSummary();
            assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
            assertEquals(0.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
            assertEquals(10.0f, loanSummary.getPenaltyChargesPaid().floatValue());
            assertEquals(1000.0f, loanSummary.getPrincipalDisbursed().floatValue());
            assertEquals(995.0f, loanSummary.getPrincipalOutstanding().floatValue());
            assertEquals(5.0f, loanSummary.getPrincipalPaid().floatValue());
            assertEquals(995.0f, loanSummary.getTotalOutstanding().floatValue());

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            GetLoansLoanIdTransactions replayedTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> externalId.equals(t.getExternalId())).findFirst().get();

            assertEquals(10.0, Utils.getDoubleValue(replayedTransaction.getAmount()));
            assertEquals(5.0, Utils.getDoubleValue(replayedTransaction.getPenaltyChargesPortion()));
            assertEquals(5.0, Utils.getDoubleValue(replayedTransaction.getPrincipalPortion()));
            assertEquals("loanTransactionType.chargeAdjustment", replayedTransaction.getType().getCode());
            assertEquals(externalId, replayedTransaction.getExternalId());

            Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations = replayedTransaction.getTransactionRelations();
            for (GetLoansLoanIdLoanTransactionRelation loanTransactionRelation : transactionRelations) {
                if ("CHARGE_ADJUSTMENT".equals(loanTransactionRelation.getRelationType())) {
                    assertEquals(replayedTransaction.getId(), loanTransactionRelation.getFromLoanTransaction());
                    assertEquals((long) penalty1LoanChargeId, loanTransactionRelation.getToLoanCharge());
                }
            }

            String uuid = UUID.randomUUID().toString();
            reverseLoanTransaction((long) loanID, replayedTransaction.getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("08 September 2022")
                            .transactionAmount(0.0).locale("en").reversalExternalId(uuid));

            // Should fail due to external id collusion
            assertThrows(CallFailedRuntimeException.class,
                    () -> reverseLoanTransaction((long) loanID, repaymentResult.getResourceId(),
                            new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                                    .transactionDate("08 September 2022").transactionAmount(0.0).locale("en").reversalExternalId(uuid)));

            loanSchedule = getLoanRepaymentSchedule(loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
            assertEquals(5.0f, loanSchedule.get(1).getPenaltyChargesPaid().floatValue());
            assertEquals(5.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getPrincipalDue().floatValue());
            assertEquals(0.0f, loanSchedule.get(1).getPrincipalPaid().floatValue());
            assertEquals(1000.0f, loanSchedule.get(1).getPrincipalOutstanding().floatValue());
            assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
            assertEquals(1005.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
            assertEquals(5.0f, loanSchedule.get(1).getTotalPaidForPeriod().floatValue());

            loanSummary = getLoanDetails(loanID).getSummary();
            assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
            assertEquals(5.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
            assertEquals(5.0f, loanSummary.getPenaltyChargesPaid().floatValue());
            assertEquals(1000.0f, loanSummary.getPrincipalDisbursed().floatValue());
            assertEquals(1000.0f, loanSummary.getPrincipalOutstanding().floatValue());
            assertEquals(0.0f, loanSummary.getPrincipalPaid().floatValue());
            assertEquals(1005.0f, loanSummary.getTotalOutstanding().floatValue());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void chargeAdjustmentAccountingValidation() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 November 2022");
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();
            final PostGLAccountsResponse uniqueIncomeAccountForFee = accountHelper.createGLAccount(new PostGLAccountsRequest()
                    .type(GLAccountType.INCOME.getValue())
                    .glCode(Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .manualEntriesAllowed(true)
                    .name(Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5)).usage(1));
            final PostGLAccountsResponse uniqueIncomeAccountForPenalty = accountHelper.createGLAccount(new PostGLAccountsRequest()
                    .type(GLAccountType.INCOME.getValue())
                    .glCode(Utils.uniqueRandomStringGenerator("UNIQUE_PENALTY_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .manualEntriesAllowed(true)
                    .name(Utils.uniqueRandomStringGenerator("UNIQUE_PENALTY_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .usage(1));

            PostChargesResponse penaltyCharge = chargesHelper.createCharge(new ChargeRequest().penalty(true).amount(10.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));

            PostChargesResponse feeCharge = chargesHelper.createCharge(new ChargeRequest().penalty(false).amount(9.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));

            PostLoanProductsRequest product = baseLoanProduct()//
                    .principal(1000.00)//
                    .numberOfRepayments(1)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                    .interestRatePerPeriod(0.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestType(LoanTestData.InterestType.FLAT)//
                    .daysInMonthType(30)//
                    .daysInYearType(365)//
                    .graceOnPrincipalPayment(0)//
                    .graceOnInterestPayment(0)//
                    .feeToIncomeAccountMappings(List.of(new LoanProductChargeToGLAccountMapper().chargeId(feeCharge.getResourceId())
                            .incomeAccountId(uniqueIncomeAccountForFee.getResourceId())))//
                    .penaltyToIncomeAccountMappings(List.of(new LoanProductChargeToGLAccountMapper().chargeId(penaltyCharge.getResourceId())
                            .incomeAccountId(uniqueIncomeAccountForPenalty.getResourceId())));
            withAccounting(product, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            // withFeeAndPenaltyAssetAccount: fee and penalty receivables point at a dedicated asset account
            product.receivableFeeAccountId(assetFeeAndPenaltyAccount.getAccountID().longValue())//
                    .receivablePenaltyAccountId(assetFeeAndPenaltyAccount.getAccountID().longValue());
            final Long loanProductID = createLoanProduct(product);

            final Long clientID = createClient();

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            List<GetLoansLoanIdRepaymentPeriod> loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));

            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
            Long penaltyLoanChargeId = addLoanCharge(loanID, penaltyCharge.getResourceId(), penaltyCharge1AddedDate, 10.0).getResourceId();

            final String penalty1LoanChargeDate = dateTimeFormatter.format(targetDate);
            runPeriodicAccrualAccounting(penalty1LoanChargeDate);

            loanDetails = getLoanDetails(loanID);
            List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();
            assertEquals(10.0, Utils.getDoubleValue(transactions.get(1).getAmount()));
            assertTrue(transactions.get(1).getType().getAccrual());
            assertEquals(10.0, Utils.getDoubleValue(transactions.get(1).getPenaltyChargesPortion()));
            Long accrualTransactionId = transactions.get(1).getId();

            List<JournalEntryTransactionItem> journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + accrualTransactionId)
                    .getPageItems();
            assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(1010.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(1010.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));

            GetLoansLoanIdSummary loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(1010.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            String externalId = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) penaltyLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).externalId(externalId));

            loanDetails = getLoanDetails(loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(1010.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalPaidForPeriod()));

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            transactions = loanDetails.getTransactions();
            assertEquals(10.0, Utils.getDoubleValue(transactions.get(2).getAmount()));
            assertTrue(transactions.get(2).getType().getChargeAdjustment());
            assertEquals(10.0, Utils.getDoubleValue(transactions.get(2).getPenaltyChargesPortion()));
            Long chargeAdjustmentTransactionId = transactions.get(2).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            String uuid = UUID.randomUUID().toString();
            reverseLoanTransaction((long) loanID, chargeAdjustmentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("08 September 2022")
                            .transactionAmount(0.0).locale("en").reversalExternalId(uuid));

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());
            assertEquals(10.0f, journalEntries.get(2).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), journalEntries.get(2).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(2).getEntryType().getValue());
            assertEquals(10.0f, journalEntries.get(3).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(3).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(3).getEntryType().getValue());

            targetDate = LocalDate.of(2022, 9, 10);
            final String feeCharge1AddedDate = dateTimeFormatter.format(targetDate);
            Long feeLoanChargeId = addLoanCharge(loanID, feeCharge.getResourceId(), feeCharge1AddedDate, 3.0).getResourceId();

            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            final String feeLoanChargeDate = dateTimeFormatter.format(targetDate);
            runPeriodicAccrualAccounting(feeLoanChargeDate);

            loanDetails = getLoanDetails(loanID);
            transactions = loanDetails.getTransactions();
            assertEquals(3.0, Utils.getDoubleValue(transactions.get(2).getAmount()));
            assertTrue(transactions.get(2).getType().getAccrual());
            assertEquals(3.0, Utils.getDoubleValue(transactions.get(2).getFeeChargesPortion()));
            assertTrue(StringUtils.isNotBlank(transactions.get(2).getExternalId()));
            accrualTransactionId = transactions.get(2).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + accrualTransactionId).getPageItems();
            // FINERACT-2323: Journal entry order changed - DEBIT entries come first, then CREDIT entries
            assertEquals(3.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(3.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesDue()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesOutstanding()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesCharged()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesOutstanding()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("11 September 2022").locale("en").transactionAmount(5.0));

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(2.0).externalId(externalId));

            loanDetails = getLoanDetails(loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(7.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesPaid()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesPaid()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalDue()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalOutstanding()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(1006.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));
            assertEquals(7.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalPaidForPeriod()));

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(7.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesPaid()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesCharged()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getFeeChargesPaid()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSummary.getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getPrincipalPaid()));
            assertEquals(7.0, Utils.getDoubleValue(loanSummary.getTotalRepayment()));
            assertEquals(1006.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            transactions = loanDetails.getTransactions();
            assertEquals(2.0, Utils.getDoubleValue(transactions.get(5).getAmount()));
            assertTrue(transactions.get(4).getType().getChargeAdjustment());
            assertEquals(2.0, Utils.getDoubleValue(transactions.get(5).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(5).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(5).getPrincipalPortion()));
            chargeAdjustmentTransactionId = transactions.get(5).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(2.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(2.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) penaltyLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(7.0).externalId(externalId));

            loanDetails = getLoanDetails(loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesDue()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalDue()));
            assertEquals(1.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalPaid()));
            assertEquals(999.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalOutstanding()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(999.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));
            assertEquals(14.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalPaidForPeriod()));

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesPaid()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesCharged()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getFeeChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesPaid()));
            assertEquals(999.0, Utils.getDoubleValue(loanSummary.getPrincipalOutstanding()));
            assertEquals(1.0, Utils.getDoubleValue(loanSummary.getPrincipalPaid()));
            assertEquals(14.0, Utils.getDoubleValue(loanSummary.getTotalRepayment()));
            assertEquals(999.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            transactions = loanDetails.getTransactions();
            assertEquals(7.0, Utils.getDoubleValue(transactions.get(6).getAmount()));
            assertTrue(transactions.get(6).getType().getChargeAdjustment());
            assertEquals(3.0, Utils.getDoubleValue(transactions.get(6).getPenaltyChargesPortion()));
            assertEquals(3.0, Utils.getDoubleValue(transactions.get(6).getFeeChargesPortion()));
            assertEquals(1.0, Utils.getDoubleValue(transactions.get(6).getPrincipalPortion()));
            chargeAdjustmentTransactionId = transactions.get(6).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(7.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            if (assetAccount.getAccountID() == journalEntries.get(1).getGlAccountId().intValue()) {
                assertEquals(1.0f, journalEntries.get(1).getAmount().floatValue());
                assertEquals(assetAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
                assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

                assertEquals(6.0f, journalEntries.get(2).getAmount().floatValue());
                assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(2).getGlAccountId().intValue());
                assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());
            } else {
                assertEquals(1.0f, journalEntries.get(2).getAmount().floatValue());
                assertEquals(assetAccount.getAccountID(), journalEntries.get(2).getGlAccountId().intValue());
                assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

                assertEquals(6.0f, journalEntries.get(1).getAmount().floatValue());
                assertEquals(assetFeeAndPenaltyAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
                assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());
            }

            makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("13 September 2022").locale("en").transactionAmount(998.0));

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).externalId(externalId));

            loanDetails = getLoanDetails(loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesDue()));
            assertEquals(10.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPenaltyChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesDue()));
            assertEquals(3.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getFeeChargesOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalDue()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getPrincipalOutstanding()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalDueForPeriod()));
            assertEquals(0.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalOutstandingForPeriod()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSchedulePeriods.get(1).getTotalPaidForPeriod()));

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesCharged()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(loanSummary.getPenaltyChargesPaid()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesCharged()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getFeeChargesOutstanding()));
            assertEquals(3.0, Utils.getDoubleValue(loanSummary.getFeeChargesPaid()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getPrincipalOutstanding()));
            assertEquals(1000.0, Utils.getDoubleValue(loanSummary.getPrincipalPaid()));
            assertEquals(1013.0, Utils.getDoubleValue(loanSummary.getTotalRepayment()));
            assertEquals(0.0, Utils.getDoubleValue(loanSummary.getTotalOutstanding()));

            transactions = loanDetails.getTransactions();
            assertEquals(1.0, Utils.getDoubleValue(transactions.get(8).getAmount()));
            assertTrue(transactions.get(8).getType().getChargeAdjustment());
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(8).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(8).getFeeChargesPortion()));
            assertEquals(1.0, Utils.getDoubleValue(transactions.get(8).getPrincipalPortion()));
            chargeAdjustmentTransactionId = transactions.get(8).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(1.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(1.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(assetAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = chargeAdjustment((long) loanID, (long) penaltyLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).externalId(externalId));

            loanDetails = getLoanDetails(loanID);

            transactions = loanDetails.getTransactions();
            assertEquals(1.0, Utils.getDoubleValue(transactions.get(9).getAmount()));
            assertTrue(transactions.get(9).getType().getChargeAdjustment());
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(9).getPenaltyChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(9).getFeeChargesPortion()));
            assertEquals(0.0, Utils.getDoubleValue(transactions.get(9).getPrincipalPortion()));
            assertEquals(1.0, Utils.getDoubleValue(transactions.get(9).getOverpaymentPortion()));
            chargeAdjustmentTransactionId = transactions.get(9).getId();

            journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId).getPageItems();
            assertEquals(1.0f, journalEntries.get(0).getAmount().floatValue());
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), journalEntries.get(0).getGlAccountId().intValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
            assertEquals(1.0f, journalEntries.get(1).getAmount().floatValue());
            assertEquals(overpaymentAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertTrue(loanDetails.getStatus().getOverpaid());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void undoWaivedChargeWaiveTransactionDoesNotExist() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient("01 January 2011");

        final Long loanID = applyForLoanApplication(clientID, loanProductID);

        verifyLoanIsPending(loanID);

        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        verifyLoanIsActive(loanStatusHashMap);
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        final Long loanTransactionId = loanDetails.getTransactions().get(0).getId();
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> undoWaiveLoanCharge(loanID,
                loanTransactionId, new PutChargeTransactionChangesRequest().id(loanTransactionId).loanId(loanID)));
        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.loan.transaction.undo.waive.charge"));
        assertTrue(exception.getMessage().contains("Transaction is not a waive charge type."));
    }

    @Test
    public void undoWaivedCharge() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0, true).getResourceId();
        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient("01 January 2011");

        final Long loanID = applyForLoanApplication(clientID, loanProductID);

        verifyLoanIsPending(loanID);

        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

        GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        verifyLoanIsActive(loanStatusHashMap);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1000.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1000.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
        Long penalty1LoanChargeId = addLoanCharge(loanID, penalty, penaltyCharge1AddedDate, 10.0).getResourceId();

        assertNoAccrualTransactions(loanID);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(0, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        GetLoansLoanIdSummary loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1010.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getTotalWaived().floatValue());

        waiveWholeLoanCharge(loanID, penalty1LoanChargeId);

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesWaived().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1000.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1000.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(10.0f, loanSummary.getTotalWaived().floatValue());

        List<GetLoansLoanIdTransactions> transactions = getLoanDetails(loanID).getTransactions();
        assertEquals(10.0f, transactions.get(1).getAmount().floatValue());
        assertEquals(9, transactions.get(1).getType().getId().intValue());
        Long waiveTransactionId = transactions.get(1).getId();
        undoWaiveLoanCharge(loanID, waiveTransactionId, new PutChargeTransactionChangesRequest().id(waiveTransactionId).loanId(loanID));

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(0, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1010.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getTotalWaived().floatValue());

        transactions = getLoanDetails(loanID).getTransactions();
        assertEquals(10.0f, transactions.get(1).getAmount().floatValue());
        assertEquals(9, transactions.get(1).getType().getId().intValue());
        assertEquals(true, transactions.get(1).getManuallyReversed());

        Long fee = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0, false).getResourceId();

        final String feeCharge1AddedDate = dateTimeFormatter.format(targetDate);
        Long fee1LoanChargeId = addLoanCharge(loanID, fee, feeCharge1AddedDate, 10.0).getResourceId();

        runPeriodicAccrualAccounting(feeCharge1AddedDate);

        transactions = getLoanDetails(loanID).getTransactions();
        assertEquals(10, transactions.get(2).getType().getId().intValue());
        assertEquals(20.0f, transactions.get(2).getAmount().floatValue());
        Long accrualTransactionId = transactions.get(2).getId();

        List<JournalEntryTransactionItem> journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + accrualTransactionId)
                .getPageItems();
        // FINERACT-2323: Due to multiple legs for journal entries, the system now uses charge-specific GL accounts
        // instead of product-level defaults. The journal entry structure has changed with alternating DEBIT/CREDIT
        // pairs.
        // This transaction accrues both penalty (10) and fee (10) charges.
        // Entry 0: DEBIT for penalty receivable
        assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
        assertEquals(assetAccount.getAccountID(), journalEntries.get(0).getGlAccountId().intValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
        // Entry 1: CREDIT for penalty income
        assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
        assertEquals(incomeAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());
        // Entry 2: DEBIT for fee receivable (uses charge-specific or fallback account due to FINERACT-2323)
        assertEquals(10.0f, journalEntries.get(2).getAmount().floatValue());
        // Due to FINERACT-2323, the fee uses a different asset account
        assertEquals(assetAccount.getAccountID(), journalEntries.get(2).getGlAccountId().intValue());
        assertEquals("DEBIT", journalEntries.get(2).getEntryType().getValue());
        // Entry 3: CREDIT for fee income
        assertEquals(10.0f, journalEntries.get(3).getAmount().floatValue());
        assertEquals(incomeAccount.getAccountID(), journalEntries.get(3).getGlAccountId().intValue());
        assertEquals("CREDIT", journalEntries.get(3).getEntryType().getValue());

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1020.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1020.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(0, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1020.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getTotalWaived().floatValue());

        waiveWholeLoanCharge(loanID, fee1LoanChargeId);

        transactions = getLoanDetails(loanID).getTransactions();
        assertEquals(10.0f, transactions.get(3).getAmount().floatValue());
        assertEquals(9, transactions.get(3).getType().getId().intValue());
        Long waive2TransactionId = transactions.get(3).getId();

        journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + waive2TransactionId).getPageItems();
        assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
        assertEquals(expenseAccount.getAccountID(), journalEntries.get(0).getGlAccountId().intValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
        assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
        assertEquals(assetAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1020.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1010.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1010.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(10.0f, loanSummary.getTotalWaived().floatValue());

        undoWaiveLoanCharge(loanID, waive2TransactionId, new PutChargeTransactionChangesRequest().id(waive2TransactionId).loanId(loanID));

        transactions = getLoanDetails(loanID).getTransactions();
        assertEquals(10.0f, transactions.get(3).getAmount().floatValue());
        assertEquals(9, transactions.get(3).getType().getId().intValue());
        assertEquals(true, transactions.get(3).getManuallyReversed());

        journalEntries = journalHelper.getJournalEntriesByTransactionId("L" + waive2TransactionId).getPageItems();
        assertEquals(10.0f, journalEntries.get(0).getAmount().floatValue());
        assertEquals(expenseAccount.getAccountID(), journalEntries.get(0).getGlAccountId().intValue());
        assertEquals("CREDIT", journalEntries.get(0).getEntryType().getValue());
        assertEquals(10.0f, journalEntries.get(1).getAmount().floatValue());
        assertEquals(assetAccount.getAccountID(), journalEntries.get(1).getGlAccountId().intValue());
        assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());
        assertEquals(10.0f, journalEntries.get(2).getAmount().floatValue());
        assertEquals(expenseAccount.getAccountID(), journalEntries.get(2).getGlAccountId().intValue());
        assertEquals("DEBIT", journalEntries.get(2).getEntryType().getValue());
        assertEquals(10.0f, journalEntries.get(3).getAmount().floatValue());
        assertEquals(assetAccount.getAccountID(), journalEntries.get(3).getGlAccountId().intValue());
        assertEquals("CREDIT", journalEntries.get(3).getEntryType().getValue());

        loanSchedule = getLoanRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesDue().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getFeeChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesDue().floatValue());
        assertEquals(0.0f, loanSchedule.get(1).getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSchedule.get(1).getPenaltyChargesOutstanding().floatValue());
        assertEquals(1020.0f, loanSchedule.get(1).getTotalDueForPeriod().floatValue());
        assertEquals(1020.0f, loanSchedule.get(1).getTotalOutstandingForPeriod().floatValue());
        assertEquals(0, loanSchedule.get(1).getTotalWaivedForPeriod().floatValue());

        loanSummary = getLoanDetails(loanID).getSummary();
        assertEquals(10.0f, loanSummary.getPenaltyChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getPenaltyChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getPenaltyChargesWaived().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesCharged().floatValue());
        assertEquals(10.0f, loanSummary.getFeeChargesOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getFeeChargesWaived().floatValue());
        assertEquals(1020.0f, loanSummary.getTotalOutstanding().floatValue());
        assertEquals(0.0f, loanSummary.getTotalWaived().floatValue());
    }

    @Test
    public void chargeOff() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("04 September 2022");
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();
            String randomText = UUID.randomUUID().toString();
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterestMultiDisbursement(assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
                chargeOffLoan(loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en")
                        .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString()).chargeOffReasonId(chargeOffReasonId));
            });

            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.active"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> undoChargeOffLoan(loanID));
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.active"));

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithTransactionAmount(loanID, "02 September 2022", "1000");
            loanStatusHashMap = disburseLoanWithTransactionAmount(loanID, "03 September 2022", "1000");
            verifyLoanIsActive(loanStatusHashMap);

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                chargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("1 October 2022").locale("en")
                        .dateFormat(DATETIME_PATTERN).chargeOffReasonId(chargeOffReasonId));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.cannot.be.a.future.date"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(2000.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertFalse(loanDetails.getChargedOff());
            assertNull(loanDetails.getSummary().getChargeOffReasonId());
            assertNull(loanDetails.getSummary().getChargeOffReason());
            assertNull(loanDetails.getTimeline().getChargedOffOnDate());
            assertNull(loanDetails.getTimeline().getChargedOffByUsername());
            assertNull(loanDetails.getTimeline().getChargedOffByFirstname());
            assertNull(loanDetails.getTimeline().getChargedOffByLastname());

            Long flatPenaltySpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 3.0, true)
                    .getResourceId();
            addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "04 September 2022", 3.0);
            Long chargeId = addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "04 September 2022", 5.0).getResourceId();

            PostLoansLoanIdChargesChargeIdResponse waiveChargeResponse = waiveLoanCharge((long) loanID, (long) chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().locale(LOCALE));

            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en")
                    .dateFormat(DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = getLoanDetails(loanID);
            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(2003.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertTrue(loanDetails.getChargedOff());
            assertEquals((long) chargeOffReasonId, loanDetails.getSummary().getChargeOffReasonId());
            assertEquals(randomText, loanDetails.getSummary().getChargeOffReason());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTimeline().getChargedOffOnDate());
            assertEquals("mifos", loanDetails.getTimeline().getChargedOffByUsername());
            assertEquals("App", loanDetails.getTimeline().getChargedOffByFirstname());
            assertEquals("Administrator", loanDetails.getTimeline().getChargedOffByLastname());

            GetLoansLoanIdTransactions chargeOffTransaction = loanDetails.getTransactions().get(loanDetails.getTransactions().size() - 1);

            assertEquals(2003.0, Utils.getDoubleValue(chargeOffTransaction.getAmount()));
            assertEquals(2000.0, Utils.getDoubleValue(chargeOffTransaction.getPrincipalPortion()));
            assertEquals(3.0, Utils.getDoubleValue(chargeOffTransaction.getPenaltyChargesPortion()));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                chargeOffLoan(loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en")
                        .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString()).chargeOffReasonId(chargeOffReasonId));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.already.charged.off"));

            CallFailedRuntimeException chargeAddingError = assertThrows(CallFailedRuntimeException.class,
                    () -> addLoanCharge(loanID, flatPenaltySpecifiedDueDate, "04 September 2022", 3.0));
            assertEquals(403, chargeAddingError.getStatus());
            assertTrue(chargeAddingError.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                undoWaiveLoanCharge(loanID, waiveChargeResponse.getSubResourceId(), new PutChargeTransactionChangesRequest());
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            undoChargeOffLoan(loanID);

            loanDetails = getLoanDetails(loanID);
            assertFalse(loanDetails.getChargedOff());
            assertNull(loanDetails.getSummary().getChargeOffReasonId());
            assertNull(loanDetails.getSummary().getChargeOffReason());
            assertNull(loanDetails.getTimeline().getChargedOffOnDate());

            GetLoansLoanIdTransactions undoChargeOffTransaction = loanDetails.getTransactions()
                    .get(loanDetails.getTransactions().size() - 1);
            assertTrue(undoChargeOffTransaction.getType().getChargeoff());
            assertTrue(undoChargeOffTransaction.getManuallyReversed());

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                undoChargeOffLoan(loanID);
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.charged.off"));

            updateBusinessDate("08 September 2022");

            PostLoansLoanIdTransactionsResponse loanRepaymentResponse = makeLoanRepayment((long) loanID,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en")
                            .transactionAmount(5.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                chargeOffLoan(loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("04 September 2022").locale("en")
                        .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString()).chargeOffReasonId(chargeOffReasonId));
            });

            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.charge.off.is.before.than.the.last.user.transaction"));

            chargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionDate("06 September 2022").locale("en")
                    .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString()).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = getLoanDetails(loanID);
            chargeOffTransaction = loanDetails.getTransactions().get(loanDetails.getTransactions().size() - 1);

            assertEquals(1998.0, Utils.getDoubleValue(chargeOffTransaction.getAmount()));
            assertEquals(1998.0, Utils.getDoubleValue(chargeOffTransaction.getPrincipalPortion()));

            makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("07 September 2022").locale("en").transactionAmount(5.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                undoChargeOffLoan(loanID);
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.charge.off.is.not.the.last.user.transaction"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                writeOffLoan(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                closeLoan(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022")
                        .locale("en"));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                forecloseLoan(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                closeRescheduledLoan(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            CallFailedRuntimeException disbursementDetailError = assertThrows(CallFailedRuntimeException.class,
                    () -> addAndDeleteDisbursementDetail(loanID, List
                            .of(new DisbursementDetail().expectedDisbursementDate("05 September 2022").principal(new BigDecimal("200")))));
            assertEquals(403, disbursementDetailError.getStatus());
            assertTrue(disbursementDetailError.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                undoLastDisbursement(loanID, new PostLoansLoanIdRequest());
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                undoDisbursement(loanID, new PostLoansLoanIdRequest());
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                makeCreditBalanceRefund(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en").transactionAmount(5.0));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                disburseLoan(loanID, new PostLoansLoanIdRequest().actualDisbursementDate("4 September 2022")
                        .transactionAmount(new BigDecimal("10")).locale("en").dateFormat(DATETIME_PATTERN));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                disburseLoan(loanID, new PostLoansLoanIdRequest().actualDisbursementDate("7 September 2022")
                        .transactionAmount(new BigDecimal("10")).locale("en").dateFormat(DATETIME_PATTERN));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"));

            makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("07 September 2022").locale("en").transactionAmount(5000.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                transactionHelper.makeRefundByCash(loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en").transactionAmount(5.0));
            });
            assertEquals(403, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("08 September 2022").locale("en").transactionAmount(3007.0));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testCloseOpenMaturityDate() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            LocalDate expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            LocalDate actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertTrue(DateUtils.isEqual(expectedMaturityDate, actualMaturityDate));

            makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            makeRepayment("05 September 2022", Float.parseFloat("700"), loanID);

            loanDetails = getLoanDetails(loanID);

            expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertNotNull(expectedMaturityDate);
            assertNull(actualMaturityDate);

            reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("04 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanID);

            expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertNotNull(expectedMaturityDate);
            assertNotNull(actualMaturityDate);

            assertTrue(expectedMaturityDate.isEqual(actualMaturityDate));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testReverseReplay() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            makeRepayment("05 September 2022", Float.parseFloat("10"), loanID);
            makeRepayment("06 September 2022", Float.parseFloat("400"), loanID);
            makeRepayment("07 September 2022", Float.parseFloat("390"), loanID);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);

            assertEquals(300.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());

            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());

            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(90.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(300.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(2).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanID);

            assertEquals(290.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());

            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(290.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanID);

            assertEquals(210.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(390.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);

            loanDetails = getLoanDetails(loanID);

            assertEquals(290.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(1).getManuallyReversed());

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(2).getDate());

            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(3).getDate());
            assertTrue(loanDetails.getTransactions().get(3).getManuallyReversed());

            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(400.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(4).getDate());

            assertEquals(390.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getPrincipalPortion()));
            assertEquals(290.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(5).getDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testCreditBalanceRefundAfterMaturityWithReverseReplayOfRepayments() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("10 October 2022");

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            makeRepayment("04 September 2022", Float.parseFloat("100"), loanID);
            makeRepayment("05 September 2022", Float.parseFloat("1100"), loanID);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(200.0)
                    .transactionDate("10 October 2022").dateFormat(DATETIME_PATTERN).locale("en").paymentTypeId(1L));

            loanDetails = getLoanDetails(loanID);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));

            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(900.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOutstandingLoanBalance()));
            assertEquals(1100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(900.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 10, 10), loanDetails.getTransactions().get(3).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(1L, loanDetails.getTransactions().get(3).getPaymentDetailData().getPaymentType().getId());
            GetJournalEntriesTransactionIdResponse journalEntriesForTransaction = journalHelper
                    .getJournalEntries("L" + loanDetails.getTransactions().get(3).getId());
            List<JournalEntryTransactionItem> journalItems = journalEntriesForTransaction.getPageItems();
            assertEquals(2, journalItems.size());
            assertEquals(200.0,
                    journalItems.stream()
                            .filter(j -> "DEBIT".equalsIgnoreCase(j.getEntryType().getValue())
                                    && j.getGlAccountId().equals(overpaymentAccount.getAccountID().longValue()))
                            .findFirst().get().getAmount());
            assertEquals(200.0, journalItems.stream().filter(j -> "CREDIT".equalsIgnoreCase(j.getEntryType().getValue())
                    && j.getGlAccountId().equals(assetAccount.getAccountID().longValue())).findFirst().get().getAmount());

            reverseLoanTransaction(loanDetails.getId(), loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionAmount(0.0)
                            .transactionDate("10 October 2022").locale("en"));

            loanDetails = getLoanDetails(loanID);

            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(1).getManuallyReversed());

            assertEquals(1100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));
            assertEquals(1, loanDetails.getTransactions().get(2).getTransactionRelations().size());

            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(LocalDate.of(2022, 10, 10), loanDetails.getTransactions().get(3).getDate());
            assertEquals(1, loanDetails.getTransactions().get(3).getTransactionRelations().size());

            assertTrue(loanDetails.getStatus().getActive());

            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertTrue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue()));
            assertFalse(loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid()));
            assertEquals(100.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding()));

            journalEntriesForTransaction = journalHelper.getJournalEntries("L" + loanDetails.getTransactions().get(3).getId());
            journalItems = journalEntriesForTransaction.getPageItems();
            assertEquals(3, journalItems.size());
            assertEquals(1,
                    journalItems.stream().filter(item -> item.getAmount() == 200.0d)
                            .filter(j -> "CREDIT".equalsIgnoreCase(j.getEntryType().getValue())
                                    && j.getGlAccountId().equals(assetAccount.getAccountID().longValue()))
                            .count());
            assertEquals(1,
                    journalItems.stream().filter(item -> item.getAmount() == 100.0d)
                            .filter(j -> "DEBIT".equalsIgnoreCase(j.getEntryType().getValue())
                                    && j.getGlAccountId().equals(overpaymentAccount.getAccountID().longValue()))
                            .count());
            assertEquals(1,
                    journalItems.stream().filter(item -> item.getAmount() == 100.0d)
                            .filter(j -> "DEBIT".equalsIgnoreCase(j.getEntryType().getValue())
                                    && j.getGlAccountId().equals(assetAccount.getAccountID().longValue()))
                            .count());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testCreditBalanceRefundBeforeMaturityWithReverseReplayOfRepaymentsAndRefund() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("10 October 2022");

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");

            final Long loanID = applyForLoanApplication(clientID, loanProductID);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            makeRepayment("05 September 2022", Float.parseFloat("700"), loanID);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(200.0)
                    .transactionDate("06 September 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            makeMerchantIssuedRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat(DATETIME_PATTERN)
                    .transactionDate("07 September 2022").transactionAmount(500.0));

            makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(500.0)
                    .transactionDate("08 September 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanDetails = getLoanDetails(loanID);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOutstandingLoanBalance()));

            assertEquals(700.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));

            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getOutstandingLoanBalance()));

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getTransactions().get(5).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getOutstandingLoanBalance()));

            reverseLoanTransaction(loanDetails.getId(), loanDetails.getTransactions().get(2).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionAmount(0.0)
                            .transactionDate("07 September 2022").locale("en"));

            loanDetails = getLoanDetails(loanID);

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(1).getOutstandingLoanBalance()));

            assertEquals(700.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getPrincipalPortion()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOverpaymentPortion()));
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getTransactions().get(2).getOutstandingLoanBalance()));
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getAmount()));
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());
            assertEquals(700.0, Utils.getDoubleValue(loanDetails.getTransactions().get(3).getOutstandingLoanBalance()));
            assertEquals(1, loanDetails.getTransactions().get(3).getTransactionRelations().size());

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());
            assertEquals(200.0, Utils.getDoubleValue(loanDetails.getTransactions().get(4).getOutstandingLoanBalance()));
            assertEquals(1, loanDetails.getTransactions().get(4).getTransactionRelations().size());

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getAmount()));
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getPrincipalPortion()));
            assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getTransactions().get(5).getDate());
            assertEquals(700.0, Utils.getDoubleValue(loanDetails.getTransactions().get(5).getOutstandingLoanBalance()));
            assertEquals(1, loanDetails.getTransactions().get(5).getTransactionRelations().size());

            assertTrue(loanDetails.getStatus().getActive());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1700.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue()));
            assertFalse(loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid()));
            assertEquals(700.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void accrualIsCalculatedWhenTheLoanIsClosed() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("10 October 2022");

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            final Long clientID = createClient("01 January 2011");
            List<PostLoansRequestChargeData> charges = new ArrayList<>();
            Long installmentFee = chargesHelper.createLoanInstallmentCharge(ChargeCalculationType.FLAT, 10.0, false).getResourceId();
            addCharges(charges, installmentFee, 10.0, null);

            final Long loanID = applyForLoanApplication(clientID, loanProductID, charges);

            verifyLoanIsPending(loanID);

            verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan("02 September 2022", loanID));

            GetLoansLoanIdResponse loanStatusHashMap = disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            verifyLoanIsActive(loanStatusHashMap);

            makeRepayment("04 September 2022", Float.parseFloat("5"), loanID);

            runPeriodicAccrualAccounting("04 September 2022");

            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 11.0, true).getResourceId();
            LocalDate targetDate = LocalDate.of(2022, 9, 6);
            final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);

            Long penalty1LoanChargeId = addLoanCharge(loanID, penalty, penaltyCharge1AddedDate, 11.0).getResourceId();

            waiveWholeLoanCharge(loanID, penalty1LoanChargeId);

            makeRepayment("08 September 2022", Float.parseFloat("1010"), loanID);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);

            GetLoansLoanIdTransactions lastAccrualTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getType().getAccrual())).findFirst().get();
            assertEquals(15.0, Utils.getDoubleValue(lastAccrualTransaction.getAmount()));
            assertEquals(5.0, Utils.getDoubleValue(lastAccrualTransaction.getPenaltyChargesPortion()));
            assertEquals(10.0, Utils.getDoubleValue(lastAccrualTransaction.getFeeChargesPortion()));

            GetLoansLoanIdTransactionsTransactionIdResponse accrualTransactionDetails = getLoanTransactionDetails(loanID,
                    lastAccrualTransaction.getId());

            assertEquals(2, accrualTransactionDetails.getLoanChargePaidByList().size());
            accrualTransactionDetails.getLoanChargePaidByList().forEach(loanCharge -> {
                if (loanCharge.getChargeId().equals((long) penalty1LoanChargeId)) {
                    assertEquals(5.0, Utils.getDoubleValue(loanCharge.getAmount()));
                } else {
                    assertEquals(10.0, Utils.getDoubleValue(loanCharge.getAmount()));
                }
            });

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanTransactionOrderAfterReverseReplay() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 January 2023");
            LOG.info("-----------------------------------NEW CLIENT-----------------------------------------");
            final Long newClientId = createClient("01 January 2023");
            LOG.info("-----------------------------------NEW LOAN PRODUCT-----------------------------------------");
            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
            final Long loanProductId = createLoanProduct(loanProductsRequest);
            LOG.info("-----------------------------------CREATE CHARGES-----------------------------------------");
            PostChargesResponse penaltyCharge = chargesHelper.createCharge(new ChargeRequest().penalty(true).amount(10.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));
            LOG.info("-----------------------------------SUBMIT LOAN-----------------------------------------");
            final PostLoansResponse loanApplicationResult = applyForLoanApplicationForOnePeriod30DaysLongNoInterestPeriodicAccrual(
                    newClientId, loanProductId, "01 January 2023",
                    DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY);
            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            PostLoansLoanIdResponse approvedLoanResult = approveLoan(loanApplicationResult.getResourceId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000.0)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));
            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDisbursementUUID = UUID.randomUUID().toString();
            PostLoansLoanIdResponse disbursedLoanResult = disburseLoan(loanApplicationResult.getResourceId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en").externalId(loanDisbursementUUID));
            Long loanId = disbursedLoanResult.getResourceId();
            LOG.info("-------------------------------ADD CHARGES-------------------------------------------");
            PostLoansLoanIdChargesResponse penaltyLoanChargeResult = addLoanCharge(loanId,
                    new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                            .amount(10.0).dueDate("10 January 2023"));
            LOG.info("-------------------------------DO SOME PARTIAL REPAYMENTS-------------------------------------------");
            updateBusinessDate("07 January 2023");
            String firstRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse firstRepaymentResult = makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(9.0).externalId(firstRepaymentUUID));
            String secondRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse secondRepaymentResult = makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(8.0).externalId(secondRepaymentUUID));
            String thirdRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse thirdRepaymentResult = makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(7.0).externalId(thirdRepaymentUUID));
            LOG.info("-------------------------------CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
            LOG.info(
                    "-------------------------------REVERT FIRST REPAYMENT AND CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            reverseLoanTransaction(loanId, firstRepaymentUUID, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").transactionAmount(0.0).locale("en"));
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
            LOG.info(
                    "-------------------------------REVERT SECOND REPAYMENT AND CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            reverseLoanTransaction(loanId, secondRepaymentUUID, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").transactionAmount(0.0).locale("en"));
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_AMOUNT_AND_INTEREST() {

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);
        if (fourMonthsfromNowCalendar.get(Calendar.DAY_OF_MONTH) > 27) {
            fourMonthsfromNowCalendar.add(Calendar.DAY_OF_MONTH, 4);
        }

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);
        final Long clientID = createClient();
        verifyClientCreatedOnServer(clientID);
        final Long loanProductID = createLoanProduct(false, NONE);

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        Long disbursementFee = chargesHelper.createLoanDisbursementCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 2.0)
                .getResourceId();
        addCharges(charges, disbursementFee, 2.0, null);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        final Long loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, null, "1000",
                DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);

        approveLoan(fourMonthsfromNow, loanID);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID);

        // check for disbursement fee: Principal 1,000 with 24% Annual Rate for 6 Months we have Total Interest of:
        // 120.00
        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanID);
        GetLoansLoanIdRepaymentPeriod disbursementDetail = loanSchedule.get(0);
        // Disbursement Fee: 2% of 1,120.00 = 22.40
        validateNumberForEqual("22.40", String.valueOf(disbursementDetail.getFeeChargesDue()));
    }

    private void checkLoanTransactionOrder(Long loanId, String... transactionUUIDs) {
        LOG.info("-------------------------------CHECK LOAN TRANSACTION ORDER-------------------------------------------");
        GetLoansLoanIdResponse loanDetailsResult = getLoanDetails(loanId);
        for (int i = 0; i < transactionUUIDs.length; i++) {
            assertEquals(transactionUUIDs[i], loanDetailsResult.getTransactions().get(i).getExternalId());
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // Loan product builders
    // ----------------------------------------------------------------------------------------------------

    /** The defaults previously supplied by {@code LoanProductTestBuilder}. */
    private PostLoanProductsRequest baseLoanProduct() {
        return new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .locale(LOCALE)//
                .dateFormat(DATETIME_PATTERN)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .principal(10000.00)//
                .minPrincipal(1000.00)//
                .maxPrincipal(10000000.00)//
                .numberOfRepayments(5)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .inArrearsTolerance(0)//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .accountingRule(NONE)//
                .isEqualAmortization(false)//
                .overdueDaysForNPA(5)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.name())//
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.name())//
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .isInterestRecalculationEnabled(false)//
                .allowPartialPeriodInterestCalculation(false)//
                .allowVariableInstallments(false);
    }

    /**
     * Mirrors {@code LoanProductTestBuilder}'s account mapping: every account of a given type overwrites the mapping
     * contributed by the previous account of that type, so the last one in {@code accounts} wins.
     */
    private PostLoanProductsRequest withAccounting(PostLoanProductsRequest product, Integer accountingRule, Account... accounts) {
        product.accountingRule(accountingRule);
        if (NONE.equals(accountingRule) || accounts == null) {
            return product;
        }
        boolean accrualBased = ACCRUAL_PERIODIC.equals(accountingRule) || ACCRUAL_UPFRONT.equals(accountingRule);
        for (Account account : accounts) {
            Long accountId = account.getAccountID().longValue();
            switch (account.getAccountType()) {
                case ASSET -> {
                    product.fundSourceAccountId(accountId)//
                            .loanPortfolioAccountId(accountId)//
                            .transfersInSuspenseAccountId(accountId);
                    if (accrualBased) {
                        product.receivableInterestAccountId(accountId)//
                                .receivableFeeAccountId(accountId)//
                                .receivablePenaltyAccountId(accountId);
                    }
                }
                case INCOME -> product.interestOnLoanAccountId(accountId)//
                        .incomeFromFeeAccountId(accountId)//
                        .incomeFromPenaltyAccountId(accountId)//
                        .incomeFromRecoveryAccountId(accountId)//
                        .incomeFromChargeOffInterestAccountId(accountId)//
                        .incomeFromChargeOffFeesAccountId(accountId)//
                        .incomeFromChargeOffPenaltyAccountId(accountId)//
                        .incomeFromGoodwillCreditInterestAccountId(accountId)//
                        .incomeFromGoodwillCreditFeesAccountId(accountId)//
                        .incomeFromGoodwillCreditPenaltyAccountId(accountId);
                case EXPENSE -> product.writeOffAccountId(accountId)//
                        .goodwillCreditAccountId(accountId)//
                        .chargeOffExpenseAccountId(accountId)//
                        .chargeOffFraudExpenseAccountId(accountId);
                case LIABILITY -> product.overpaymentLiabilityAccountId(accountId);
                default -> throw new IllegalArgumentException("Unsupported GL account type: " + account.getAccountType());
            }
        }
        return product;
    }

    private PostLoanProductsRequest withTranches(PostLoanProductsRequest product) {
        return product.multiDisburseLoan(true)//
                .allowFullTermForTranche(false)//
                .maxTrancheCount(3)//
                .outstandingLoanBalance(35000.0)//
                .disallowExpectedDisbursements(false);
    }

    private Long createLoanProduct(final boolean multiDisburseLoan, final Integer accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(12000.00)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE);
        if (multiDisburseLoan) {
            withTranches(product);
            product.interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .allowPartialPeriodInterestCalculation(true);
        }
        withAccounting(product, accountingRule, accounts);
        return createLoanProduct(product);
    }

    private Long createLoanProduct(final Integer inMultiplesOf, final Integer digitsAfterDecimal, final String repaymentStrategy) {
        return createLoanProduct(inMultiplesOf, digitsAfterDecimal, repaymentStrategy, NONE);
    }

    private Long createLoanProduct(final Integer inMultiplesOf, final Integer digitsAfterDecimal, final String repaymentStrategy,
            final Integer accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(10000000.00)//
                .numberOfRepayments(24)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .digitsAfterDecimal(digitsAfterDecimal)//
                .inMultiplesOf(inMultiplesOf);
        withAccounting(product, accountingRule, accounts);
        return createLoanProduct(product);
    }

    private Long createLoanProductWithPeriodicAccrualAccountingNoInterest(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(1000.00)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(0.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .daysInMonthType(30)//
                .daysInYearType(365)//
                .graceOnPrincipalPayment(0)//
                .graceOnInterestPayment(0);
        withAccounting(product, ACCRUAL_PERIODIC, accounts);
        return createLoanProduct(product);
    }

    private Long createLoanProductWithPeriodicAccrualAccountingNoInterestMultiDisbursement(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(1000.00)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(0.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .allowPartialPeriodInterestCalculation(true)//
                .daysInMonthType(30)//
                .daysInYearType(365)//
                .graceOnPrincipalPayment(0)//
                .graceOnInterestPayment(0);
        withTranches(product);
        // The pre-migration LoanProductTestBuilder.withDisallowExpectedDisbursements(true) implicitly enabled a 100%
        // over-applied allowance. chargeOff() relies on it to disburse two 1000 tranches against a 1000 approval.
        product.disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(100);
        withAccounting(product, ACCRUAL_PERIODIC, accounts);
        return createLoanProduct(product);
    }

    private PostLoanProductsRequest withInterestRecalculation(PostLoanProductsRequest product,
            final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer preCloseInterestCalculationStrategy, final Integer recalculationRestFrequencyType,
            final Integer recalculationRestFrequencyInterval, final Integer recalculationCompoundingFrequencyType,
            final Integer recalculationCompoundingFrequencyInterval, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        product.isInterestRecalculationEnabled(true)//
                .interestRecalculationCompoundingMethod(interestRecalculationCompoundingMethod)//
                .rescheduleStrategyMethod(rescheduleStrategyMethod)//
                .recalculationRestFrequencyType(recalculationRestFrequencyType)//
                .recalculationRestFrequencyInterval(recalculationRestFrequencyInterval)//
                .preClosureInterestCalculationStrategy(preCloseInterestCalculationStrategy)//
                .recalculationCompoundingFrequencyOnDayType(recalculationCompoundingFrequencyOnDayType)//
                .recalculationCompoundingFrequencyDayOfWeekType(recalculationCompoundingFrequencyDayOfWeekType)//
                .recalculationRestFrequencyOnDayType(recalculationRestFrequencyOnDayType)//
                .recalculationRestFrequencyDayOfWeekType(recalculationRestFrequencyDayOfWeekType);
        if (!LoanTestData.InterestRecalculationCompoundingMethod.NONE.equals(interestRecalculationCompoundingMethod)) {
            product.recalculationCompoundingFrequencyType(recalculationCompoundingFrequencyType)//
                    .recalculationCompoundingFrequencyInterval(recalculationCompoundingFrequencyInterval);
        }
        return product;
    }

    private Long createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final Integer preCloseInterestCalculationStrategy, final Account[] accounts, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        return createLoanProductWithInterestRecalculationAndCompoundingDetails(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval, null, null,
                preCloseInterestCalculationStrategy, accounts, null, null, recalculationRestFrequencyOnDayType,
                recalculationRestFrequencyDayOfWeekType);
    }

    /** The 24-repayment weekly product used by the interest-recalculation tests. */
    private Long createLoanProductWithInterestRecalculationAndCompoundingDetails(final String repaymentStrategy,
            final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final Integer recalculationCompoundingFrequencyType, final Integer recalculationCompoundingFrequencyInterval,
            final Integer preCloseInterestCalculationStrategy, final Account[] accounts,
            final Integer recalculationCompoundingFrequencyOnDayType, final Integer recalculationCompoundingFrequencyDayOfWeekType,
            final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval, preCloseInterestCalculationStrategy,
                accounts, null, false, recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
    }

    /**
     * The 24-repayment weekly product used by the interest-recalculation tests, optionally with a charge and arrears
     * config.
     */
    private Long createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final Integer recalculationCompoundingFrequencyType, final Integer recalculationCompoundingFrequencyInterval,
            final Integer preCloseInterestCalculationStrategy, final Account[] accounts, final Long chargeId,
            boolean isArrearsBasedOnOriginalSchedule, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(10000000.00)//
                .numberOfRepayments(24)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS_L)//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .allowPartialPeriodInterestCalculation(true)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE);
        withInterestRecalculation(product, interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                preCloseInterestCalculationStrategy, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
        if (accounts != null) {
            withAccounting(product, ACCRUAL_PERIODIC, accounts);
        }
        if (isArrearsBasedOnOriginalSchedule) {
            product.isArrearsBasedOnOriginalSchedule(true);
        }
        if (chargeId != null) {
            product.charges(List.of(new LoanProductChargeData().id(chargeId)));
        }
        return createLoanProduct(product);
    }

    /** The 12-repayment monthly 19.9% product used by the daily-interest-calculation recalculation tests. */
    private Long createLoanProductWithInterestRecalculationAndInstallmentAmount(final String repaymentStrategy,
            final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer preCloseInterestCalculationStrategy, final Account[] accounts, final Integer installmentAmountInMultiplesOf) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = baseLoanProduct()//
                .principal(10000.00)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(19.9)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .canDefineInstallmentAmount(true)//
                .installmentAmountInMultiplesOf(installmentAmountInMultiplesOf);
        // The RestAssured builder left the rest frequency at its defaults (daily, every 1) for this product shape.
        withInterestRecalculation(product, interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                preCloseInterestCalculationStrategy, LoanTestData.RecalculationRestFrequencyType.DAILY, 1, null, null, null, null, null,
                null);
        if (accounts != null) {
            withAccounting(product, ACCRUAL_PERIODIC, accounts);
        }
        return createLoanProduct(product);
    }

    // ----------------------------------------------------------------------------------------------------
    // Loan application builders
    // ----------------------------------------------------------------------------------------------------

    /** The defaults previously supplied by {@code LoanApplicationTestBuilder}. */
    private PostLoansRequest baseLoanApplication(final Long clientId, final Long loanProductId) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .loanType("individual")//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .maxOutstandingLoanBalance(BigDecimal.valueOf(36000))//
                .collateral(Collections.emptyList());
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final List<PostLoansRequestChargeData> charges) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(BigDecimal.valueOf(1000))//
                .loanTermFrequency(1)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .expectedDisbursementDate("03 September 2022")//
                .submittedOnDate("01 September 2022")//
                .charges(charges));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(BigDecimal.valueOf(1000))//
                .loanTermFrequency(1)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate("03 September 2022")//
                .submittedOnDate("01 September 2022"));
    }

    /** The 4-month, 2%, equal-installment application shared by the charge tests. */
    private PostLoansRequest fourMonthLoanApplication(final Long clientId, final Long loanProductId, final BigDecimal principal,
            final List<PostLoansRequestChargeData> charges, final Long savingsId, final List<PostLoansRequestCollateralData> collaterals) {
        return baseLoanApplication(clientId, loanProductId)//
                .principal(principal)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate("20 September 2011")//
                .submittedOnDate("20 September 2011")//
                .collateral(collaterals)//
                .charges(charges)//
                .linkAccountId(savingsId);
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final List<PostLoansRequestChargeData> charges,
            final Long savingsId, final String principal, final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(fourMonthLoanApplication(clientId, loanProductId, toAmount(principal), charges, savingsId, collaterals));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final String disbursementDate,
            final String submissionDate, final String interestRate, final List<PostLoansRequestChargeData> charges, final Long savingsId,
            final String principal, final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(toAmount(principal))//
                .loanTermFrequency(2)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(2)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(new BigDecimal(interestRate))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate(disbursementDate)//
                .submittedOnDate(submissionDate)//
                .collateral(collaterals)//
                .charges(charges)//
                .linkAccountId(savingsId));
    }

    /** The 24-repayment, equal-principal application used by the currency-detail tests. */
    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final Integer graceOnPrincipalPayment,
            final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(new BigDecimal("10000000.00"))//
                .loanTermFrequency(24)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(24)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .graceOnPrincipalPayment(graceOnPrincipalPayment)//
                .expectedDisbursementDate("02 June 2014")//
                .submittedOnDate("02 June 2014")//
                .collateral(collaterals));
    }

    private Long applyForLoanApplicationWithExternalId(final Long clientId, final Long loanProductId, final String principal,
            final String externalId) {
        LOG.info("------------------------APPLYING FOR LOAN APPLICATION WITH EXTERNALID------------------------");
        return applyForLoan(fourMonthLoanApplication(clientId, loanProductId, toAmount(principal), null, null, Collections.emptyList())
                .externalId(externalId));
    }

    private Long applyForLoanApplicationWithTranches(final Long clientId, final Long loanProductId,
            final List<PostLoansRequestChargeData> charges, final Long savingsId, final String principal,
            final List<PostLoansDisbursementData> tranches, final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(fourMonthLoanApplication(clientId, loanProductId, toAmount(principal), charges, savingsId, collaterals)//
                .expectedDisbursementDate("01 March 2014")//
                .submittedOnDate("01 March 2014")//
                .disbursementData(tranches)//
                .fixedEmiAmount(BigDecimal.valueOf(10000)));
    }

    /** Re-expresses an application's charge list as the update endpoint's charge model. */
    private List<PutLoansLoanIdChargeData> toUpdateCharges(final List<PostLoansRequestChargeData> charges) {
        return charges.stream().map(charge -> new PutLoansLoanIdChargeData().chargeId(charge.getChargeId()).amount(charge.getAmount())
                .dueDate(charge.getDueDate())).toList();
    }

    /** Re-expresses an application's collateral list as the update endpoint's collateral model. */
    private List<PutLoansLoanIdCollateral> toUpdateCollaterals(final List<PostLoansRequestCollateralData> collaterals) {
        return collaterals.stream()
                .map(item -> new PutLoansLoanIdCollateral().clientCollateralId(item.getClientCollateralId()).quantity(item.getQuantity()))
                .toList();
    }

    private PutLoansLoanIdRequest updateLoanRequest(final Long clientId, final Long loanProductId,
            final List<PutLoansLoanIdChargeData> charges, final Long savingsId, final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------UPDATING LOAN APPLICATION--------------------------------");
        return new PutLoansLoanIdRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .loanType("individual")//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .principal(10000L)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .maxOutstandingLoanBalance(36000L)//
                .expectedDisbursementDate("20 September 2011")//
                .submittedOnDate("20 September 2011")//
                .collateral(toUpdateCollaterals(collaterals))//
                .charges(charges)//
                .linkAccountId(savingsId);
    }

    private Long applyForLoanApplicationWithPaymentStrategy(final Long clientId, final Long loanProductId,
            final List<PostLoansRequestChargeData> charges, final Long savingsId, final String principal, final String repaymentStrategy,
            final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(fourMonthLoanApplication(clientId, loanProductId, toAmount(principal), charges, savingsId, collaterals)//
                .transactionProcessingStrategyCode(repaymentStrategy));
    }

    private Long applyForLoanApplicationWithPaymentStrategyAndPastMonth(final Long clientId, final Long loanProductId,
            final List<PostLoansRequestChargeData> charges, final Long savingsId, final String principal, final String repaymentStrategy,
            final String fourMonthsfromNow, final List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(toAmount(principal))//
                .loanTermFrequency(6)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(6)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .expectedDisbursementDate(fourMonthsfromNow)//
                .submittedOnDate(fourMonthsfromNow)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .collateral(collaterals)//
                .charges(charges)//
                .linkAccountId(savingsId));
    }

    private Long applyForLoanApplicationWithProductConfigurationAsTrue(final Long clientId, final Long loanProductId,
            final String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(toAmount(principal))//
                .repaymentEvery(1)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .expectedDisbursementDate("01 March 2014")//
                .submittedOnDate("01 March 2014")//
                .collateral(createClientCollateral(clientId)));
    }

    private Long applyForLoanApplicationWithProductConfigurationAsFalse(final Long clientId, final Long loanProductId,
            final String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(toAmount(principal))//
                .repaymentEvery(2)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .transactionProcessingStrategyCode(RBI_INDIA_STRATEGY)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .graceOnPrincipalPayment(1)//
                .graceOnInterestPayment(1)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .expectedDisbursementDate("01 March 2014")//
                .submittedOnDate("01 March 2014")//
                .collateral(createClientCollateral(clientId)));
    }

    /** The 4-week, 2%, equal-installment application used by the interest-recalculation tests. */
    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, final Long loanProductId,
            final String disbursementDate, final String repaymentStrategy, final List<PostLoansRequestChargeData> charges) {
        return applyForLoanApplicationForInterestRecalculation(clientId, loanProductId, disbursementDate, repaymentStrategy, charges, null,
                null);
    }

    private Long applyForLoanApplicationForInterestRecalculationWithMoratorium(final Long clientId, final Long loanProductId,
            final String disbursementDate, final String repaymentStrategy, final List<PostLoansRequestChargeData> charges,
            final Integer graceOnInterestPayment, final Integer graceOnPrincipalPayment) {
        return applyForLoanApplicationForInterestRecalculation(clientId, loanProductId, disbursementDate, repaymentStrategy, charges,
                graceOnInterestPayment, graceOnPrincipalPayment);
    }

    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, final Long loanProductId,
            final String disbursementDate, final String repaymentStrategy, final List<PostLoansRequestChargeData> charges,
            final Integer graceOnInterestPayment, final Integer graceOnPrincipalPayment) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(baseLoanApplication(clientId, loanProductId)//
                .principal(new BigDecimal("10000.00"))//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate(disbursementDate)//
                .submittedOnDate(disbursementDate)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .graceOnPrincipalPayment(graceOnPrincipalPayment)//
                .graceOnInterestPayment(graceOnInterestPayment)//
                .charges(charges)//
                .collateral(createClientCollateral(clientId)));
    }

    /** The 12-month, 19.9%, daily-interest application whose first repayment date is pinned. */
    private Long applyForLoanApplicationForInterestRecalculationWithFirstRepaymentDate(final Long clientId, final Long loanProductId,
            final String disbursementDate, final String repaymentStrategy, final String firstRepaymentDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        PostLoansRequest request = baseLoanApplication(clientId, loanProductId)//
                .principal(new BigDecimal("10000.00"))//
                .loanTermFrequency(12)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(19.9))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .expectedDisbursementDate(disbursementDate)//
                .submittedOnDate(disbursementDate)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .collateral(createClientCollateral(clientId));
        return applyForLoan(LoanRequestBuilders.applyLoanWithLegacyDates(request, null, firstRepaymentDate));
    }

    private PostLoansResponse applyForLoanApplicationForOnePeriod30DaysLongNoInterestPeriodicAccrual(Long clientId, Long loanProductId,
            String loanDisbursementDate, String repaymentStrategyCode) {
        return loanHelper.applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .expectedDisbursementDate(loanDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(repaymentStrategyCode).locale("en").submittedOnDate(loanDisbursementDate)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS).interestRatePerPeriod(BigDecimal.ZERO)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)
                .repaymentEvery(30).numberOfRepayments(1).loanTermFrequency(30)
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).principal(BigDecimal.valueOf(1000.0))
                .loanType("individual"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Collateral / charge / savings helpers
    // ----------------------------------------------------------------------------------------------------

    /**
     * Creates a collateral product plus a client collateral, returning the single-entry collateral list a loan needs.
     */
    private List<PostLoansRequestCollateralData> createClientCollateral(final Long clientId) {
        Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        return List.of(collateral(clientCollateralId, BigDecimal.ONE));
    }

    private PostLoansRequestCollateralData collateral(final Long clientCollateralId, final BigDecimal quantity) {
        return new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(quantity);
    }

    /**
     * Parses an amount that may carry {@code en_GB} grouping separators, e.g. {@code "12,000.00"}. The RestAssured
     * builders sent these strings verbatim and let the server parse them under the request's locale; the typed models
     * carry a JSON number, so the grouping has to be removed here.
     */
    private BigDecimal toAmount(final String value) {
        return new BigDecimal(value.replace(",", ""));
    }

    private PostLoansRequestChargeData charge(final Long chargeId, final Double amount, final String dueDate) {
        PostLoansRequestChargeData charge = new PostLoansRequestChargeData().chargeId(chargeId).amount(BigDecimal.valueOf(amount));
        if (dueDate != null) {
            charge.dueDate(dueDate);
        }
        return charge;
    }

    private void addCharges(List<PostLoansRequestChargeData> charges, Long chargeId, Double amount, String dueDate) {
        charges.add(charge(chargeId, amount, dueDate));
    }

    /**
     * Compares an installment's period against an installment number. The schedule exposes the period as an
     * {@link Integer} while installment numbers are sent as {@link Long}, so {@code equals} between the two is always
     * false - compare the numeric values instead.
     */
    private boolean isPeriod(GetLoansLoanIdRepaymentPeriod installment, Long installmentNumber) {
        return installment.getPeriod() != null && installmentNumber != null && installment.getPeriod().longValue() == installmentNumber;
    }

    private GetLoansLoanIdChargesChargeIdResponse getloanCharge(Long chargeId, List<GetLoansLoanIdChargesChargeIdResponse> charges) {
        return charges.stream().filter(charge -> Objects.equals(charge.getChargeId(), chargeId)).reduce((first, second) -> second)
                .orElse(null);
    }

    /**
     * Rebuilds the loan's charge array for a loan-application update, optionally dropping one charge and overriding the
     * amount.
     */
    private List<PutLoansLoanIdChargeData> copyChargesForUpdate(List<GetLoansLoanIdChargesChargeIdResponse> charges,
            Long deleteWithChargeId, Double amount) {
        List<PutLoansLoanIdChargeData> loanCharges = new ArrayList<>();
        for (GetLoansLoanIdChargesChargeIdResponse charge : charges) {
            if (!Objects.equals(charge.getChargeId(), deleteWithChargeId)) {
                loanCharges.add(copyForUpdate(charge, amount));
            }
        }
        return loanCharges;
    }

    private PutLoansLoanIdChargeData copyForUpdate(GetLoansLoanIdChargesChargeIdResponse charge, Double amount) {
        PutLoansLoanIdChargeData data = new PutLoansLoanIdChargeData()//
                .id(charge.getId())//
                .chargeId(charge.getChargeId())//
                .amount(BigDecimal.valueOf(amount == null ? charge.getAmountOrPercentage() : amount));
        if (charge.getDueDate() != null) {
            data.dueDate(dateTimeFormatter.format(charge.getDueDate()));
        }
        return data;
    }

    private PostLoansDisbursementData createTrancheDetail(final String date, final String amount) {
        return new PostLoansDisbursementData().expectedDisbursementDate(date).principal(toAmount(amount));
    }

    /** Adds an installment-fee loan charge whose amount is sent as a JSON number under the given locale. */
    private void addInstallmentChargeWithLocale(final Long loanId, final Long chargeId, final Double amount, final Locale locale) {
        addLoanCharge(loanId, new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale(locale.getLanguage())//
                .dateFormat(DATETIME_PATTERN));
    }

    /**
     * Adds an installment-fee loan charge whose {@code amount} is a locale-formatted <em>string</em> (the German
     * {@code "50,05"}), which is what this test exists to prove the server parses.
     *
     * <p>
     * {@code PostLoansLoanIdChargesRequest.amount} is a JSON number, so the generated model cannot carry a decimal
     * comma - see {@link LoanChargeCommandsApi#createLoanChargeWithLocaleFormattedAmount} for why this gets its own
     * request model instead of a Swagger change.
     *
     * @param amountText
     *            the {@code amount} formatted for {@code locale}, e.g. the German {@code "50,05"}
     */
    private void addInstallmentChargeWithLocaleFormattedAmount(final Long loanId, final Long chargeId, final String amountText,
            final Locale locale) {
        ok(() -> loanChargeCommands().createLoanChargeWithLocaleFormattedAmount(loanId,
                new LoanChargeCommandsApi.LocaleFormattedAmountLoanChargeRequest()//
                        .chargeId(String.valueOf(chargeId))//
                        .amount(amountText)//
                        .locale(locale.getLanguage())//
                        .dateFormat(DATETIME_PATTERN)));
    }

    /**
     * Waives a loan charge in full: no installment is named, so the request carries no fields.
     *
     * @return the waived loan charge's id
     */
    private Long waiveWholeLoanCharge(final Long loanId, final Long loanChargeId) {
        return waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest()).getResourceId();
    }

    private static LoanChargeCommandsApi loanChargeCommands() {
        return FineractFeignClientHelper.getFineractFeignClient().create(LoanChargeCommandsApi.class);
    }

    /**
     * Re-submits a savings application with a new {@code submittedOnDate} and reports whether the server echoed that
     * field back as changed.
     *
     * @return whether {@code submittedOnDate} comes back in the response's {@code changes}
     */
    private boolean updateSavingsSubmittedOnDate(final Long savingsId, final Long clientId, final Long productId,
            final String submittedOnDate) {
        PutSavingsAccountsAccountIdRequest request = new PutSavingsAccountsAccountIdRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .locale(LOCALE)//
                .dateFormat(DATETIME_PATTERN);
        PutSavingsAccountsAccountIdResponse response = ok(() -> FineractFeignClientHelper.getFineractFeignClient().savingsAccount()
                .updateSavingsAccount(savingsId, request, (String) null));
        return response.getChanges() != null && response.getChanges().getSubmittedOnDate() != null;
    }

    private Long createSavingsProduct(final String minOpenningBalance) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        return savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct()//
                .minRequiredOpeningBalance(new BigDecimal(minOpenningBalance))).getResourceId();
    }

    /**
     * Opens a savings account submitted on {@code 08 January 2013} and activated on {@code 01 March 2013}, funded by
     * the product's minimum opening balance. The inherited {@code openSavingsAccount} activates on today's date, which
     * is after the 2011-2013 transaction dates these tests use.
     */
    private Long openSavingsAccountActivatedOnTransactionDate(final Long clientId, final String minimumOpeningBalance) {
        final Long savingsProductId = createSavingsProduct(minimumOpeningBalance);
        Assertions.assertNotNull(savingsProductId);
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, SAVINGS_CREATED_DATE).getSavingsId();
        Assertions.assertNotNull(savingsId);
        savingsHelper.approveSavings(savingsId, SAVINGS_TRANSACTION_DATE);
        savingsHelper.activateSavings(savingsId, SAVINGS_TRANSACTION_DATE);
        return savingsId;
    }

    // ----------------------------------------------------------------------------------------------------
    // Assertion helpers
    // ----------------------------------------------------------------------------------------------------

    /**
     * Asserts the loan did not override any of the product's defaults — the same attribute set the RestAssured version
     * compared. The enum-valued attributes reach the two responses as different generated wrapper types, so they are
     * compared on their {@code id} and {@code code} rather than by {@code equals}.
     */
    private void validateIfValuesAreNotOverridden(Long loanId, Long loanProductId) {
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

        assertEnumEquals(loanProduct.getAmortizationType().getId(), loanProduct.getAmortizationType().getCode(),
                loanDetails.getAmortizationType().getId(), loanDetails.getAmortizationType().getCode(), "amortizationType");
        assertEnumEquals(loanProduct.getInterestType().getId(), loanProduct.getInterestType().getCode(),
                loanDetails.getInterestType().getId(), loanDetails.getInterestType().getCode(), "interestType");
        assertEnumEquals(loanProduct.getInterestCalculationPeriodType().getId(), loanProduct.getInterestCalculationPeriodType().getCode(),
                loanDetails.getInterestCalculationPeriodType().getId(), loanDetails.getInterestCalculationPeriodType().getCode(),
                "interestCalculationPeriodType");
        assertEnumEquals(loanProduct.getRepaymentFrequencyType().getId(), loanProduct.getRepaymentFrequencyType().getCode(),
                loanDetails.getRepaymentFrequencyType().getId(), loanDetails.getRepaymentFrequencyType().getCode(),
                "repaymentFrequencyType");

        assertEquals(loanProduct.getTransactionProcessingStrategyCode(), loanDetails.getTransactionProcessingStrategyCode(),
                OVERRIDE_MESSAGE.formatted("transactionProcessingStrategyCode"));
        assertEquals(loanProduct.getGraceOnPrincipalPayment(), loanDetails.getGraceOnPrincipalPayment(),
                OVERRIDE_MESSAGE.formatted("graceOnPrincipalPayment"));
        assertEquals(loanProduct.getGraceOnInterestPayment(), loanDetails.getGraceOnInterestPayment(),
                OVERRIDE_MESSAGE.formatted("graceOnInterestPayment"));
        assertEquals(loanProduct.getInArrearsTolerance(), loanDetails.getInArrearsTolerance(),
                OVERRIDE_MESSAGE.formatted("inArrearsTolerance"));
        assertEquals(loanProduct.getGraceOnArrearsAgeing(), loanDetails.getGraceOnArrearsAgeing(),
                OVERRIDE_MESSAGE.formatted("graceOnArrearsAgeing"));
    }

    private static void assertEnumEquals(Long productId, String productCode, Long loanId, String loanCode, String attribute) {
        assertEquals(productId, loanId, OVERRIDE_MESSAGE.formatted(attribute));
        assertEquals(productCode, loanCode, OVERRIDE_MESSAGE.formatted(attribute));
    }

    /**
     * A product whose {@code allowAttributeOverrides} are all set to {@code allowOverride}, so a loan application
     * either may or may not deviate from the product's defaults.
     */
    private Long createLoanProductWithAttributeOverrides(boolean allowOverride) {
        PostLoanProductsRequest product = baseLoanProduct()//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .repaymentEvery(1)//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .inArrearsTolerance(10)//
                .graceOnPrincipalPayment(2)//
                .graceOnInterestPayment(3)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(allowOverride)//
                        .interestType(allowOverride)//
                        .transactionProcessingStrategyCode(allowOverride)//
                        .interestCalculationPeriodType(allowOverride)//
                        .inArrearsTolerance(allowOverride)//
                        .repaymentEvery(allowOverride)//
                        .graceOnPrincipalAndInterestPayment(allowOverride)//
                        .graceOnArrearsAgeing(allowOverride));
        return createLoanProduct(product);
    }

    private Integer getDayOfWeek(Calendar date) {
        int dayOfWeek = 0;
        if (null != date) {
            dayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
        }
        return dayOfWeek;
    }

    private Integer getDayOfMonth(Calendar date) {
        int dayOfMonth = 0;
        if (null != date) {
            dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth > 28) {
                dayOfMonth = 28;
            }
        }
        return dayOfMonth;
    }

    private void validateCharge(Long chargeId, final List<GetLoansLoanIdChargesChargeIdResponse> loanCharges, final String amount,
            final String outstanding, String amountPaid, String amountWaived) {
        GetLoansLoanIdChargesChargeIdResponse chargeDetail = getloanCharge(chargeId, loanCharges);
        assertNotNull(chargeDetail, "Loan charge not found for charge " + chargeId);
        validateNumberForEqual(amount, String.valueOf(chargeDetail.getAmountOrPercentage()));
        validateNumberForEqual(outstanding, String.valueOf(chargeDetail.getAmountOutstanding()));
        validateNumberForEqual(amountPaid, String.valueOf(chargeDetail.getAmountPaid()));
        validateNumberForEqual(amountWaived, String.valueOf(chargeDetail.getAmountWaived()));
    }

    private void validateChargeExcludePrecission(Long chargeId, final List<GetLoansLoanIdChargesChargeIdResponse> loanCharges,
            final String amount, final String outstanding, String amountPaid, String amountWaived) {
        GetLoansLoanIdChargesChargeIdResponse chargeDetail = getloanCharge(chargeId, loanCharges);
        assertNotNull(chargeDetail, "Loan charge not found for charge " + chargeId);
        validateNumberForEqualExcludePrecission(amount, String.valueOf(chargeDetail.getAmountOrPercentage()));
        validateNumberForEqualExcludePrecission(outstanding, String.valueOf(chargeDetail.getAmountOutstanding()));
        validateNumberForEqualExcludePrecission(amountPaid, String.valueOf(chargeDetail.getAmountPaid()));
        validateNumberForEqualExcludePrecission(amountWaived, String.valueOf(chargeDetail.getAmountWaived()));
    }

    private void validateNumberForEqual(String val, String val2) {
        assertEquals(0, Float.valueOf(val).compareTo(Float.valueOf(val2)), String.format("%s is not equal to %s", val, val2));
    }

    private void validateNumberForEqualWithMsg(String msg, String val, String val2) {
        assertEquals(0, Float.valueOf(val).compareTo(Float.valueOf(val2)), msg + "expected " + val + " but was " + val2);
    }

    private void validateNumberForEqualExcludePrecission(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        assertEquals(0, Float.valueOf(twoDForm.format(Float.valueOf(val))).compareTo(Float.valueOf(twoDForm.format(Float.valueOf(val2)))),
                String.format("%s is not equal to %s", val, val2));
    }

    private List<GetLoansLoanIdRepaymentPeriod> getLoanRepaymentSchedule(Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
    }

    /**
     * The future (not-yet-due) repayment periods. {@code getLoanDetails} excludes the {@code futureSchedule}
     * association, so this asks for it explicitly.
     */
    private List<GetLoansLoanIdRepaymentPeriod> getLoanFutureRepaymentSchedule(Long loanId) {
        return loanHelper.getLoanDetails(loanId, "repaymentSchedule,futureSchedule").getRepaymentSchedule().getFuturePeriods();
    }

    /**
     * The prepayment template with no explicit transaction date, letting the server default it, exactly as the
     * RestAssured helper did. Passing a date here would require a business date to be set.
     */
    private GetLoansLoanIdTransactionsTemplateResponse getPrepayAmount(Long loanId) {
        return transactionHelper.getPrepaymentAmount(loanId, null, null);
    }

    private void verifyLoanRepaymentSchedule(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals(LocalDate.of(2011, 10, 20), loanSchedule.get(1).getDueDate(), "Checking for Due Date for 1st Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 1st Month", "2911.49",
                String.valueOf(loanSchedule.get(1).getPrincipalOriginalDue()));
        validateNumberForEqualWithMsg("Checking for Interest Due for 1st Month", "240.00",
                String.valueOf(loanSchedule.get(1).getInterestOriginalDue()));

        assertEquals(LocalDate.of(2011, 11, 20), loanSchedule.get(2).getDueDate(), "Checking for Due Date for 2nd Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 2nd Month", "2969.72",
                String.valueOf(loanSchedule.get(2).getPrincipalDue()));
        validateNumberForEqualWithMsg("Checking for Interest Due for 2nd Month", "181.77",
                String.valueOf(loanSchedule.get(2).getInterestOriginalDue()));

        assertEquals(LocalDate.of(2011, 12, 20), loanSchedule.get(3).getDueDate(), "Checking for Due Date for 3rd Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 3rd Month", "3029.11",
                String.valueOf(loanSchedule.get(3).getPrincipalDue()));
        validateNumberForEqualWithMsg("Checking for Interest Due for 3rd Month", "122.38",
                String.valueOf(loanSchedule.get(3).getInterestOriginalDue()));

        assertEquals(LocalDate.of(2012, 1, 20), loanSchedule.get(4).getDueDate(), "Checking for Due Date for 4th Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 4th Month", "3089.68",
                String.valueOf(loanSchedule.get(4).getPrincipalDue()));
        validateNumberForEqualWithMsg("Checking for Interest Due for 4th Month", "61.79",
                String.valueOf(loanSchedule.get(4).getInterestOriginalDue()));
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipal(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        verifyEqualPrincipalInstallment(loanSchedule, 1, LocalDate.of(2014, 7, 2), "416700", "200000", true);
        verifyEqualPrincipalInstallment(loanSchedule, 2, LocalDate.of(2014, 8, 2), "416700", "191700", false);
        verifyEqualPrincipalInstallment(loanSchedule, 3, LocalDate.of(2014, 9, 2), "416700", "183300", false);
        verifyEqualPrincipalInstallment(loanSchedule, 4, LocalDate.of(2014, 10, 2), "416700", "175000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 5, LocalDate.of(2014, 11, 2), "416700", "166700", false);
        verifyEqualPrincipalInstallment(loanSchedule, 6, LocalDate.of(2014, 12, 2), "416700", "158300", false);
        verifyEqualPrincipalInstallment(loanSchedule, 10, LocalDate.of(2015, 4, 2), "416700", "125000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 20, LocalDate.of(2016, 2, 2), "416700", "41700", false);
        verifyEqualPrincipalInstallment(loanSchedule, 24, LocalDate.of(2016, 6, 2), "415900", "8300", false);
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        verifyEqualPrincipalInstallment(loanSchedule, 1, LocalDate.of(2014, 7, 2), "0.0", "200000", true);
        verifyEqualPrincipalInstallment(loanSchedule, 2, LocalDate.of(2014, 8, 2), "0.0", "200000", true);
        verifyEqualPrincipalInstallment(loanSchedule, 3, LocalDate.of(2014, 9, 2), "0.0", "200000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 4, LocalDate.of(2014, 10, 2), "0", "200000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 5, LocalDate.of(2014, 11, 2), "0", "200000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 6, LocalDate.of(2014, 12, 2), "526300", "200000", false);
        verifyEqualPrincipalInstallment(loanSchedule, 7, LocalDate.of(2015, 1, 2), "526300", "189500", false);
        verifyEqualPrincipalInstallment(loanSchedule, 10, LocalDate.of(2015, 4, 2), "526300", "157900", false);
        verifyEqualPrincipalInstallment(loanSchedule, 20, LocalDate.of(2016, 2, 2), "526300", "52600", false);
        verifyEqualPrincipalInstallment(loanSchedule, 24, LocalDate.of(2016, 6, 2), "526600", "10500", false);
    }

    /**
     * @param useOriginalPrincipalDue
     *            reads {@code principalOriginalDue} instead of {@code principalDue}, mirroring the assertions the
     *            RestAssured version of this test made per installment.
     */
    private void verifyEqualPrincipalInstallment(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule, int installment,
            LocalDate expectedDueDate, String expectedPrincipalDue, String expectedInterestDue, boolean useOriginalPrincipalDue) {
        GetLoansLoanIdRepaymentPeriod period = loanSchedule.get(installment);
        assertEquals(expectedDueDate, period.getDueDate(), "Checking for Due Date for installment " + installment);
        BigDecimal principalDue = useOriginalPrincipalDue ? period.getPrincipalOriginalDue() : period.getPrincipalDue();
        validateNumberForEqualWithMsg("Checking for Principal Due for installment " + installment, expectedPrincipalDue,
                String.valueOf(principalDue));
        validateNumberForEqualWithMsg("Checking for Interest Due for installment " + installment, expectedInterestDue,
                String.valueOf(period.getInterestOriginalDue()));
    }

    private void addRepaymentValues(List<ExpectedInstallment> expectedvalues, Calendar todaysDate, int addPeriod, boolean isAddDays,
            String principalDue, String interestDue, String feeChargesDue, String penaltyChargesDue) {
        LocalDate dueDate = isAddDays ? addDays(todaysDate, addPeriod) : addDays(todaysDate, addPeriod * 7);
        LOG.info("Updated date {}", dueDate);
        expectedvalues.add(new ExpectedInstallment(dueDate, principalDue, interestDue, feeChargesDue, penaltyChargesDue));
    }

    private LocalDate addDays(Calendar todaysDate, int addValue) {
        todaysDate.add(Calendar.DAY_OF_MONTH, addValue);
        return toLocalDate(todaysDate);
    }

    private LocalDate toLocalDate(Calendar date) {
        return LocalDate.of(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * One expected repayment-schedule row, replacing the untyped {@code Map<String, Object>} the RestAssured test used.
     */
    private record ExpectedInstallment(LocalDate dueDate, String principalDue, String interestDue, String feeChargesDue,
            String penaltyChargesDue) {
    }

    private void verifyLoanRepaymentSchedule(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule,
            List<ExpectedInstallment> expectedvalues) {
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 1);
    }

    private void verifyLoanRepaymentSchedule(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule,
            List<ExpectedInstallment> expectedvalues, int index) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");
        for (ExpectedInstallment values : expectedvalues) {
            GetLoansLoanIdRepaymentPeriod period = loanSchedule.get(index);
            assertEquals(values.dueDate(), period.getDueDate(), "Checking for Due Date for  installment " + index);
            validateNumberForEqualWithMsg("Checking for Principal Due for installment " + index, values.principalDue(),
                    String.valueOf(period.getPrincipalDue()));
            validateNumberForEqualWithMsg("Checking for Interest Due for installment " + index, values.interestDue(),
                    String.valueOf(period.getInterestDue()));
            validateNumberForEqualWithMsg("Checking for Fee charge Due for installment " + index, values.feeChargesDue(),
                    String.valueOf(period.getFeeChargesDue()));
            validateNumberForEqualWithMsg("Checking for Penalty charge Due for installment " + index, values.penaltyChargesDue(),
                    String.valueOf(period.getPenaltyChargesDue()));
            index++;
        }
    }

    private void checkAccrualTransactions(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule, final Long loanId) {
        for (int i = 1; i < loanSchedule.size(); i++) {
            final GetLoansLoanIdRepaymentPeriod repayment = loanSchedule.get(i);
            final LocalDate transactionDate = repayment.getDueDate();

            final Float interestPortion = repayment.getInterestDue().subtract(repayment.getInterestWaived())
                    .subtract(repayment.getInterestWrittenOff()).floatValue();
            final Float feePortion = repayment.getFeeChargesDue().subtract(repayment.getFeeChargesWaived())
                    .subtract(repayment.getFeeChargesWrittenOff()).floatValue();
            final Float penaltyPortion = repayment.getPenaltyChargesDue().subtract(repayment.getPenaltyChargesWaived())
                    .subtract(repayment.getPenaltyChargesWrittenOff()).floatValue();

            checkAccrualTransactionForRepayment(transactionDate, interestPortion, feePortion, penaltyPortion, loanId);
        }
    }

    /** Asserts that an accrual transaction on {@code transactionDate} carries the expected portions. */
    private void checkAccrualTransactionForRepayment(final LocalDate transactionDate, final Float interestPortion, final Float feePortion,
            final Float penaltyPortion, final Long loanId) {
        boolean isAccrualTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual()) && transactionDate.equals(transaction.getDate())) {
                assertEquals(interestPortion, transaction.getInterestPortion().floatValue(), "Mismatch in transaction amounts");
                assertEquals(feePortion, transaction.getFeeChargesPortion().floatValue(), "Mismatch in transaction amounts");
                assertEquals(penaltyPortion, transaction.getPenaltyChargesPortion().floatValue(), "Mismatch in transaction amounts");
                isAccrualTransactionFound = true;
                break;
            }
        }
        assertTrue(isAccrualTransactionFound, "No accrual transaction found on " + transactionDate);
    }

    // ----------------------------------------------------------------------------------------------------
    // Loan lifecycle helpers
    // ----------------------------------------------------------------------------------------------------

    /** Approves the loan for its full principal on {@code approvalDate}, as the RestAssured helper did. */
    private GetLoansLoanIdResponse approveLoan(String approvalDate, Long loanId) {
        approveLoan(loanId, new PostLoansLoanIdRequest().approvedOnDate(approvalDate).dateFormat(DATETIME_PATTERN).locale(LOCALE));
        return getLoanDetails(loanId);
    }

    /** Disburses echoing the loan's own {@code netDisbursalAmount}, mirroring the RestAssured helper. */
    private GetLoansLoanIdResponse disburseLoanWithNetDisbursalAmount(String date, Long loanId, String transactionAmount) {
        BigDecimal netDisbursalAmount = getLoanDetails(loanId).getNetDisbursalAmount();
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest()//
                .actualDisbursementDate(date)//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .netDisbursalAmount(netDisbursalAmount);
        if (transactionAmount != null) {
            request.transactionAmount(toAmount(transactionAmount));
        }
        disburseLoan(loanId, request);
        return getLoanDetails(loanId);
    }

    private GetLoansLoanIdResponse disburseLoanWithNetDisbursalAmount(String date, Long loanId) {
        return disburseLoanWithNetDisbursalAmount(date, loanId, null);
    }

    /** Disburses a tranche of {@code transactionAmount} without echoing a net disbursal amount. */
    private GetLoansLoanIdResponse disburseLoanWithTransactionAmount(Long loanId, String date, String transactionAmount) {
        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(date)//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .transactionAmount(toAmount(transactionAmount)));
        return getLoanDetails(loanId);
    }

    /** Disburses into the loan's linked savings account, echoing the loan's own {@code netDisbursalAmount}. */
    private GetLoansLoanIdResponse disburseToSavingsWithNetDisbursalAmount(String date, Long loanId) {
        BigDecimal netDisbursalAmount = getLoanDetails(loanId).getNetDisbursalAmount();
        disburseToSavings(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(date)//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .netDisbursalAmount(netDisbursalAmount));
        return getLoanDetails(loanId);
    }

    private void waiveInterestOnLoan(Long loanId, String date, double amountToBeWaived) {
        addInterestWaiver(loanId, LoanRequestBuilders.waiveInterest(amountToBeWaived, date));
    }

    /**
     * Repays a loan through the interoperation API, which addresses the loan by account number. That endpoint forwards
     * its body straight to the standard loan repayment command, so it takes {@code PostLoansLoanIdTransactionsRequest}.
     */
    private String makeRepaymentByAccountNo(String accountNo, String transactionDate, Float transactionAmount) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(transactionDate)//
                .transactionAmount(transactionAmount.doubleValue())//
                .note("Repayment Made!!!")//
                .locale(INTEROP_LOCALE)//
                .dateFormat(DATETIME_PATTERN);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().interOperation().loanRepayment(accountNo, request));
    }

    /** Asserts that no accrual transaction has been posted on the loan. */
    private void assertNoAccrualTransactions(Long loanId) {
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            assertFalse(Boolean.TRUE.equals(transaction.getType().getAccrual()), "Accrual entries are posted!");
        }
    }

    private void verifyClientCreatedOnServer(Long clientId) {
        assertNotNull(clientHelper.getClient(clientId), "Client not found on server: " + clientId);
    }

    private void verifyLoanIsPending(Long loanId) {
        verifyLoanStatus(getLoanDetails(loanId), LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
    }

    private void verifyLoanIsApprovedAndWaitingForDisbursal(GetLoansLoanIdResponse loanDetails) {
        verifyLoanStatus(loanDetails, LoanStatus.APPROVED);
        verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getWaitingForDisbursal);
    }

    private void verifyLoanIsActive(GetLoansLoanIdResponse loanDetails) {
        verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
    }

    private void verifyLoanAccountIsClosed(Long loanId) {
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);
    }

    private String todayAsString(DateFormat dateFormat) {
        return dateFormat.format(Calendar.getInstance(Utils.getTimeZoneOfTenant()).getTime());
    }

    private String prepayAmount(Long loanId, String date) {
        return String.valueOf(getPrepayAmount(loanId, date).getAmount());
    }

    // ----------------------------------------------------------------------------------------------------
    // Shared interest-recalculation test bodies
    // ----------------------------------------------------------------------------------------------------

    private void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(final Integer preCloseStrategy,
            final String preCloseAmount) {
        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());

        final Long clientId = createClient();
        final Long loanProductId = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.NONE, LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 0, preCloseStrategy, null, null, null);

        final Long loanId = applyForLoanApplicationForInterestRecalculationWithMoratorium(clientId, loanProductId, loanDisbursementDate,
                DEFAULT_STRATEGY, Collections.emptyList(), 1, null);

        Assertions.assertNotNull(loanId);
        verifyLoanIsPending(loanId);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanId);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanId));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanId));

        loanSchedule = getLoanRepaymentSchedule(loanId);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final String loanRepaymentDate = todayAsString(dateFormat);
        String prepayAmount = prepayAmount(loanId, loanRepaymentDate);
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanId);
        verifyLoanAccountIsClosed(loanId);
    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
            Integer preCloseInterestStrategy, String preCloseAmount) {
        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());

        final Long clientId = createClient();
        final Long loanProductId = createLoanProductWithInterestRecalculation(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.NONE, LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT,
                LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD, 0, preCloseInterestStrategy, null, null, null);

        final Long loanId = applyForLoanApplicationForInterestRecalculation(clientId, loanProductId, loanDisbursementDate, DEFAULT_STRATEGY,
                Collections.emptyList());

        Assertions.assertNotNull(loanId);
        verifyLoanIsPending(loanId);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanId);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanId));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanId));

        loanSchedule = getLoanRepaymentSchedule(loanId);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2551.72", "11.78", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -9);
        final String loanFirstRepaymentDate = dateFormat.format(todaysDate.getTime());
        float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(loanFirstRepaymentDate, totalDueForCurrentPeriod, loanId);

        loanSchedule = getLoanRepaymentSchedule(loanId);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.8", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final String loanRepaymentDate = todayAsString(dateFormat);
        String prepayAmount = prepayAmount(loanId, loanRepaymentDate);
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanId);
        verifyLoanAccountIsClosed(loanId);
    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
            Integer preCloseInterestStrategy, String preCloseAmount) {
        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -4);
        Integer restDateOfMonth = getDayOfMonth(todaysDate);
        Integer restDateOfWeek = getDayOfWeek(todaysDate);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        todaysDate.add(Calendar.DAY_OF_MONTH, 2);
        final String loanFlatChargeDate = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, 14);
        final String loanInterestChargeDate = dateFormat.format(todaysDate.getTime());

        List<PostLoansRequestChargeData> charges = new ArrayList<>(2);
        Long flat = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 100.0, false).getResourceId();
        Long principalPercentage = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT, 2.0, false)
                .getResourceId();
        addCharges(charges, flat, 100.0, loanFlatChargeDate);
        addCharges(charges, principalPercentage, 2.0, loanInterestChargeDate);

        final Long clientId = createClient();
        final Long loanProductId = createLoanProductWithInterestRecalculationAndCompoundingDetails(DEFAULT_STRATEGY,
                LoanTestData.InterestRecalculationCompoundingMethod.INTEREST_AND_FEE,
                LoanTestData.RescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS, LoanTestData.RecalculationRestFrequencyType.WEEKLY, 1,
                LoanTestData.RecalculationCompoundingFrequencyType.SAME_AS_REPAYMENT_PERIOD, null, preCloseInterestStrategy, null, null,
                null, restDateOfMonth, restDateOfWeek);

        final Long loanId = applyForLoanApplicationForInterestRecalculation(clientId, loanProductId, loanDisbursementDate, DEFAULT_STRATEGY,
                charges);

        Assertions.assertNotNull(loanId);
        verifyLoanIsPending(loanId);

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getLoanRepaymentSchedule(loanId);
        List<ExpectedInstallment> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        verifyLoanIsApprovedAndWaitingForDisbursal(approveLoan(loanDisbursementDate, loanId));

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        verifyLoanIsActive(disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanId));

        loanSchedule = getLoanRepaymentSchedule(loanId);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.08", "46.83", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2481.87", "47.04", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2553.29", "11.78", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -9);
        final String loanFirstRepaymentDate = dateFormat.format(repaymentDate.getTime());
        float totalDueForCurrentPeriod = loanSchedule.get(1).getTotalDueForPeriod().floatValue();
        makeRepayment(loanFirstRepaymentDate, totalDueForCurrentPeriod, loanId);

        loanSchedule = getLoanRepaymentSchedule(loanId);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.05", "34.86", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.97", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final String loanRepaymentDate = todayAsString(dateFormat);
        String prepayAmount = prepayAmount(loanId, loanRepaymentDate);
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanId);
        verifyLoanAccountIsClosed(loanId);
    }

    @Override
    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Loan Product Description")//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)//
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(true)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalculation(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(Collections.emptyList())//
                .accountingRule(ACCRUAL_PERIODIC)//
                .fundSourceAccountId(suspenseClearingAccount.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .dateFormat(DATETIME_PATTERN)//
                .locale(LOCALE)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(creditLossBadDebtAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(creditLossBadDebtFraudAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue());
    }
}
