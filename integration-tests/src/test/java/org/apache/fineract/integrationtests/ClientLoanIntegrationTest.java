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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
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
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.AccountTransferHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Application Repayments Schedule, loan charges, penalties, loan
 * repayments and verifying accounting transactions
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class ClientLoanIntegrationTest {

    static {
        Utils.initializeRESTAssured();
    }

    private static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanIntegrationTest.class);
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String ACCRUAL_UPFRONT = "4";

    private static final ResponseSpecification RESPONSE_SPEC = createResponseSpecification(200);
    private static final RequestSpecification REQUEST_SPEC = createRequestSpecification();
    private static final LoanTransactionHelper LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
    private static final JournalEntryHelper JOURNAL_ENTRY_HELPER = new JournalEntryHelper(REQUEST_SPEC, RESPONSE_SPEC);
    private static final AccountHelper ACCOUNT_HELPER = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
    // asset
    private static final Account LOANS_RECEIVABLE_ACCOUNT = ACCOUNT_HELPER.createAssetAccount();
    private static final Account INTEREST_FEE_RECEIVABLE_ACCOUNT = ACCOUNT_HELPER.createAssetAccount();
    private static final Account SUSPENSE_ACCOUNT = ACCOUNT_HELPER.createAssetAccount();
    // liability
    private static final Account SUSPENSE_CLEARING_ACCOUNT = ACCOUNT_HELPER.createLiabilityAccount();
    private static final Account OVERPAYMENT_ACCOUNT = ACCOUNT_HELPER.createLiabilityAccount();
    // income
    private static final Account INTEREST_INCOME_ACCOUNT = ACCOUNT_HELPER.createIncomeAccount();
    private static final Account FEE_INCOME_ACCOUNT = ACCOUNT_HELPER.createIncomeAccount();
    private static final Account FEE_CHARGE_OFF_ACCOUNT = ACCOUNT_HELPER.createIncomeAccount();
    private static final Account RECOVERIES_ACCOUNT = ACCOUNT_HELPER.createIncomeAccount();
    private static final Account INTEREST_INCOME_CHARGE_OFF_ACCOUNT = ACCOUNT_HELPER.createIncomeAccount();
    // expense
    private static final Account CREDIT_LOSS_BAD_DEBT_ACCOUNT = ACCOUNT_HELPER.createExpenseAccount();
    private static final Account CREDIT_LOSS_BAD_DEBT_FRAUD_ACCOUNT = ACCOUNT_HELPER.createExpenseAccount();
    private static final Account WRITTEN_OFF_ACCOUNT = ACCOUNT_HELPER.createExpenseAccount();
    private static final Account GOODWILL_EXPENSE_ACCOUNT = ACCOUNT_HELPER.createExpenseAccount();
    private static final SchedulerJobHelper SCHEDULER_JOB_HELPER = new SchedulerJobHelper(REQUEST_SPEC);
    private static final PeriodicAccrualAccountingHelper PERIODIC_ACCRUAL_ACCOUNTING_HELPER = new PeriodicAccrualAccountingHelper(
            REQUEST_SPEC, RESPONSE_SPEC);
    private static final SavingsAccountHelper SAVINGS_ACCOUNT_HELPER = new SavingsAccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
    private static final AccountTransferHelper ACCOUNT_TRANSFER_HELPER = new AccountTransferHelper(REQUEST_SPEC, RESPONSE_SPEC);
    private static final LoanProductHelper LOAN_PRODUCT_HELPER = new LoanProductHelper();
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern(DATETIME_PATTERN)
            .toFormatter();
    private static final BusinessDateHelper BUSINESS_DATE_HELPER = new BusinessDateHelper();
    private static final ChargesHelper CHARGES_HELPER = new ChargesHelper();
    private static final ClientHelper CLIENT_HELPER = new ClientHelper(REQUEST_SPEC, RESPONSE_SPEC);

    private static RequestSpecification createRequestSpecification() {
        RequestSpecification request = new RequestSpecBuilder().setContentType(ContentType.JSON).build();

        request.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        request.header("Fineract-Platform-TenantId", "default");
        return request;
    }

    private static ResponseSpecification createResponseSpecification(int statusCode) {
        return new ResponseSpecBuilder().expectStatusCode(statusCode).build();
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        List<HashMap> collaterals = new ArrayList<>();

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);

        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, NONE);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "12,000.00", collaterals);
        final ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);
    }

    @Test
    public void validateClientLoanWithUniqueExternalId() {
        // Given
        final ResponseSpecification responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final String externalId = UUID.randomUUID().toString();

        // When
        final Integer loanID = applyForLoanApplicationWithExternalId(REQUEST_SPEC, RESPONSE_SPEC, clientID, loanProductID, "12,000.00",
                externalId);

        // Then
        assertNotNull(loanID);
        applyForLoanApplicationWithExternalId(REQUEST_SPEC, responseSpec403, clientID, loanProductID, "12,000.00", externalId);
    }

    @Test
    public void testAddingLoanChargeIncludesLoanIdInTheResponse() {
        // given
        Integer clientId = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        Integer loanProductId = createLoanProduct(false, NONE);
        Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientId), collateralId);
        List<HashMap> collaterals = List.of(collaterals(clientCollateralId, BigDecimal.ONE));

        Integer chargeId = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        List<HashMap> charges = List.of(charges(chargeId, "1", null));
        // when
        Integer loanId = applyForLoanApplication(clientId, loanProductId, charges, null, "12,000.00", collaterals);
        // then
        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanId);
        Integer loanChargeId = (Integer) loanCharges.get(0).get("id");
        HashMap loanChargeDetail = LOAN_TRANSACTION_HELPER.getLoanCharge(loanId, loanChargeId);
        assertEquals(loanId, loanChargeDetail.get("loanId"));
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE() {
        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        List<HashMap> collaterals = new ArrayList<>();
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper.getLoanDisbursementJSON());

        Integer amountPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, amountPercentage, "1", null);
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentage, "1", null);
        Integer interestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1"));
        addCharges(charges, interestPercentage, "1", null);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap disbursementDetail = loanSchedule.get(0);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getDisbursementChargesForLoanAsJSON(String.valueOf(flatDisbursement)));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flatDisbursement, "1"), null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flatDisbursement, "100", null);
        LOAN_TRANSACTION_HELPER.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.deleteChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);
        Assertions.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE_WITH_AMOUNT_CHANGE() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer amountPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, amountPercentage, "1", null);
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentage, "1", null);
        Integer interestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1"));
        addCharges(charges, interestPercentage, "1", null);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap disbursementDetail = loanSchedule.get(0);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");
        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "100.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "0.0", "5.05", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "0.0", "105.05", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.get("feeChargesDue")));

    }

    @Test
    public void testLoanDisbursedTodayIsRetrieved() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "5", null);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String LOAN_DISBURSEMENT_DATE = "2 June 2014";

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE on todays date so that loan can't be in arrears
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID, "10000",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        // Test added because loans created without arrears were failing to be retrieved (associations=all) due to inner
        // join on m_loan_arrears_aging (now left join)
        Assertions.assertNotNull(loanDetails, "Empty Loan Details");
        Assertions.assertNotNull(JsonPath.from(loanDetails).get("id"), "No id Found");

    }

    @Test
    public void testLoanCharges_SPECIFIED_DUE_DATE_FEE() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer flat = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer amountPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap firstInstallment = loanSchedule.get(1);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), "29 September 2011", "100"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);

        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(firstInstallment.get("feeChargesDue")));

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(REQUEST_SPEC, RESPONSE_SPEC, clientID, MINIMUM_OPENING_BALANCE);
        LOAN_TRANSACTION_HELPER.updateLoan(loanID, updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null),
                String.valueOf(savingsId), collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(firstInstallment.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flat, "100", "29 September 2011");
        LOAN_TRANSACTION_HELPER.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        Assertions.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "29 September 2011", "100"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        // DISBURSE
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(amountPercentage), "29 September 2011", "1"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"), "");
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesOutstanding")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesWaived")));

        LOAN_TRANSACTION_HELPER.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, null));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "0.0", "100.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesWaived")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesPaid")));
        validateNumberForEqual("0.0", String.valueOf(firstInstallment.get("feeChargesOutstanding")));
    }

    @Test
    public void testLoanCharges_INSTALMENT_FEE() {
        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer flat = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));

        Integer amountPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        Float totalPerOfAmout = 0F;
        Float totalPerOfAmoutPlusInt = 0F;
        Float totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 50;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        LOAN_TRANSACTION_HELPER.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("100"));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(REQUEST_SPEC, RESPONSE_SPEC, clientID, MINIMUM_OPENING_BALANCE);
        LOAN_TRANSACTION_HELPER.updateLoan(loanID, updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null),
                String.valueOf(savingsId), collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        LOAN_TRANSACTION_HELPER.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        charges.clear();
        addCharges(charges, flat, "50", "29 September 2011");
        LOAN_TRANSACTION_HELPER.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null, collaterals));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("50", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        LOAN_TRANSACTION_HELPER.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("0", String.valueOf(installment.get("feeChargesDue")));
        }

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "100"));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("100", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");

        // DISBURSE
        String loanDetail = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10000",
                JsonPath.from(loanDetail).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));

        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        Integer waivePeriodnum = 1;
        final Integer waivedChargeId = LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(waivePeriodnum)));

        // Get loan transaction details
        ArrayList<HashMap> loanDetails = LOAN_TRANSACTION_HELPER.getLoanTransactionDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        Assertions.assertNotNull(loanDetails, "Empty Loan Details");
        Gson gson = new Gson();
        Integer transId = null;
        Integer chargeId = null;
        for (HashMap detail : loanDetails) {
            String resultObject = gson.toJson(detail);
            JsonObject reportObject = JsonParser.parseString(resultObject).getAsJsonObject();
            JsonObject type = reportObject.getAsJsonObject("type");
            final Integer transTypeId = type.get("id").getAsInt();
            Assertions.assertNotNull(transTypeId);
            if (Integer.valueOf(9).compareTo(transTypeId) == 0) {
                transId = reportObject.get("id").getAsInt();
                Assertions.assertNotNull(transId);
                final HashMap<String, String> map = new HashMap<>();
                map.put("id", transId.toString());
                map.put("loanId", loanID.toString());
                final String putBody = gson.toJson(map);
                chargeId = LOAN_TRANSACTION_HELPER.undoWaiveChargesForLoanReturnResourceId(loanID, transId, putBody);
                break;
            }
        }

        Assertions.assertEquals(waivedChargeId, chargeId);

        // Validate the undo process
        ArrayList<HashMap> loanTransactionDetails = LOAN_TRANSACTION_HELPER.getLoanTransactionDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        Assertions.assertNotNull(loanTransactionDetails, "Empty Loan Transaction Details");
        for (int i = 0; i < loanTransactionDetails.size(); i++) {
            String resultObject = gson.toJson(loanTransactionDetails.get(i));
            JsonObject reportObject = JsonParser.parseString(resultObject).getAsJsonObject();
            final Boolean isReversed = reportObject.get("manuallyReversed").getAsBoolean();
            final Integer id = reportObject.get("id").getAsInt();

            if (transId.compareTo(id) == 0) {
                final HashMap chargeDetails = LOAN_TRANSACTION_HELPER.getLoanCharge(loanID, waivedChargeId);
                String resultChargeObject = gson.toJson(chargeDetails);
                JsonObject reportChargeObject = JsonParser.parseString(resultChargeObject).getAsJsonObject();
                BigDecimal waiveAmount = reportChargeObject.get("amountWaived").getAsBigDecimal();

                Assertions.assertEquals(true, isReversed);
                Assertions.assertEquals(Double.valueOf(0), waiveAmount.doubleValue());
                break;
            } else if (transId.compareTo(id) != 0 && i == loanTransactionDetails.size() - 1) {
                Assertions.assertEquals(transId, id);
            }
        }

        // Re-waive charge
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, waivedChargeId,
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(waivePeriodnum)));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (waivePeriodnum.equals(installment.get("period"))) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesWaived")));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.get("feeChargesWaived")));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

        Integer payPeriodnum = 2;
        LOAN_TRANSACTION_HELPER.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, String.valueOf(payPeriodnum)));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (payPeriodnum.equals(installment.get("period"))) {
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesPaid")));
            } else if (waivePeriodnum.equals(installment.get("period"))) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesWaived")));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.get("feeChargesPaid")));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "300", "100.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

        // Loan Charges with US Locale using the amount as a number in the JSON body
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), 50.05, Locale.US));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("200.05", String.valueOf(installment.get("feeChargesDue")));
        }

        // Loan Charges with other Locale using comma (,) as decimal delimiter
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50,05", Locale.GERMAN));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("250.10", String.valueOf(installment.get("feeChargesDue")));
        }

        // Loan Charges with German Locale (where the comma is the decimal delimiter) using the amount as a number in
        // the JSON body
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), 50.05, Locale.GERMAN));
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("300.15", String.valueOf(installment.get("feeChargesDue")));
        }
    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(REQUEST_SPEC, RESPONSE_SPEC, clientID, MINIMUM_OPENING_BALANCE);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, savingsId.toString(), "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        // DISBURSE
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanToSavings(SavingsAccountHelper.TRANSACTION_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("12000");
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_TRANCHES() {
        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, null, "45,000.00", tranches, collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("01 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE first Tranche
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 March 2014", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // DISBURSE Second Tranche
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("23 April 2014", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS_WITH_TRANCHES() {
        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(REQUEST_SPEC, RESPONSE_SPEC, clientID, MINIMUM_OPENING_BALANCE);

        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, savingsId.toString(), "45,000.00",
                tranches, collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("01 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        // DISBURSE first Tranche
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanToSavings("01 March 2014", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("25000");
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        // DISBURSE Second Tranche
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanToSavings("23 April 2014", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat("25000") + Float.parseFloat("20000");
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);
        balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndCashBasedAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2911.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("150.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240.00"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3251.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2969.72"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("181.77"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2811.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("150.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3129.11"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("50.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("122.38"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.DEBIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3139.68"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3139.68"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3089.68"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("50.00"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithCahargesOfTypeAmountPercentageAndCashBasedAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2911.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("149.11"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240.00"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3271.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2969.72"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("181.77"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2791.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("149.11"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3301.78"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3149.11"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("30.29"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("122.38"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3120.58"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3120.58"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3089.68"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("30.90"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges with calculation type percentage of
     * amount plus interest
     */
    @Test
    public void loanWithChargesOfTypeAmountPlusInterestPercentageAndCashBasedAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2911.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("157.57"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240.00"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3277.55"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2969.72"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("181.77"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2791.49"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("157.57"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3303"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3149.11"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("31.51"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("122.38"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3121.19"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3121.19"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3089.68"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("31.51"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndUpfrontAccrualAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("50.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("200.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("200.00"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), "29 September 2011", "100"));

        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.DEBIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT));

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.DEBIT));

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3251.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3251.49"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make over payment for repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3220.60"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3220.60"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3139.68"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("80.92"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithCahargesAndUpfrontAccrualAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.DEBIT));

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3271.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3271.49"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3301.78"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.78"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make over payment for repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3220.58"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3220.58"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3120.58"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding charges with calculation type percentage of
     * amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndUpfrontAccrualAccountingEnabled() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("31.51", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("126.04"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("605.94"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("126.04"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 September 2011", "1"));

        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.DEBIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.CREDIT));

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("31.51"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("31.51"), JournalEntry.TransactionType.DEBIT));

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3277.55"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3277.55"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3303"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3303"), JournalEntry.TransactionType.CREDIT));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make over payment for repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3221.61"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3221.61"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3121.19"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100.42"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type flat
     */
    @Test
    public void loanWithFlatChargesAndPeriodicAccrualAccountingEnabled() throws InterruptedException {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        /*
         * JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount( assetAccount, "20 September 2011", new
         * JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.CREDIT));
         * JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.parseFloat("50.0"), JournalEntry.TransactionType.DEBIT));
         */
        final String jobName = "Add Accrual Transactions";

        SCHEDULER_JOB_HELPER.executeAndAwaitJob(jobName);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3251.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3251.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3251.49"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.49"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3301.49"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("100"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3139.68"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3139.68"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3139.68"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    /**
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type percentage of
     * amount
     */
    @Test
    public void loanWithChargesOfTypeAmountPercentageAndPeriodicAccrualAccountingEnabled() throws InterruptedException {
        try {
            GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                    GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, true);
            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
            ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

            // Add charges with payment mode regular
            List<HashMap> charges = new ArrayList<>();
            Integer percentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
            addCharges(charges, percentageDisbursementCharge, "1", null);

            Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
            addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

            Integer percentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
            addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            List<HashMap> collaterals = new ArrayList<>();

            final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

            final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                    String.valueOf(clientID), collateralId);
            addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

            final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);
            final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
            Assertions.assertNotNull(loanID);
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            verifyLoanRepaymentSchedule(loanSchedule);

            List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
            validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
            validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

            // check for disbursement fee
            HashMap disbursementDetail = loanSchedule.get(0);
            validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

            // check for charge at specified date and installment fee
            HashMap firstInstallment = loanSchedule.get(1);
            validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

            // check for installment fee
            HashMap secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            ArrayList<HashMap> loanTransactionDetails = LOAN_TRANSACTION_HELPER.getLoanTransactionDetails(REQUEST_SPEC, RESPONSE_SPEC,
                    loanID);
            validateAccrualTransactionForDisbursementCharge(loanTransactionDetails);
            final JournalEntry[] assetAccountInitialEntry = {
                    new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                    new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
            JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                    new JournalEntry(Float.parseFloat("120.00"), JournalEntry.TransactionType.CREDIT));
            loanCharges.clear();
            loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

            LOG.info("-------------Make repayment 1-----------");
            LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3300.60"), loanID);
            loanCharges.clear();
            loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
            validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
            validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
            validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                    new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.CREDIT));

            LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
            LOG.info("----------- Waive installment charge for 2nd installment ---------");
            LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                    LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
            loanCharges.clear();
            loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
            validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

            /*
             * JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount( assetAccount, "20 September 2011", new
             * JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.CREDIT));
             * JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount (expenseAccount, "20 September 2011", new
             * JournalEntry(Float.parseFloat("29.7"), JournalEntry.TransactionType.DEBIT));
             */

            final String jobName = "Add Accrual Transactions";

            SCHEDULER_JOB_HELPER.executeAndAwaitJob(jobName);

            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            checkAccrualTransactions(loanSchedule, loanID);

            LOG.info("----------Make repayment 2------------");
            LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3271.49"), loanID);
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                    new JournalEntry(Float.parseFloat("3271.49"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("3271.49"), JournalEntry.TransactionType.CREDIT));

            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

            LOG.info("--------------Waive interest---------------");
            LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            HashMap thirdInstallment = loanSchedule.get(3);
            validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                    new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
            JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                    new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

            Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
            LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
            loanCharges.clear();
            loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
            validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            secondInstallment = loanSchedule.get(2);
            validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

            // checking the journal entry as applied penalty has been collected
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                    new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("3300.60"), JournalEntry.TransactionType.CREDIT));

            LOG.info("----------Make repayment 3 advance------------");
            LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3301.78"), loanID);
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                    new JournalEntry(Float.parseFloat("3301.78"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("3301.78"), JournalEntry.TransactionType.CREDIT));

            LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            HashMap fourthInstallment = loanSchedule.get(4);
            validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
            validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

            LOG.info("----------Pay applied penalty ------------");
            LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                    new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.CREDIT));
            loanSchedule.clear();
            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            fourthInstallment = loanSchedule.get(4);
            validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
            validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

            LOG.info("----------Make repayment 4 ------------");
            LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3120.58"), loanID);
            JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                    new JournalEntry(Float.parseFloat("3120.58"), JournalEntry.TransactionType.DEBIT),
                    new JournalEntry(Float.parseFloat("3120.58"), JournalEntry.TransactionType.CREDIT));
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
            LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
        } finally {
            GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                    GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, false);
        }
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding charges with calculation type percentage of
     * amount and interest
     */
    @Test
    public void loanWithChargesOfTypeAmountPlusInterestPercentageAndPeriodicAccrualAccountingEnabled() throws InterruptedException {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00", collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.parseFloat("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        LOG.info("-------------Make repayment 1-----------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        LOG.info("----------- Waive installment charge for 2nd installment ---------");
        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        /*
         * JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount( assetAccount, "20 September 2011", new JournalEntry(
         * Float.parseFloat("31.51"), JournalEntry.TransactionType.CREDIT));
         * JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.parseFloat("31.51"), JournalEntry.TransactionType.DEBIT));
         */

        final String jobName = "Add Accrual Transactions";

        SCHEDULER_JOB_HELPER.executeAndAwaitJob(jobName);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        LOG.info("----------Make repayment 2------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3277.55"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3277.55"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3277.55"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        LOG.info("--------------Waive interest---------------");
        LOAN_TRANSACTION_HELPER.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.parseFloat("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = LOAN_TRANSACTION_HELPER.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011",
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3309.06"), JournalEntry.TransactionType.CREDIT));

        LOG.info("----------Make repayment 3 advance------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3303"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.parseFloat("3303"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3303"), JournalEntry.TransactionType.CREDIT));

        LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Pay applied penalty ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("120"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        LOG.info("----------Make repayment 4 ------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3121.19"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012",
                new JournalEntry(Float.parseFloat("3121.19"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("3121.19"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, collaterals);
        final ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentScheduleForEqualPrincipal(loanSchedule);

    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails_with_grace() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "5", collaterals);
        final ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(loanSchedule);

    }

    /***
     * Test case to verify RBI payment strategy
     */
    @Test
    public void testRBIPaymentStrategy() {

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        /***
         * Create loan product with RBI strategy
         */
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.RBI_INDIA_STRATEGY);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithPaymentStrategy(clientID, loanProductID, null, savingsId, principal,
                LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("3200", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        /***
         * Make payment for installment #1
         */
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        /***
         * Verify 2nd and 3rd repayments dues before making excess payment for installment no 2
         */
        HashMap secondInstallment = loanSchedule.get(2);
        HashMap thirdInstallment = loanSchedule.get(3);

        validateNumberForEqual("3200", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

        validateNumberForEqual("3000", String.valueOf(secondInstallment.get("principalOutstanding")));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.get("principalOutstanding")));

        /***
         * Make payment for installment #2
         */
        LOAN_TRANSACTION_HELPER.makeRepayment("20 November 2011", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        /***
         * Verify 2nd and 3rd repayments after making excess payment for installment no 2
         */
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        /***
         * According to RBI Excess payment should go to principal portion of next installment, but as interest
         * recalculation is not implemented, it wont make any difference to schedule even though if we made excess
         * payment, so excess payments will behave the same as regular payment with the excess amount
         */
        thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.get("principalOutstanding")));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.get("principalPaid")));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.get("interestPaid")));
        validateNumberForEqual("100.00", String.valueOf(thirdInstallment.get("interestOutstanding")));

        /***
         * Make payment with due amount of 3rd installment on 4th installment date
         */
        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);

        /***
         * Verify overdue interests are deducted first and then remaining amount for interest portion of due installment
         */
        thirdInstallment = loanSchedule.get(3);
        HashMap fourthInstallment = loanSchedule.get(4);

        validateNumberForEqual("100", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("100", String.valueOf(thirdInstallment.get("principalOutstanding")));

        validateNumberForEqual("2900", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("interestPaid")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));

        LOAN_TRANSACTION_HELPER.makeRepayment("20 January 2012", Float.parseFloat("3000"), loanID);

        /***
         * verify loan is closed as we paid full amount
         */
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3965.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1771.88", "16.39", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1780.05", "8.22", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", "3551.93", prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
        String preCloseAmount = "7561.84";
        testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_REST_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE;
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

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, null);

        List<HashMap> charges = new ArrayList<>();
        Integer installmentCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "10", false));
        addCharges(charges, installmentCharge, "10", null);
        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "1.16", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3961.84", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1773.61", "16.41", "1.64", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1781.79", "8.22", "0.82", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.RBI_INDIA_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanFutureRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Map<String, Object> paymentday = new HashMap<>(3);
        paymentday.put("dueDate", getDateAsArray(today, -5, Calendar.DAY_OF_MONTH));
        paymentday.put("principalDue", "3990.09");
        paymentday.put("interestDue", "9.91");
        paymentday.put("feeChargesDue", "0");
        paymentday.put("penaltyChargesDue", "0");
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        expectedvalues.add(paymentday);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.31", "11.6", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1009.84", "4.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testInteroperationLoanRepaymentAPI() {
        try {
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", true);
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
            ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
            final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                    LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY,
                    LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                    LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
                    LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, "12");

            final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, loanDisbursementDate,
                    LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(loanDisbursementDate, loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            Assertions.assertNotNull(loanSchedule);
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());

            Float earlyPayment = Float.parseFloat("3000");
            String accountNo = JsonPath.from(loanDetails).get("accountNo").toString();

            HashMap loanRepayment = LOAN_TRANSACTION_HELPER.makeRepaymentWithAccountNo(loanFirstRepaymentDate, earlyPayment, accountNo);
            assertNotNull(loanRepayment);
        } finally {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", false);
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
        List<HashMap> charges = new ArrayList<>(2);
        Integer flat = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer principalPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "2", false));

        addCharges(charges, flat, "100", LOAN_FLAT_CHARGE_DATE);
        addCharges(charges, principalPercentage, "2", LOAN_INTEREST_CHARGE_DATE);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.DEFAULT_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, compoundingDayOfMonth, compoundingDayOfWeek,
                restDayOfMonth, restDayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "5065.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "0", "11.32", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2451.93", "11.32", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
        String preCloseAmount = "7761.89";
        testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_REST_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE;
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

        Integer overdueFeeChargeId = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("10"));
        Assertions.assertNotNull(overdueFeeChargeId);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null,
                overdueFeeChargeId.toString(), false, null, null, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                REST_START_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, null);

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());

        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.33", "46.58", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2552.37", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        String JobName = "Apply penalty to overdue loans";
        SCHEDULER_JOB_HELPER.executeAndAwaitJob(JobName);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        totalDueForCurrentPeriod = totalDueForCurrentPeriod - Float.parseFloat("252.89");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("Disbursal Date Calendar {}", todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        Account[] accounts = { assetAccount, incomeAccount, expenseAccount, overpaymentAccount };
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, accounts, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("Date during repayment schedule {}", todaysDate.getTime());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(10000.0f, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(10000.0f, JournalEntry.TransactionType.DEBIT), };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String runOndate = dateFormat.format(todaysDate.getTime());
        LOG.info("runOndate : {}", runOndate);
        PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting(runOndate);
        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 46.15f, 0f, 0f, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting(runOndate);
        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_CURRENT_REPAYMENT_BASED_ARREARS_AGEING() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.RBI_INDIA_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, getDayOfMonth(todaysDate),
                getDayOfWeek(todaysDate), getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        HashMap loanSummary = LOAN_TRANSACTION_HELPER.getLoanSummary(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = LOAN_TRANSACTION_HELPER.getLoanSummary(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_ORIGINAL_REPAYMENT_BASED_ARREARS_AGEING() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        LOG.info("----timeeeeeeeeeeeeee------> {}", dateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null,
                true, null, null, getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        HashMap loanSummary = LOAN_TRANSACTION_HELPER.getLoanSummary(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = LOAN_TRANSACTION_HELPER.getLoanSummary(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        dates = (List) loanSummary.get("overdueSinceDate");
        Assertions.assertNull(dates);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, "10006.59");
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_REST_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE, "10046.15");
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

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, CASH_BASED, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId,
                principal, LoanApplicationTestBuilder.DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = {
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                new JournalEntry(Float.parseFloat("2290"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2000"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow,
                new JournalEntry(Float.parseFloat("50"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                new JournalEntry(Float.parseFloat("4580"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("4000"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow,
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("480"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

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

        LOAN_TRANSACTION_HELPER.makeRefundByCash(now, Float.parseFloat("20"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        LOAN_TRANSACTION_HELPER.makeRefundByCash(now, Float.parseFloat("2000"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("2000"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("1980"), JournalEntry.TransactionType.DEBIT));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

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

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, ACCRUAL_UPFRONT, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);// ,
        // LoanProductTestBuilder.EQUAL_INSTALLMENTS,
        // LoanProductTestBuilder.FLAT_BALANCE);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId,
                principal, LoanApplicationTestBuilder.DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.parseFloat("1440"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("300.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                new JournalEntry(Float.parseFloat("2290"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2290"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                new JournalEntry(Float.parseFloat("4580"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("4580"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

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

        LOAN_TRANSACTION_HELPER.makeRefundByCash(now, Float.parseFloat("20"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        LOAN_TRANSACTION_HELPER.makeRefundByCash(now, Float.parseFloat("2000"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("2000"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("1980"), JournalEntry.TransactionType.DEBIT));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

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

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);

        final Integer savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = SAVINGS_ACCOUNT_HELPER.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = SAVINGS_ACCOUNT_HELPER.updateSavingsAccount(clientID, savingsProductID, savingsId, ACCOUNT_TYPE_INDIVIDUAL);
        assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(REQUEST_SPEC, RESPONSE_SPEC, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = SAVINGS_ACCOUNT_HELPER.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = SAVINGS_ACCOUNT_HELPER.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, CASH_BASED, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */

        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        List<HashMap> collaterals = new ArrayList<>();

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, null, principal,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, fourMonthsfromNow, collaterals);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(fourMonthsfromNow, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = {
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("12000.00"), JournalEntry.TransactionType.DEBIT) };
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(threeMonthsfromNow, Float.parseFloat("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(twoMonthsfromNow, Float.parseFloat("2290"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow,
                new JournalEntry(Float.parseFloat("2290"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("2000"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow,
                new JournalEntry(Float.parseFloat("50"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("240"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        LOAN_TRANSACTION_HELPER.makeRepayment(oneMonthfromNow, Float.parseFloat("4580"), loanID);
        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow,
                new JournalEntry(Float.parseFloat("4580"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.parseFloat("4000"), JournalEntry.TransactionType.CREDIT));
        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow,
                new JournalEntry(Float.parseFloat("100"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("480"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

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

        ACCOUNT_TRANSFER_HELPER.refundLoanByTransfer(now, clientID, loanID, clientID, savingsId, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, transferAmountValue.toString());

        Float toSavingsBalance = Float.parseFloat(MINIMUM_OPENING_BALANCE);

        HashMap toSavingsSummaryAfter = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);

        toSavingsBalance += transferAmountValue;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals(toSavingsBalance, toSavingsSummaryAfter.get("accountBalance"),
                "Verifying From Savings Account Balance after Account Transfer");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0
        // final String now = Utils.convertDate(fourMonthsfromNowCalendar);

        transferAmountValue = 2000f;

        ACCOUNT_TRANSFER_HELPER.refundLoanByTransfer(now, clientID, loanID, clientID, savingsId, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, transferAmountValue.toString());

        toSavingsSummaryAfter = SAVINGS_ACCOUNT_HELPER.getSavingsSummary(savingsId);

        toSavingsBalance += transferAmountValue;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals(toSavingsBalance, toSavingsSummaryAfter.get("accountBalance"),
                "Verifying From Savings Account Balance after Account Transfer");

        JOURNAL_ENTRY_HELPER.checkJournalEntryForAssetAccount(assetAccount, now,
                new JournalEntry(Float.parseFloat("2000"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.parseFloat("1980"), JournalEntry.TransactionType.DEBIT));

        JOURNAL_ENTRY_HELPER.checkJournalEntryForIncomeAccount(incomeAccount, now,
                new JournalEntry(Float.parseFloat("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

    }

    @Test
    public void testLoanProductConfiguration() {
        final String proposedAmount = "5000";
        JsonObject loanProductConfigurationAsTrue = new JsonObject();
        loanProductConfigurationAsTrue = createLoanProductConfigurationDetail(loanProductConfigurationAsTrue, true);

        JsonObject loanProductConfigurationAsFalse = new JsonObject();
        loanProductConfigurationAsFalse = createLoanProductConfigurationDetail(loanProductConfigurationAsFalse, false);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2012");
        Integer loanProductID = LOAN_TRANSACTION_HELPER
                .getLoanProductId(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments().withRepaymentTypeAsMonth()
                        .withRepaymentAfterEvery("1").withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY)
                        .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays().withInArrearsTolerance("10")
                        .withMoratorium("2", "3").withLoanProductConfiguration(loanProductConfigurationAsTrue).build(null));
        LOG.info("-----------------------LOAN PRODUCT CREATED WITH ATTRIBUTE CONFIGURATION AS TRUE-------------------------- {}",
                loanProductID);
        Integer loanID = applyForLoanApplicationWithProductConfigurationAsTrue(clientID, loanProductID, proposedAmount);
        LOG.info("------------------------LOAN CREATED WITH ID------------------------------{}", loanID);

        loanProductID = LOAN_TRANSACTION_HELPER.getLoanProductId(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments()
                .withRepaymentTypeAsMonth().withRepaymentAfterEvery("1").withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY)
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays().withInArrearsTolerance("10")
                .withMoratorium("2", "3").withLoanProductConfiguration(loanProductConfigurationAsFalse).build(null));
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

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();

        Integer flatAmountChargeOne = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatAmountChargeOne, "50", "01 October 2011");
        Integer flatAmountChargeTwo = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        addCharges(charges, flatAmountChargeTwo, "100", "15 December 2011");

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "10,000.00", collaterals);
        Assertions.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("----------------------------------- APPROVE LOAN -----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("----------------------------------- DISBURSE LOAN ----------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "10,000.00",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        LOG.info("---------------------------------- Make repayment 1 --------------------------------------");
        LOAN_TRANSACTION_HELPER.makeRepayment("20 October 2011", Float.parseFloat("2676.24"), loanID);

        LOG.info("---------------------------------- FORECLOSE LOAN ----------------------------------------");
        LOAN_TRANSACTION_HELPER.forecloseLoan("08 November 2011", loanID);

        // retrieving the loan status
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        // verifying the loan status is closed
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
        // retrieving the loan sub-status
        loanStatusHashMap = LoanStatusChecker.getSubStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        // verifying the loan sub-status is foreclosed
        LoanStatusChecker.verifyLoanAccountForeclosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_INTEREST_FIRST_STRATEGY_AND_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, new ArrayList<HashMap>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanFutureRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Map<String, Object> paymentday = new HashMap<>(3);
        paymentday.put("dueDate", getDateAsArray(today, -5, Calendar.DAY_OF_MONTH));
        paymentday.put("principalDue", "3990.09");
        paymentday.put("interestDue", "9.91");
        paymentday.put("feeChargesDue", "0");
        paymentday.put("penaltyChargesDue", "0");
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        expectedvalues.add(paymentday);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.31", "11.6", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1009.84", "4.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_INTEREST_FIRST_STRATEGY_AND_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS_EARLY_REPAYMENT() {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, new ArrayList<HashMap>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanFutureRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
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
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        // early-repayment
        addRepaymentValues(expectedvalues, todaysDate, 5, true, "2504.13", "24.78", "0.0", "0.0");

        addRepaymentValues(expectedvalues, todaysDate, 2, true, "2522.33", "6.58", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2490.78", "11.5", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculationMakePrepaymentAfterRepayment() {
        try {
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", true);
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            Calendar currentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
            ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
            final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                    LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY,
                    LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                    LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
                    LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, "12");

            final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, loanDisbursementDate,
                    LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(loanDisbursementDate, loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            Assertions.assertNotNull(loanSchedule);
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());
            //
            Float earlyPayment = Float.parseFloat("3000");
            LOAN_TRANSACTION_HELPER.makeRepayment(loanFirstRepaymentDate, earlyPayment, loanID);

            HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            String prepayAmount = String.valueOf(prepayDetail.get("amount"));
            String loanPrepaymentDate = dateFormat.format(currentDate.getTime());
            LOAN_TRANSACTION_HELPER.makeRepayment(loanPrepaymentDate, Float.parseFloat(prepayAmount), loanID);
            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
        } finally {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", false);
        }
    }

    @Test
    public void testLoanScheduleWithInterestRecalculationMakeAdvancePaymentTillSettlement() {
        try {
            final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
            final LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponse);
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
            dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", true);
            Calendar startDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            Calendar currentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
            startDate.add(Calendar.MONTH, -8);

            Calendar firstRepaymentDate = (Calendar) startDate.clone();
            firstRepaymentDate.add(Calendar.MONTH, 1);
            firstRepaymentDate.add(Calendar.DAY_OF_MONTH,
                    firstRepaymentDate.getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.DAY_OF_MONTH);
            String firstRepayment = dateFormat.format(firstRepaymentDate.getTime());

            final String loanDisbursementDate = dateFormat.format(startDate.getTime());
            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
            ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
            final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                    LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY,
                    LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                    LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                    LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
                    LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, "12");

            final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, loanDisbursementDate,
                    LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY, firstRepayment);

            Assertions.assertNotNull(loanID);
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(loanDisbursementDate, loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            Assertions.assertNotNull(loanSchedule);
            Calendar repaymentDate = (Calendar) firstRepaymentDate.clone();
            startDate.add(Calendar.DAY_OF_MONTH, 2);
            String loanFirstRepaymentDate = dateFormat.format(startDate.getTime());
            //
            Float earlyPayment = Float.parseFloat("3000");
            String retrieveDueDate = null;
            Float amount = null;
            LOAN_TRANSACTION_HELPER.makeRepayment(loanFirstRepaymentDate, earlyPayment, loanID);
            for (int i = 1; i < loanSchedule.size(); i++) {

                retrieveDueDate = dateFormat.format(repaymentDate.getTime());
                amount = (Float) loanSchedule.get(i).get("principalOriginalDue") + (Float) loanSchedule.get(i).get("interestOriginalDue");
                if (currentDate.after(repaymentDate)) {
                    LOAN_TRANSACTION_HELPER.makeRepayment(retrieveDueDate, amount, loanID);
                } else {
                    break;
                }
                repaymentDate.add(Calendar.MONTH, 1);
            }
            HashMap savingsAccountErrorData = validationErrorHelper.makeRepayment(retrieveDueDate, amount, loanID);
            ArrayList<HashMap> error = (ArrayList<HashMap>) savingsAccountErrorData.get("errors");
            assertEquals("error.msg.loan.transaction.cannot.be.a.future.date", error.get(0).get("userMessageGlobalisationCode"));
        } finally {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", false);
        }
    }

    @Test
    public void testCollateralDataIsAvailableWhenRequested() {
        // given

        Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        List<HashMap> collaterals = new ArrayList<>();
        Integer clientId = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientId);

        Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientId), collateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        Integer loanProductId = createLoanProduct(false, NONE);

        // when
        Integer loanId = applyForLoanApplication(clientId, loanProductId, null, null, "12,000.00", collaterals);

        // then
        List<Integer> clientCollateralIds = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanId,
                "collateral.clientCollateralId");
        Integer clientCollateralIdResult = clientCollateralIds.get(0);
        assertEquals(clientCollateralId, clientCollateralIdResult);
    }

    @Test
    public void undoWaivedChargeTransactionDoesNotExist() {
        LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(REQUEST_SPEC, createResponseSpecification(404));
        HashMap response = loanTransactionHelper.undoWaiveChargesForLoan(-1, -2, "");
        assertEquals("error.msg.loan.transaction.id.invalid",
                ((Map) ((List) response.get("errors")).get(0)).get("userMessageGlobalisationCode"));
        assertEquals("Transaction with identifier -2 does not exist for loan with identifier -1.",
                ((Map) ((List) response.get("errors")).get(0)).get("defaultUserMessage"));
    }

    @Test
    public void chargeAdjustmentChargeWrongParams() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.chargeAdjustment(0L, 0L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanId.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanChargeId.not.greater.than.zero"));
        exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.chargeAdjustment(1L, 0L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.loanChargeId.not.greater.than.zero"));
        exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.chargeAdjustment(1L, 1L, new PostLoansLoanIdChargesChargeIdRequest().amount(0.0)));
        assertEquals(400, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("validation.msg.loan.charge.adjustment.request.amount.not.greater.than.zero"));
    }

    @Test
    public void chargeAdjustmentChargeDoesNotExist() {
        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID, 1L, new PostLoansLoanIdChargesChargeIdRequest().amount(1.0)));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid"));
    }

    @Test
    public void chargeAdjustmentChargeDoesNotExistForLoan() {
        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        Integer penalty = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));
        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
        Integer penalty1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        final Integer loanID2 = applyForLoanApplication(clientID, loanProductID);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> LOAN_TRANSACTION_HELPER
                .chargeAdjustment((long) loanID2, (long) penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(1.0)));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));
    }

    @Test
    public void chargeAdjustmentForUnpaidCharge() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.TRUE);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("01 November 2022").dateFormat(DATETIME_PATTERN).locale("en"));
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            Integer penalty = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(0, loanSchedule.get(1).get("penaltyChargesDue"));
            assertEquals(0, loanSchedule.get(1).get("penaltyChargesOutstanding"));
            assertEquals(1000.0f, loanSchedule.get(1).get("totalDueForPeriod"));
            assertEquals(1000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
            Integer penalty1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            LOAN_TRANSACTION_HELPER.noAccrualTransactionForRepayment(loanID);

            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
            assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
            assertEquals(1010.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
            assertEquals(0, loanSchedule.get(1).get("totalWaivedForPeriod"));

            HashMap loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
            assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
            assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
            assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
            assertEquals(1010.0f, loanSummary.get("totalOutstanding"));
            assertEquals(0.0f, loanSummary.get("totalWaived"));

            String externalId = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID,
                    (long) penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).externalId(externalId).paymentTypeId(1L));

            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesPaid"));
            assertEquals(0.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
            assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
            assertEquals(1000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
            assertEquals(10.0f, loanSchedule.get(1).get("totalPaidForPeriod"));

            loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
            assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
            assertEquals(0.0f, loanSummary.get("penaltyChargesOutstanding"));
            assertEquals(10.0f, loanSummary.get("penaltyChargesPaid"));
            assertEquals(1000.0f, loanSummary.get("totalOutstanding"));

            GetLoansLoanIdTransactionsTransactionIdResponse chargeAdjustmentTransaction = LOAN_TRANSACTION_HELPER
                    .getLoanTransactionDetails((long) loanID, chargeAdjustmentResponse.getSubResourceId());
            assertEquals(10.0, chargeAdjustmentTransaction.getAmount());
            assertEquals(10.0, chargeAdjustmentTransaction.getPenaltyChargesPortion());
            assertEquals("loanTransactionType.chargeAdjustment", chargeAdjustmentTransaction.getType().getCode());
            assertEquals(externalId, chargeAdjustmentTransaction.getExternalId());
            GetLoanTransactionRelation transactionRelation = chargeAdjustmentTransaction.getTransactionRelations().iterator().next();
            assertEquals(chargeAdjustmentResponse.getSubResourceId(), transactionRelation.getFromLoanTransaction());
            assertEquals((long) penalty1LoanChargeId, transactionRelation.getToLoanCharge());
            assertEquals("CHARGE_ADJUSTMENT", transactionRelation.getRelationType());
            assertEquals(1L, chargeAdjustmentTransaction.getPaymentDetailData().getPaymentType().getId());

            PostLoansLoanIdTransactionsResponse repaymentResult = LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));

            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesPaid"));
            assertEquals(0.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
            assertEquals(1000.0f, loanSchedule.get(1).get("principalDue"));
            assertEquals(5.0f, loanSchedule.get(1).get("principalPaid"));
            assertEquals(995.0f, loanSchedule.get(1).get("principalOutstanding"));
            assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
            assertEquals(995.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
            assertEquals(15.0f, loanSchedule.get(1).get("totalPaidForPeriod"));

            loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
            assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
            assertEquals(0.0f, loanSummary.get("penaltyChargesOutstanding"));
            assertEquals(10.0f, loanSummary.get("penaltyChargesPaid"));
            assertEquals(1000.0f, loanSummary.get("principalDisbursed"));
            assertEquals(995.0f, loanSummary.get("principalOutstanding"));
            assertEquals(5.0f, loanSummary.get("principalPaid"));
            assertEquals(995.0f, loanSummary.get("totalOutstanding"));

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            GetLoansLoanIdTransactions replayedTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> externalId.equals(t.getExternalId())).findFirst().get();

            assertEquals(10.0, replayedTransaction.getAmount());
            assertEquals(5.0, replayedTransaction.getPenaltyChargesPortion());
            assertEquals(5.0, replayedTransaction.getPrincipalPortion());
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
            LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, replayedTransaction.getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("08 September 2022")
                            .transactionAmount(0.0).locale("en").reversalExternalId(uuid));

            // Should fail due to external id collusion
            assertThrows(CallFailedRuntimeException.class,
                    () -> LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, repaymentResult.getResourceId(),
                            new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                                    .transactionDate("08 September 2022").transactionAmount(0.0).locale("en").reversalExternalId(uuid)));

            loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            assertEquals(2, loanSchedule.size());
            assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
            assertEquals(5.0f, loanSchedule.get(1).get("penaltyChargesPaid"));
            assertEquals(5.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
            assertEquals(1000.0f, loanSchedule.get(1).get("principalDue"));
            assertEquals(0, loanSchedule.get(1).get("principalPaid"));
            assertEquals(1000.0f, loanSchedule.get(1).get("principalOutstanding"));
            assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
            assertEquals(1005.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
            assertEquals(5.0f, loanSchedule.get(1).get("totalPaidForPeriod"));

            loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
            assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
            assertEquals(5.0f, loanSummary.get("penaltyChargesOutstanding"));
            assertEquals(5.0f, loanSummary.get("penaltyChargesPaid"));
            assertEquals(1000.0f, loanSummary.get("principalDisbursed"));
            assertEquals(1000.0f, loanSummary.get("principalOutstanding"));
            assertEquals(0.0f, loanSummary.get("principalPaid"));
            assertEquals(1005.0f, loanSummary.get("totalOutstanding"));
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.FALSE);
        }
    }

    @Test
    public void chargeAdjustmentAccountingValidation() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.TRUE);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("01 November 2022").dateFormat(DATETIME_PATTERN).locale("en"));
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account assetFeeAndPenaltyAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();
            final PostGLAccountsResponse uniqueIncomeAccountForFee = ACCOUNT_HELPER.createGLAccount(new PostGLAccountsRequest()
                    .type(GLAccountType.INCOME.getValue())
                    .glCode(Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .manualEntriesAllowed(true)
                    .name(Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5)).usage(1));
            final PostGLAccountsResponse uniqueIncomeAccountForPenalty = ACCOUNT_HELPER.createGLAccount(new PostGLAccountsRequest()
                    .type(GLAccountType.INCOME.getValue())
                    .glCode(Utils.uniqueRandomStringGenerator("UNIQUE_PENALTY_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .manualEntriesAllowed(true)
                    .name(Utils.uniqueRandomStringGenerator("UNIQUE_PENALTY_INCOME" + Calendar.getInstance().getTimeInMillis(), 5))
                    .usage(1));

            PostChargesResponse penaltyCharge = CHARGES_HELPER.createCharges(new PostChargesRequest().penalty(true).amount(10.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));

            PostChargesResponse feeCharge = CHARGES_HELPER.createCharges(new PostChargesRequest().penalty(false).amount(9.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));

            final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                    .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                    .withFeeToIncomeAccountMapping(feeCharge.getResourceId(), uniqueIncomeAccountForFee.getResourceId())
                    .withPenaltyToIncomeAccountMapping(penaltyCharge.getResourceId(), uniqueIncomeAccountForPenalty.getResourceId())
                    .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
            final Integer loanProductID = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);

            final PostClientsResponse client = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest());

            final Integer loanID = applyForLoanApplication(client.getClientId().intValue(), loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            List<GetLoansLoanIdRepaymentPeriod> loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());

            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
            Integer penaltyLoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penaltyCharge.getResourceId()), penaltyCharge1AddedDate, "10"));

            final String penalty1LoanChargeDate = DATE_TIME_FORMATTER.format(targetDate);
            PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting(penalty1LoanChargeDate);

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();
            assertEquals(10.0, transactions.get(1).getAmount());
            assertTrue(transactions.get(1).getType().getAccrual());
            assertEquals(10.0, transactions.get(1).getPenaltyChargesPortion());
            Long accrualTransactionId = transactions.get(1).getId();

            List<HashMap> journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + accrualTransactionId);
            assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(1010.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(1010.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());

            GetLoansLoanIdSummary loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(10.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(1010.0, loanSummary.getTotalOutstanding());

            String externalId = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID,
                    (long) penaltyLoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).externalId(externalId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(1010.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());
            assertEquals(10.0, loanSchedulePeriods.get(1).getTotalPaidForPeriod());

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(0.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(10.0, loanSummary.getPenaltyChargesPaid());
            assertEquals(1000.0, loanSummary.getTotalOutstanding());

            transactions = loanDetails.getTransactions();
            assertEquals(10.0, transactions.get(2).getAmount());
            assertTrue(transactions.get(2).getType().getChargeAdjustment());
            assertEquals(10.0, transactions.get(2).getPenaltyChargesPortion());
            Long chargeAdjustmentTransactionId = transactions.get(2).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            String uuid = UUID.randomUUID().toString();
            LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, chargeAdjustmentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("08 September 2022")
                            .transactionAmount(0.0).locale("en").reversalExternalId(uuid));

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));
            assertEquals(10.0f, (float) journalEntries.get(2).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(2).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(2).get("entryType")).get("value"));
            assertEquals(10.0f, (float) journalEntries.get(3).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(3).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(3).get("entryType")).get("value"));

            targetDate = LocalDate.of(2022, 9, 10);
            final String feeCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge.getResourceId()), feeCharge1AddedDate, "3"));

            GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                    GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, true);
            final String feeLoanChargeDate = DATE_TIME_FORMATTER.format(targetDate);
            PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting(feeLoanChargeDate);

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            transactions = loanDetails.getTransactions();
            assertEquals(3.0, transactions.get(2).getAmount());
            assertTrue(transactions.get(2).getType().getAccrual());
            assertEquals(3.0, transactions.get(2).getFeeChargesPortion());
            assertTrue(StringUtils.isNotBlank(transactions.get(2).getExternalId()));
            accrualTransactionId = transactions.get(2).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + accrualTransactionId);
            assertEquals(3.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(3.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesDue());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesOutstanding());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(10.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(3.0, loanSummary.getFeeChargesCharged());
            assertEquals(3.0, loanSummary.getFeeChargesOutstanding());
            assertEquals(1013.0, loanSummary.getTotalOutstanding());

            LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("11 September 2022").locale("en").transactionAmount(5.0));

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(2.0).externalId(externalId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(7.0, loanSchedulePeriods.get(1).getPenaltyChargesPaid());
            assertEquals(3.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesDue());
            assertEquals(0.0, loanSchedulePeriods.get(1).getFeeChargesPaid());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getPrincipalDue());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPrincipalPaid());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getPrincipalOutstanding());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(1006.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());
            assertEquals(7.0, loanSchedulePeriods.get(1).getTotalPaidForPeriod());

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(3.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(7.0, loanSummary.getPenaltyChargesPaid());
            assertEquals(3.0, loanSummary.getFeeChargesCharged());
            assertEquals(3.0, loanSummary.getFeeChargesOutstanding());
            assertEquals(0.0, loanSummary.getFeeChargesPaid());
            assertEquals(1000.0, loanSummary.getPrincipalOutstanding());
            assertEquals(0.0, loanSummary.getPrincipalPaid());
            assertEquals(7.0, loanSummary.getTotalRepayment());
            assertEquals(1006.0, loanSummary.getTotalOutstanding());

            transactions = loanDetails.getTransactions();
            assertEquals(2.0, transactions.get(5).getAmount());
            assertTrue(transactions.get(4).getType().getChargeAdjustment());
            assertEquals(2.0, transactions.get(5).getPenaltyChargesPortion());
            assertEquals(0.0, transactions.get(5).getFeeChargesPortion());
            assertEquals(0.0, transactions.get(5).getPrincipalPortion());
            chargeAdjustmentTransactionId = transactions.get(5).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(2.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(2.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID, (long) penaltyLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(7.0).externalId(externalId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesDue());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getPrincipalDue());
            assertEquals(1.0, loanSchedulePeriods.get(1).getPrincipalPaid());
            assertEquals(999.0, loanSchedulePeriods.get(1).getPrincipalOutstanding());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(999.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());
            assertEquals(14.0, loanSchedulePeriods.get(1).getTotalPaidForPeriod());

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(0.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(10.0, loanSummary.getPenaltyChargesPaid());
            assertEquals(3.0, loanSummary.getFeeChargesCharged());
            assertEquals(0.0, loanSummary.getFeeChargesOutstanding());
            assertEquals(3.0, loanSummary.getFeeChargesPaid());
            assertEquals(999.0, loanSummary.getPrincipalOutstanding());
            assertEquals(1.0, loanSummary.getPrincipalPaid());
            assertEquals(14.0, loanSummary.getTotalRepayment());
            assertEquals(999.0, loanSummary.getTotalOutstanding());

            transactions = loanDetails.getTransactions();
            assertEquals(7.0, transactions.get(6).getAmount());
            assertTrue(transactions.get(6).getType().getChargeAdjustment());
            assertEquals(3.0, transactions.get(6).getPenaltyChargesPortion());
            assertEquals(3.0, transactions.get(6).getFeeChargesPortion());
            assertEquals(1.0, transactions.get(6).getPrincipalPortion());
            chargeAdjustmentTransactionId = transactions.get(6).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(7.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            if (assetAccount.getAccountID() == (int) journalEntries.get(1).get("glAccountId")) {
                assertEquals(1.0f, (float) journalEntries.get(1).get("amount"));
                assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
                assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

                assertEquals(6.0f, (float) journalEntries.get(2).get("amount"));
                assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(2).get("glAccountId"));
                assertEquals("CREDIT", ((HashMap) journalEntries.get(2).get("entryType")).get("value"));
            } else {
                assertEquals(1.0f, (float) journalEntries.get(2).get("amount"));
                assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(2).get("glAccountId"));
                assertEquals("CREDIT", ((HashMap) journalEntries.get(2).get("entryType")).get("value"));

                assertEquals(6.0f, (float) journalEntries.get(1).get("amount"));
                assertEquals(assetFeeAndPenaltyAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
                assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));
            }

            LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("13 September 2022").locale("en").transactionAmount(998.0));

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).externalId(externalId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            loanSchedulePeriods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(2, loanSchedulePeriods.size());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesDue());
            assertEquals(10.0, loanSchedulePeriods.get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPenaltyChargesOutstanding());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesDue());
            assertEquals(3.0, loanSchedulePeriods.get(1).getFeeChargesPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getPrincipalDue());
            assertEquals(1000.0, loanSchedulePeriods.get(1).getPrincipalPaid());
            assertEquals(0.0, loanSchedulePeriods.get(1).getPrincipalOutstanding());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalDueForPeriod());
            assertEquals(0.0, loanSchedulePeriods.get(1).getTotalOutstandingForPeriod());
            assertEquals(1013.0, loanSchedulePeriods.get(1).getTotalPaidForPeriod());

            loanSummary = loanDetails.getSummary();
            assertEquals(10.0, loanSummary.getPenaltyChargesCharged());
            assertEquals(0.0, loanSummary.getPenaltyChargesOutstanding());
            assertEquals(10.0, loanSummary.getPenaltyChargesPaid());
            assertEquals(3.0, loanSummary.getFeeChargesCharged());
            assertEquals(0.0, loanSummary.getFeeChargesOutstanding());
            assertEquals(3.0, loanSummary.getFeeChargesPaid());
            assertEquals(0.0, loanSummary.getPrincipalOutstanding());
            assertEquals(1000.0, loanSummary.getPrincipalPaid());
            assertEquals(1013.0, loanSummary.getTotalRepayment());
            assertEquals(0.0, loanSummary.getTotalOutstanding());

            transactions = loanDetails.getTransactions();
            assertEquals(1.0, transactions.get(8).getAmount());
            assertTrue(transactions.get(8).getType().getChargeAdjustment());
            assertEquals(0.0, transactions.get(8).getPenaltyChargesPortion());
            assertEquals(0.0, transactions.get(8).getFeeChargesPortion());
            assertEquals(1.0, transactions.get(8).getPrincipalPortion());
            chargeAdjustmentTransactionId = transactions.get(8).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(1.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForFee.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(1.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            externalId = UUID.randomUUID().toString();
            chargeAdjustmentResponse = LOAN_TRANSACTION_HELPER.chargeAdjustment((long) loanID, (long) penaltyLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).externalId(externalId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            transactions = loanDetails.getTransactions();
            assertEquals(1.0, transactions.get(9).getAmount());
            assertTrue(transactions.get(9).getType().getChargeAdjustment());
            assertEquals(0.0, transactions.get(9).getPenaltyChargesPortion());
            assertEquals(0.0, transactions.get(9).getFeeChargesPortion());
            assertEquals(0.0, transactions.get(9).getPrincipalPortion());
            assertEquals(1.0, transactions.get(9).getOverpaymentPortion());
            chargeAdjustmentTransactionId = transactions.get(9).getId();

            journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + chargeAdjustmentTransactionId);
            assertEquals(1.0f, (float) journalEntries.get(0).get("amount"));
            assertEquals(uniqueIncomeAccountForPenalty.getResourceId().intValue(), (int) journalEntries.get(0).get("glAccountId"));
            assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
            assertEquals(1.0f, (float) journalEntries.get(1).get("amount"));
            assertEquals(overpaymentAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
            assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

            assertTrue(loanDetails.getStatus().getOverpaid());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.FALSE);
            GlobalConfigurationHelper.manageConfigurations(REQUEST_SPEC, RESPONSE_SPEC,
                    GlobalConfigurationHelper.ENABLE_AUTOGENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void undoWaivedChargeWaiveTransactionDoesNotExist() {
        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        final Integer loanTransactionId = (Integer) ((Map) ((List) JsonPath.from(loanDetails).get("transactions")).get(0)).get("id");
        LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(REQUEST_SPEC, createResponseSpecification(403));
        HashMap response = loanTransactionHelper.undoWaiveChargesForLoan(loanID, loanTransactionId, "");
        assertEquals("error.msg.loan.transaction.undo.waive.charge",
                ((Map) ((List) response.get("errors")).get(0)).get("userMessageGlobalisationCode"));
        assertEquals("Transaction is not a waive charge type.", ((Map) ((List) response.get("errors")).get(0)).get("defaultUserMessage"));
    }

    @Test
    public void undoWaivedCharge() {
        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        Integer penalty = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1000.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
        Integer penalty1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        LOAN_TRANSACTION_HELPER.noAccrualTransactionForRepayment(loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(0, loanSchedule.get(1).get("totalWaivedForPeriod"));

        HashMap loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(0.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(0.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1010.0f, loanSummary.get("totalOutstanding"));
        assertEquals(0.0f, loanSummary.get("totalWaived"));

        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, penalty1LoanChargeId, "");

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesWaived"));
        assertEquals(0.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(10.0f, loanSchedule.get(1).get("totalWaivedForPeriod"));

        loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(0.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(0.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1000.0f, loanSummary.get("totalOutstanding"));
        assertEquals(10.0f, loanSummary.get("totalWaived"));

        List<HashMap> transactions = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "transactions");
        assertEquals(10.0f, (float) transactions.get(1).get("amount"));
        assertEquals(9, (int) ((HashMap) transactions.get(1).get("type")).get("id"));
        Integer waiveTransactionId = (int) transactions.get(1).get("id");
        LOAN_TRANSACTION_HELPER.undoWaiveChargesForLoan(loanID, waiveTransactionId, "");

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0.0f, loanSchedule.get(1).get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(0.0f, loanSchedule.get(1).get("totalWaivedForPeriod"));

        loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(0.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(0.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1010.0f, loanSummary.get("totalOutstanding"));
        assertEquals(0.0f, loanSummary.get("totalWaived"));

        transactions = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "transactions");
        assertEquals(10.0f, (float) transactions.get(1).get("amount"));
        assertEquals(9, (int) ((HashMap) transactions.get(1).get("type")).get("id"));
        assertEquals(true, transactions.get(1).get("manuallyReversed"));

        Integer fee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        final String feeCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);
        Integer fee1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), feeCharge1AddedDate, "10"));

        PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting(feeCharge1AddedDate);

        transactions = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "transactions");
        assertEquals(10, (int) ((HashMap) transactions.get(2).get("type")).get("id"));
        assertEquals(20.0f, (float) transactions.get(2).get("amount"));
        Integer accrualTransactionId = (int) transactions.get(2).get("id");

        List<HashMap> journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + accrualTransactionId);
        assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
        assertEquals(incomeAccount.getAccountID(), (int) journalEntries.get(0).get("glAccountId"));
        assertEquals("CREDIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
        assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
        assertEquals("DEBIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(2).get("amount"));
        assertEquals(incomeAccount.getAccountID(), (int) journalEntries.get(2).get("glAccountId"));
        assertEquals("CREDIT", ((HashMap) journalEntries.get(2).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(3).get("amount"));
        assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(3).get("glAccountId"));
        assertEquals("DEBIT", ((HashMap) journalEntries.get(3).get("entryType")).get("value"));

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1020.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1020.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(0, loanSchedule.get(1).get("totalWaivedForPeriod"));

        loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(10.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1020.0f, loanSummary.get("totalOutstanding"));
        assertEquals(0.0f, loanSummary.get("totalWaived"));

        LOAN_TRANSACTION_HELPER.waiveChargesForLoan(loanID, fee1LoanChargeId, "");

        transactions = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "transactions");
        assertEquals(10.0f, (float) transactions.get(3).get("amount"));
        assertEquals(9, (int) ((HashMap) transactions.get(3).get("type")).get("id"));
        Integer waive2TransactionId = (int) transactions.get(3).get("id");

        journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + waive2TransactionId);
        assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
        assertEquals(expenseAccount.getAccountID(), (int) journalEntries.get(0).get("glAccountId"));
        assertEquals("DEBIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
        assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
        assertEquals("CREDIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0.0f, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1020.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1010.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(10.0f, loanSchedule.get(1).get("totalWaivedForPeriod"));

        loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(0.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(10.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1010.0f, loanSummary.get("totalOutstanding"));
        assertEquals(10.0f, loanSummary.get("totalWaived"));

        LOAN_TRANSACTION_HELPER.undoWaiveChargesForLoan(loanID, waive2TransactionId, "");

        transactions = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "transactions");
        assertEquals(10.0f, (float) transactions.get(3).get("amount"));
        assertEquals(9, (int) ((HashMap) transactions.get(3).get("type")).get("id"));
        assertEquals(true, transactions.get(3).get("manuallyReversed"));

        journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("L" + waive2TransactionId);
        assertEquals(10.0f, (float) journalEntries.get(0).get("amount"));
        assertEquals(expenseAccount.getAccountID(), (int) journalEntries.get(0).get("glAccountId"));
        assertEquals("CREDIT", ((HashMap) journalEntries.get(0).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(1).get("amount"));
        assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(1).get("glAccountId"));
        assertEquals("DEBIT", ((HashMap) journalEntries.get(1).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(2).get("amount"));
        assertEquals(expenseAccount.getAccountID(), (int) journalEntries.get(2).get("glAccountId"));
        assertEquals("DEBIT", ((HashMap) journalEntries.get(2).get("entryType")).get("value"));
        assertEquals(10.0f, (float) journalEntries.get(3).get("amount"));
        assertEquals(assetAccount.getAccountID(), (int) journalEntries.get(3).get("glAccountId"));
        assertEquals("CREDIT", ((HashMap) journalEntries.get(3).get("entryType")).get("value"));

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(10.0f, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSchedule.get(1).get("feeChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(1020.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(1020.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        assertEquals(0.0f, loanSchedule.get(1).get("totalWaivedForPeriod"));

        loanSummary = LOAN_TRANSACTION_HELPER.getLoanDetail(REQUEST_SPEC, RESPONSE_SPEC, loanID, "summary");
        assertEquals(10.0f, loanSummary.get("penaltyChargesCharged"));
        assertEquals(10.0f, loanSummary.get("penaltyChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("penaltyChargesWaived"));
        assertEquals(10.0f, loanSummary.get("feeChargesCharged"));
        assertEquals(10.0f, loanSummary.get("feeChargesOutstanding"));
        assertEquals(0.0f, loanSummary.get("feeChargesWaived"));
        assertEquals(1020.0f, loanSummary.get("totalOutstanding"));
        assertEquals(0.0f, loanSummary.get("totalWaived"));
    }

    @Test
    public void chargeOff() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("30 September 2022").dateFormat(DATETIME_PATTERN).locale("en"));
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();
            String randomText = UUID.randomUUID().toString();
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(REQUEST_SPEC, RESPONSE_SPEC, randomText, 1);
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterestMultiDisbursement(assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            ResponseSpecification errorResponseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
            LoanTransactionHelper errorLoanTransactionHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponseSpec);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.chargeOffLoan((long) loanID,
                        new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en")
                                .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString())
                                .chargeOffReasonId((long) chargeOffReasonId));
            });

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.active"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                LOAN_TRANSACTION_HELPER.undoChargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.active"));

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithTransactionAmount("02 September 2022", loanID, "1000");
            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithTransactionAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                LOAN_TRANSACTION_HELPER.chargeOffLoan((long) loanID,
                        new PostLoansLoanIdTransactionsRequest().transactionDate("1 October 2022").locale("en").dateFormat(DATETIME_PATTERN)
                                .chargeOffReasonId((long) chargeOffReasonId));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.cannot.be.a.future.date"));

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(2000.0, loanDetails.getSummary().getTotalOutstanding());
            assertFalse(loanDetails.getChargedOff());
            assertNull(loanDetails.getSummary().getChargeOffReasonId());
            assertNull(loanDetails.getSummary().getChargeOffReason());
            assertNull(loanDetails.getTimeline().getChargedOffOnDate());
            assertNull(loanDetails.getTimeline().getChargedOffByUsername());
            assertNull(loanDetails.getTimeline().getChargedOffByFirstname());
            assertNull(loanDetails.getTimeline().getChargedOffByLastname());

            Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "3", true));
            LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "04 September 2022", "3"));
            Integer chargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "04 September 2022", "5"));

            PostLoansLoanIdChargesChargeIdResponse waiveChargeResponse = LOAN_TRANSACTION_HELPER.waiveLoanCharge((long) loanID,
                    (long) chargeId, new PostLoansLoanIdChargesChargeIdRequest());

            String transactionExternalId = UUID.randomUUID().toString();
            LOAN_TRANSACTION_HELPER.chargeOffLoan((long) loanID,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en").dateFormat(DATETIME_PATTERN)
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(2003.0, loanDetails.getSummary().getTotalOutstanding());
            assertTrue(loanDetails.getChargedOff());
            assertEquals((long) chargeOffReasonId, loanDetails.getSummary().getChargeOffReasonId());
            assertEquals(randomText, loanDetails.getSummary().getChargeOffReason());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTimeline().getChargedOffOnDate());
            assertEquals("mifos", loanDetails.getTimeline().getChargedOffByUsername());
            assertEquals("App", loanDetails.getTimeline().getChargedOffByFirstname());
            assertEquals("Administrator", loanDetails.getTimeline().getChargedOffByLastname());

            GetLoansLoanIdTransactions chargeOffTransaction = loanDetails.getTransactions().get(loanDetails.getTransactions().size() - 1);

            assertEquals(2003.0, chargeOffTransaction.getAmount());
            assertEquals(2000.0, chargeOffTransaction.getPrincipalPortion());
            assertEquals(3.0, chargeOffTransaction.getPenaltyChargesPortion());

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.chargeOffLoan((long) loanID,
                        new PostLoansLoanIdTransactionsRequest().transactionDate("4 September 2022").locale("en")
                                .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString())
                                .chargeOffReasonId((long) chargeOffReasonId));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.already.charged.off"));

            HashMap chargeAddingError = errorLoanTransactionHelper.addChargesForLoanGetFullResponse(loanID, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatPenaltySpecifiedDueDate), "04 September 2022", "3"));

            assertEquals("error.msg.loan.is.charged.off",
                    ((Map) ((List) chargeAddingError.get("errors")).get(0)).get("userMessageGlobalisationCode"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.undoWaiveLoanCharge((long) loanID, waiveChargeResponse.getSubResourceId(),
                        new PutChargeTransactionChangesRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            LOAN_TRANSACTION_HELPER.undoChargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest());

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertFalse(loanDetails.getChargedOff());
            assertNull(loanDetails.getSummary().getChargeOffReasonId());
            assertNull(loanDetails.getSummary().getChargeOffReason());
            assertNull(loanDetails.getTimeline().getChargedOffOnDate());

            GetLoansLoanIdTransactions undoChargeOffTransaction = loanDetails.getTransactions()
                    .get(loanDetails.getTransactions().size() - 1);
            assertTrue(undoChargeOffTransaction.getType().getChargeoff());
            assertTrue(undoChargeOffTransaction.getManuallyReversed());

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.undoChargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.not.charged.off"));

            PostLoansLoanIdTransactionsResponse loanRepaymentResponse = LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en")
                            .transactionAmount(5.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.chargeOffLoan((long) loanID,
                        new PostLoansLoanIdTransactionsRequest().transactionDate("04 September 2022").locale("en")
                                .dateFormat(DATETIME_PATTERN).externalId(UUID.randomUUID().toString())
                                .chargeOffReasonId((long) chargeOffReasonId));
            });

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.charge.off.is.before.than.the.last.user.transaction"));

            LOAN_TRANSACTION_HELPER.chargeOffLoan((long) loanID,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("06 September 2022").locale("en").dateFormat(DATETIME_PATTERN)
                            .externalId(UUID.randomUUID().toString()).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            chargeOffTransaction = loanDetails.getTransactions().get(loanDetails.getTransactions().size() - 1);

            assertEquals(1998.0, chargeOffTransaction.getAmount());
            assertEquals(1998.0, chargeOffTransaction.getPrincipalPortion());

            LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("07 September 2022").locale("en").transactionAmount(5.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.undoChargeOffLoan((long) loanID, new PostLoansLoanIdTransactionsRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.charge.off.is.not.the.last.user.transaction"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.makeWriteoff((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.closeLoan((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.forecloseLoan((long) loanID, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.closeRescheduledLoan((long) loanID, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en"));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            HashMap disbursementDetailREsponse = (HashMap) errorLoanTransactionHelper.addAndDeleteDisbursementDetail(loanID, "1000",
                    "03 September 2022", List.of(LOAN_TRANSACTION_HELPER.createTrancheDetail(null, "05 September 2022", "200")), "");

            assertEquals("error.msg.loan.is.charged.off",
                    ((Map) ((List) disbursementDetailREsponse.get("errors")).get(0)).get("userMessageGlobalisationCode"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.undoLastDisbursalLoan((long) loanID, new PostLoansLoanIdRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.undoDisbursalLoan((long) loanID, new PostLoansLoanIdRequest());
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.is.charged.off"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en").transactionAmount(5.0));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.disburseLoan((long) loanID,
                        new PostLoansLoanIdRequest().actualDisbursementDate("4 September 2022").transactionAmount(new BigDecimal("10"))
                                .locale("en").dateFormat(DATETIME_PATTERN));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            LOAN_TRANSACTION_HELPER.makeLoanRepayment((long) loanID, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("07 September 2022").locale("en").transactionAmount(5000.0));

            exception = assertThrows(CallFailedRuntimeException.class, () -> {
                errorLoanTransactionHelper.makeRefundByCash((long) loanID, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022").locale("en").transactionAmount(5.0));
            });
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date"));

            LOAN_TRANSACTION_HELPER.makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("08 September 2022").locale("en").transactionAmount(3007.0));
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.FALSE);
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void testCloseOpenMaturityDate() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            LocalDate expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            LocalDate actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertTrue(DateUtils.isEqual(expectedMaturityDate, actualMaturityDate));

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("05 September 2022", Float.parseFloat("700"), loanID);

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertNotNull(expectedMaturityDate);
            assertNull(actualMaturityDate);

            LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("04 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            expectedMaturityDate = loanDetails.getTimeline().getExpectedMaturityDate();
            actualMaturityDate = loanDetails.getTimeline().getActualMaturityDate();

            assertNotNull(expectedMaturityDate);
            assertNotNull(actualMaturityDate);

            assertTrue(expectedMaturityDate.isEqual(actualMaturityDate));
        } finally {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void testReverseReplay() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("05 September 2022", Float.parseFloat("10"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("06 September 2022", Float.parseFloat("400"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("07 September 2022", Float.parseFloat("390"), loanID);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(300.0, loanDetails.getTotalOverpaid());

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());

            assertEquals(10.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(10.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());

            assertEquals(400.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(400.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(90.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(300.0, loanDetails.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(2).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(290.0, loanDetails.getTotalOverpaid());

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());

            assertEquals(10.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(10.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(400.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(400.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(100.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(290.0, loanDetails.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            LOAN_TRANSACTION_HELPER.reverseLoanTransaction((long) loanID, loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2022")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(210.0, loanDetails.getSummary().getTotalOutstanding());

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(10.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(10.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(400.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(400.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());

            assertEquals(390.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(390.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(290.0, loanDetails.getTotalOverpaid());

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(1).getManuallyReversed());

            assertEquals(500.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(2).getDate());

            assertEquals(10.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(10.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(3).getDate());
            assertTrue(loanDetails.getTransactions().get(3).getManuallyReversed());

            assertEquals(400.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(400.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(4).getDate());

            assertEquals(390.0, loanDetails.getTransactions().get(5).getAmount());
            assertEquals(100.0, loanDetails.getTransactions().get(5).getPrincipalPortion());
            assertEquals(290.0, loanDetails.getTransactions().get(5).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(5).getDate());
        } finally {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void testCreditBalanceRefundAfterMaturityWithReverseReplayOfRepayments() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("10 October 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("100"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("05 September 2022", Float.parseFloat("1100"), loanID);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertEquals(200.0, loanDetails.getTotalOverpaid());
            assertTrue(loanDetails.getStatus().getOverpaid());

            LOAN_TRANSACTION_HELPER.makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(200.0)
                    .transactionDate("10 October 2022").dateFormat(DATETIME_PATTERN).locale("en").paymentTypeId(1L));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());

            assertEquals(100.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(100.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(900.0, loanDetails.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(1100.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(900.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(200.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(200.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(200.0, loanDetails.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 10, 10), loanDetails.getTransactions().get(3).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(1L, loanDetails.getTransactions().get(3).getPaymentDetailData().getPaymentType().getId());
            GetJournalEntriesTransactionIdResponse journalEntriesForTransaction = JOURNAL_ENTRY_HELPER
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

            LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanDetails.getId(), loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionAmount(0.0)
                            .transactionDate("10 October 2022").locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(100.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(100.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertTrue(loanDetails.getTransactions().get(1).getManuallyReversed());

            assertEquals(1100.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(1000.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(100.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(1, loanDetails.getTransactions().get(2).getTransactionRelations().size());

            assertEquals(200.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(100.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(100.0, loanDetails.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(100.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(LocalDate.of(2022, 10, 10), loanDetails.getTransactions().get(3).getDate());
            assertEquals(1, loanDetails.getTransactions().get(3).getTransactionRelations().size());

            assertTrue(loanDetails.getStatus().getActive());

            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertTrue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());
            assertEquals(200, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertFalse(loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());
            assertEquals(100.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(100.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());

            journalEntriesForTransaction = JOURNAL_ENTRY_HELPER.getJournalEntries("L" + loanDetails.getTransactions().get(3).getId());
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
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void testCreditBalanceRefundBeforeMaturityWithReverseReplayOfRepaymentsAndRefund() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("10 October 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("500"), loanID);
            LOAN_TRANSACTION_HELPER.makeRepayment("05 September 2022", Float.parseFloat("700"), loanID);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertEquals(200.0, loanDetails.getTotalOverpaid());
            assertTrue(loanDetails.getStatus().getOverpaid());

            LOAN_TRANSACTION_HELPER.makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(200.0)
                    .transactionDate("06 September 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            LOAN_TRANSACTION_HELPER.makeMerchantIssuedRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().locale("en")
                    .dateFormat(DATETIME_PATTERN).transactionDate("07 September 2022").transactionAmount(500.0));

            LOAN_TRANSACTION_HELPER.makeCreditBalanceRefund((long) loanID, new PostLoansLoanIdTransactionsRequest().transactionAmount(500.0)
                    .transactionDate("08 September 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1000, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getOutstandingLoanBalance());

            assertEquals(700.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(200.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());

            assertEquals(200.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(200.0, loanDetails.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());

            assertEquals(500.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOutstandingLoanBalance());

            assertEquals(500.0, loanDetails.getTransactions().get(5).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(5).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getTransactions().get(5).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(5).getOutstandingLoanBalance());

            LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanDetails.getId(), loanDetails.getTransactions().get(2).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionAmount(0.0)
                            .transactionDate("07 September 2022").locale("en"));

            loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            assertEquals(500.0, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 4), loanDetails.getTransactions().get(1).getDate());
            assertEquals(500.0, loanDetails.getTransactions().get(1).getOutstandingLoanBalance());

            assertEquals(700.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(200.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(LocalDate.of(2022, 9, 5), loanDetails.getTransactions().get(2).getDate());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());
            assertTrue(loanDetails.getTransactions().get(2).getManuallyReversed());

            assertEquals(200.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(200.0, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 6), loanDetails.getTransactions().get(3).getDate());
            assertEquals(700.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(1, loanDetails.getTransactions().get(3).getTransactionRelations().size());

            assertEquals(500.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getTransactions().get(4).getDate());
            assertEquals(200.0, loanDetails.getTransactions().get(4).getOutstandingLoanBalance());
            assertEquals(1, loanDetails.getTransactions().get(4).getTransactionRelations().size());

            assertEquals(500.0, loanDetails.getTransactions().get(5).getAmount());
            assertEquals(500.0, loanDetails.getTransactions().get(5).getPrincipalPortion());
            assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getTransactions().get(5).getDate());
            assertEquals(700.0, loanDetails.getTransactions().get(5).getOutstandingLoanBalance());
            assertEquals(1, loanDetails.getTransactions().get(5).getTransactionRelations().size());

            assertTrue(loanDetails.getStatus().getActive());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(1700, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertFalse(loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(700.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());

        } finally {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void accrualIsCalculatedWhenTheLoanIsClosed() {
        try {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, true);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("10 October 2022").dateFormat(DATETIME_PATTERN).locale("en"));

            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC, "01 January 2011");
            List<HashMap> charges = new ArrayList<>();
            Integer installmentFee = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));
            addCharges(charges, installmentFee, "10", null);

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges);

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount("03 September 2022", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            LOAN_TRANSACTION_HELPER.makeRepayment("04 September 2022", Float.parseFloat("5"), loanID);

            PERIODIC_ACCRUAL_ACCOUNTING_HELPER.runPeriodicAccrualAccounting("04 September 2022");

            Integer penalty = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "11", true));
            LocalDate targetDate = LocalDate.of(2022, 9, 6);
            final String penaltyCharge1AddedDate = DATE_TIME_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "11"));

            LOAN_TRANSACTION_HELPER.waiveLoanCharge((long) loanID, (long) penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest());

            LOAN_TRANSACTION_HELPER.makeRepayment("08 September 2022", Float.parseFloat("1010"), loanID);

            GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanID);

            GetLoansLoanIdTransactions lastAccrualTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getType().getAccrual())).findFirst().get();
            assertEquals(15, lastAccrualTransaction.getAmount());
            assertEquals(5, lastAccrualTransaction.getPenaltyChargesPortion());
            assertEquals(10, lastAccrualTransaction.getFeeChargesPortion());

            GetLoansLoanIdTransactionsTransactionIdResponse accrualTransactionDetails = LOAN_TRANSACTION_HELPER
                    .getLoanTransactionDetails((long) loanID, lastAccrualTransaction.getId());

            assertEquals(2, accrualTransactionDetails.getLoanChargePaidByList().size());
            accrualTransactionDetails.getLoanChargePaidByList().forEach(loanCharge -> {
                if (loanCharge.getChargeId().equals((long) penalty1LoanChargeId)) {
                    assertEquals(5, loanCharge.getAmount());
                } else {
                    assertEquals(10, loanCharge.getAmount());
                }
            });

        } finally {
            GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, false);
        }
    }

    @Test
    public void testLoanTransactionOrderAfterReverseReplay() {
        try {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.TRUE);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("01 January 2023").dateFormat(DATETIME_PATTERN).locale("en"));
            LOG.info("-----------------------------------NEW CLIENT-----------------------------------------");
            final PostClientsRequest newClient = createRandomClientWithDate("01 January 2023");
            final PostClientsResponse clientResponse = CLIENT_HELPER.createClient(newClient);
            LOG.info("-----------------------------------NEW LOAN PRODUCT-----------------------------------------");
            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
            final PostLoanProductsResponse loanProductResponse = LOAN_PRODUCT_HELPER.createLoanProduct(loanProductsRequest);
            LOG.info("-----------------------------------CREATE CHARGES-----------------------------------------");
            PostChargesResponse penaltyCharge = CHARGES_HELPER.createCharges(new PostChargesRequest().penalty(true).amount(10.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));
            LOG.info("-----------------------------------SUBMIT LOAN-----------------------------------------");
            final PostLoansResponse loanApplicationResult = applyForLoanApplicationForOnePeriod30DaysLongNoInterestPeriodicAccrual(
                    clientResponse.getResourceId(), loanProductResponse.getResourceId(), "01 January 2023",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY);
            LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
            PostLoansLoanIdResponse approvedLoanResult = LOAN_TRANSACTION_HELPER.approveLoan(loanApplicationResult.getResourceId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000.0)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));
            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDisbursementUUID = UUID.randomUUID().toString();
            PostLoansLoanIdResponse disbursedLoanResult = LOAN_TRANSACTION_HELPER.disburseLoan(loanApplicationResult.getResourceId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en").externalId(loanDisbursementUUID));
            Long loanId = disbursedLoanResult.getResourceId();
            LOG.info("-------------------------------ADD CHARGES-------------------------------------------");
            PostLoansLoanIdChargesResponse penaltyLoanChargeResult = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanId,
                    new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                            .amount(10.0).dueDate("10 January 2023"));
            LOG.info("-------------------------------DO SOME PARTIAL REPAYMENTS-------------------------------------------");
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("07 January 2023").dateFormat(DATETIME_PATTERN).locale("en"));
            String firstRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse firstRepaymentResult = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(9.0).externalId(firstRepaymentUUID));
            String secondRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse secondRepaymentResult = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(8.0).externalId(secondRepaymentUUID));
            String thirdRepaymentUUID = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse thirdRepaymentResult = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").locale("en")
                            .transactionAmount(7.0).externalId(thirdRepaymentUUID));
            LOG.info("-------------------------------CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
            LOG.info(
                    "-------------------------------REVERT FIRST REPAYMENT AND CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanId, firstRepaymentUUID, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023").transactionAmount(0.0).locale("en"));
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
            LOG.info(
                    "-------------------------------REVERT SECOND REPAYMENT AND CHECK LOAN TRANSACTION ORDER-------------------------------------------");
            LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanId, secondRepaymentUUID,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("07 January 2023")
                            .transactionAmount(0.0).locale("en"));
            checkLoanTransactionOrder(loanId, loanDisbursementUUID, firstRepaymentUUID, secondRepaymentUUID, thirdRepaymentUUID);
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(REQUEST_SPEC, RESPONSE_SPEC, Boolean.FALSE);
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(REQUEST_SPEC, RESPONSE_SPEC, "42", false);
        }
    }

    private void checkLoanTransactionOrder(Long loanId, String... transactionUUIDs) {
        LOG.info("-------------------------------CHECK LOAN TRANSACTION ORDER-------------------------------------------");
        GetLoansLoanIdResponse loanDetailsResult = LOAN_TRANSACTION_HELPER.getLoanDetails(loanId);
        for (int i = 0; i < transactionUUIDs.length; i++) {
            assertEquals(transactionUUIDs[i], loanDetailsResult.getTransactions().get(i).getExternalId());
        }
    }

    private PostClientsRequest createRandomClientWithDate(String date) {
        return new PostClientsRequest().officeId(1L).legalFormId(1L).firstname(Utils.randomStringGenerator("", 5))
                .lastname(Utils.randomStringGenerator("", 5)).active(true).locale("en").activationDate(date).dateFormat(DATETIME_PATTERN);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final List<HashMap> charges) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withCharges(charges).withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022")
                .withLoanType("individual").build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingNoInterest(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .build(null);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingNoInterestMultiDisbursement(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true).build(null);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private void validateIfValuesAreNotOverridden(Integer loanID, Integer loanProductID) {
        String loanProductDetails = LOAN_TRANSACTION_HELPER.getLoanProductDetails(REQUEST_SPEC, RESPONSE_SPEC, loanProductID);
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<String> comparisonAttributes = Arrays.asList("amortizationType", "interestType", "transactionProcessingStrategyCode",
                "interestCalculationPeriodType", "repaymentFrequencyType", "graceOnPrincipalPayment", "graceOnInterestPayment",
                "inArrearsTolerance", "graceOnArrearsAgeing");

        for (String comparisonAttribute : comparisonAttributes) {
            Object val1 = JsonPath.from(loanProductDetails).get(comparisonAttribute);
            Object val2 = JsonPath.from(loanDetails).get(comparisonAttribute);
            assertEquals(val1, val2);
        }
    }

    private JsonObject createLoanProductConfigurationDetail(JsonObject loanProductConfiguration, Boolean bool) {
        loanProductConfiguration.addProperty("amortizationType", bool);
        loanProductConfiguration.addProperty("interestType", bool);
        loanProductConfiguration.addProperty("transactionProcessingStrategyCode", bool);
        loanProductConfiguration.addProperty("interestCalculationPeriodType", bool);
        loanProductConfiguration.addProperty("inArrearsTolerance", bool);
        loanProductConfiguration.addProperty("repaymentEvery", bool);
        loanProductConfiguration.addProperty("graceOnPrincipalAndInterestPayment", bool);
        loanProductConfiguration.addProperty("graceOnArrearsAgeing", bool);
        return loanProductConfiguration;
    }

    private Integer applyForLoanApplicationWithProductConfigurationAsTrue(final Integer clientID, final Integer loanProductID,
            String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        List<HashMap> collaterals = new ArrayList<>();

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withRepaymentEveryAfter("1") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("01 March 2014") //
                .withSubmittedOnDate("01 March 2014") //
                .withCollaterals(collaterals).build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithProductConfigurationAsFalse(final Integer clientID, final Integer loanProductID,
            String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        List<HashMap> collaterals = new ArrayList<>();

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplicationJSON = new LoanApplicationTestBuilder()
                //
                .withPrincipal(principal)
                //
                .withRepaymentEveryAfter("2")
                //
                .withAmortizationTypeAsEqualPrincipalPayments().withRepaymentFrequencyTypeAsWeeks()
                .withRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY).withInterestTypeAsFlatBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withPrincipalGrace("1").withInterestGrace("1")
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("01 March 2014") //
                .withSubmittedOnDate("01 March 2014") //
                .withCollaterals(collaterals).build(clientID.toString(), loanProductID.toString(), null);

        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
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

    private void validateCharge(Integer amountPercentage, final List<HashMap> loanCharges, final String amount, final String outstanding,
            String amountPaid, String amountWaived) {
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        assertTrue(Float.valueOf(amount).compareTo(Float.valueOf(String.valueOf(chargeDetail.get("amountOrPercentage")))) == 0);
        assertTrue(Float.valueOf(outstanding).compareTo(Float.valueOf(String.valueOf(chargeDetail.get("amountOutstanding")))) == 0);
        assertTrue(Float.valueOf(amountPaid).compareTo(Float.valueOf(String.valueOf(chargeDetail.get("amountPaid")))) == 0);
        assertTrue(Float.valueOf(amountWaived).compareTo(Float.valueOf(String.valueOf(chargeDetail.get("amountWaived")))) == 0);
    }

    private void validateChargeExcludePrecission(Integer amountPercentage, final List<HashMap> loanCharges, final String amount,
            final String outstanding, String amountPaid, String amountWaived) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        assertTrue(Float.valueOf(twoDForm.format(Float.valueOf(amount)))
                .compareTo(Float.parseFloat(twoDForm.format(Float.valueOf(String.valueOf(chargeDetail.get("amountOrPercentage")))))) == 0);
        assertTrue(Float.valueOf(twoDForm.format(Float.valueOf(outstanding)))
                .compareTo(Float.valueOf(twoDForm.format(Float.parseFloat(String.valueOf(chargeDetail.get("amountOutstanding")))))) == 0);
        assertTrue(Float.valueOf(twoDForm.format(Float.parseFloat(amountPaid)))
                .compareTo(Float.valueOf(twoDForm.format(Float.parseFloat(String.valueOf(chargeDetail.get("amountPaid")))))) == 0);
        assertTrue(Float.valueOf(twoDForm.format(Float.parseFloat(amountWaived)))
                .compareTo(Float.valueOf(twoDForm.format(Float.parseFloat(String.valueOf(chargeDetail.get("amountWaived")))))) == 0);
    }

    private void validateNumberForEqual(String val, String val2) {
        assertTrue(Float.valueOf(val).compareTo(Float.valueOf(val2)) == 0);
    }

    private void validateNumberForEqualWithMsg(String msg, String val, String val2) {
        assertTrue(Float.valueOf(val).compareTo(Float.valueOf(val2)) == 0, msg + "expected " + val + " but was " + val2);
    }

    private void validateNumberForEqualExcludePrecission(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        assertTrue(Float.valueOf(twoDForm.format(Float.valueOf(val))).compareTo(Float.valueOf(twoDForm.format(Float.valueOf(val2)))) == 0,
                String.format("%s is not equal to %s", val, val2));
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
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
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
                .currencyDetails(digitsAfterDecimal, inMultiplesOf).build(null);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy,
            final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
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
                .currencyDetails(digitsAfterDecimal, inMultiplesOf).withAccounting(accountingRule, accounts).build(null);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String graceOnPrincipalPayment,
            List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000000.00") //
                .withLoanTermFrequency("24") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("24") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualPrincipalPayments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withPrincipalGrace(graceOnPrincipalPayment).withExpectedDisbursementDate("02 June 2014") //
                .withSubmittedOnDate("02 June 2014") //
                .withCollaterals(collaterals).build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
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
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithExternalId(RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification, final Integer clientID, final Integer loanProductID, String principal,
            final String externalId) {
        LOG.info("------------------------APPLYING FOR LOAN APPLICATION WITH EXTERNALID------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withExternalId(externalId) //
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
                .build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON, requestSpecification, responseSpecification);
    }

    private Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> tranches, List<HashMap> collaterals) {
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
                .withExpectedDisbursementDate("01 March 2014") //
                .withCollaterals(collaterals).withTranches(tranches) //
                .withSubmittedOnDate("01 March 2014") //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private String updateLoanJson(final Integer clientID, final Integer loanProductID, List<HashMap> charges, String savingsId,
            List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10,000.00") //
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
        return loanApplicationJSON;
    }

    private Integer applyForLoanApplicationWithPaymentStrategy(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, final String repaymentStrategy, final List<HashMap> collaterals) {
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
                .withRepaymentStrategy(repaymentStrategy) //
                .withCollaterals(collaterals).withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithPaymentStrategyAndPastMonth(final Integer clientID, final Integer loanProductID,
            List<HashMap> charges, final String savingsId, String principal, final String repaymentStrategy, final String fourMonthsfromNow,
            List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
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
                .withExpectedDisbursementDate(fourMonthsfromNow) //
                .withSubmittedOnDate(fourMonthsfromNow) //
                .withRepaymentStrategy(repaymentStrategy) //
                .withCollaterals(collaterals).withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals(new ArrayList<>(Arrays.asList(2011, 10, 20)), loanSchedule.get(1).get("dueDate"),
                "Checking for Due Date for 1st Month");
        assertEquals(Float.parseFloat("2911.49"), loanSchedule.get(1).get("principalOriginalDue"),
                "Checking for Principal Due for 1st Month");
        assertEquals(Float.parseFloat("240.00"), loanSchedule.get(1).get("interestOriginalDue"), "Checking for Interest Due for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2011, 11, 20)), loanSchedule.get(2).get("dueDate"),
                "Checking for Due Date for 2nd Month");
        assertEquals(Float.parseFloat("2969.72"), loanSchedule.get(2).get("principalDue"), "Checking for Principal Due for 2nd Month");
        assertEquals(Float.parseFloat("181.77"), loanSchedule.get(2).get("interestOriginalDue"), "Checking for Interest Due for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2011, 12, 20)), loanSchedule.get(3).get("dueDate"),
                "Checking for Due Date for 3rd Month");
        assertEquals(Float.parseFloat("3029.11"), loanSchedule.get(3).get("principalDue"), "Checking for Principal Due for 3rd Month");
        assertEquals(Float.parseFloat("122.38"), loanSchedule.get(3).get("interestOriginalDue"), "Checking for Interest Due for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2012, 1, 20)), loanSchedule.get(4).get("dueDate"),
                "Checking for Due Date for 4th Month");
        assertEquals(Float.parseFloat("3089.68"), loanSchedule.get(4).get("principalDue"), "Checking for Principal Due for 4th Month");
        assertEquals(Float.parseFloat("61.79"), loanSchedule.get(4).get("interestOriginalDue"), "Checking for Interest Due for 4th Month");
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipal(final ArrayList<HashMap> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 7, 2)), loanSchedule.get(1).get("dueDate"), "Checking for Due Date for 1st Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(1).get("principalOriginalDue"),
                "Checking for Principal Due for 1st Month");
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(1).get("interestOriginalDue"), "Checking for Interest Due for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 8, 2)), loanSchedule.get(2).get("dueDate"), "Checking for Due Date for 2nd Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(2).get("principalDue"), "Checking for Principal Due for 2nd Month");
        assertEquals(Float.parseFloat("191700"), loanSchedule.get(2).get("interestOriginalDue"), "Checking for Interest Due for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 9, 2)), loanSchedule.get(3).get("dueDate"), "Checking for Due Date for 3rd Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(3).get("principalDue"), "Checking for Principal Due for 3rd Month");
        assertEquals(Float.parseFloat("183300"), loanSchedule.get(3).get("interestOriginalDue"), "Checking for Interest Due for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 10, 2)), loanSchedule.get(4).get("dueDate"),
                "Checking for Due Date for 4th Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(4).get("principalDue"), "Checking for Principal Due for 4th Month");
        assertEquals(Float.parseFloat("175000"), loanSchedule.get(4).get("interestOriginalDue"), "Checking for Interest Due for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 11, 2)), loanSchedule.get(5).get("dueDate"),
                "Checking for Due Date for 5th Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(5).get("principalDue"), "Checking for Principal Due for 5th Month");
        assertEquals(Float.parseFloat("166700"), loanSchedule.get(5).get("interestOriginalDue"), "Checking for Interest Due for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 12, 2)), loanSchedule.get(6).get("dueDate"),
                "Checking for Due Date for 6th Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(6).get("principalDue"), "Checking for Principal Due for 6th Month");
        assertEquals(Float.parseFloat("158300"), loanSchedule.get(6).get("interestOriginalDue"), "Checking for Interest Due for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2015, 4, 2)), loanSchedule.get(10).get("dueDate"),
                "Checking for Due Date for 10th Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(10).get("principalDue"), "Checking for Principal Due for 10th Month");
        assertEquals(Float.parseFloat("125000"), loanSchedule.get(10).get("interestOriginalDue"),
                "Checking for Interest Due for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2016, 2, 2)), loanSchedule.get(20).get("dueDate"),
                "Checking for Due Date for 20th Month");
        assertEquals(Float.parseFloat("416700"), loanSchedule.get(20).get("principalDue"), "Checking for Principal Due for 20th Month");
        assertEquals(Float.parseFloat("41700"), loanSchedule.get(20).get("interestOriginalDue"),
                "Checking for Interest Due for 20th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2016, 6, 2)), loanSchedule.get(24).get("dueDate"),
                "Checking for Due Date for 24th Month");
        assertEquals(Float.parseFloat("415900"), loanSchedule.get(24).get("principalDue"), "Checking for Principal Due for 24th Month");
        assertEquals(Float.parseFloat("8300"), loanSchedule.get(24).get("interestOriginalDue"), "Checking for Interest Due for 24th Month");

    }

    private void verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(final ArrayList<HashMap> loanSchedule) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 7, 2)), loanSchedule.get(1).get("dueDate"), "Checking for Due Date for 1st Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 1st Month",
                String.valueOf(loanSchedule.get(1).get("principalOriginalDue")), "0.0");
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(1).get("interestOriginalDue"), "Checking for Interest Due for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 8, 2)), loanSchedule.get(2).get("dueDate"), "Checking for Due Date for 2nd Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 2nd Month", "0.0",
                String.valueOf(loanSchedule.get(2).get("principalOriginalDue")));
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(2).get("interestOriginalDue"), "Checking for Interest Due for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 9, 2)), loanSchedule.get(3).get("dueDate"), "Checking for Due Date for 3rd Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 3rd Month", "0.0",
                String.valueOf(loanSchedule.get(3).get("principalDue")));
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(3).get("interestOriginalDue"), "Checking for Interest Due for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 10, 2)), loanSchedule.get(4).get("dueDate"),
                "Checking for Due Date for 4th Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 4th Month", "0",
                String.valueOf(loanSchedule.get(4).get("principalDue")));
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(4).get("interestOriginalDue"), "Checking for Interest Due for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 11, 2)), loanSchedule.get(5).get("dueDate"),
                "Checking for Due Date for 5th Month");
        validateNumberForEqualWithMsg("Checking for Principal Due for 5th Month", "0",
                String.valueOf(loanSchedule.get(5).get("principalDue")));
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(5).get("interestOriginalDue"), "Checking for Interest Due for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2014, 12, 2)), loanSchedule.get(6).get("dueDate"),
                "Checking for Due Date for 6th Month");
        assertEquals(Float.parseFloat("526300"), loanSchedule.get(6).get("principalDue"), "Checking for Principal Due for 6th Month");
        assertEquals(Float.parseFloat("200000"), loanSchedule.get(6).get("interestOriginalDue"), "Checking for Interest Due for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2015, 1, 2)), loanSchedule.get(7).get("dueDate"), "Checking for Due Date for 7th Month");
        assertEquals(Float.parseFloat("526300"), loanSchedule.get(7).get("principalDue"), "Checking for Principal Due for 7th Month");
        assertEquals(Float.parseFloat("189500"), loanSchedule.get(7).get("interestOriginalDue"), "Checking for Interest Due for 7th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2015, 4, 2)), loanSchedule.get(10).get("dueDate"),
                "Checking for Due Date for 10th Month");
        assertEquals(Float.parseFloat("526300"), loanSchedule.get(10).get("principalDue"), "Checking for Principal Due for 10th Month");
        assertEquals(Float.parseFloat("157900"), loanSchedule.get(10).get("interestOriginalDue"),
                "Checking for Interest Due for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2016, 2, 2)), loanSchedule.get(20).get("dueDate"),
                "Checking for Due Date for 20th Month");
        assertEquals(Float.parseFloat("526300"), loanSchedule.get(20).get("principalDue"), "Checking for Principal Due for 20th Month");
        assertEquals(Float.parseFloat("52600"), loanSchedule.get(20).get("interestOriginalDue"),
                "Checking for Interest Due for 20th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2016, 6, 2)), loanSchedule.get(24).get("dueDate"),
                "Checking for Due Date for 24th Month");
        assertEquals(Float.parseFloat("526600"), loanSchedule.get(24).get("principalDue"), "Checking for Principal Due for 24th Month");
        assertEquals(Float.parseFloat("10500"), loanSchedule.get(24).get("interestOriginalDue"),
                "Checking for Interest Due for 24th Month");
    }

    private void addCharges(List<HashMap> charges, Integer chargeId, String amount, String duedate) {
        charges.add(charges(chargeId, amount, duedate));
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap charges(Integer chargeId, String amount, String duedate) {
        HashMap charge = new HashMap(2);
        charge.put("chargeId", chargeId.toString());
        charge.put("amount", amount);
        if (duedate != null) {
            charge.put("dueDate", duedate);
        }
        return charge;
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    private HashMap getloanCharge(Integer chargeId, List<HashMap> charges) {
        HashMap charge = null;
        for (HashMap loancharge : charges) {
            if (loancharge.get("chargeId").equals(chargeId)) {
                charge = loancharge;
            }
        }
        return charge;
    }

    private List<HashMap> copyChargesForUpdate(List<HashMap> charges, Integer deleteWithChargeId, String amount) {
        List<HashMap> loanCharges = new ArrayList<>();
        for (HashMap charge : charges) {
            if (!charge.get("chargeId").equals(deleteWithChargeId)) {
                loanCharges.add(copyForUpdate(charge, amount));
            }
        }
        return loanCharges;
    }

    private HashMap copyForUpdate(HashMap charge, String amount) {
        HashMap map = new HashMap();
        map.put("id", charge.get("id"));
        if (amount == null) {
            map.put("amount", charge.get("amountOrPercentage"));
        } else {
            map.put("amount", amount);
        }
        if (charge.get("dueDate") != null) {
            map.put("dueDate", DATE_TIME_FORMATTER.format(fromArrayToLocalDate((List) charge.get("dueDate"))));
        }
        map.put("chargeId", charge.get("chargeId"));
        return map;
    }

    private LocalDate fromArrayToLocalDate(List<Integer> dueDate) {
        return LocalDate.of(dueDate.get(0), dueDate.get(1), dueDate.get(2));
    }

    private HashMap createTrancheDetail(final String date, final String amount) {
        HashMap detail = new HashMap();
        detail.put("expectedDisbursementDate", date);
        detail.put("principal", amount);

        return detail;
    }

    private void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(final String preCloseStrategy,
            final String preCloseAmount) {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null, preCloseStrategy, null, null,
                null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculationWithMoratorium(clientID, loanProductID,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0), "1", null);

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    private void validateAccrualTransactionForDisbursementCharge(ArrayList<HashMap> loanTransactionDetails) {
        List<HashMap> disbursementTransactions = loanTransactionDetails.stream()
                .filter(transactionDetail -> (Boolean) ((LinkedHashMap) transactionDetail.get("type")).get("repaymentAtDisbursement"))
                .toList();
        List<HashMap> accrualTransactions = loanTransactionDetails.stream()
                .filter(transactionDetail -> (Boolean) ((LinkedHashMap) transactionDetail.get("type")).get("accrual")).toList();
        assertEquals(disbursementTransactions.size(), accrualTransactions.size(), 1);
        assertEquals((Float) disbursementTransactions.get(0).get("amount"), (Float) accrualTransactions.get(0).get("amount"));
        assertTrue(StringUtils.isNotBlank((String) accrualTransactions.get(0).get("externalId")));
    }

    private void addRepaymentValues(List<Map<String, Object>> expectedvalues, Calendar todaysDate, int addPeriod, boolean isAddDays,
            String principalDue, String interestDue, String feeChargesDue, String penaltyChargesDue) {
        Map<String, Object> values = new HashMap<>(3);
        if (isAddDays) {
            values.put("dueDate", getDateAsArray(todaysDate, addPeriod));
        } else {
            values.put("dueDate", getDateAsArray(todaysDate, addPeriod * 7));
        }
        LOG.info("Updated date {}", values.get("dueDate"));
        values.put("principalDue", principalDue);
        values.put("interestDue", interestDue);
        values.put("feeChargesDue", feeChargesDue);
        values.put("penaltyChargesDue", penaltyChargesDue);
        expectedvalues.add(values);
    }

    private List getDateAsArray(Calendar todaysDate, int addPeriod) {
        return getDateAsArray(todaysDate, addPeriod, Calendar.DAY_OF_MONTH);
    }

    private List getDateAsArray(Calendar todaysDate, int addvalue, int type) {
        todaysDate.add(type, addvalue);
        return new ArrayList<>(
                Arrays.asList(todaysDate.get(Calendar.YEAR), todaysDate.get(Calendar.MONTH) + 1, todaysDate.get(Calendar.DAY_OF_MONTH)));
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        final String recalculationCompoundingFrequencyType = null;
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer recalculationCompoundingFrequencyOnDayType = null;
        final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationRestFrequencyDate, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, preCloseInterestCalculationStrategy, accounts, null, false,
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
    }

    private Integer createLoanProductWithInterestRecalculationAndCompoundingDetails(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final String recalculationCompoundingFrequencyDate,
            final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final Integer recalculationCompoundingFrequencyOnDayType, final Integer recalculationCompoundingFrequencyDayOfWeekType,
            final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationRestFrequencyDate, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, preCloseInterestCalculationStrategy, accounts, null, false,
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final String recalculationCompoundingFrequencyDate,
            final String preCloseInterestCalculationStrategy, final Account[] accounts, final String chargeId,
            boolean isArrearsBasedOnOriginalSchedule, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("10000000.00").withNumberOfRepayments("24")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(repaymentStrategy)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType);
        if (accounts != null) {
            builder = builder.withAccountingRulePeriodicAccrual(accounts);
        }

        if (isArrearsBasedOnOriginalSchedule) {
            builder = builder.withArrearsConfiguration();
        }

        final String loanProductJSON = builder.build(chargeId);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProductWithInterestRecalculationAndCompoundingDetails(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final String installmentMultipleOf) {
        final String recalculationCompoundingFrequencyType = null;
        final String recalculationCompoundingFrequencyInterval = null;
        final Integer recalculationCompoundingFrequencyOnDayType = null;
        final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                preCloseInterestCalculationStrategy, accounts, null, false, recalculationCompoundingFrequencyOnDayType,
                recalculationCompoundingFrequencyDayOfWeekType, installmentMultipleOf);
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationCompoundingFrequencyType, final String recalculationCompoundingFrequencyInterval,
            final String preCloseInterestCalculationStrategy, final Account[] accounts, final String chargeId,
            boolean isArrearsBasedOnOriginalSchedule, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final String installmentsMultiplesOf) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("12")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("19.9")
                .withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(repaymentStrategy).withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType)
                .withDefineInstallmentAmount(true).withInstallmentAmountInMultiplesOf(installmentsMultiplesOf);
        if (accounts != null) {
            builder = builder.withAccountingRulePeriodicAccrual(accounts);
        }

        if (isArrearsBasedOnOriginalSchedule) {
            builder = builder.withArrearsConfiguration();
        }

        final String loanProductJSON = builder.build(chargeId);
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final String firstRepaymentDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        List<HashMap> collaterals = new ArrayList<>();

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000.00") //
                .withLoanTermFrequency("12") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("12") //
                .withRepaymentEveryAfter("1") //
                .withLoanTermFrequencyAsMonths() //
                .withInterestRatePerPeriod("19.9") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeAsDays() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .withRepaymentStrategy(repaymentStrategy).withRepaymentFrequencyTypeAsMonths()//
                .withFirstRepaymentDate(firstRepaymentDate).withCollaterals(collaterals)
                .build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges, null,
                null);
    }

    private Integer applyForLoanApplicationForInterestRecalculationWithMoratorium(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges, final String graceOnInterestPayment,
            final String graceOnPrincipalPayment) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges,
                graceOnInterestPayment, graceOnPrincipalPayment);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String compoundingStartDate, final String repaymentStrategy, final List<HashMap> charges) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges, null,
                null);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges, final String graceOnInterestPayment,
            final String graceOnPrincipalPayment) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(REQUEST_SPEC, RESPONSE_SPEC);
        Assertions.assertNotNull(collateralId);
        List<HashMap> collaterals = new ArrayList<>();

        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(REQUEST_SPEC, RESPONSE_SPEC,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsWeeks() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsWeeks() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .withRepaymentStrategy(repaymentStrategy) //
                .withPrincipalGrace(graceOnPrincipalPayment) //
                .withInterestGrace(graceOnInterestPayment)//
                .withCharges(charges)//
                .withCollaterals(collaterals).build(clientID.toString(), loanProductID.toString(), null);
        return LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule, List<Map<String, Object>> expectedvalues) {
        int index = 1;
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, index);

    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule, List<Map<String, Object>> expectedvalues, int index) {
        LOG.info("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");
        for (Map<String, Object> values : expectedvalues) {
            assertEquals(values.get("dueDate"), loanSchedule.get(index).get("dueDate"), "Checking for Due Date for  installment " + index);
            validateNumberForEqualWithMsg("Checking for Principal Due for installment " + index, String.valueOf(values.get("principalDue")),
                    String.valueOf(loanSchedule.get(index).get("principalDue")));
            validateNumberForEqualWithMsg("Checking for Interest Due for installment " + index, String.valueOf(values.get("interestDue")),
                    String.valueOf(loanSchedule.get(index).get("interestDue")));
            validateNumberForEqualWithMsg("Checking for Fee charge Due for installment " + index,
                    String.valueOf(values.get("feeChargesDue")), String.valueOf(loanSchedule.get(index).get("feeChargesDue")));
            validateNumberForEqualWithMsg("Checking for Penalty charge Due for installment " + index,
                    String.valueOf(values.get("penaltyChargesDue")), String.valueOf(loanSchedule.get(index).get("penaltyChargesDue")));
            index++;
        }
    }

    private void checkAccrualTransactions(final ArrayList<HashMap> loanSchedule, final Integer loanID) {

        for (int i = 1; i < loanSchedule.size(); i++) {

            final HashMap repayment = loanSchedule.get(i);

            final ArrayList<Integer> dueDateAsArray = (ArrayList<Integer>) repayment.get("dueDate");
            final LocalDate transactionDate = LocalDate.of(dueDateAsArray.get(0), dueDateAsArray.get(1), dueDateAsArray.get(2));

            final Float interestPortion = BigDecimal.valueOf(Double.parseDouble(repayment.get("interestDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("interestWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("interestWrittenOff").toString()))).floatValue();

            final Float feePortion = BigDecimal.valueOf(Double.parseDouble(repayment.get("feeChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("feeChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("feeChargesWrittenOff").toString()))).floatValue();

            final Float penaltyPortion = BigDecimal.valueOf(Double.parseDouble(repayment.get("penaltyChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("penaltyChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.parseDouble(repayment.get("penaltyChargesWrittenOff").toString()))).floatValue();

            LOAN_TRANSACTION_HELPER.checkAccrualTransactionForRepayment(transactionDate, interestPortion, feePortion, penaltyPortion,
                    loanID);
        }
    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
            String preCloseInterestStrategy, String preCloseAmount) {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null, preCloseInterestStrategy, null,
                null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0));

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2551.72", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -9);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.8", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
            String preCloseInterestStrategy, String preCloseAmount) {

        DateFormat dateFormat = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -4);
        Integer restDateOfMonth = getDayOfMonth(todaysDate);
        Integer restDateOfWeek = getDayOfWeek(todaysDate);
        final String REST_START_DATE = null;

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        todaysDate.add(Calendar.DAY_OF_MONTH, 2);
        final String LOAN_FLAT_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, 14);
        final String LOAN_INTEREST_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        List<HashMap> charges = new ArrayList<>(2);
        Integer flat = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer principalPercentage = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "2", false));

        addCharges(charges, flat, "100", LOAN_FLAT_CHARGE_DATE);
        addCharges(charges, principalPercentage, "2", LOAN_INTEREST_CHARGE_DATE);

        final Integer clientID = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.DEFAULT_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, null, null, preCloseInterestStrategy, null,
                null, null, restDateOfMonth, restDateOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                REST_START_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        loanStatusHashMap = LOAN_TRANSACTION_HELPER.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.08", "46.83", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2481.87", "47.04", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2553.29", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -9);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        LOAN_TRANSACTION_HELPER.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = LOAN_TRANSACTION_HELPER.getLoanRepaymentSchedule(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.05", "34.86", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.97", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = LOAN_TRANSACTION_HELPER.getPrepayAmount(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        LOAN_TRANSACTION_HELPER.makeRepayment(loanRepaymentDate, Float.parseFloat(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(REQUEST_SPEC, RESPONSE_SPEC, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    private Integer createSavingsProduct(final String minOpenningBalance) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();

        final String savingsProductJSON = savingsProductHelper
                //
                .withInterestCompoundingPeriodTypeAsDaily()
                //
                .withInterestPostingPeriodTypeAsMonthly()
                //
                .withInterestCalculationPeriodTypeAsDailyBalance()

                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, REQUEST_SPEC, RESPONSE_SPEC);
    }

    private PostLoansResponse applyForLoanApplicationForOnePeriod30DaysLongNoInterestPeriodicAccrual(Long clientId, Long loanProductId,
            String loanDisbursementDate, String repaymentStrategyCode) {
        return LOAN_TRANSACTION_HELPER.applyLoan(new PostLoansRequest().clientId(clientId.longValue()).productId(loanProductId)
                .expectedDisbursementDate(loanDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(repaymentStrategyCode).locale("en").submittedOnDate(loanDisbursementDate)
                .amortizationType(1).interestRatePerPeriod(BigDecimal.ZERO).interestCalculationPeriodType(1).interestType(0)
                .repaymentFrequencyType(0).repaymentEvery(30).repaymentFrequencyType(0).numberOfRepayments(1).loanTermFrequency(30)
                .loanTermFrequencyType(0).principal(BigDecimal.valueOf(1000.0)).loanType("individual"));
    }

    private PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() {
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
                .interestRateFrequencyType(2)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)//
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
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(Collections.emptyList())//
                .accountingRule(3)//
                .fundSourceAccountId(SUSPENSE_CLEARING_ACCOUNT.getAccountID().longValue())//
                .loanPortfolioAccountId(LOANS_RECEIVABLE_ACCOUNT.getAccountID().longValue())//
                .transfersInSuspenseAccountId(SUSPENSE_ACCOUNT.getAccountID().longValue())//
                .interestOnLoanAccountId(INTEREST_INCOME_ACCOUNT.getAccountID().longValue())//
                .incomeFromFeeAccountId(FEE_INCOME_ACCOUNT.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(FEE_INCOME_ACCOUNT.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(RECOVERIES_ACCOUNT.getAccountID().longValue())//
                .writeOffAccountId(WRITTEN_OFF_ACCOUNT.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(OVERPAYMENT_ACCOUNT.getAccountID().longValue())//
                .receivableInterestAccountId(INTEREST_FEE_RECEIVABLE_ACCOUNT.getAccountID().longValue())//
                .receivableFeeAccountId(INTEREST_FEE_RECEIVABLE_ACCOUNT.getAccountID().longValue())//
                .receivablePenaltyAccountId(INTEREST_FEE_RECEIVABLE_ACCOUNT.getAccountID().longValue())//
                .dateFormat(DATETIME_PATTERN)//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .goodwillCreditAccountId(GOODWILL_EXPENSE_ACCOUNT.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(INTEREST_INCOME_CHARGE_OFF_ACCOUNT.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(FEE_CHARGE_OFF_ACCOUNT.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(FEE_CHARGE_OFF_ACCOUNT.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(INTEREST_INCOME_CHARGE_OFF_ACCOUNT.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(FEE_CHARGE_OFF_ACCOUNT.getAccountID().longValue())//
                .chargeOffExpenseAccountId(CREDIT_LOSS_BAD_DEBT_ACCOUNT.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(CREDIT_LOSS_BAD_DEBT_FRAUD_ACCOUNT.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(FEE_CHARGE_OFF_ACCOUNT.getAccountID().longValue());
    }
}
