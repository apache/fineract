/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
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
            case APPLY_CHARGES:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.APPLY_CHARGES.getValue().longValue(),
                        SavingsAccountTransactionType.APPLY_CHARGES.getCode(), "Apply Charge");
            break;
            case WAIVE_CHARGES:
                optionData = new SavingsAccountTransactionEnumData(SavingsAccountTransactionType.WAIVE_CHARGES.getValue().longValue(),
                        SavingsAccountTransactionType.WAIVE_CHARGES.getCode(), "Waive Charge");
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
        final boolean isTransferInProgress = type.isTransferInProgress();
        final boolean isTransferOnHold = type.isTransferOnHold();

        SavingsAccountStatusEnumData optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                SavingsAccountStatusType.INVALID.getCode(), "Invalid", submittedAndPendingApproval, isApproved, isRejected,
                isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);

        switch (type) {
            case INVALID:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                        SavingsAccountStatusType.INVALID.getCode(), "Invalid", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case SUBMITTED_AND_PENDING_APPROVAL:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue()
                        .longValue(), SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval",
                        submittedAndPendingApproval, isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed,
                        isTransferInProgress, isTransferOnHold);
            break;
            case REJECTED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.REJECTED.getValue().longValue(),
                        SavingsAccountStatusType.REJECTED.getCode(), "Rejected", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case WITHDRAWN_BY_APPLICANT:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue().longValue(),
                        SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getCode(), "Withdrawn by applicant", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case APPROVED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.APPROVED.getValue().longValue(),
                        SavingsAccountStatusType.APPROVED.getCode(), "Approved", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case ACTIVE:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.ACTIVE.getValue().longValue(),
                        SavingsAccountStatusType.ACTIVE.getCode(), "Active", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case CLOSED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.CLOSED.getValue().longValue(),
                        SavingsAccountStatusType.CLOSED.getCode(), "Closed", submittedAndPendingApproval, isApproved, isRejected,
                        isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case TRANSFER_IN_PROGRESS:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
            break;
            case TRANSFER_ON_HOLD:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        SavingsAccountStatusType.TRANSFER_ON_HOLD.getCode(), "Transfer in progress", submittedAndPendingApproval,
                        isApproved, isRejected, isWithdrawnByApplicant, isActive, isClosed, isTransferInProgress, isTransferOnHold);
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
        // case QUATERLY:
        // optionData = new
        // EnumOptionData(SavingsCompoundingInterestPeriodType.QUATERLY.getValue().longValue(),
        // codePrefix
        // + SavingsCompoundingInterestPeriodType.QUATERLY.getCode(),
        // "Quarterly");
        // break;
        // case BI_ANNUAL:
        // optionData = new
        // EnumOptionData(SavingsCompoundingInterestPeriodType.BI_ANNUAL.getValue().longValue(),
        // codePrefix
        // + SavingsCompoundingInterestPeriodType.BI_ANNUAL.getCode(),
        // "Semi-Annual");
        // break;
        // case ANNUAL:
        // optionData = new
        // EnumOptionData(SavingsCompoundingInterestPeriodType.ANNUAL.getValue().longValue(),
        // codePrefix
        // + SavingsCompoundingInterestPeriodType.ANNUAL.getCode(), "Annually");
        // break;
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
}