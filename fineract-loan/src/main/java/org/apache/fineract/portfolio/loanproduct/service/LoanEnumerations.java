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
package org.apache.fineract.portfolio.loanproduct.service;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductParamType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;

public final class LoanEnumerations {

    private LoanEnumerations() {

    }

    public static final String LOAN_TERM_FREQUENCY_TYPE = "loanTermFrequencyType";
    public static final String TERM_FREQUENCY_TYPE = "termFrequencyType";
    public static final String REPAYMENT_FREQUENCY_TYPE = "repaymentFrequencyType";
    public static final String INTEREST_RATE_FREQUENCY_TYPE = "interestRateFrequencyType";
    public static final String AMORTIZATION_TYPE = "amortizationType";
    public static final String INTEREST_TYPE = "interestType";
    public static final String INTEREST_CALCULATION_PERIOD_TYPE = "interestCalculationPeriodType";
    public static final String PAYMENT_TYPE = "paymentType";
    public static final String ACCOUNTING_RULE_TYPE = "accountingRule";
    public static final String LOAN_TYPE = "loanType";
    public static final String INTEREST_RECALCULATION_COMPOUNDING_TYPE = "interestRecalculationCompoundingType";
    public static final String RESCHEDULE_STRATEGY_TYPE = "rescheduleStrategyType";
    public static final String REPAYMENT_START_DATE_TYPE = "repaymentStartDateType";

    public static EnumOptionData loanEnumeration(final String typeName, final int id) {
        return switch (typeName) {
            case LOAN_TERM_FREQUENCY_TYPE -> loanTermFrequencyType(id);
            case TERM_FREQUENCY_TYPE -> termFrequencyType(id);
            case REPAYMENT_FREQUENCY_TYPE -> repaymentFrequencyType(id);
            case INTEREST_RATE_FREQUENCY_TYPE -> interestRateFrequencyType(id);
            case AMORTIZATION_TYPE -> amortizationType(id);
            case INTEREST_TYPE -> interestType(id);
            case INTEREST_CALCULATION_PERIOD_TYPE -> interestCalculationPeriodType(id);
            case ACCOUNTING_RULE_TYPE -> AccountingEnumerations.accountingRuleType(id);
            case LOAN_TYPE -> AccountEnumerations.loanType(id);
            case INTEREST_RECALCULATION_COMPOUNDING_TYPE -> interestRecalculationCompoundingType(id);
            case RESCHEDULE_STRATEGY_TYPE -> rescheduleStrategyType(id);
            case REPAYMENT_START_DATE_TYPE -> repaymentStartDateType(id);
            default -> null;
        };
    }

    public static EnumOptionData repaymentStartDateType(int id) {
        return repaymentStartDateType(RepaymentStartDateType.fromInt(id));
    }

    public static EnumOptionData repaymentStartDateType(final RepaymentStartDateType type) {
        return switch (type) {
            case DISBURSEMENT_DATE -> new EnumOptionData(RepaymentStartDateType.DISBURSEMENT_DATE.getValue().longValue(),
                    RepaymentStartDateType.DISBURSEMENT_DATE.getCode(), "Disbursement Date");
            case SUBMITTED_ON_DATE -> new EnumOptionData(RepaymentStartDateType.SUBMITTED_ON_DATE.getValue().longValue(),
                    RepaymentStartDateType.SUBMITTED_ON_DATE.getCode(), "Submitted On Date");
            default -> new EnumOptionData(RepaymentStartDateType.INVALID.getValue().longValue(), RepaymentStartDateType.INVALID.getCode(),
                    "Invalid");
        };
    }

    public static EnumOptionData loanTermFrequencyType(final int id) {
        return loanTermFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData loanTermFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "loanTermFrequency.";
        return switch (type) {
            case DAYS -> new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
            case WEEKS -> new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            case MONTHS -> new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
            case YEARS -> new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
            default ->
                new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData termFrequencyType(final int id) {
        return termFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData termFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "termFrequency.";
        return switch (type) {
            case DAYS -> new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
            case WEEKS -> new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            case MONTHS -> new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
            case YEARS -> new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
            default ->
                new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData repaymentFrequencyType(final int id) {
        return repaymentFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyNthDayType(final Integer id) {
        if (id == null) {
            return null;
        }
        return repaymentFrequencyNthDayType(NthDayType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyNthDayType(final NthDayType type) {
        final String codePrefix = "repaymentFrequency.";
        long nthDayValue = type.getValue().longValue();

        return switch (type) {
            case ONE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "first");
            case TWO -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "second");
            case THREE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "third");
            case FOUR -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fourth");
            case FIVE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fifth");
            case LAST -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "last");
            default -> new EnumOptionData(Integer.valueOf(0).longValue(), codePrefix + type.getCode(), "invalid");
        };
    }

    public static EnumOptionData repaymentFrequencyDayOfWeekType(final Integer id) {
        if (id == null) {
            return null;
        }
        return repaymentFrequencyDayOfWeekType(DayOfWeekType.fromInt(id));
    }

    public static EnumOptionData repaymentFrequencyDayOfWeekType(final DayOfWeekType type) {
        final String codePrefix = "repaymentFrequency.";

        return new EnumOptionData(type.getValue().longValue(), codePrefix + type.getCode(), type.toString());
    }

    public static EnumOptionData repaymentFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "repaymentFrequency.";
        return switch (type) {
            case DAYS -> new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
            case WEEKS -> new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            case MONTHS -> new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
            case YEARS -> new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
            default ->
                new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData interestRateFrequencyType(final Integer id) {
        return interestRateFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRateFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "interestRateFrequency.";
        return switch (type) {
            case MONTHS -> new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Per month");
            case YEARS -> new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.YEARS.getCode(), "Per year");
            case WHOLE_TERM -> new EnumOptionData(PeriodFrequencyType.WHOLE_TERM.getValue().longValue(),
                    codePrefix + PeriodFrequencyType.WHOLE_TERM.getCode(), "Whole term");
            default ->
                new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData amortizationType(final Integer id) {
        return amortizationType(AmortizationMethod.fromInt(id));
    }

    public static EnumOptionData amortizationType(final AmortizationMethod amortizationMethod) {
        return switch (amortizationMethod) {
            case EQUAL_INSTALLMENTS -> new EnumOptionData(AmortizationMethod.EQUAL_INSTALLMENTS.getValue().longValue(),
                    AmortizationMethod.EQUAL_INSTALLMENTS.getCode(), "Equal installments");
            case EQUAL_PRINCIPAL -> new EnumOptionData(AmortizationMethod.EQUAL_PRINCIPAL.getValue().longValue(),
                    AmortizationMethod.EQUAL_PRINCIPAL.getCode(), "Equal principal payments");
            default ->
                new EnumOptionData(AmortizationMethod.INVALID.getValue().longValue(), AmortizationMethod.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData interestType(final Integer id) {
        return interestType(InterestMethod.fromInt(id));
    }

    public static EnumOptionData interestType(final InterestMethod type) {
        return switch (type) {
            case FLAT -> new EnumOptionData(InterestMethod.FLAT.getValue().longValue(), InterestMethod.FLAT.getCode(), "Flat");
            case DECLINING_BALANCE -> new EnumOptionData(InterestMethod.DECLINING_BALANCE.getValue().longValue(),
                    InterestMethod.DECLINING_BALANCE.getCode(), "Declining Balance");
            default -> new EnumOptionData(InterestMethod.INVALID.getValue().longValue(), InterestMethod.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData interestCalculationPeriodType(final Integer id) {
        return interestCalculationPeriodType(InterestCalculationPeriodMethod.fromInt(id));
    }

    public static EnumOptionData interestCalculationPeriodType(final InterestCalculationPeriodMethod type) {
        return switch (type) {
            case DAILY -> new EnumOptionData(InterestCalculationPeriodMethod.DAILY.getValue().longValue(),
                    InterestCalculationPeriodMethod.DAILY.getCode(), "Daily");
            case SAME_AS_REPAYMENT_PERIOD ->
                new EnumOptionData(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue().longValue(),
                        InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getCode(), "Same as repayment period");
            default -> new EnumOptionData(InterestCalculationPeriodMethod.INVALID.getValue().longValue(),
                    InterestCalculationPeriodMethod.INVALID.getCode(), "Invalid");
        };
    }

    public static LoanTransactionEnumData transactionType(final Integer id) {
        return transactionType(LoanTransactionType.fromInt(id));
    }

    public static LoanTransactionEnumData transactionType(final LoanTransactionType type) {
        return switch (type) {
            case INVALID -> new LoanTransactionEnumData(LoanTransactionType.INVALID.getValue().longValue(),
                    LoanTransactionType.INVALID.getCode(), "Invalid");
            case DISBURSEMENT -> new LoanTransactionEnumData(LoanTransactionType.DISBURSEMENT.getValue().longValue(),
                    LoanTransactionType.DISBURSEMENT.getCode(), "Disbursement");
            case REPAYMENT -> new LoanTransactionEnumData(LoanTransactionType.REPAYMENT.getValue().longValue(),
                    LoanTransactionType.REPAYMENT.getCode(), "Repayment");
            case REPAYMENT_AT_DISBURSEMENT ->
                new LoanTransactionEnumData(LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue().longValue(),
                        LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getCode(), "Repayment (at time of disbursement)");
            case CONTRA -> new LoanTransactionEnumData(LoanTransactionType.CONTRA.getValue().longValue(),
                    LoanTransactionType.CONTRA.getCode(), "Reversal");
            case WAIVE_INTEREST -> new LoanTransactionEnumData(LoanTransactionType.WAIVE_INTEREST.getValue().longValue(),
                    LoanTransactionType.WAIVE_INTEREST.getCode(), "Waive interest");
            case MARKED_FOR_RESCHEDULING -> new LoanTransactionEnumData(LoanTransactionType.MARKED_FOR_RESCHEDULING.getValue().longValue(),
                    LoanTransactionType.MARKED_FOR_RESCHEDULING.getCode(), "Close (as rescheduled)");
            case WRITEOFF -> new LoanTransactionEnumData(LoanTransactionType.WRITEOFF.getValue().longValue(),
                    LoanTransactionType.WRITEOFF.getCode(), "Close (as written-off)");
            case RECOVERY_REPAYMENT -> new LoanTransactionEnumData(LoanTransactionType.RECOVERY_REPAYMENT.getValue().longValue(),
                    LoanTransactionType.RECOVERY_REPAYMENT.getCode(), "Repayment (after write-off)");
            case WAIVE_CHARGES -> new LoanTransactionEnumData(LoanTransactionType.WAIVE_CHARGES.getValue().longValue(),
                    LoanTransactionType.WAIVE_CHARGES.getCode(), "Waive loan charges");
            case ACCRUAL -> new LoanTransactionEnumData(LoanTransactionType.ACCRUAL.getValue().longValue(),
                    LoanTransactionType.ACCRUAL.getCode(), "Accrual");
            case APPROVE_TRANSFER -> new LoanTransactionEnumData(LoanTransactionType.APPROVE_TRANSFER.getValue().longValue(),
                    LoanTransactionType.APPROVE_TRANSFER.getCode(), "Transfer approved");
            case INITIATE_TRANSFER -> new LoanTransactionEnumData(LoanTransactionType.INITIATE_TRANSFER.getValue().longValue(),
                    LoanTransactionType.INITIATE_TRANSFER.getCode(), "Transfer initiated");
            case WITHDRAW_TRANSFER -> new LoanTransactionEnumData(LoanTransactionType.WITHDRAW_TRANSFER.getValue().longValue(),
                    LoanTransactionType.WITHDRAW_TRANSFER.getCode(), "Transfer Withdrawn");
            case REJECT_TRANSFER -> new LoanTransactionEnumData(LoanTransactionType.REJECT_TRANSFER.getValue().longValue(),
                    LoanTransactionType.REJECT_TRANSFER.getCode(), "Transfer Rejected");
            case REFUND -> new LoanTransactionEnumData(LoanTransactionType.REFUND.getValue().longValue(),
                    LoanTransactionType.REFUND.getCode(), "Transfer Refund");
            case CHARGE_PAYMENT -> new LoanTransactionEnumData(LoanTransactionType.CHARGE_PAYMENT.getValue().longValue(),
                    LoanTransactionType.CHARGE_PAYMENT.getCode(), "Charge Payment");
            case REFUND_FOR_ACTIVE_LOAN -> new LoanTransactionEnumData(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.getValue().longValue(),
                    LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.getCode(), "Refund");
            case INCOME_POSTING -> new LoanTransactionEnumData(LoanTransactionType.INCOME_POSTING.getValue().longValue(),
                    LoanTransactionType.INCOME_POSTING.getCode(), "Income Posting");
            case CREDIT_BALANCE_REFUND -> new LoanTransactionEnumData(LoanTransactionType.CREDIT_BALANCE_REFUND.getValue().longValue(),
                    LoanTransactionType.CREDIT_BALANCE_REFUND.getCode(), "Credit Balance Refund");
            case MERCHANT_ISSUED_REFUND -> new LoanTransactionEnumData(LoanTransactionType.MERCHANT_ISSUED_REFUND.getValue().longValue(),
                    LoanTransactionType.MERCHANT_ISSUED_REFUND.getCode(), "Merchant Issued Refund");
            case PAYOUT_REFUND -> new LoanTransactionEnumData(LoanTransactionType.PAYOUT_REFUND.getValue().longValue(),
                    LoanTransactionType.PAYOUT_REFUND.getCode(), "Payout Refund");
            case GOODWILL_CREDIT -> new LoanTransactionEnumData(LoanTransactionType.GOODWILL_CREDIT.getValue().longValue(),
                    LoanTransactionType.GOODWILL_CREDIT.getCode(), "Goodwill Credit");
            case INTEREST_PAYMENT_WAIVER -> new LoanTransactionEnumData(LoanTransactionType.INTEREST_PAYMENT_WAIVER.getValue().longValue(),
                    LoanTransactionType.INTEREST_PAYMENT_WAIVER.getCode(), "Interest Payment Waiver");
            case CHARGE_REFUND -> new LoanTransactionEnumData(LoanTransactionType.CHARGE_REFUND.getValue().longValue(),
                    LoanTransactionType.CHARGE_REFUND.getCode(), "Charge Refund");
            case CHARGEBACK -> new LoanTransactionEnumData(LoanTransactionType.CHARGEBACK.getValue().longValue(),
                    LoanTransactionType.CHARGEBACK.getCode(), "Chargeback");
            case CHARGE_ADJUSTMENT -> new LoanTransactionEnumData(LoanTransactionType.CHARGE_ADJUSTMENT.getValue().longValue(),
                    LoanTransactionType.CHARGE_ADJUSTMENT.getCode(), "Charge Adjustment");
            case CHARGE_OFF -> new LoanTransactionEnumData(LoanTransactionType.CHARGE_OFF.getValue().longValue(),
                    LoanTransactionType.CHARGE_OFF.getCode(), "Charge-off");
            case DOWN_PAYMENT -> new LoanTransactionEnumData(LoanTransactionType.DOWN_PAYMENT.getValue().longValue(),
                    LoanTransactionType.DOWN_PAYMENT.getCode(), "Down Payment");
            case REAGE -> new LoanTransactionEnumData(LoanTransactionType.REAGE.getValue().longValue(), LoanTransactionType.REAGE.getCode(),
                    "Re-age");
            case REAMORTIZE -> new LoanTransactionEnumData(LoanTransactionType.REAMORTIZE.getValue().longValue(),
                    LoanTransactionType.REAMORTIZE.getCode(), "Re-amortize");
            case ACCRUAL_ACTIVITY -> new LoanTransactionEnumData(LoanTransactionType.ACCRUAL_ACTIVITY.getValue().longValue(),
                    LoanTransactionType.ACCRUAL_ACTIVITY.getCode(), "Accrual Activity");
            case INTEREST_REFUND -> new LoanTransactionEnumData(LoanTransactionType.INTEREST_REFUND.getValue().longValue(),
                    LoanTransactionType.INTEREST_REFUND.getCode(), "Interest Refund");
            case ACCRUAL_ADJUSTMENT -> new LoanTransactionEnumData(LoanTransactionType.ACCRUAL_ADJUSTMENT.getValue().longValue(),
                    LoanTransactionType.ACCRUAL_ADJUSTMENT.getCode(), "Accrual Adjustment");
        };
    }

    public static EnumOptionData status(final LoanStatusEnumData status) {

        Long id = status.getId();
        String code = status.getCode();
        String value = status.getValue();

        return new EnumOptionData(id, code, value);
    }

    public static LoanStatusEnumData status(final Integer statusId) {
        return status(LoanStatus.fromInt(statusId));
    }

    public static LoanStatusEnumData status(final LoanStatus status) {
        return switch (status) {
            case INVALID -> new LoanStatusEnumData(LoanStatus.INVALID.getValue().longValue(), LoanStatus.INVALID.getCode(), "Invalid");
            case SUBMITTED_AND_PENDING_APPROVAL -> new LoanStatusEnumData(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
                    LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
            case APPROVED -> new LoanStatusEnumData(LoanStatus.APPROVED.getValue().longValue(), LoanStatus.APPROVED.getCode(), "Approved");
            case ACTIVE -> new LoanStatusEnumData(LoanStatus.ACTIVE.getValue().longValue(), LoanStatus.ACTIVE.getCode(), "Active");
            case REJECTED -> new LoanStatusEnumData(LoanStatus.REJECTED.getValue().longValue(), LoanStatus.REJECTED.getCode(), "Rejected");
            case WITHDRAWN_BY_CLIENT -> new LoanStatusEnumData(LoanStatus.WITHDRAWN_BY_CLIENT.getValue().longValue(),
                    LoanStatus.WITHDRAWN_BY_CLIENT.getCode(), "Withdrawn by applicant");
            case CLOSED_OBLIGATIONS_MET -> new LoanStatusEnumData(LoanStatus.CLOSED_OBLIGATIONS_MET.getValue().longValue(),
                    LoanStatus.CLOSED_OBLIGATIONS_MET.getCode(), "Closed (obligations met)");
            case CLOSED_WRITTEN_OFF -> new LoanStatusEnumData(LoanStatus.CLOSED_WRITTEN_OFF.getValue().longValue(),
                    LoanStatus.CLOSED_WRITTEN_OFF.getCode(), "Closed (written off)");
            case CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT ->
                new LoanStatusEnumData(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT.getValue().longValue(),
                        LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT.getCode(), "Closed (rescheduled)");
            case OVERPAID -> new LoanStatusEnumData(LoanStatus.OVERPAID.getValue().longValue(), LoanStatus.OVERPAID.getCode(), "Overpaid");
            case TRANSFER_IN_PROGRESS -> new LoanStatusEnumData(LoanStatus.TRANSFER_IN_PROGRESS.getValue().longValue(),
                    LoanStatus.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress");
            case TRANSFER_ON_HOLD -> new LoanStatusEnumData(LoanStatus.TRANSFER_ON_HOLD.getValue().longValue(),
                    LoanStatus.TRANSFER_ON_HOLD.getCode(), "Transfer on hold");
        };
    }

    public static EnumOptionData loanCycleValueConditionType(final int id) {
        return loanCycleValueConditionType(LoanProductValueConditionType.fromInt(id));
    }

    public static EnumOptionData loanCycleValueConditionType(final LoanProductValueConditionType type) {
        return switch (type) {
            case EQUAL -> new EnumOptionData(LoanProductValueConditionType.EQUAL.getValue().longValue(),
                    LoanProductValueConditionType.EQUAL.getCode(), "equals");
            case GREATERTHAN -> new EnumOptionData(LoanProductValueConditionType.GREATERTHAN.getValue().longValue(),
                    LoanProductValueConditionType.GREATERTHAN.getCode(), "greater than");
            default -> new EnumOptionData(LoanProductValueConditionType.INVALID.getValue().longValue(),
                    LoanProductValueConditionType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData loanCycleParamType(final int id) {
        return loanCycleParamType(LoanProductParamType.fromInt(id));
    }

    public static EnumOptionData loanCycleParamType(final LoanProductParamType type) {
        return switch (type) {
            case PRINCIPAL -> new EnumOptionData(LoanProductParamType.PRINCIPAL.getValue().longValue(),
                    LoanProductParamType.PRINCIPAL.getCode(), "principal");
            case INTERESTRATE -> new EnumOptionData(LoanProductParamType.INTERESTRATE.getValue().longValue(),
                    LoanProductParamType.INTERESTRATE.getCode(), "Interest rate");
            case REPAYMENT -> new EnumOptionData(LoanProductParamType.REPAYMENT.getValue().longValue(),
                    LoanProductParamType.REPAYMENT.getCode(), "repayment");
            default ->
                new EnumOptionData(LoanProductParamType.INVALID.getValue().longValue(), LoanProductParamType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData loanVariationType(final int id) {
        return loanVariationType(LoanTermVariationType.fromInt(id));
    }

    public static EnumOptionData loanVariationType(final LoanTermVariationType type) {
        return switch (type) {
            case EMI_AMOUNT -> new EnumOptionData(LoanTermVariationType.EMI_AMOUNT.getValue().longValue(),
                    LoanTermVariationType.EMI_AMOUNT.getCode(), "emiAmount");
            case INTEREST_RATE -> new EnumOptionData(LoanTermVariationType.INTEREST_RATE.getValue().longValue(),
                    LoanTermVariationType.INTEREST_RATE.getCode(), "interestRate");
            case DELETE_INSTALLMENT -> new EnumOptionData(LoanTermVariationType.DELETE_INSTALLMENT.getValue().longValue(),
                    LoanTermVariationType.DELETE_INSTALLMENT.getCode(), "deleteInstallment");
            case DUE_DATE -> new EnumOptionData(LoanTermVariationType.DUE_DATE.getValue().longValue(),
                    LoanTermVariationType.DUE_DATE.getCode(), "dueDate");
            case INSERT_INSTALLMENT -> new EnumOptionData(LoanTermVariationType.INSERT_INSTALLMENT.getValue().longValue(),
                    LoanTermVariationType.DUE_DATE.getCode(), "insertInstallment");
            case PRINCIPAL_AMOUNT -> new EnumOptionData(LoanTermVariationType.PRINCIPAL_AMOUNT.getValue().longValue(),
                    LoanTermVariationType.PRINCIPAL_AMOUNT.getCode(), "principalAmount");
            case GRACE_ON_INTEREST -> new EnumOptionData(LoanTermVariationType.GRACE_ON_INTEREST.getValue().longValue(),
                    LoanTermVariationType.GRACE_ON_INTEREST.getCode(), "graceOnInterest");
            case GRACE_ON_PRINCIPAL -> new EnumOptionData(LoanTermVariationType.GRACE_ON_PRINCIPAL.getValue().longValue(),
                    LoanTermVariationType.GRACE_ON_PRINCIPAL.getCode(), "graceOnPrincipal");
            case EXTEND_REPAYMENT_PERIOD -> new EnumOptionData(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD.getValue().longValue(),
                    LoanTermVariationType.EXTEND_REPAYMENT_PERIOD.getCode(), "extendRepaymentPeriod");
            case INTEREST_RATE_FROM_INSTALLMENT ->
                new EnumOptionData(LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT.getValue().longValue(),
                        LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT.getCode(), "interestRateForInstallment");
            case INTEREST_PAUSE -> new EnumOptionData(LoanTermVariationType.INTEREST_PAUSE.getValue().longValue(),
                    LoanTermVariationType.INTEREST_PAUSE.getCode(), "interestPause");
            default -> new EnumOptionData(LoanTermVariationType.INVALID.getValue().longValue(), LoanTermVariationType.INVALID.getCode(),
                    "Invalid");
        };
    }

    public static EnumOptionData interestRecalculationCompoundingType(final int id) {
        return interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingType(final InterestRecalculationCompoundingMethod type) {
        return switch (type) {
            case FEE -> new EnumOptionData(InterestRecalculationCompoundingMethod.FEE.getValue().longValue(),
                    InterestRecalculationCompoundingMethod.FEE.getCode(), "Fee");
            case INTEREST -> new EnumOptionData(InterestRecalculationCompoundingMethod.INTEREST.getValue().longValue(),
                    InterestRecalculationCompoundingMethod.INTEREST.getCode(), "Interest");
            case INTEREST_AND_FEE -> new EnumOptionData(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue().longValue(),
                    InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getCode(), "Fee and Interest");
            default -> new EnumOptionData(InterestRecalculationCompoundingMethod.NONE.getValue().longValue(),
                    InterestRecalculationCompoundingMethod.NONE.getCode(), "None");
        };
    }

    public static EnumOptionData interestRecalculationCompoundingNthDayType(final Integer id) {
        if (id == null) {
            return null;
        }
        return interestRecalculationCompoundingNthDayType(NthDayType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingNthDayType(final NthDayType type) {
        final String codePrefix = "interestRecalculationCompounding.";
        long nthDayValue = type.getValue().longValue();
        return switch (type) {
            case ONE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "first");
            case TWO -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "second");
            case THREE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "third");
            case FOUR -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fourth");
            case FIVE -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "fifth");
            case LAST -> new EnumOptionData(nthDayValue, codePrefix + type.getCode(), "last");
            default -> new EnumOptionData(Integer.valueOf(0).longValue(), codePrefix + type.getCode(), "invalid");
        };
    }

    public static EnumOptionData interestRecalculationCompoundingDayOfWeekType(final Integer id) {
        if (id == null) {
            return null;
        }
        return interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationCompoundingDayOfWeekType(final DayOfWeekType type) {
        final String codePrefix = "interestRecalculationCompounding.";
        return new EnumOptionData(type.getValue().longValue(), codePrefix + type.getCode(), type.toString());
    }

    public static EnumOptionData rescheduleStrategyType(final int id) {
        return rescheduleStrategyType(LoanRescheduleStrategyMethod.fromInt(id));
    }

    public static EnumOptionData rescheduleStrategyType(final LoanRescheduleStrategyMethod type) {
        return switch (type) {
            case REDUCE_EMI_AMOUNT -> new EnumOptionData(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT.getValue().longValue(),
                    LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT.getCode(), "Reduce EMI amount");
            case REDUCE_NUMBER_OF_INSTALLMENTS ->
                new EnumOptionData(LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS.getValue().longValue(),
                        LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS.getCode(), "Reduce number of installments");
            case RESCHEDULE_NEXT_REPAYMENTS ->
                new EnumOptionData(LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS.getValue().longValue(),
                        LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS.getCode(), "Reschedule next repayments");
            case ADJUST_LAST_UNPAID_PERIOD ->
                new EnumOptionData(LoanRescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD.getValue().longValue(),
                        LoanRescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD.getCode(), "Adjust last, unpaid period");
            default -> new EnumOptionData(LoanRescheduleStrategyMethod.INVALID.getValue().longValue(),
                    LoanRescheduleStrategyMethod.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData interestRecalculationFrequencyType(final int id) {
        return interestRecalculationFrequencyType(RecalculationFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRecalculationFrequencyType(final RecalculationFrequencyType type) {
        return switch (type) {
            case DAILY -> new EnumOptionData(RecalculationFrequencyType.DAILY.getValue().longValue(),
                    RecalculationFrequencyType.DAILY.getCode(), "Daily");
            case MONTHLY -> new EnumOptionData(RecalculationFrequencyType.MONTHLY.getValue().longValue(),
                    RecalculationFrequencyType.MONTHLY.getCode(), "Monthly");
            case SAME_AS_REPAYMENT_PERIOD -> new EnumOptionData(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getValue().longValue(),
                    RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getCode(), "Same as repayment period");
            case WEEKLY -> new EnumOptionData(RecalculationFrequencyType.WEEKLY.getValue().longValue(),
                    RecalculationFrequencyType.WEEKLY.getCode(), "Weekly");
            default -> new EnumOptionData(RecalculationFrequencyType.INVALID.getValue().longValue(),
                    RecalculationFrequencyType.INVALID.getCode(), "Invalid");
        };
    }

    public static EnumOptionData preCloseInterestCalculationStrategy(final int id) {
        return preCloseInterestCalculationStrategy(LoanPreCloseInterestCalculationStrategy.fromInt(id));
    }

    public static EnumOptionData preCloseInterestCalculationStrategy(final LoanPreCloseInterestCalculationStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case TILL_PRE_CLOSURE_DATE:
                optionData = new EnumOptionData(LoanPreCloseInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue().longValue(),
                        LoanPreCloseInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getCode(), "Till Pre-Close Date");
            break;
            case TILL_REST_FREQUENCY_DATE:
                optionData = new EnumOptionData(LoanPreCloseInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.getValue().longValue(),
                        LoanPreCloseInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.getCode(), "Till Rest Frequency Date");
            break;
            case NONE:
            break;
        }
        return optionData;
    }

}
