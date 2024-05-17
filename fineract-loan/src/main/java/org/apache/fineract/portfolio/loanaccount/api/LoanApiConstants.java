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

    String emiAmountParameterName = "fixedEmiAmount";
    String maxOutstandingBalanceParameterName = "maxOutstandingLoanBalance";
    String disbursementDataParameterName = "disbursementData";
    String expectedDisbursementDateParameterName = "expectedDisbursementDate";
    String disbursementPrincipalParameterName = "principal";
    String principalParameterName = "principal";
    String disbursementReversedParameterName = "reversed";
    String disbursementNetDisbursalAmountParameterName = "netDisbursalAmount";
    String updatedDisbursementDateParameterName = "updatedExpectedDisbursementDate";
    String updatedDisbursementPrincipalParameterName = "updatedPrincipal";
    String disbursementIdParameterName = "id";
    String loanChargeIdParameterName = "loanChargeId";
    String chargeIdParameterName = "chargeId";
    String amountParameterName = "amount";
    String chargeTimeTypeParameterName = "chargeTimeType";
    String chargeCalculationTypeParameterName = "chargeCalculationType";
    String principalDisbursedParameterName = "transactionAmount";
    String chargesParameterName = "charges";
    String loanIdTobeApproved = "loanId";
    String postDatedChecks = "postDatedChecks";

    String approvedLoanAmountParameterName = "approvedLoanAmount";
    String approvedOnDateParameterName = "approvedOnDate";
    String noteParameterName = "note";
    String localeParameterName = "locale";
    String dateFormatParameterName = "dateFormat";
    String rejectedOnDateParameterName = "rejectedOnDate";
    String withdrawnOnDateParameterName = "withdrawnOnDate";

    String transactionProcessingStrategyCodeParameterName = "transactionProcessingStrategyCode";
    String loanPurposeIdParameterName = "loanPurposeId";
    String loanOfficerIdParameterName = "loanOfficerId";
    String fundIdParameterName = "fundId";
    String externalIdParameterName = "externalId";
    String accountNoParameterName = "accountNo";
    String productIdParameterName = "productId";
    String calendarIdParameterName = "calendarId";
    String loanTypeParameterName = "loanType";
    String groupIdParameterName = "groupId";
    String clientIdParameterName = "clientId";
    String idParameterName = "id";
    String graceOnInterestChargedParameterName = "graceOnInterestCharged";
    String graceOnInterestPaymentParameterName = "graceOnInterestPayment";
    String graceOnPrincipalPaymentParameterName = "graceOnPrincipalPayment";
    String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
    String interestRateFrequencyTypeParameterName = "interestRateFrequencyType";
    String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
    String interestTypeParameterName = "interestType";
    String amortizationTypeParameterName = "amortizationType";
    String amortizationTypeOptionsParameterName = "amortizationTypeOptions";
    String repaymentFrequencyTypeParameterName = "repaymentFrequencyType";
    String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
    String loanTermFrequencyParameterName = "loanTermFrequency";
    String numberOfRepaymentsParameterName = "numberOfRepayments";
    String repaymentEveryParameterName = "repaymentEvery";
    String interestRatePerPeriodParameterName = "interestRatePerPeriod";
    String inArrearsToleranceParameterName = "inArrearsTolerance";
    String interestChargedFromDateParameterName = "interestChargedFromDate";
    String submittedOnDateParameterName = "submittedOnDate";
    String submittedOnNoteParameterName = "submittedOnNote";
    String collateralParameterName = "collateral";
    String collateralsParameterName = "collaterals";
    String clientCollateralIdParameterName = "clientCollateralId";

    String quantityParameterName = "quantity";
    String syncDisbursementWithMeetingParameterName = "syncDisbursementWithMeeting";
    String linkAccountIdParameterName = "linkAccountId";
    String createStandingInstructionAtDisbursementParameterName = "createStandingInstructionAtDisbursement";
    String daysInYearTypeParameterName = "daysInYearType";
    String daysInMonthTypeParameterName = "daysInMonthType";

    String MULTIDISBURSE_DETAILS_PARAMNAME = "multiDisburseDetails";
    String EMI_AMOUNT_VARIATIONS_PARAMNAME = "emiAmountVariations";
    String COLLECTION_PARAMNAME = "collection";

    // Interest recalculation related
    String isInterestRecalculationEnabledParameterName = "isInterestRecalculationEnabled";
    String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    String repaymentFrequencyNthDayTypeParameterName = "repaymentFrequencyNthDayType";
    String repaymentFrequencyDayOfWeekTypeParameterName = "repaymentFrequencyDayOfWeekType";

    // Floating interest rate related
    String interestRateDifferentialParameterName = "interestRateDifferential";
    String isFloatingInterestRateParameterName = "isFloatingInterestRate";

    // Error codes
    String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE = "loancharge.with.calculation.type.interest.not.allowed";
    String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE = "loancharge.with.calculation.type.principal.not.allowed";
    String DISBURSEMENT_DATE_START_WITH_ERROR = "first.disbursement.date.must.start.with.expected.disbursement.date";
    String PRINCIPAL_AMOUNT_SHOULD_BE_SAME = "sum.of.multi.disburse.amounts.must.equal.with.total.principal";
    String DISBURSEMENT_DATE_UNIQUE_ERROR = "disbursement.date.must.be.unique.for.tranches";
    String ALREADY_DISBURSED = "can.not.change.disbursement.date";
    String APPROVED_AMOUNT_IS_LESS_THAN_SUM_OF_TRANCHES = "sum.of.multi.disburse.amounts.must.be.equal.to.or.lesser.than.approved.principal";
    String DISBURSEMENT_DATES_NOT_IN_ORDER = "disbursements.should.be.ordered.based.on.their.disbursement.dates";
    String DISBURSEMENT_DATE_BEFORE_ERROR = "disbursement.date.of.tranche.cannot.be.before.expected.disbursement.date";
    String LOAN_COLLATERAL_TOTAL_VALUE_SHOULD_BE_SUFFICIENT = "sum.of.multi.collateral.values.must.equal.or.greater.than.principal.amount";

    String isFloatingInterestRate = "isFloatingInterestRate";
    String interestRateDifferential = "interestRateDifferential";

    String exceptionParamName = "exceptions";
    String modifiedinstallmentsParamName = "modifiedinstallments";
    String newinstallmentsParamName = "newinstallments";
    String deletedinstallmentsParamName = "deletedinstallments";
    String dueDateParamName = "dueDate";
    String modifiedDueDateParamName = "modifiedDueDate";
    String principalParamName = "principal";
    String parentAccountParamName = "isParentAccount";
    String totalLoanParamName = "totalLoan";
    String installmentAmountParamName = "installmentAmount";
    // loan write off
    String WRITEOFFREASONS = "WriteOffReasons";
    // loan charge-off
    String CHARGE_OFF_REASONS = "ChargeOffReasons";
    // fore closure constants
    String transactionDateParamName = "transactionDate";
    String noteParamName = "note";

    String canUseForTopup = "canUseForTopup";
    String clientActiveLoanOptions = "clientActiveLoanOptions";
    String isTopup = "isTopup";
    String loanIdToClose = "loanIdToClose";
    String topupAmount = "topupAmount";

    String datatables = "datatables";

    String isEqualAmortizationParam = "isEqualAmortization";
    String applicationId = "applicationId";
    String lastApplication = "lastApplication";

    String fixedPrincipalPercentagePerInstallmentParamName = "fixedPrincipalPercentagePerInstallment";

    String LOAN_ASSOCIATIONS_ALL = "all";
    String chargeOffReasonIdParamName = "chargeOffReasonId";

    // Reversal Transation Data
    String REVERSAL_EXTERNAL_ID_PARAMNAME = "reversalExternalId";
    String REVERSED_ON_DATE_PARAMNAME = "reversedOnDate";

    String DISALLOW_EXPECTED_DISBURSEMENTS = "disallowExpectedDisbursements";

    String TRANSACTION_AMOUNT_PARAMNAME = "transactionAmount";
    String PAYMENT_TYPE_PARAMNAME = "paymentTypeId";
    String FRAUD_ATTRIBUTE_NAME = "fraud";

    // Commands
    String CHARGEBACK_TRANSACTION_COMMAND = "chargeback";
    String MARK_AS_FRAUD_COMMAND = "markAsFraud";

    // Data Validator names
    String LOAN_FRAUD_DATAVALIDATOR_PREFIX = "loans.fraud";

    String BALLOON_REPAYMENT_AMOUNT_PARAMNAME = "balloonRepaymentAmount";

}
