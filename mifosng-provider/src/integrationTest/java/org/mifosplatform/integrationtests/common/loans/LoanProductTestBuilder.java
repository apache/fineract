package org.mifosplatform.integrationtests.common.loans;

import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.accounting.Account;

import com.google.gson.Gson;

public class LoanProductTestBuilder {

    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "2";
    private static final String INR = "INR";
    private static final String DAYS = "0";
    private static final String WEEK = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD = "1";
    private static final String EQUAL_PRINCIPAL_PAYMENTS = "0";
    private static final String EQUAL_INSTALLMENTS = "1";
    private static final String DECLINING_BALANCE = "0";
    private static final String FLAT_BALANCE = "1";
    private static final String MIFOS_STANDARD_STRATEGY = "1";
    // private static final String HEAVENS_FAMILY_STRATEGY ="2";
    // private static final String CREO_CORE_STRATEGY ="3";
    // private static final String RBI_INDIA_STRATEGY ="4";
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_BASED = "3";

    private String nameOfLoanProduct = ClientHelper.randomNameGenerator("LOAN_PRODUCT_", 6);
    private String principal = "10000.00";
    private String numberOfRepayments = "5";
    private String repaymentFrequency = MONTHS;
    private String repaymentPeriod = "1";
    private String interestRatePerPeriod = "2";
    private String interestRateFrequencyType = MONTHS;
    private String interestType = FLAT_BALANCE;
    private String interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private String inArrearsTolerance = "0";
    private final String transactionProcessingStrategy = MIFOS_STANDARD_STRATEGY;
    private String accountingRule = NONE;
    private final String currencyCode = INR;
    private String amortizationType = EQUAL_INSTALLMENTS;
    private String minPrincipal = "1000.00";
    private String maxPrincipal = "100000.00";
    private Account[] accountList = null;

    public String build() {
        final HashMap<String, String> map = new HashMap<String, String>();

        map.put("name", this.nameOfLoanProduct);
        map.put("currencyCode", this.currencyCode);
        map.put("locale", LOCALE);
        map.put("digitsAfterDecimal", DIGITS_AFTER_DECIMAL);
        map.put("principal", this.principal);
        map.put("numberOfRepayments", this.numberOfRepayments);
        map.put("repaymentEvery", this.repaymentPeriod);
        map.put("repaymentFrequencyType", this.repaymentFrequency);
        map.put("interestRatePerPeriod", this.interestRatePerPeriod);
        map.put("interestRateFrequencyType", this.interestRateFrequencyType);
        map.put("amortizationType", this.amortizationType);
        map.put("interestType", this.interestType);
        map.put("interestCalculationPeriodType", this.interestCalculationPeriodType);
        map.put("inArrearsTolerance", this.inArrearsTolerance);
        map.put("transactionProcessingStrategyId", this.transactionProcessingStrategy);
        map.put("accountingRule", this.accountingRule);
        map.put("minPrincipal", this.minPrincipal);
        map.put("maxPrincipal", this.maxPrincipal);

        if (this.accountingRule.equals(ACCRUAL_BASED)) {
            map.putAll(getAccountMappingForAccrualBased());
        } else if (this.accountingRule.equals(CASH_BASED)) {
            map.putAll(getAccountMappingForCashBased());
        }
        return new Gson().toJson(map);
    }

    public LoanProductTestBuilder withMinPrincipal(final String minPrincipal) {
        this.minPrincipal = minPrincipal;
        return this;
    }

    public LoanProductTestBuilder withMaxPrincipal(final String maxPrincipal) {
        this.maxPrincipal = maxPrincipal;
        return this;
    }

    public LoanProductTestBuilder withLoanName(final String loanName) {
        this.nameOfLoanProduct = loanName;
        return this;
    }

    public LoanProductTestBuilder withPrincipal(final String principal) {
        this.principal = principal;
        return this;
    }

    public LoanProductTestBuilder withNumberOfRepayments(final String numberOfRepayment) {
        this.numberOfRepayments = numberOfRepayment;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsMonth() {
        this.repaymentFrequency = MONTHS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsWeek() {
        this.repaymentFrequency = WEEK;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsDays() {
        this.repaymentFrequency = DAYS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentAfterEvery(final String repaymentAfterEvery) {
        this.repaymentPeriod = repaymentAfterEvery;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsMonths() {
        this.interestRateFrequencyType = MONTHS;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsYear() {
        this.interestRateFrequencyType = YEARS;
        return this;
    }

    public LoanProductTestBuilder withinterestRatePerPeriod(final String interestRatePerPeriod) {
        this.interestRatePerPeriod = interestRatePerPeriod;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualPrincipalPayment() {
        this.amortizationType = EQUAL_PRINCIPAL_PAYMENTS;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualInstallments() {
        this.amortizationType = EQUAL_INSTALLMENTS;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsFlat() {
        this.interestType = FLAT_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsDecliningBalance() {
        this.interestType = DECLINING_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsDays() {
        this.interestCalculationPeriodType = DAYS;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsRepaymentPeriod() {
        this.interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
        return this;
    }

    public LoanProductTestBuilder withInArrearsTolerance(final String amountCanBeWaved) {
        this.inArrearsTolerance = amountCanBeWaved;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsNone() {
        this.accountingRule = NONE;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsCashBased(final Account[] account_list) {
        this.accountingRule = CASH_BASED;
        this.accountList = account_list;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsAccrualBased(final Account[] account_list) {
        this.accountingRule = ACCRUAL_BASED;
        this.accountList = account_list;
        return this;
    }

    private Map<String, String> getAccountMappingForCashBased() {
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < this.accountList.length; i++) {
            if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("fundSourceAccountId", ID);
                map.put("loanPortfolioAccountId", ID);
                map.put("transfersInSuspenseAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("interestOnLoanAccountId", ID);
                map.put("incomeFromFeeAccountId", ID);
                map.put("incomeFromPenaltyAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("writeOffAccountId", ID);
            }
        }
        return map;
    }

    private Map<String, String> getAccountMappingForAccrualBased() {
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < this.accountList.length; i++) {
            if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("fundSourceAccountId", ID);
                map.put("loanPortfolioAccountId", ID);
                map.put("transfersInSuspenseAccountId", ID);
                map.put("receivableInterestAccountId", ID);
                map.put("receivableFeeAccountId", ID);
                map.put("receivablePenaltyAccountId", ID);

            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("interestOnLoanAccountId", ID);
                map.put("incomeFromFeeAccountId", ID);
                map.put("incomeFromPenaltyAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("writeOffAccountId", ID);
            }
        }

        return map;
    }

}
