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
package org.apache.fineract.integrationtests.common.loans;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanApplicationTestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(LoanApplicationTestBuilder.class);
    private static final String DAYS = "0";
    private static final String WEEKS = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String DECLINING_BALANCE = "0";
    private static final String FLAT_BALANCE = "1";
    private static final String EQUAL_PRINCIPAL_PAYMENTS = "0";
    private static final String EQUAL_INSTALLMENTS = "1";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD = "1";
    private static final String LOCALE = "en_GB";
    public static final String DEFAULT_STRATEGY = "mifos-standard-strategy";
    public static final String RBI_INDIA_STRATEGY = "rbi-india-strategy";
    public static final String INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY = "interest-principal-penalties-fees-order-strategy";
    public static final String DUE_DATE_RESPECTIVE_STRATEGY = "due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy";

    private String externalId = null;
    private String principal = "10,000";
    private String glimPrincipal = "1000";
    private String loanTermFrequency = "";
    private String loanTermFrequencyType = "";
    private String numberOfRepayment = "0";
    private String repaymentPeriod = "0";
    private String repaymentFrequencyType = "";

    private String interestRate = "2";
    private String interestType = FLAT_BALANCE;
    private String amortizationType = EQUAL_PRINCIPAL_PAYMENTS;
    private String interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private String transactionProcessingCode = DEFAULT_STRATEGY;
    private String expectedDisbursmentDate = "";
    private String submittedOnDate = "";
    private String loanType = "individual";
    private String fixedEmiAmount = "10000";
    private String maxOutstandingLoanBalance = "36000";
    private String graceOnPrincipalPayment = null;
    private String graceOnInterestPayment = null;
    @SuppressWarnings("rawtypes")
    private List<HashMap> disbursementData = null;
    @SuppressWarnings("rawtypes")
    private List<HashMap> charges = new ArrayList<>();
    private List<HashMap> collaterals = new ArrayList<>();
    private String repaymentsStartingFromDate = null;
    private String isParentAccount = null;
    private String totalLoan = "0";

    private String calendarId;
    private boolean syncDisbursementWithMeeting = false;
    private List<HashMap<String, Object>> datatables = null;
    private List<Map<String, Object>> approvalFormData = null;
    private String fixedPrincipalPercentagePerInstallment;
    private String interestChargedFromDate;
    private String linkAccountId;
    private String inArrearsTolerance;

    public String build(final String clientID, final String groupID, final String loanProductId, final String savingsID) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupID);
        map.put("clientId", clientID);
        if ("jlg".equals(this.loanType)) {
            if (this.calendarId != null) {
                map.put("calendarId", this.calendarId);
            }
            map.put("syncDisbursementWithMeeting", this.syncDisbursementWithMeeting);
        }

        if ("glim".equals(this.loanType)) {
            if (isParentAccount != null) {
                map.put("isParentAccount", this.isParentAccount);
            }

            if (totalLoan != null) {
                map.put("totalLoan", this.totalLoan);
            }
        }
        return build(map, loanProductId, savingsID);
    }

    public String build(final String id, final String loanProductId, final String savingsID) {

        final HashMap<String, Object> map = new HashMap<>();

        if ("group".equals(this.loanType)) {
            map.put("groupId", id);
        } else {
            map.put("clientId", id);
        }
        return build(map, loanProductId, savingsID);
    }

    public String build() {
        final HashMap<String, Object> map = new HashMap<>();

        if (this.approvalFormData != null) {
            map.put("approvalFormData", this.approvalFormData);
        }

        if (this.glimPrincipal != null) {
            map.put("glimPrincipal", this.glimPrincipal);
        }
        map.put("locale", LOCALE);

        String approvalFormData = new Gson().toJson(map);
        LOG.info("approvalFormData: {} ", approvalFormData);
        return approvalFormData;
    }

    private String build(final HashMap<String, Object> map, final String loanProductId, final String savingsID) {
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("productId", loanProductId);
        map.put("principal", this.principal);
        map.put("loanTermFrequency", this.loanTermFrequency);
        map.put("loanTermFrequencyType", this.loanTermFrequencyType);
        map.put("numberOfRepayments", this.numberOfRepayment);
        map.put("repaymentEvery", this.repaymentPeriod);
        map.put("repaymentFrequencyType", this.repaymentFrequencyType);
        map.put("interestRatePerPeriod", this.interestRate);
        map.put("amortizationType", this.amortizationType);
        map.put("fixedPrincipalPercentagePerInstallment", fixedPrincipalPercentagePerInstallment);
        map.put("interestType", this.interestType);
        map.put("interestCalculationPeriodType", this.interestCalculationPeriodType);
        map.put("transactionProcessingStrategyCode", this.transactionProcessingCode);
        map.put("expectedDisbursementDate", this.expectedDisbursmentDate);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("loanType", this.loanType);
        map.put("collateral", this.collaterals);
        map.put("interestChargedFromDate", this.interestChargedFromDate);

        if (this.externalId != null) {
            map.put("externalId", this.externalId);
        }

        if (repaymentsStartingFromDate != null) {
            map.put("repaymentsStartingFromDate", this.repaymentsStartingFromDate);
        }
        if (charges != null) {
            map.put("charges", charges);
        }

        if (savingsID != null) {
            map.put("linkAccountId", savingsID);
        }

        if (this.linkAccountId != null) {
            map.put("linkAccountId", this.linkAccountId);
        }

        if (this.inArrearsTolerance != null) {
            map.put("inArrearsTolerance", this.inArrearsTolerance);
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
        }
        map.put("maxOutstandingLoanBalance", maxOutstandingLoanBalance);

        if (datatables != null) {
            map.put("datatables", this.datatables);
        }
        LOG.info("Loan Application request : {} ", map);
        return new Gson().toJson(map);
    }

    public LoanApplicationTestBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
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

    public LoanApplicationTestBuilder withLinkedAccount(String linkAccountId) {
        this.linkAccountId = linkAccountId;
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

    public LoanApplicationTestBuilder withCollaterals(final List<HashMap> collaterals) {
        this.collaterals = collaterals;
        return this;
    }

    public LoanApplicationTestBuilder withLoanType(final String loanType) {
        this.loanType = loanType;
        return this;
    }

    public LoanApplicationTestBuilder withtotalLoan(final String totalLoan) {
        this.totalLoan = totalLoan;
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

    public LoanApplicationTestBuilder withRepaymentStrategy(final String transactionProcessingStrategyCode) {
        this.transactionProcessingCode = transactionProcessingStrategyCode;
        return this;
    }

    public LoanApplicationTestBuilder withFirstRepaymentDate(final String firstRepaymentDate) {
        this.repaymentsStartingFromDate = firstRepaymentDate;
        return this;
    }

    public LoanApplicationTestBuilder withParentAccount(final String parentAccount) {
        this.isParentAccount = parentAccount;
        return this;
    }

    public LoanApplicationTestBuilder withApprovalFormData(final List<Map<String, Object>> approvalFormData) {
        this.approvalFormData = new ArrayList<>();
        this.approvalFormData.addAll(approvalFormData);
        return this;
    }

    /**
     * calendarID parameter is used to sync repayments with group meetings, especially when using jlg loans
     *
     * @param calendarId
     *            the id of the calender record of the group meeting from m_calendar table
     * @return
     */
    public LoanApplicationTestBuilder withCalendarID(String calendarId) {
        this.calendarId = calendarId;
        return this;
    }

    /**
     * This indicator is used mainly for jlg loans when we want to sync disbursement with the group meetings (it seems
     * that if we do use this parameter we should also use calendarID to sync repayment with group meetings)
     *
     * @return
     */
    public LoanApplicationTestBuilder withSyncDisbursementWithMeetin() {
        this.syncDisbursementWithMeeting = true;
        return this;
    }

    public LoanApplicationTestBuilder withFixedEmiAmount(final String installmentAmount) {
        this.fixedEmiAmount = installmentAmount;
        return this;
    }

    public LoanApplicationTestBuilder withDatatables(final List<HashMap<String, Object>> datatables) {
        this.datatables = datatables;
        return this;
    }

    public LoanApplicationTestBuilder withPrinciplePercentagePerInstallment(String fixedPrincipalPercentagePerInstallment) {
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
        return this;
    }

    public LoanApplicationTestBuilder withinterestChargedFromDate(String interestChargedFromDate) {
        this.interestChargedFromDate = interestChargedFromDate;
        return this;
    }

    public LoanApplicationTestBuilder withInArrearsTolerance(String amount) {
        this.inArrearsTolerance = amount;
        return this;
    }
}
