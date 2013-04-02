package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;

import java.util.HashMap;

public class LoanProductTestBuilder {
    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "2";
    private static final String INR = "INR";
    private static final String DAYS = "0";
    private static final String WEEK = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD="1";
    private static final String EQUAL_PRINCIPLE_PAYMENTS="0";
    private static final String EQUAL_INSTALLMENTS="1";
    private static final String DECLINING_BALANCE="0";
    private static final String FLAT_BALANCE="1";
    private static final String MIFOS_STANDARD_STRATEGY ="1";
    private static final String HEAVENS_FAMILY_STRATEGY ="2";
    private static final String CREO_CORE_STRATEGY ="3";
    private static final String RBI_INDIA_STRATEGY ="4";
    private static final String NONE ="1";
    private static final String CASH_BASED ="2";
    private static final String ACCRUAL_BASED ="3";


    private String nameOfLoanProduct = ClientHelper.randomNameGenerator("LOAN_PRODUCT_", 6);
    private String principal = "10000.00";
    private String numberOfRepayments = "0";
    private String repaymentFrequency = WEEK;
    private String repaymentPeriod = "0";
    private String interestRatePerPeriod= "2";
    private String interestRateFrequencyType = MONTHS;
    private String interestType = FLAT_BALANCE;
    private String interestCalculationPeriodType=CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private String inArrearsTolerance="0";
    private String transactionProcessingStrategy=MIFOS_STANDARD_STRATEGY;
    private String accountingRule =NONE;
    private String currencyCode = INR;
    private String amortizationType= EQUAL_INSTALLMENTS;

    public String build() {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put("name", nameOfLoanProduct);
        map.put("currencyCode", currencyCode);
        map.put("locale", LOCALE);
        map.put("digitsAfterDecimal",DIGITS_AFTER_DECIMAL);
        map.put("principal", principal);
        map.put("numberOfRepayments", numberOfRepayments);
        map.put("repaymentEvery",repaymentPeriod );
        map.put("repaymentFrequencyType", repaymentFrequency);
        map.put("interestRatePerPeriod", interestRatePerPeriod);
        map.put("interestRateFrequencyType", interestRateFrequencyType);
        map.put("amortizationType",amortizationType);
        map.put("interestType", interestType);
        map.put("interestCalculationPeriodType", interestCalculationPeriodType);
        map.put("inArrearsTolerance", inArrearsTolerance);
        map.put("transactionProcessingStrategyId", transactionProcessingStrategy);
        map.put("accountingRule", accountingRule);
        return new Gson().toJson(map);
    }

    public LoanProductTestBuilder withLoanName(final String loanName){
        this.nameOfLoanProduct=loanName;
        return this;
    }
    public LoanProductTestBuilder withPrincipal(final String principal){
        this.principal = principal;
        return this;
    }

    public LoanProductTestBuilder withNumberOfRepayments(final String numberOfRepayment){
        this.numberOfRepayments = numberOfRepayment;
        return this;
    }


    public LoanProductTestBuilder withRepaymentTypeAsMonth(){
        this.repaymentFrequency = MONTHS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsWeek(){
        this.repaymentFrequency = WEEK;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsDays(){
        this.repaymentFrequency = DAYS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentAfterEvery(final String repaymentAfterEvery){
        this.repaymentPeriod=repaymentAfterEvery;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsMonths(){
        this.interestRateFrequencyType=MONTHS;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsYear(){
        this.interestRateFrequencyType=YEARS;
        return this;
    }

    public LoanProductTestBuilder withinterestRatePerPeriod(final String interestRatePerPeriod) {
        this.interestRatePerPeriod=interestRatePerPeriod;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualPrinciplePayment(){
        this.amortizationType = EQUAL_PRINCIPLE_PAYMENTS;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualInstallments(){
        this.amortizationType = EQUAL_INSTALLMENTS;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsFlat(){
        this.interestType=FLAT_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsDecliningBalance (){
        this.interestType=DECLINING_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsDays (){
        this.interestCalculationPeriodType=DAYS;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsRepaymentPeriod (){
        this.interestCalculationPeriodType=CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
        return this;
    }

    public LoanProductTestBuilder withInArrearsTolerance (final String amountCanBeWaved){
        this.inArrearsTolerance = amountCanBeWaved;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsNone (){
        this.accountingRule= NONE;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsCashBased (){
        this.accountingRule= CASH_BASED;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsAccrualBased (){
        this.accountingRule= ACCRUAL_BASED;
        return this;
    }

}
