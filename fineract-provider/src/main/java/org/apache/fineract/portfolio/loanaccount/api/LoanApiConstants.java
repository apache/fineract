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
package org.apache.fineract.portfolio.loanaccount.api;

public interface LoanApiConstants {

    public static final String emiAmountParameterName = "fixedEmiAmount";
    public static final String maxOutstandingBalanceParameterName = "maxOutstandingLoanBalance";
    public static final String disbursementDataParameterName = "disbursementData";
    public static final String disbursementDateParameterName = "expectedDisbursementDate";
    public static final String disbursementPrincipalParameterName = "principal";
    public static final String updatedDisbursementDateParameterName = "updatedExpectedDisbursementDate";
    public static final String updatedDisbursementPrincipalParameterName = "updatedPrincipal";
    public static final String disbursementIdParameterName = "id";
    public static final String loanChargeIdParameterName = "loanChargeId";
    public static final String principalDisbursedParameterName = "transactionAmount";
    public static final String chargesParameterName = "charges";

    public static final String approvedLoanAmountParameterName = "approvedLoanAmount";
    public static final String approvedOnDateParameterName = "approvedOnDate";
    public static final String noteParameterName = "note";
    public static final String localeParameterName = "locale";
    public static final String dateFormatParameterName = "dateFormat";
    public static final String rejectedOnDateParameterName = "rejectedOnDate";
    public static final String withdrawnOnDateParameterName = "withdrawnOnDate";

    public static final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
    public static final String loanPurposeIdParameterName = "loanPurposeId";
    public static final String loanOfficerIdParameterName = "loanOfficerId";
    public static final String fundIdParameterName = "fundId";
    public static final String externalIdParameterName = "externalId";
    public static final String accountNoParameterName = "accountNo";
    public static final String productIdParameterName = "productId";
    public static final String calendarIdParameterName = "calendarId";
    public static final String loanTypeParameterName = "loanType";
    public static final String groupIdParameterName = "groupId";
    public static final String clientIdParameterName = "clientId";
    public static final String idParameterName = "id";
    public static final String graceOnInterestChargedParameterName = "graceOnInterestCharged";
    public static final String graceOnInterestPaymentParameterName = "graceOnInterestPayment";
    public static final String graceOnPrincipalPaymentParameterName = "graceOnPrincipalPayment";
    public static final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
    public static final String interestRateFrequencyTypeParameterName = "interestRateFrequencyType";
    public static final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
    public static final String interestTypeParameterName = "interestType";
    public static final String amortizationTypeParameterName = "amortizationType";
    public static final String repaymentFrequencyTypeParameterName = "repaymentFrequencyType";
    public static final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
    public static final String loanTermFrequencyParameterName = "loanTermFrequency";
    public static final String numberOfRepaymentsParameterName = "numberOfRepayments";
    public static final String repaymentEveryParameterName = "repaymentEvery";
    public static final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
    public static final String inArrearsToleranceParameterName = "inArrearsTolerance";
    public static final String interestChargedFromDateParameterName = "interestChargedFromDate";
    public static final String submittedOnDateParameterName = "submittedOnDate";
    public static final String submittedOnNoteParameterName = "interestChargedFromDate";
    public static final String collateralParameterName = "collateral";
    public static final String syncDisbursementWithMeetingParameterName = "syncDisbursementWithMeeting";
    public static final String linkAccountIdParameterName = "linkAccountId";
    public static final String createStandingInstructionAtDisbursementParameterName = "createStandingInstructionAtDisbursement";
    public static final String daysInYearTypeParameterName = "daysInYearType";
    public static final String daysInMonthTypeParameterName = "daysInMonthType";

    // Interest recalculation related
    public static final String isInterestRecalculationEnabledParameterName = "isInterestRecalculationEnabled";
    public static final String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    public static final String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    public static final String repaymentFrequencyNthDayTypeParameterName = "repaymentFrequencyNthDayType";
    public static final String repaymentFrequencyDayOfWeekTypeParameterName = "repaymentFrequencyDayOfWeekType";

    // Floating interest rate related
    public static final String interestRateDifferentialParameterName = "interestRateDifferential";
    public static final String isFloatingInterestRateParameterName = "isFloatingInterestRate";

    // Error codes
    public static final String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE = "loancharge.with.calculation.type.interest.not.allowed";
    public static final String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE = "loancharge.with.calculation.type.principal.not.allowed";
    public static final String DISBURSEMENT_DATE_START_WITH_ERROR = "first.disbursement.date.must.start.with.expected.disbursement.date";
    public static final String PRINCIPAL_AMOUNT_SHOULD_BE_SAME = "sum.of.multi.disburse.amounts.must.equal.with.total.principal";
    public static final String DISBURSEMENT_DATE_UNIQUE_ERROR = "disbursement.date.must.be.unique.for.tranches";
    public static final String ALREADY_DISBURSED = "can.not.change.disbursement.date";
    public static final String APPROVED_AMOUNT_IS_LESS_THAN_SUM_OF_TRANCHES = "sum.of.multi.disburse.amounts.must.be.equal.to.or.lesser.than.approved.principal";
    public static final String DISBURSEMENT_DATES_NOT_IN_ORDER = "disbursements.should.be.ordered.based.on.their.disbursement.dates";
    public static final String DISBURSEMENT_DATE_BEFORE_ERROR = "disbursement.date.of.tranche.cannot.be.before.expected.disbursement.date";

    public static final String isFloatingInterestRate = "isFloatingInterestRate";
    public static final String interestRateDifferential = "interestRateDifferential";

    public static final String exceptionParamName = "exceptions";
    public static final String modifiedinstallmentsParamName = "modifiedinstallments";
    public static final String newinstallmentsParamName = "newinstallments";
    public static final String deletedinstallmentsParamName = "deletedinstallments";
    public static final String dueDateParamName = "dueDate";
    public static final String modifiedDueDateParamName = "modifiedDueDate";
    public static final String principalParamName = "principal";
    public static final String installmentAmountParamName = "installmentAmount";
    //loan write off
    public static final String WRITEOFFREASONS = "WriteOffReasons";

    // fore closure constants
    public static final String transactionDateParamName = "transactionDate";
    public static final String noteParamName = "note";

    public static final String canUseForTopup = "canUseForTopup";
    public static final String clientActiveLoanOptions = "clientActiveLoanOptions";
    public static final String isTopup = "isTopup";
    public static final String loanIdToClose = "loanIdToClose";
    public static final String topupAmount = "topupAmount";

    public static final String datatables = "datatables";
    
    public static final String isEqualAmortizationParam = "isEqualAmortization";
}
