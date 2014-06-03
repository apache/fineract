package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.SchedulerJobHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.accounting.JournalEntry;
import org.mifosplatform.integrationtests.common.accounting.JournalEntryHelper;
import org.mifosplatform.integrationtests.common.charges.ChargesHelper;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsStatusChecker;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Client Loan Integration Test for checking Loan Application Repayments
 * Schedule, loan charges, penalties, loan repayments and verifying accounting
 * transactions
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientLoanIntegrationTest {

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String ACCRUAL_UPFRONT = "4";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private SchedulerJobHelper schedulerJobHelper;

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
    public void checkClientLoanCreateAndDisburseFlow() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "12,000.00");
        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, amountPercentage, "1", null);
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentage, "1", null);
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1"));
        addCharges(charges, interestPercentage, "1", null);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap disbursementDetail = loanSchedule.get(0);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getDisbursementChargesForLoanAsJSON(String.valueOf(flatDisbursement)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flatDisbursement, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flatDisbursement, "100", null);
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        Assert.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

    }

    @Test
    public void testLoanCharges_SPECIFIED_DUE_DATE_FEE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), "29 September 2011", "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);

        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(firstInstallment.get("feeChargesDue")));

        final Integer savingsId = createSavings(clientID);
        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), String.valueOf(savingsId)));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(firstInstallment.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flat, "100", "29 September 2011");
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        Assert.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0", String.valueOf(firstInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "29 September 2011", "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(amountPercentage), "29 September 2011", "1"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"), "");
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesOutstanding")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesWaived")));

        this.loanTransactionHelper.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, null));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
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
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

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

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
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

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("100"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
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

        final Integer savingsId = createSavings(clientID);
        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), String.valueOf(savingsId)));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
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

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
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
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("50", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("0", String.valueOf(installment.get("feeChargesDue")));
        }

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("100", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        Integer waivePeriodnum = 1;
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(waivePeriodnum)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (waivePeriodnum == installment.get("period")) {
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
        this.loanTransactionHelper.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, String.valueOf(payPeriodnum)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (payPeriodnum == installment.get("period")) {
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesPaid")));
            } else if (waivePeriodnum == installment.get("period")) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesWaived")));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.get("feeChargesPaid")));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "300", "100.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final Integer savingsId = createSavings(clientID);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, savingsId.toString(), "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings(SavingsAccountHelper.TRANSACTION_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("12000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_TRANCHES() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        List<HashMap> tranches = new ArrayList<HashMap>();
        tranches.add(createTrancheDetail("1 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, null, "45,000.00", tranches);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("1 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE first Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("1 March 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // DISBURSE Second Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("23 April 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS_WITH_TRANCHES() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        final Integer savingsId = createSavings(clientID);

        List<HashMap> tranches = new ArrayList<HashMap>();
        tranches.add(createTrancheDetail("1 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, savingsId.toString(), "45,000.00",
                tranches);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("1 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE first Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings("1 March 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("25000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE Second Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings("23 April 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("25000") + new Float("20000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

    }

    private void validateCharge(Integer amountPercentage, final List<HashMap> loanCharges, final String amount, final String outstanding,
            String amountPaid, String amountWaived) {
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        Assert.assertTrue(new Float(amount).compareTo(new Float(String.valueOf(chargeDetail.get("amountOrPercentage")))) == 0);
        Assert.assertTrue(new Float(outstanding).compareTo(new Float(String.valueOf(chargeDetail.get("amountOutstanding")))) == 0);
        Assert.assertTrue(new Float(amountPaid).compareTo(new Float(String.valueOf(chargeDetail.get("amountPaid")))) == 0);
        Assert.assertTrue(new Float(amountWaived).compareTo(new Float(String.valueOf(chargeDetail.get("amountWaived")))) == 0);
    }

    private void validateChargeExcludePrecission(Integer amountPercentage, final List<HashMap> loanCharges, final String amount,
            final String outstanding, String amountPaid, String amountWaived) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amount))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountOrPercentage")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(outstanding))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountOutstanding")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amountPaid))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountPaid")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amountWaived))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountWaived")))))) == 0);
    }

    public void validateNumberForEqual(String val, String val2) {
        Assert.assertTrue(new Float(val).compareTo(new Float(val2)) == 0);
    }

    public void validateNumberForEqualExcludePrecission(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        Assert.assertTrue(new Float(twoDForm.format(new Float(val))).compareTo(new Float(twoDForm.format(new Float(val2)))) == 0);
    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
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

    private Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> tranches) {
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
                .withExpectedDisbursementDate("1 March 2014") //
                .withTranches(tranches) //
                .withSubmittedOnDate("1 March 2014") //

                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private String updateLoanJson(final Integer clientID, final Integer loanProductID, List<HashMap> charges, String savingsId) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
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
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return loanApplicationJSON;
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals("Checking for Due Date for 1st Month", new ArrayList<Integer>(Arrays.asList(2011, 10, 20)),
                loanSchedule.get(1).get("dueDate"));
        assertEquals("Checking for Principal Due for 1st Month", new Float("2911.49"), loanSchedule.get(1).get("principalOriginalDue"));
        assertEquals("Checking for Interest Due for 1st Month", new Float("240.00"), loanSchedule.get(1).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 2nd Month", new ArrayList<Integer>(Arrays.asList(2011, 11, 20)),
                loanSchedule.get(2).get("dueDate"));
        assertEquals("Checking for Principal Due for 2nd Month", new Float("2969.71"), loanSchedule.get(2).get("principalDue"));
        assertEquals("Checking for Interest Due for 2nd Month", new Float("181.77"), loanSchedule.get(2).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 3rd Month", new ArrayList<Integer>(Arrays.asList(2011, 12, 20)),
                loanSchedule.get(3).get("dueDate"));
        assertEquals("Checking for Principal Due for 3rd Month", new Float("3029.1"), loanSchedule.get(3).get("principalDue"));
        assertEquals("Checking for Interest Due for 3rd Month", new Float("122.38"), loanSchedule.get(3).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 4th Month", new ArrayList<Integer>(Arrays.asList(2012, 1, 20)),
                loanSchedule.get(4).get("dueDate"));
        assertEquals("Checking for Principal Due for 4th Month", new Float("3089.7"), loanSchedule.get(4).get("principalDue"));
        assertEquals("Checking for Interest Due for 4th Month", new Float("61.79"), loanSchedule.get(4).get("interestOriginalDue"));
    }

    private void addCharges(List<HashMap> charges, Integer chargeId, String amount, String duedate) {
        charges.add(charges(chargeId, amount, duedate));
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
        List<HashMap> loanCharges = new ArrayList<HashMap>();
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
            map.put("dueDate", charge.get("dueDate"));
        }
        map.put("chargeId", charge.get("chargeId"));
        return map;
    }

    private Integer createSavings(Integer clientID) {
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer savingsId = savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private HashMap createTrancheDetail(final String date, final String amount) {
        HashMap detail = new HashMap();
        detail.put("expectedDisbursementDate", date);
        detail.put("principal", amount);

        return detail;
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("150.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2811.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("150.00"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3129.1"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("50.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3139.70"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3139.70"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.70"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("50.00"), JournalEntry.TransactionType.CREDIT));
    }
    
    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesOfTypeAmountPercentageAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("149.11"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2791.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("149.11"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.77"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.77"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3149.10"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("30.29"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3120.60"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3120.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.70"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("30.90"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type percentage of amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("157.57"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.54"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.54"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2791.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("157.57"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3302.99"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3302.99"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3149.10"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3121.21"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3121.21"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.70"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("200.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("100.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("200.00"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 September 2011", "100"));

        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("150.00"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(Float.valueOf("50.0"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("50.0"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("150"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2811.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("50.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3129.10"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3220.60"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3139.70"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("50.00"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("120.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("120.00"),
                        JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("149.11"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(Float.valueOf("29.7"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("29.7"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("149.11"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2791.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.77"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.77"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("30.29"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3149.10"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3220.60"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3120.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("30.90"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    
    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type percentage of amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("126.04"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("126.06"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("126.04"),
                        JournalEntry.TransactionType.CREDIT));
        
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 September 2011", "1"));
        
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("157.57"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(Float.valueOf("31.51"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("31.51"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.54"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.54"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("126.06"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("157.57"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2791.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3302.99"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3302.99"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("31.51"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3149.10"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3221.61"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3121.21"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("31.51"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }
    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("150.00"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(
                Float.valueOf("50.0"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("50.0"), JournalEntry.TransactionType.DEBIT));

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.excuteJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("150"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2811.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("50"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3129.10"),
                        JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.70", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3139.70"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3139.70"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("50.00"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }
    
    /**
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesOfTypeAmountPercentageAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("149.11"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(
                Float.valueOf("29.7"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("29.7"), JournalEntry.TransactionType.DEBIT));

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.excuteJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.48"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.48"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("149.11"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2791.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.77"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.77"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("30.29"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3149.1"),
                        JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.60", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3120.60"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3120.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("30.90"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }
    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type percentage of amount and interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<HashMap>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
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

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("157.57"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("2911.49"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", new JournalEntry(
                Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("31.51"), JournalEntry.TransactionType.DEBIT));

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.excuteJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.54"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.54"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("126.06"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("181.77"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2969.71"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("157.57"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("2791.49"),
                        JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3302.99"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3302.99"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("31.51"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("122.38"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3149.1"),
                        JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.21", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3121.21"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3121.21"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("31.51"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("3089.70"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    private void checkAccrualTransactions(final ArrayList<HashMap> loanSchedule, final Integer loanID) {

        for (int i = 1; i < loanSchedule.size(); i++) {

            final HashMap repayment = loanSchedule.get(i);

            final ArrayList<Integer> dueDateAsArray = (ArrayList<Integer>) repayment.get("dueDate");
            final LocalDate transactionDate = new LocalDate(dueDateAsArray.get(0), dueDateAsArray.get(1), dueDateAsArray.get(2));

            final Float interestPortion = BigDecimal.valueOf(Double.valueOf(repayment.get("interestDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("interestWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("interestWrittenOff").toString()))).floatValue();

            final Float feePortion = BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesWrittenOff").toString()))).floatValue();

            final Float penaltyPortion = BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesWrittenOff").toString()))).floatValue();

            this.loanTransactionHelper.checkAccrualTransactionForRepayment(transactionDate, interestPortion, feePortion, penaltyPortion,
                    loanID);
        }
    }
}