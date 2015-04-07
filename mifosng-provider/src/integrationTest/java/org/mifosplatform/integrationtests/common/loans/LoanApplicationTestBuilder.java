/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.loans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public static final String MIFOS_STANDARD_STRATEGY = "1";
    public static final String RBI_INDIA_STRATEGY = "4";

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
    private String transactionProcessingID = MIFOS_STANDARD_STRATEGY;
    private String expectedDisbursmentDate = "";
    private String submittedOnDate = "";
    private String loanType = "individual";
    private String fixedEmiAmount = "10000";
    private String maxOutstandingLoanBalance = "36000";
    private String graceOnPrincipalPayment = null;
    private String graceOnInterestPayment = null;
    private List<HashMap> disbursementData = null;
    private List<HashMap> charges = new ArrayList<HashMap>();
    private String recalculationRestFrequencyDate = null;
    private String repaymentsStartingFromDate = null;

    public String build(final String ID, final String loanProductId, final String savingsID) {

        final HashMap<String, Object> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        if (this.loanType == "group") {
            map.put("groupId", ID);
        } else {
            map.put("clientId", ID);
        }
        map.put("productId", loanProductId);
        map.put("principal", this.principal);
        map.put("loanTermFrequency", this.loanTermFrequency);
        map.put("loanTermFrequencyType", this.loanTermFrequencyType);
        map.put("numberOfRepayments", this.numberOfRepayment);
        map.put("repaymentEvery", this.repaymentPeriod);
        map.put("repaymentFrequencyType", this.repaymentFrequencyType);
        map.put("interestRatePerPeriod", this.interestRate);
        map.put("amortizationType", this.amortizationType);
        map.put("interestType", this.interestType);
        map.put("interestCalculationPeriodType", this.interestCalculationPeriodType);
        map.put("transactionProcessingStrategyId", this.transactionProcessingID);
        map.put("expectedDisbursementDate", this.expectedDisbursmentDate);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("loanType", this.loanType);
        if(repaymentsStartingFromDate != null){
            map.put("repaymentsStartingFromDate", this.repaymentsStartingFromDate);
        }
        if (charges != null) {
            map.put("charges", charges);
        }
        if (savingsID != null) {
            map.put("linkAccountId", savingsID);
        }

        if (graceOnPrincipalPayment != null) {
            map.put("graceOnPrincipalPayment", graceOnPrincipalPayment);
        }

        if (graceOnInterestPayment != null) {
            map.put("graceOnInterestPayment", graceOnInterestPayment);
        }

        if (disbursementData != null) {
            map.put("disbursementData", disbursementData);
            map.put("fixedEmiAmount", fixedEmiAmount);
            map.put("maxOutstandingLoanBalance", maxOutstandingLoanBalance);

        }
        if (recalculationRestFrequencyDate != null) {
            map.put("recalculationRestFrequencyDate", recalculationRestFrequencyDate);
        }
        System.out.println("Loan Application request : " + map);
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

    public LoanApplicationTestBuilder withRepaymentFrequencyTypeAsYear() {
        this.repaymentFrequencyType = YEARS;
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

    public LoanApplicationTestBuilder withCharges(final List<HashMap> charges) {
        this.charges = charges;
        return this;
    }

    public LoanApplicationTestBuilder withLoanType(final String loanType) {
        this.loanType = loanType;
        return this;
    }

    public LoanApplicationTestBuilder withPrincipalGrace(final String graceOnPrincipalPayment) {
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        return this;
    }

    public LoanApplicationTestBuilder withInterestGrace(final String graceOnInterestPayment) {
        this.graceOnInterestPayment = graceOnInterestPayment;
        return this;
    }

    public LoanApplicationTestBuilder withTranches(final List<HashMap> disbursementData) {
        this.disbursementData = disbursementData;
        return this;
    }

    public LoanApplicationTestBuilder withwithRepaymentStrategy(final String transactionProcessingStrategy) {
        this.transactionProcessingID = transactionProcessingStrategy;
        return this;
    }

    public LoanApplicationTestBuilder withRestFrequencyDate(final String recalculationRestFrequencyDate) {
        this.recalculationRestFrequencyDate = recalculationRestFrequencyDate;
        return this;
    }
    
    public LoanApplicationTestBuilder withFirstRepaymentDate(final String firstRepaymentDate) {
        this.repaymentsStartingFromDate = firstRepaymentDate;
        return this;
    }
}
