package org.mifosplatform.integrationtests.common.loans;

import java.util.HashMap;

import com.google.gson.Gson;

public class LoanApplicationTestBuilder {

    private static final String DAYS = "0";
    private static final String WEEKS = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String DECLINING_BALANCE = "0";
    private static final String FLAT_BALANCE = "1";
    private static final String EQUAL_PRINCIPAL_PAYMENTS = "0";
    private static final String EQUAL_INSTALLMENTS = "1";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD = "1";
    private static final String MIFOS_STANDARD_STRATEGY = "1";

    private String principal = "10,000";
    private String loanTermFrequency = "";
    private String loanTermFrequencyType = "";
    private String numberOfRepayment = "0";
    private String repaymentPeriod = "0";
    private String repaymentFrequencyType = "";

    private String interestRate = "2";
    private String interestType = FLAT_BALANCE;
    private String amortizationType = EQUAL_PRINCIPAL_PAYMENTS;
    private String interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private final String transactionProcessingID = MIFOS_STANDARD_STRATEGY;
    private String expectedDisbursmentDate = "";
    private String submittedOnDate = "";
    private String loanType = "individual"; //default it to individual

    public String build(final String clientID, final String loanProductId) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("clientId", clientID);
        map.put("productId", loanProductId);
        map.put("principal", principal);
        map.put("loanTermFrequency", loanTermFrequency);
        map.put("loanTermFrequencyType", loanTermFrequencyType); // FIXME - Should this be using
        map.put("numberOfRepayments", numberOfRepayment);
        map.put("repaymentEvery", repaymentPeriod);
        map.put("repaymentFrequencyType", repaymentFrequencyType);
        map.put("interestRatePerPeriod", interestRate);
        map.put("amortizationType", amortizationType);
        map.put("interestType", interestType);
        map.put("interestCalculationPeriodType", interestCalculationPeriodType);
        map.put("transactionProcessingStrategyId", transactionProcessingID);
        map.put("expectedDisbursementDate", expectedDisbursmentDate);
        map.put("submittedOnDate", submittedOnDate);
        map.put("loanType", "individual");
        return new Gson().toJson(map);
    }

    public LoanApplicationTestBuilder withPrincipal(final String principalAmount) {
        this.principal = principalAmount;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequency(final String loanToBePayedDuration) {
        this.loanTermFrequency = loanToBePayedDuration;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequencyAsDays() {
        this.loanTermFrequencyType = DAYS;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequencyAsMonths() {
        this.loanTermFrequencyType = MONTHS;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequencyAsWeeks() {
        this.loanTermFrequencyType = WEEKS;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequencyAsYears() {
        this.loanTermFrequencyType = YEARS;
        return this;
    }

    public LoanApplicationTestBuilder withNumberOfRepayments(final String numberOfRepayments) {
        this.numberOfRepayment = numberOfRepayments;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentEveryAfter(final String repaymentPeriod) {
        this.repaymentPeriod = repaymentPeriod;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsDays() {
        this.repaymentFrequencyType = DAYS;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsMonths() {
        this.repaymentFrequencyType = MONTHS;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsWeeks() {
        this.repaymentFrequencyType = WEEKS;
        return this;
    }

    public LoanApplicationTestBuilder withInterestRatePerPeriod(final String interestRate) {
        this.interestRate = interestRate;
        return this;
    }

    public LoanApplicationTestBuilder withInterestTypeAsFlatBalance() {
        this.interestType = FLAT_BALANCE;
        return this;
    }

    public LoanApplicationTestBuilder withInterestTypeAsDecliningBalance() {
        this.interestType = DECLINING_BALANCE;
        return this;
    }

    public LoanApplicationTestBuilder withAmortizationTypeAsEqualInstallments() {
        this.amortizationType = EQUAL_INSTALLMENTS;
        return this;
    }

    public LoanApplicationTestBuilder withAmortizationTypeAsEqualPrincipalPayments() {
        this.amortizationType = EQUAL_PRINCIPAL_PAYMENTS;
        return this;
    }

    public LoanApplicationTestBuilder withInterestCalculationPeriodTypeSameAsRepaymentPeriod() {
        this.interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
        return this;
    }

    public LoanApplicationTestBuilder withInterestCalculationPeriodTypeAsDays() {
        this.interestCalculationPeriodType = DAYS;
        return this;
    }

    public LoanApplicationTestBuilder withExpectedDisbursementDate(final String expectedDisbursementDate) {
        this.expectedDisbursmentDate = expectedDisbursementDate;
        return this;
    }

    public LoanApplicationTestBuilder withSubmittedOnDate(final String loanApplicationSubmittedDate) {
        this.submittedOnDate = loanApplicationSubmittedDate;
        return this;
    }

    public LoanApplicationTestBuilder withLoanType(final String loanType){
        this.loanType = loanType;
        return this;
    }

}
