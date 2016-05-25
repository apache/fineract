package org.apache.fineract.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class LoanSubsidyIntegrationTest {
	
	private static final String NONE = "1";
	private final Float LP_PRINCIPAL = 10000.0f;
    private final String LP_REPAYMENTS = "5";
    private final String LP_REPAYMENT_PERIOD = "2";
	private final String LP_INTEREST_RATE = "1";
	
	private RequestSpecification requestSpec;
	private ResponseSpecification responseSpec;
	private LoanTransactionHelper loanTransactionHelper;
	private LoanApplicationApprovalTest loanApplicationApprovalTest;
	private AccountHelper accountHelper;

	@Before
    public void setup() {
		Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanApplicationApprovalTest = new LoanApplicationApprovalTest();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkCreateLoanProductWithSubsidyApplicable() {

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withInterestTypeAsDecliningBalance().withTranches(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withIsSubsidyApplicable(true).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

    }
    
    @Test
    public void checkCreateLoanProductWithOutSubsidyApplicable() {

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withInterestTypeAsDecliningBalance().withTranches(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withIsSubsidyApplicable(false).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

    }
    
    @Test
    public void checkCreateLoanProductWithSubsidyAccounting() {
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();
        
        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount};

        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withIsSubsidyApplicable(true)
                .withDaysInMonth("30").withDaysInYear("365").build(null);
        
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

    }
    
    @Test
    public void checkCreateLoanProductWithOutSubsidyAccounting() {
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();
        final Account subsidyFundSource = this.accountHelper.createAssetAccount();
        final Account subsidyAccountId = this.accountHelper.createLiabilityAccount();
        
        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount, subsidyFundSource, subsidyAccountId};

        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withIsSubsidyApplicable(false)
                .withDaysInMonth("30").withDaysInYear("365").build(null);
        
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);
        
    }
    
    
    @Test
    public void loanApplicationWithSubsidy(){
    	
    	final String proposedAmount = "5000000";
        final String approvalAmount = "5000000";
        final String approveDate = "11 May 2013";
        final String expectedDisbursementDate = "11 May 2013";
        final String disbursalDate = "11 May 2013";
        
        ResponseSpecification generalResponseSpec = new ResponseSpecBuilder().build();
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount};
        
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
        		.withPrincipal("5000000").withMinPrincipal("1").withMaxPrincipal("1000000000").withinterestRatePerPeriod("13")
        		.withInterestRateFrequencyTypeAsYear().withRepaymentTypeAsMonth().withNumberOfRepayments("36").withInterestTypeAsDecliningBalance()
        		.withTranches(true).withInterestCalculationPeriodTypeAsDays().withAccountingRulePeriodicAccrual(accounts)
        		.withIsSubsidyApplicable(true).withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
        				LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS, 
        				LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
        				.withInterestRecalculationCompoundingFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
        						null, null, null).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("11 June 2013", Float.valueOf("168041.2"), loanID);
        System.out.println("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2013", Float.valueOf("168041.2"), loanID);
        System.out.println("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("11 August 2013", Float.valueOf("168041.2"), loanID);
        System.out.println("-------------Add Subsidy 1-----------");
        this.loanTransactionHelper.addSubsidy("01 September 2013", Float.valueOf("800000"), loanID);
        System.out.println("-------------Make repayment 4-----------");
        this.loanTransactionHelper.makeRepayment("11 September 2013", Float.valueOf("168041.2"), loanID);
        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        HashMap installement  = loanRepaymnetSchedule.get(4);
        assertEquals("48557.82", String.valueOf(installement.get("interestDue")));
        System.out.println("-------------Make repayment 5-----------");
        this.loanTransactionHelper.makeRepayment("11 October 2013", Float.valueOf("168041.2"), loanID);
        System.out.println("-------------Revoke Subsidy 1-----------");
        this.loanTransactionHelper.revokeSubsidy("01 November 2013", loanID);
        System.out.println("-------------Make repayment 6-----------");
        this.loanTransactionHelper.makeRepayment("11 November 2013", Float.valueOf("168041.2"), loanID);
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        System.out.println("-------------Revoke Subsidy Response-----------"+loanRepaymnetSchedule.get(6));
        installement  = loanRepaymnetSchedule.get(6);
        assertEquals("42689.8", String.valueOf(installement.get("interestDue")));
    }
    
    @Test
    public void loanApplicationWithSubsidyRealization(){
    	
    	final String proposedAmount = "5000000";
        final String approvalAmount = "5000000";
        final String approveDate = "11 May 2013";
        final String expectedDisbursementDate = "11 May 2013";
        final String disbursalDate = "11 May 2013";
        
        ResponseSpecification generalResponseSpec = new ResponseSpecBuilder().build();
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount};
        
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
        		.withPrincipal("5000000").withMinPrincipal("1").withMaxPrincipal("1000000000").withinterestRatePerPeriod("13")
        		.withInterestRateFrequencyTypeAsYear().withRepaymentTypeAsMonth().withNumberOfRepayments("36").withInterestTypeAsDecliningBalance()
        		.withTranches(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withAccountingRulePeriodicAccrual(accounts)
        		.withIsSubsidyApplicable(true).withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
        				LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS, 
        				LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
        				.withInterestRecalculationCompoundingFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
        						null, null, null).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
//        System.out.println("-------------Revoke repayment 0-----------");
        this.loanTransactionHelper.makeRepayment("11 June 2013", Float.valueOf("194094.37"), loanID);
        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2013", Float.valueOf("194094.37"), loanID);
        System.out.println("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("11 August 2013", Float.valueOf("194094.37"), loanID);
        System.out.println("-------------Add Subsidy 1-----------");
        this.loanTransactionHelper.addSubsidy("01 September 2013", Float.valueOf("800000"), loanID);
        System.out.println("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("11 September 2013", Float.valueOf("194094.37"), loanID);
        System.out.println("-------------Make repayment 4-----------");
        
        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        HashMap installement  = loanRepaymnetSchedule.get(4);
        assertEquals("47685.4", String.valueOf(installement.get("interestDue")));
        
        this.loanTransactionHelper.makeRepayment("11 October 2013", Float.valueOf("177681.14"), loanID);
        System.out.println("-------------Make repayment 5-----------");
        this.loanTransactionHelper.revokeSubsidy("01 November 2013", loanID);
        System.out.println("-------------Revoke Subsidy 1-----------");
        this.loanTransactionHelper.makeRepayment("11 November 2013", Float.valueOf("180290.04"), loanID);
        System.out.println("-------------Make repayment 6-----------");

        
        
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        System.out.println("-------------loanRepaymnetSchedule.get(6)-----------"+loanRepaymnetSchedule.get(6));
        installement  = loanRepaymnetSchedule.get(6);
        assertEquals("41401.15", String.valueOf(installement.get("interestDue")));
        
        this.loanTransactionHelper.makeRepayment("11 December 2013", Float.valueOf("183261.04"), loanID);
        System.out.println("-------------Make repayment 7-----------");
        this.loanTransactionHelper.makeRepayment("11 January 2014", Float.valueOf("183206.63"), loanID);
        System.out.println("-------------Make repayment 8-----------");
        this.loanTransactionHelper.addSubsidy("01 February 2014", Float.valueOf("200000"), loanID);
        System.out.println("-------------Add Subsidy 2-----------");
        this.loanTransactionHelper.makeRepayment("11 February 2014", Float.valueOf("180960.81"), loanID);
        System.out.println("-------------Make repayment 9-----------");
        this.loanTransactionHelper.addSubsidy("06 March 2014", Float.valueOf("600000"), loanID);
        System.out.println("-------------Add Subsidy 3-----------");
         this.loanTransactionHelper.makeRepayment("11 March 2014", Float.valueOf("174344.07"), loanID);
        System.out.println("-------------Make repayment 10-----------");
        this.loanTransactionHelper.makeRepayment("11 April 2014", Float.valueOf("169773.3"), loanID);
        System.out.println("-------------Make repayment 11-----------");
        this.loanTransactionHelper.revokeSubsidy("20 April 2014", loanID);
        System.out.println("-------------Revoke Subsidy 2-----------");
        this.loanTransactionHelper.makeRepayment("11 May 2014", Float.valueOf("173276.57"), loanID);
        System.out.println("-------------Make repayment 12-----------");
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        System.out.println("-------------loanRepaymnetSchedule.get(12)-----------"+loanRepaymnetSchedule.get(12));
        installement  = loanRepaymnetSchedule.get(12);
        assertEquals("34374.07", String.valueOf(installement.get("interestDue")));
        this.loanTransactionHelper.makeRepayment("11 June 2014", Float.valueOf("175539.2"), loanID);
        System.out.println("-------------Make repayment 13-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2014", Float.valueOf("172872.92"), loanID);
        System.out.println("-------------Make repayment 14-----------");
        this.loanTransactionHelper.makeRepayment("11 August 2014", Float.valueOf("174005.72"), loanID);
        System.out.println("-------------Make repayment 15-----------");
        this.loanTransactionHelper.makeRepayment("11 September 2014", Float.valueOf("174005.72"), loanID);
        System.out.println("-------------Make repayment 16-----------");
        this.loanTransactionHelper.makeRepayment("11 October 2014", Float.valueOf("172872.92"), loanID);
        System.out.println("-------------Make repayment 17-----------");
        this.loanTransactionHelper.makeRepayment("11 November 2014", Float.valueOf("174005.72"), loanID);
        System.out.println("-------------Make repayment 18-----------");
        this.loanTransactionHelper.makeRepayment("11 December 2014", Float.valueOf("172872.92"), loanID);
        System.out.println("-------------Make repayment 19-----------");
        this.loanTransactionHelper.makeRepayment("11 January 2015", Float.valueOf("174005.72"), loanID);
        System.out.println("-------------Make repayment 20-----------");
        this.loanTransactionHelper.makeRepayment("11 February 2015", Float.valueOf("174005.72"), loanID);
        System.out.println("-------------Make repayment 21-----------");
        this.loanTransactionHelper.makeRepayment("11 March 2015", Float.valueOf("170607.32"), loanID);
        System.out.println("-------------Make repayment 22-----------");
        this.loanTransactionHelper.addSubsidy("04 April 2015", Float.valueOf("1000000"), loanID);
        System.out.println("-------------Add Subsidy 4-----------");
        this.loanTransactionHelper.makeRepayment("11 April 2015", Float.valueOf("157103.96"), loanID);
        System.out.println("-------------Make repayment 23-----------");
        this.loanTransactionHelper.makeRepayment("11 May 2015", Float.valueOf("146760.15"), loanID);
        System.out.println("-------------Make repayment 24-----------");
        this.loanTransactionHelper.makeRepayment("11 June 2015", Float.valueOf("145489.04"), loanID);
        System.out.println("-------------Make repayment 25-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2015", Float.valueOf("143792.12"), loanID);
        System.out.println("-------------Make repayment 26-----------");
        this.loanTransactionHelper.makeRepayment("11 August 2015", Float.valueOf("142422.07"), loanID);
        System.out.println("-------------Make repayment 27-----------");
        this.loanTransactionHelper.makeRepayment("11 September 2015", Float.valueOf("140888.59"), loanID);
        System.out.println("-------------Make repayment 28-----------");
        this.loanTransactionHelper.makeRepayment("11 October 2015", Float.valueOf("139340.06"), loanID);
        System.out.println("-------------Make repayment 29-----------");
        
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
        
    }
    
    
    @Test
    public void loanApplicationWithSubsidyAndLatePayment(){
    	
    	final String proposedAmount = "5000000";
        final String approvalAmount = "5000000";
        final String approveDate = "11 May 2013";
        final String expectedDisbursementDate = "11 May 2013";
        final String disbursalDate = "11 May 2013";
        
        ResponseSpecification generalResponseSpec = new ResponseSpecBuilder().build();
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount};
        
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
        		.withPrincipal("5000000").withMinPrincipal("1").withMaxPrincipal("1000000000").withinterestRatePerPeriod("13")
        		.withInterestRateFrequencyTypeAsYear().withRepaymentTypeAsMonth().withNumberOfRepayments("36").withInterestTypeAsDecliningBalance()
        		.withTranches(true).withRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
        		.withAccountingRulePeriodicAccrual(accounts).withIsSubsidyApplicable(true)
        		.withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
        				LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS, 
        				LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
        				.withInterestRecalculationCompoundingFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
        						null, null, null).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
//        System.out.println("-------------Revoke repayment 0-----------");
        this.loanTransactionHelper.makeRepayment("11 June 2013", Float.valueOf("194094.38"), loanID);
        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2013", Float.valueOf("194094.38"), loanID);
        System.out.println("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("15 August 2013", Float.valueOf("190991.35"), loanID);
        System.out.println("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("15 August 2013", Float.valueOf("6722.9"), loanID);
        System.out.println("-------------Make repayment 4-----------");
        
        
        this.loanTransactionHelper.addSubsidy("31 August 2013", Float.valueOf("800000"), loanID);
        System.out.println("-------------Add Subsidy 1-----------");
        this.loanTransactionHelper.makeRepayment("15 September 2013", Float.valueOf("181134.2"), loanID);
        System.out.println("-------------Make repayment 5-----------");
        this.loanTransactionHelper.makeRepayment("15 September 2013", Float.valueOf("5385.3"), loanID);
        System.out.println("-------------Make repayment 6-----------");
        
        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        HashMap installement  = loanRepaymnetSchedule.get(6);
        assertEquals("5385.3", String.valueOf(installement.get("interestDue")));
        
    }
    
    @Test
    public void loanApplicationWithSubsidyAndEarlyPayment(){
    	
    	final String proposedAmount = "5000000";
        final String approvalAmount = "5000000";
        final String approveDate = "11 May 2013";
        final String expectedDisbursementDate = "11 May 2013";
        final String disbursalDate = "11 May 2013";
        
        ResponseSpecification generalResponseSpec = new ResponseSpecBuilder().build();
    	
    	final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Account[] accounts = {assetAccount, incomeAccount, expenseAccount, overpaymentAccount};
        
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
        		.withPrincipal("5000000").withMinPrincipal("1").withMaxPrincipal("1000000000").withinterestRatePerPeriod("13")
        		.withInterestRateFrequencyTypeAsYear().withRepaymentTypeAsMonth().withNumberOfRepayments("36").withInterestTypeAsDecliningBalance()
        		.withTranches(true).withRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
        		.withAccountingRulePeriodicAccrual(accounts).withIsSubsidyApplicable(true)
        		.withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
        				LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS, 
        				LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
        				.withInterestRecalculationCompoundingFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
        						null, null, null).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("11 May 2013", "5000000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
//        System.out.println("-------------Revoke repayment 0-----------");
        this.loanTransactionHelper.makeRepayment("11 June 2013", Float.valueOf("194094.38"), loanID);
        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("11 July 2013", Float.valueOf("194094.38"), loanID);
        System.out.println("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("02 August 2013", Float.valueOf("190991.35"), loanID);
        System.out.println("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("11 August 2013", Float.valueOf("154015.41"), loanID);
        System.out.println("-------------Make repayment 4-----------");
        
        
        this.loanTransactionHelper.addSubsidy("02 September 2013", Float.valueOf("800000"), loanID);
        System.out.println("-------------Add Subsidy 1-----------");
        this.loanTransactionHelper.makeRepayment("11 September 2013", Float.valueOf("185192.98"), loanID);
        System.out.println("-------------Make repayment 5-----------");
        
        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        HashMap installement  = loanRepaymnetSchedule.get(5);
        assertEquals("46304.09", String.valueOf(installement.get("interestDue")));
        
    }
    
    
    
    public Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, String principal,
            List<HashMap> tranches) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("36") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("36") //
                .withRepaymentEveryAfter("1") // 
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("13") //
                .withExpectedDisbursementDate("11 May 2013") //
                .withTranches(tranches) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate("1 March 2012") //
                .withLoanSubsidy(true) //
                .withInterestCalculationPeriodTypeAsDays() //
                .withFixedEmiAmount(null)
                .withMaxOutstandingLoanBalance("5000000")
                .withwithRepaymentStrategy(LoanApplicationTestBuilder.RBI_INDIA_STRATEGY)
                .build(clientID.toString(), loanProductID.toString(), null);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}
