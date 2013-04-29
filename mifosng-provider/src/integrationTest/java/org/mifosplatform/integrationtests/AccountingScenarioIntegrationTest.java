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

    private final Float LP_PRINCIPAL=10000.0f;
    private final String LP_REPAYMENTS = "5";
    private final String LP_REPAYMENT_PERIOD = "2";
    private final String LP_INTEREST_RATE="1";
    private final String EXPECTED_DISBURSAL_DATE="04 March 2011";
    private final String LOAN_APPLICATION_SUBMISSION_DATE= "3 March 2011";
    private final String LOAN_TERM_FREQUENCY= "10";
    private final String INDIVIDUAL_LOAN = "individual";

    private final String REPAYMENT_DATE[]={"","04 May 2011","04 July 2011","04 September 2011","04 November 2011","04 January 2012"};
    private final Float REPAYMENT_AMOUNT [] = {.0f,2200.0f,3000.0f,900.0f,2000.0f,2500.0f};

    private final Float AMOUNT_TO_BE_WAIVE = 400.0f;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;


    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(requestSpec,responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    }

    @Test
    public void checkAccountingFlow() {
        Account assetAccount  = accountHelper.createAssetAccount();
        Account incomeAccount = accountHelper.createIncomeAccount();
        Account expenseAccount= accountHelper.createExpenseAccount();

        Integer loanProductID = createLoanProduct(assetAccount,incomeAccount,expenseAccount);

        Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);
        Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.disburseLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);


        //CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST =1000.0f;
        JournalEntry[] assetAccountInitialEntry =
                                 { new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.DEBIT),
                                   new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                                   new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT),
        };
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);
        System.out.println("CHECKING INCOME: ******************************************");
        JournalEntry incomeJournalEntry = new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.CREDIT);
        journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount,EXPECTED_DISBURSAL_DATE, incomeJournalEntry);

        //MAKE 1
        System.out.println("Repayment 1 ......");
        loanTransactionHelper.makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        float FIRST_INTEREST = 200.0f;
        float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = LP_PRINCIPAL- PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        JournalEntry[] assetAccountFirstEntry=
                              { new JournalEntry(REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                                new JournalEntry(FIRST_INTEREST, JournalEntry.TransactionType.CREDIT),
                                new JournalEntry(FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
        };
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1],assetAccountFirstEntry);
        System.out.println("Repayment 1 Done......");

        //REPAYMENT 2
        System.out.println("Repayment 2 ......");
        loanTransactionHelper.makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        float SECOND_AND_THIRD_INTEREST = 400.0f;
        float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2]-SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(2,expected_value,loanID);
        JournalEntry[] assetAccountSecondEntry = { new JournalEntry(REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                                                   new JournalEntry(SECOND_AND_THIRD_INTEREST, JournalEntry.TransactionType.CREDIT),
                                                   new JournalEntry(SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
        };
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], assetAccountSecondEntry);
        System.out.println("Repayment 2 Done ......");

        //WAIVE INTEREST
        System.out.println("Waive Interest  ......");
        loanTransactionHelper.waiveInterest(REPAYMENT_DATE[4], AMOUNT_TO_BE_WAIVE.toString(), loanID);

        JournalEntry waivedEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4],waivedEntry);

        JournalEntry expenseJournalEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, REPAYMENT_DATE[4], expenseJournalEntry);
        System.out.println("Waive Interest Done......");

        //REPAYMENT 3
        System.out.println("Repayment 3 ......");
        loanTransactionHelper.makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        JournalEntry[] assetAccountThirdEntry = {
                new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT)
        };
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], assetAccountThirdEntry);
        System.out.println("Repayment 3 Done ......");

        //REPAYMENT 4
        System.out.println("Repayment 4 ......");
        loanTransactionHelper.makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        JournalEntry[] assetAccountFourthEntry = { new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                                                   new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT)
        };
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount,REPAYMENT_DATE[4],assetAccountFourthEntry);
        System.out.println("Repayment 4 Done  ......");

        //Repayment 5
        System.out.println("Repayment 5 ......");
        JournalEntry[] assetAccountFifthEntry = { new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                                                  new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT)
        };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        loanTransactionHelper.makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        loanTransactionHelper.verifyRepaymentScheduleEntryFor(5,expected_value,loanID);
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], assetAccountFifthEntry);
        System.out.println("Repayment 5 Done  ......");
    }

    private Integer createLoanProduct(final Account ... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRuleAsAccrualBased(accounts)
                .build();
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withLoanTermFrequency(LOAN_TERM_FREQUENCY)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(LP_REPAYMENTS).withRepaymentEveryAfter(LP_REPAYMENT_PERIOD)
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(LP_INTEREST_RATE).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE)
                .withLoanType(INDIVIDUAL_LOAN)
                .build(clientID.toString(), loanProductID.toString());
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}

