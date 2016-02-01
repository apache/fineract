/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.DepositAccountOnClosureType;
import org.mifosplatform.portfolio.savings.DepositAccountOnHoldTransactionType;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.RecurringDepositType;
import org.mifosplatform.portfolio.savings.SavingsAccountTransactionType;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsWithdrawalFeesType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountStatusType;

public class SavingsEnumerations {

    public static final String INTEREST_COMPOUNDING_PERIOD_TYPE = "interestCompoundingPeriodType";
    public static final String INTEREST_POSTING_PERIOD_TYPE = "interestPostingPeriodType";
    public static final String INTEREST_CALCULATION_TYPE = "interestCalculationType";
    public static final String MIN_DEPOSIT_TERM_TYPE = "minDepositTermTypeId";
    public static final String MAX_DEPOSIT_TERM_TYPE = "maxDepositTermTypeId";
    public static final String IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE = "inMultiplesOfDepositTermTypeId";
    public static final String DEPOSIT_PERIOD_FREQUNCY_TYPE = "depositPeriodFrequencyId";
    public static final String LOCKIN_PERIOD_FREQUNCY_TYPE = "lockinPeriodFrequencyType";
    public static final String ACCOUNTING_RULE_TYPE = "accountingRule";
    public static final String PRE_CLOSURE_PENAL_INTEREST_TYPE = "preClosurePenalInterestOnTypeId";
    public static final String INTEREST_CALCULATION_DAYS_IN_YEAR = "interestCalculationDaysInYearType";
    public static final String RECURRING_FREQUENCY_TYPE = "recurringFrequencyType";

    public static EnumOptionData savingEnumueration(final String typeName, final int id) {
        if (typeName.equals(INTEREST_COMPOUNDING_PERIOD_TYPE)) {
            return compoundingInterestPeriodType(id);
        } else if (typeName.equals(INTEREST_POSTING_PERIOD_TYPE)) {
            return interestPostingPeriodType(id);
        } else if (typeName.equals(INTEREST_CALCULATION_TYPE)) {
            return interestCalculationType(id);
        } else if (typeName.equals(MIN_DEPOSIT_TERM_TYPE)) {
            return depositTermFrequencyType(id);
        } else if (typeName.equals(MAX_DEPOSIT_TERM_TYPE)) {
            return depositTermFrequencyType(id);
        } else if (typeName.equals(IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE)) {
            return inMultiplesOfDepositTermFrequencyType(id);
        } else if (typeName.equals(DEPOSIT_PERIOD_FREQUNCY_TYPE)) {
            return depositPeriodFrequency(id);
        } else if (typeName.equals(LOCKIN_PERIOD_FREQUNCY_TYPE)) {
            return lockinPeriodFrequencyType(id);
        } else if (typeName.equals(ACCOUNTING_RULE_TYPE)) {
            return AccountingEnumerations.accountingRuleType(id);
        } else if (typeName.equals(PRE_CLOSURE_PENAL_INTEREST_TYPE)) {
            return preClosurePenaltyInterestOnType(id);
        } else if (typeName.equals(INTEREST_CALCULATION_DAYS_IN_YEAR)) {
            return interestCalculationDaysInYearType(id);
        } else if (typeName.equals(RECURRING_FREQUENCY_TYPE)) { return depositPeriodFrequency(id); }
        return null;
    }

    public static EnumOptionData lockinPeriodFrequencyType(final int id) {
        return lockinPeriodFrequencyType(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData lockinPeriodFrequencyType(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "savings.lockin.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static SavingsAccountTransactionEnumData transactionType(final int transactionType) {
        return transactionType(SavingsAccountTransactionType.fromInt(transactionType));
    }

    public static SavingsAccountTransactionEnumData transactionType(final SavingsAccountTransactionType type) {

        SavingsAccountTransactionEnumData optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.INVALID
                .getValue().longValue(), SavingsAccountTransactionType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.INVALID.getValue().longValue(),
                        SavingsAccountTransactionType.INVALID.getCode(), "Invalid");
            break;
            case DEPOSIT:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.DEPOSIT.getValue().longValue(),
                        SavingsAccountTransactionType.DEPOSIT.getCode(), "Deposit");
            break;
            case WITHDRAWAL:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WITHDRAWAL.getValue().longValue(),
                        SavingsAccountTransactionType.WITHDRAWAL.getCode(), "Withdrawal");
            break;
            case INTEREST_POSTING:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.INTEREST_POSTING.getValue().longValue(),
                        SavingsAccountTransactionType.INTEREST_POSTING.getCode(), "Interest posting");
            break;
            case WITHDRAWAL_FEE:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue().longValue(),
                        SavingsAccountTransactionType.WITHDRAWAL_FEE.getCode(), "Withdrawal fee");
            break;
            case ANNUAL_FEE:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.ANNUAL_FEE.getValue().longValue(),
                        SavingsAccountTransactionType.ANNUAL_FEE.getCode(), "Annual fee");
            break;
            case APPROVE_TRANSFER:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.APPROVE_TRANSFER.getValue().longValue(),
                        SavingsAccountTransactionType.APPROVE_TRANSFER.getCode(), "Transfer approved");
            break;
            case INITIATE_TRANSFER:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.INITIATE_TRANSFER.getValue().longValue(),
                        SavingsAccountTransactionType.INITIATE_TRANSFER.getCode(), "Transfer initiated");
            break;
            case REJECT_TRANSFER:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.REJECT_TRANSFER.getValue().longValue(),
                        SavingsAccountTransactionType.REJECT_TRANSFER.getCode(), "Transfer Rejected");
            break;
            case WITHDRAW_TRANSFER:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue().longValue(),
                        SavingsAccountTransactionType.WITHDRAW_TRANSFER.getCode(), "Transfer Withdrawn");
            break;
            default:
            case PAY_CHARGE:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.PAY_CHARGE.getValue().longValue(),
                        SavingsAccountTransactionType.PAY_CHARGE.getCode(), "Pay Charge");
            break;
            case WAIVE_CHARGES:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WAIVE_CHARGES.getValue().longValue(),
                        SavingsAccountTransactionType.WAIVE_CHARGES.getCode(), "Waive Charge");
            break;
            case WRITTEN_OFF:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WRITTEN_OFF.getValue().longValue(),
                        SavingsAccountTransactionType.WRITTEN_OFF.getCode(), "writtenoff");
            break;
            case OVERDRAFT_INTEREST:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue().longValue(),
                        SavingsAccountTransactionType.OVERDRAFT_INTEREST.getCode(), "Overdraft Interest");
            break;
        }
        return optionData;
    }

    public static SavingsAccountStatusEnumData status(final Integer statusEnum) {
        return status(SavingsAccountStatusType.fromInt(statusEnum));
    }

    public static SavingsAccountStatusEnumData status(final SavingsAccountStatusType type) {

        final boolean submittedAndPendingApproval = type.isSubmittedAndPendingApproval();
        final boolean isApproved = type.isApproved();
        final boolean isRejected = type.isRejected();
        final boolean isWithdrawnByApplicant = type.isApplicationWithdrawnByApplicant();
        final boolean isActive = type.isActive();
        final boolean isClosed = type.isClosed();
        final boolean isPrematureClosed = type.isPreMatureClosure();
        final boolean isTransferInProgress = type.isTransferInProgress();
        final boolean isTransferOnHold = type.isTransferOnHold();

        SavingsAccountStatusEnumData optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                SavingsAccountStatusType.INVALID.getCode(), "Invalid", submittedAndPendingApproval, isApproved, isRejected,
                isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);

        switch (type) {
            case INVALID:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                        SavingsAccountStatusType.INVALID.getCode(), "Invalid", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case SUBMITTED_AND_PENDING_APPROVAL:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue()
                        .longValue(), SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval",
                        submittedAndPendingApproval, isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed,
                        isTransferInProgress, isTransferOnHold);
            break;
            case REJECTED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.REJECTED.getValue().longValue(),
                        SavingsAccountStatusType.REJECTED.getCode(), "Rejected", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case WITHDRAWN_BY_APPLICANT:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue().longValue(),
                        SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getCode(), "Withdrawn by applicant", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress,
                        isTransferOnHold);
            break;
            case APPROVED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.APPROVED.getValue().longValue(),
                        SavingsAccountStatusType.APPROVED.getCode(), "Approved", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case ACTIVE:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.ACTIVE.getValue().longValue(),
                        SavingsAccountStatusType.ACTIVE.getCode(), "Active", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case CLOSED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.CLOSED.getValue().longValue(),
                        SavingsAccountStatusType.CLOSED.getCode(), "Closed", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case TRANSFER_IN_PROGRESS:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress,
                        isTransferOnHold);
            break;
            case TRANSFER_ON_HOLD:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue().longValue(),
                        SavingsAccountStatusType.TRANSFER_ON_HOLD.getCode(), "Transfer on hold", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress,
                        isTransferOnHold);
            break;
            case PRE_MATURE_CLOSURE:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.PRE_MATURE_CLOSURE.getValue().longValue(),
                        SavingsAccountStatusType.PRE_MATURE_CLOSURE.getCode(), "Premature Closed", submittedAndPendingApproval, isApproved,
                        isRejected, isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            case MATURED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.MATURED.getValue().longValue(),
                        SavingsAccountStatusType.MATURED.getCode(), "Matured", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isPrematureClosed, isTransferInProgress, isTransferOnHold);
            break;
            default:
            break;
        }
        return optionData;
    }

    public static EnumOptionData interestPostingPeriodType(final Integer type) {
        return interestPostingPeriodType(SavingsPostingInterestPeriodType.fromInt(type));
    }

    public static EnumOptionData interestPostingPeriodType(final SavingsPostingInterestPeriodType type) {

        final String codePrefix = "savings.interest.posting.period.";
        EnumOptionData optionData = new EnumOptionData(SavingsPostingInterestPeriodType.INVALID.getValue().longValue(),
                SavingsPostingInterestPeriodType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case MONTHLY:
                optionData = new EnumOptionData(SavingsPostingInterestPeriodType.MONTHLY.getValue().longValue(), codePrefix
                        + SavingsPostingInterestPeriodType.MONTHLY.getCode(), "Monthly");
            break;
            case QUATERLY:
                optionData = new EnumOptionData(SavingsPostingInterestPeriodType.QUATERLY.getValue().longValue(), codePrefix
                        + SavingsPostingInterestPeriodType.QUATERLY.getCode(), "Quarterly");
            break;
            case BIANNUAL:
                optionData = new EnumOptionData(SavingsPostingInterestPeriodType.BIANNUAL.getValue().longValue(), codePrefix
                        + SavingsPostingInterestPeriodType.BIANNUAL.getCode(), "BiAnnual");
            break;
            case ANNUAL:
                optionData = new EnumOptionData(SavingsPostingInterestPeriodType.ANNUAL.getValue().longValue(), codePrefix
                        + SavingsPostingInterestPeriodType.ANNUAL.getCode(), "Annually");
            break;
        }

        return optionData;
    }

    public static EnumOptionData compoundingInterestPeriodType(final Integer type) {
        return compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.fromInt(type));
    }

    public static EnumOptionData compoundingInterestPeriodType(final SavingsCompoundingInterestPeriodType type) {

        final String codePrefix = "savings.interest.period.";
        EnumOptionData optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.INVALID.getValue().longValue(),
                SavingsCompoundingInterestPeriodType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAILY:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.DAILY.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.DAILY.getCode(), "Daily");
            break;
            // case WEEKLY:
            // optionData = new
            // EnumOptionData(SavingsCompoundingInterestPeriodType.WEEKLY.getValue().longValue(),
            // codePrefix
            // + SavingsCompoundingInterestPeriodType.WEEKLY.getCode(),
            // "Weekly");
            // break;
            // case BIWEEKLY:
            // optionData = new
            // EnumOptionData(SavingsCompoundingInterestPeriodType.BIWEEKLY.getValue().longValue(),
            // codePrefix
            // + SavingsCompoundingInterestPeriodType.BIWEEKLY.getCode(),
            // "Bi-Weekly");
            // break;
            case MONTHLY:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.MONTHLY.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.MONTHLY.getCode(), "Monthly");
            break;
            case QUATERLY:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.QUATERLY.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.QUATERLY.getCode(), "Quarterly");
            break;
            case BI_ANNUAL:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.BI_ANNUAL.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.BI_ANNUAL.getCode(), "Semi-Annual");
            break;
            case ANNUAL:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.ANNUAL.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.ANNUAL.getCode(), "Annually");
            break;
        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // optionData = new
        // EnumOptionData(SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST.getValue().longValue(),
        // codePrefix +
        // SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST.getCode(),
        // "No Compounding - Simple Interest");
        // break;
        }

        return optionData;
    }

    public static EnumOptionData interestCalculationType(final Integer type) {
        return interestCalculationType(SavingsInterestCalculationType.fromInt(type));
    }

    public static EnumOptionData interestCalculationType(final SavingsInterestCalculationType type) {

        EnumOptionData optionData = new EnumOptionData(SavingsInterestCalculationType.INVALID.getValue().longValue(),
                SavingsInterestCalculationType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAILY_BALANCE:
                optionData = new EnumOptionData(SavingsInterestCalculationType.DAILY_BALANCE.getValue().longValue(),
                        SavingsInterestCalculationType.DAILY_BALANCE.getCode(), "Daily Balance");
            break;
            case AVERAGE_DAILY_BALANCE:
                optionData = new EnumOptionData(SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE.getValue().longValue(),
                        SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE.getCode(), "Average Daily Balance");
            break;
        }

        return optionData;
    }

    public static EnumOptionData interestCalculationDaysInYearType(final Integer type) {
        return interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.fromInt(type));
    }

    public static EnumOptionData interestCalculationDaysInYearType(final SavingsInterestCalculationDaysInYearType type) {
        EnumOptionData optionData = new EnumOptionData(SavingsInterestCalculationDaysInYearType.INVALID.getValue().longValue(),
                SavingsInterestCalculationDaysInYearType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAYS_360:
                optionData = new EnumOptionData(SavingsInterestCalculationDaysInYearType.DAYS_360.getValue().longValue(),
                        SavingsInterestCalculationDaysInYearType.DAYS_360.getCode(), "360 Days");
            break;
            case DAYS_365:
                optionData = new EnumOptionData(SavingsInterestCalculationDaysInYearType.DAYS_365.getValue().longValue(),
                        SavingsInterestCalculationDaysInYearType.DAYS_365.getCode(), "365 Days");
            break;
        }

        return optionData;
    }

    public static EnumOptionData withdrawalFeeType(final Integer type) {
        return withdrawalFeeType(SavingsWithdrawalFeesType.fromInt(type));
    }

    public static EnumOptionData withdrawalFeeType(final SavingsWithdrawalFeesType type) {
        EnumOptionData optionData = new EnumOptionData(SavingsWithdrawalFeesType.INVALID.getValue().longValue(),
                SavingsWithdrawalFeesType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case FLAT:
                optionData = new EnumOptionData(SavingsWithdrawalFeesType.FLAT.getValue().longValue(),
                        SavingsWithdrawalFeesType.FLAT.getCode(), "Flat");
            break;
            case PERCENT_OF_AMOUNT:
                optionData = new EnumOptionData(SavingsWithdrawalFeesType.PERCENT_OF_AMOUNT.getValue().longValue(),
                        SavingsWithdrawalFeesType.PERCENT_OF_AMOUNT.getCode(), "% of Amount");
            break;
        }

        return optionData;
    }

    public static EnumOptionData preClosurePenaltyInterestOnType(final Integer type) {
        return preClosurePenaltyInterestOnType(PreClosurePenalInterestOnType.fromInt(type));
    }

    public static EnumOptionData preClosurePenaltyInterestOnType(final PreClosurePenalInterestOnType type) {
        EnumOptionData optionData = new EnumOptionData(PreClosurePenalInterestOnType.INVALID.getValue().longValue(),
                PreClosurePenalInterestOnType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case WHOLE_TERM:
                optionData = new EnumOptionData(PreClosurePenalInterestOnType.WHOLE_TERM.getValue().longValue(),
                        PreClosurePenalInterestOnType.WHOLE_TERM.getCode(), "Whole term");
            break;
            case TILL_PREMATURE_WITHDRAWAL:
                optionData = new EnumOptionData(PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL.getValue().longValue(),
                        PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL.getCode(), "Till Premature Withdrawal");
            break;
        }

        return optionData;
    }

    public static List<EnumOptionData> preClosurePenaltyInterestOnType(final PreClosurePenalInterestOnType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final PreClosurePenalInterestOnType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(preClosurePenaltyInterestOnType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData recurringDepositType(final Integer type) {
        return recurringDepositType(RecurringDepositType.fromInt(type));
    }

    public static EnumOptionData recurringDepositType(final RecurringDepositType type) {
        EnumOptionData optionData = new EnumOptionData(RecurringDepositType.INVALID.getValue().longValue(),
                RecurringDepositType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case VOLUNTARY:
                optionData = new EnumOptionData(RecurringDepositType.VOLUNTARY.getValue().longValue(),
                        RecurringDepositType.VOLUNTARY.getCode(), "Voluntary");
            break;
            case MANDATORY:
                optionData = new EnumOptionData(RecurringDepositType.MANDATORY.getValue().longValue(),
                        RecurringDepositType.MANDATORY.getCode(), "Mandatory");
            break;
        }

        return optionData;
    }

    public static List<EnumOptionData> recurringDepositType(final RecurringDepositType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final RecurringDepositType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(recurringDepositType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData recurringDepositFrequencyType(final int id) {
        return recurringDepositFrequencyType(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData recurringDepositFrequencyType(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "recurring.deposit.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> recurringDepositFrequencyType(final SavingsPeriodFrequencyType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final SavingsPeriodFrequencyType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(recurringDepositFrequencyType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData depositTermFrequencyType(final int id) {
        return depositTermFrequencyType(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData depositTermFrequencyType(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "deposit.term.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> depositTermFrequencyType(final SavingsPeriodFrequencyType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final SavingsPeriodFrequencyType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(recurringDepositFrequencyType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData inMultiplesOfDepositTermFrequencyType(final int id) {
        return depositTermFrequencyType(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData inMultiplesOfDepositTermFrequencyType(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "inmultiples.of.deposit.term.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> inMultiplesOfDepositTermFrequencyType(final SavingsPeriodFrequencyType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final SavingsPeriodFrequencyType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(recurringDepositFrequencyType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData depositType(final int id) {
        return depositType(DepositAccountType.fromInt(id));
    }

    public static EnumOptionData depositType(final DepositAccountType type) {
        EnumOptionData optionData = new EnumOptionData(DepositAccountType.INVALID.getValue().longValue(),
                DepositAccountType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case SAVINGS_DEPOSIT:
                optionData = new EnumOptionData(DepositAccountType.SAVINGS_DEPOSIT.getValue().longValue(),
                        DepositAccountType.SAVINGS_DEPOSIT.getCode(), "Savings");
            break;
            case FIXED_DEPOSIT:
                optionData = new EnumOptionData(DepositAccountType.FIXED_DEPOSIT.getValue().longValue(),
                        DepositAccountType.FIXED_DEPOSIT.getCode(), "Fixed Deposit");
            break;
            case RECURRING_DEPOSIT:
                optionData = new EnumOptionData(DepositAccountType.RECURRING_DEPOSIT.getValue().longValue(),
                        DepositAccountType.RECURRING_DEPOSIT.getCode(), "Recurring Deposit");
            break;
            case CURRENT_DEPOSIT:
                optionData = new EnumOptionData(DepositAccountType.CURRENT_DEPOSIT.getValue().longValue(),
                        DepositAccountType.CURRENT_DEPOSIT.getCode(), "Current Deposit");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> depositType(final DepositAccountType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final DepositAccountType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(depositType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData depositPeriodFrequency(final int id) {
        return depositPeriodFrequency(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData depositPeriodFrequency(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "deposit.period.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> depositPeriodFrequency(final SavingsPeriodFrequencyType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final SavingsPeriodFrequencyType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(recurringDepositFrequencyType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData depositAccountOnClosureType(final int id) {
        return depositAccountOnClosureType(DepositAccountOnClosureType.fromInt(id));
    }

    public static EnumOptionData depositAccountOnClosureType(final DepositAccountOnClosureType type) {
        EnumOptionData optionData = new EnumOptionData(DepositAccountOnClosureType.INVALID.getValue().longValue(),
                DepositAccountOnClosureType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case WITHDRAW_DEPOSIT:
                optionData = new EnumOptionData(DepositAccountOnClosureType.WITHDRAW_DEPOSIT.getValue().longValue(),
                        DepositAccountOnClosureType.WITHDRAW_DEPOSIT.getCode(), "Withdraw Deposit");
            break;
            case TRANSFER_TO_SAVINGS:
                optionData = new EnumOptionData(DepositAccountOnClosureType.TRANSFER_TO_SAVINGS.getValue().longValue(),
                        DepositAccountOnClosureType.TRANSFER_TO_SAVINGS.getCode(), "Transfer to Savings");
            break;
            case REINVEST:
                optionData = new EnumOptionData(DepositAccountOnClosureType.REINVEST.getValue().longValue(),
                        DepositAccountOnClosureType.REINVEST.getCode(), "Re-Invest");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> depositAccountOnClosureType(final DepositAccountOnClosureType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final DepositAccountOnClosureType type : types) {
            if (!type.isInvalid()) {
                optionDatas.add(depositAccountOnClosureType(type));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData onHoldTransactionType(final int id) {
        return onHoldTransactionType(DepositAccountOnHoldTransactionType.fromInt(id));
    }

    public static EnumOptionData onHoldTransactionType(final DepositAccountOnHoldTransactionType type) {
        EnumOptionData optionData = new EnumOptionData(DepositAccountOnHoldTransactionType.INVALID.getValue().longValue(),
                DepositAccountType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case HOLD:
                optionData = new EnumOptionData(DepositAccountOnHoldTransactionType.HOLD.getValue().longValue(),
                        DepositAccountOnHoldTransactionType.HOLD.getCode(), "hold");
            break;
            case RELEASE:
                optionData = new EnumOptionData(DepositAccountOnHoldTransactionType.RELEASE.getValue().longValue(),
                        DepositAccountOnHoldTransactionType.RELEASE.getCode(), "release");
            break;

        }
        return optionData;
    }

}