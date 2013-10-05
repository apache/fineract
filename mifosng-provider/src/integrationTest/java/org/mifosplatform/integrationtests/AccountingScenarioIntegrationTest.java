package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.accounting.JournalEntry;
import org.mifosplatform.integrationtests.common.accounting.JournalEntryHelper;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class AccountingScenarioIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private final String DATE_OF_JOINING = "01 January 2011";

    private final Float LP_PRINCIPAL = 10000.0f;
    private final String LP_REPAYMENTS = "5";
    private final String LP_REPAYMENT_PERIOD = "2";
    private final String LP_INTEREST_RATE = "1";
    private final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private final String LOAN_APPLICATION_SUBMISSION_DATE = "3 March 2011";
    private final String LOAN_TERM_FREQUENCY = "10";
    private final String INDIVIDUAL_LOAN = "individual";

    private final String REPAYMENT_DATE[] = { "", "04 May 2011", "04 July 2011", "04 September 2011", "04 November 2011", "04 January 2012" };
    private final Float REPAYMENT_AMOUNT[] = { .0f, 2200.0f, 3000.0f, 900.0f, 2000.0f, 2500.0f };

    private final Float AMOUNT_TO_BE_WAIVE = 400.0f;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkAccountingFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);
        System.out.println("CHECKING INCOME: ******************************************");
        final JournalEntry incomeJournalEntry = new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, this.EXPECTED_DISBURSAL_DATE, incomeJournalEntry);

        // MAKE 1
        System.out.println("Repayment 1 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[1], this.REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = this.LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_INTEREST, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[1], assetAccountFirstEntry);
        System.out.println("Repayment 1 Done......");

        // REPAYMENT 2
        System.out.println("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[2], this.REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = this.REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_AND_THIRD_INTEREST, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[2], assetAccountSecondEntry);
        System.out.println("Repayment 2 Done ......");

        // WAIVE INTEREST
        System.out.println("Waive Interest  ......");
        this.loanTransactionHelper.waiveInterest(this.REPAYMENT_DATE[4], this.AMOUNT_TO_BE_WAIVE.toString(), loanID);

        final JournalEntry waivedEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], waivedEntry);

        final JournalEntry expenseJournalEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, this.REPAYMENT_DATE[4], expenseJournalEntry);
        System.out.println("Waive Interest Done......");

        // REPAYMENT 3
        System.out.println("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[3], this.REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[3], assetAccountThirdEntry);
        System.out.println("Repayment 3 Done ......");

        // REPAYMENT 4
        System.out.println("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[4], this.REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], assetAccountFourthEntry);
        System.out.println("Repayment 4 Done  ......");

        // Repayment 5
        System.out.println("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[5], this.REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[5], assetAccountFifthEntry);
        System.out.println("Repayment 5 Done  ......");
    }

    private Integer createLoanProduct(final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsAccrualBased(accounts).build();
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString())
                .withLoanTermFrequency(this.LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(this.LP_REPAYMENTS)
                .withRepaymentEveryAfter(this.LP_REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(this.EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(this.LOAN_APPLICATION_SUBMISSION_DATE)
                .withLoanType(this.INDIVIDUAL_LOAN).build(clientID.toString(), loanProductID.toString());
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
