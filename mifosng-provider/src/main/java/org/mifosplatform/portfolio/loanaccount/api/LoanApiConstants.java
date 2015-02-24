/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.api;

public interface LoanApiConstants {

    public static final String emiAmountParameterName = "fixedEmiAmount";
    public static final String maxOutstandingBalanceParameterName = "maxOutstandingLoanBalance";
    public static final String disbursementDataParameterName = "disbursementData";
    public static final String disbursementDateParameterName = "expectedDisbursementDate";
    public static final String disbursementPrincipalParameterName = "principal";
    public static final String updatedDisbursementDateParameterName = "updatedExpectedDisbursementDate";
    public static final String updatedDisbursementPrincipalParameterName = "updatedPrincipal";
    public static final String disbursementIdParameterName = "id";
    public static final String principalDisbursedParameterName = "transactionAmount";

    public static final String approvedLoanAmountParameterName = "approvedLoanAmount";
    public static final String approvedOnDateParameterName = "approvedOnDate";
    public static final String noteParameterName = "note";
    public static final String localeParameterName = "locale";
    public static final String dateFormatParameterName = "dateFormat";
    public static final String rejectedOnDateParameterName = "rejectedOnDate";
    public static final String withdrawnOnDateParameterName = "withdrawnOnDate";

    // Interest recalculation related
    public static final String isInterestRecalculationEnabledParameterName = "isInterestRecalculationEnabled";
    public static final String daysInYearTypeParameterName = "daysInYearType";
    public static final String daysInMonthTypeParameterName = "daysInMonthType";
    public static final String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    public static final String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    
    // Error codes
    public static final String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_INTEREST_CALCULATION_TYPE= "loancharge.with.calculation.type.interest.not.allowed";
    public static final String LOAN_CHARGE_CAN_NOT_BE_ADDED_WITH_PRINCIPAL_CALCULATION_TYPE= "loancharge.with.calculation.type.principal.not.allowed";
    public static final String DISBURSEMENT_DATE_START_WITH_ERROR = "first.disbursement.date.must.start.with.expected.disbursement.date";
    public static final String PRINCIPAL_AMOUNT_SHOULD_BE_SAME = "sum.of.multi.disburse.amounts.must.equal.with.total.principal";
    public static final String DISBURSEMENT_DATE_UNIQUE_ERROR = "disbursement.date.must.be.unique.for.tranches";
    public static final String ALREADY_DISBURSED = "can.not.change.disbursement.date";
    public static final String APPROVED_AMOUNT_IS_LESS_THAN_SUM_OF_TRANCHES = "sum.of.multi.disburse.amounts.must.be.equal.to.or.lesser.than.approved.principal";
    public static final String DISBURSEMENT_DATES_NOT_IN_ORDER = "disbursements.should.be.ordered.based.on.their.disbursement.dates";
    public static final String DISBURSEMENT_DATE_BEFORE_ERROR = "disbursement.date.of.tranche.cannot.be.before.expected.disbursement.date";


}
