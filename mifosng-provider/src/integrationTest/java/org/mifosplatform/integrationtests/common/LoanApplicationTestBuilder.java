package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;

import java.util.HashMap;


public class LoanApplicationTestBuilder {
    private static final String DAYS = "0";
    private static final String WEEKS = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String DECLINING_BALANCE = "0";
    private static final String FLAT_BALANCE = "1";
    private static final String EQUAL_PRINCIPLE_PAYMENTS="0";
    private static final String EQUAL_INSTALLMENTS="1";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD="1";
    private static final String MIFOS_STANDARD_STRATEGY ="1";


    private String principal = "10,000";
    private String clientID="0";
    private String loanProductId = "0";
    private String loanTermFrequency = "";
    private String loanTermFrequencyType="";       //0=Days, 1=Weeks, 2=Months, 3=Years
    private String numberOfRepayment = "0";
    private String repaymentPeriod = "0";
    private String repaymentFrequencyType = "";    //0=Days, 1=Weeks, 2=Months

                                                    // 12 %             per Year                 Declining Balance
                                                    // interest rate    per Interest_frequency   interest type
    private String interestRate  = "2";
    private String interestRateFrequencyType="0";       //2=Per month, 3=Per year
    private String interestType= "";                    //0=Declining Balance, 1=Flat
    private String amortizationType ="";                //0=Equal principle payments, 1=Equal installments
    private String interestCalculationPeriodType =CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private String transactionProcessingID= MIFOS_STANDARD_STRATEGY;
    private String expectedDisbursmentDate="";
    private String submittedOndate ="";

    public String Build(String clientID, String loanProductId){
        this.clientID = clientID;
        this.loanProductId = loanProductId;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("clientId", clientID);
        map.put("productId",loanProductId);
        map.put("principal", principal);
        map.put("loanTermFrequency",loanTermFrequency);
        map.put("loanTermFrequencyType", "2");
        map.put("numberOfRepayments", numberOfRepayment);
        map.put("repaymentEvery", repaymentPeriod);
        map.put("repaymentFrequencyType", repaymentFrequencyType);
        map.put("interestRateFrequencyType", interestRateFrequencyType);
        map.put("interestRatePerPeriod", interestRate);
        map.put("amortizationType", amortizationType);
        map.put("interestType", interestType);
        map.put("interestCalculationPeriodType", interestCalculationPeriodType);
        map.put("transactionProcessingStrategyId", transactionProcessingID);
        map.put("expectedDisbursementDate", expectedDisbursmentDate);
        map.put("submittedOnDate", submittedOndate);
        return new Gson().toJson(map);
    }

    public LoanApplicationTestBuilder withPrincipal(String principalAmount){
        this.principal = principalAmount;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequency(String loanToBePayedDuration){
        this.loanTermFrequency= loanToBePayedDuration;
        return this;
    }

    public LoanApplicationTestBuilder withLoanTermFrequencyAsDays(){
        this.loanTermFrequencyType=DAYS;
        return this;
    }
    public LoanApplicationTestBuilder withLoanTermFrequencyAsMonths(){
        this.loanTermFrequencyType=MONTHS;
        return this;
    }
    public LoanApplicationTestBuilder withLoanTermFrequencyAsWeeks(){
        this.loanTermFrequencyType=WEEKS;
        return this;
    }
    public LoanApplicationTestBuilder withLoanTermFrequencyAsYears(){
        this.loanTermFrequencyType=YEARS;
        return this;
    }
    public LoanApplicationTestBuilder withNumberOfRepayments(String numberOfRepayments){
        this.numberOfRepayment= numberOfRepayments;
        return this;
    }
    public LoanApplicationTestBuilder withRepaymentEveryAfter(String repaymentPeriod){
        this.repaymentPeriod=repaymentPeriod;
        return this;
    }
    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsDays(){
        this.repaymentFrequencyType = DAYS;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsMonths(){
        this.repaymentFrequencyType = MONTHS;
        return this;
    }

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsWeeks(){
        this.repaymentFrequencyType = WEEKS;
        return this;
    }

    public LoanApplicationTestBuilder withInterestRatePerPeriod(String interestRate){
        this.interestRate=interestRate;
        return this;
    }
    public LoanApplicationTestBuilder withInterestRateFrequencyTypeAsMonths(){
        this.interestRateFrequencyType=MONTHS;
        return this;
    }
    public LoanApplicationTestBuilder withInterestRateFrequencyTypeAsYears(){
        this.interestRateFrequencyType=YEARS;
        return this;
    }
    public LoanApplicationTestBuilder withInterestTypeAsFlatBalance(){
        this.interestType=FLAT_BALANCE;
        return this;
    }
    public LoanApplicationTestBuilder withInterestTypeAsDecliningBalance(){
        this.interestType=DECLINING_BALANCE;
        return this;
    }

    public LoanApplicationTestBuilder withAmortizationTypeAsEqualInstallments(){
        this.amortizationType=EQUAL_INSTALLMENTS;
        return this;
    }

    public LoanApplicationTestBuilder withAmortizationTypeAsEqualPrincipalPayments(){
        this.amortizationType=EQUAL_PRINCIPLE_PAYMENTS;
        return this;
    }

    public LoanApplicationTestBuilder withInterestCalculationPeriodTypeSameAsRepaymentPeriod(){
        this.interestCalculationPeriodType=CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
        return this;
    }

    public LoanApplicationTestBuilder withInterestCalculationPeriodTypeAsDays(){
        this.interestCalculationPeriodType=DAYS;
        return this;
    }
    public LoanApplicationTestBuilder withExpectedDisbursementDate(String expectedDisbursementDate){
        this.expectedDisbursmentDate=expectedDisbursementDate;
        return this;
    }
    public LoanApplicationTestBuilder withSubmittedOnDate(String loanApplicationSubmittedDate){
        this.submittedOndate=loanApplicationSubmittedDate;
        return this;
    }
}
