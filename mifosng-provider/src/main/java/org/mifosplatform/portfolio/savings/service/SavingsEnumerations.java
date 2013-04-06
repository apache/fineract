/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountStatusType;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransactionType;
import org.mifosplatform.portfolio.savings.domain.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestPostingPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsPeriodFrequencyType;

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
        }
        return optionData;
    }

    public static SavingsAccountStatusEnumData status(final Integer statusEnum) {
        return status(SavingsAccountStatusType.fromInt(statusEnum));
    }

    public static SavingsAccountStatusEnumData status(final SavingsAccountStatusType type) {

        SavingsAccountStatusEnumData optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                SavingsAccountStatusType.INVALID.getCode(), "Invalid", false, false, false);

        switch (type) {
            case INVALID:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.INVALID.getValue().longValue(),
                        SavingsAccountStatusType.INVALID.getCode(), "Invalid", type.isUnactivated(), type.isActive(), type.isClosed());
            break;
            case UNACTIVATED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.UNACTIVATED.getValue().longValue(),
                        SavingsAccountStatusType.UNACTIVATED.getCode(), "Unactivated", type.isUnactivated(), type.isActive(),
                        type.isClosed());
            break;
            case ACTIVE:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.ACTIVE.getValue().longValue(),
                        SavingsAccountStatusType.ACTIVE.getCode(), "Active", type.isUnactivated(), type.isActive(), type.isClosed());
            break;
            case CLOSED:
                optionData = new SavingsAccountStatusEnumData(SavingsAccountStatusType.CLOSED.getValue().longValue(),
                        SavingsAccountStatusType.CLOSED.getCode(), "Closed", type.isUnactivated(), type.isActive(), type.isClosed());
            break;
        }
        return optionData;
    }

    public static EnumOptionData interestPostingPeriodType(final Integer type) {
        return interestPostingPeriodType(SavingsInterestPostingPeriodType.fromInt(type));
    }

    public static EnumOptionData interestPostingPeriodType(final SavingsInterestPostingPeriodType type) {

        final String codePrefix = "savings.interest.posting.period.";
        EnumOptionData optionData = new EnumOptionData(SavingsInterestPostingPeriodType.INVALID.getValue().longValue(),
                SavingsInterestPostingPeriodType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case MONTHLY:
                optionData = new EnumOptionData(SavingsInterestPostingPeriodType.MONTHLY.getValue().longValue(), codePrefix
                        + SavingsInterestPostingPeriodType.MONTHLY.getCode(), "Monthly");
            break;
            case QUATERLY:
                optionData = new EnumOptionData(SavingsInterestPostingPeriodType.QUATERLY.getValue().longValue(), codePrefix
                        + SavingsInterestPostingPeriodType.QUATERLY.getCode(), "Quarterly");
            break;
            case BI_ANNUAL:
                optionData = new EnumOptionData(SavingsInterestPostingPeriodType.BI_ANNUAL.getValue().longValue(), codePrefix
                        + SavingsInterestPostingPeriodType.BI_ANNUAL.getCode(), "Semi-Annual");
            break;
            case ANNUAL:
                optionData = new EnumOptionData(SavingsInterestPostingPeriodType.ANNUAL.getValue().longValue(), codePrefix
                        + SavingsInterestPostingPeriodType.ANNUAL.getCode(), "Annually");
            break;
        }

        return optionData;
    }

    public static EnumOptionData interestPeriodType(final Integer type) {
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
            case WEEKLY:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.WEEKLY.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.WEEKLY.getCode(), "Weekly");
            break;
            case BIWEEKLY:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.BIWEEKLY.getValue().longValue(), codePrefix
                        + SavingsCompoundingInterestPeriodType.BIWEEKLY.getCode(), "Bi-Weekly");
            break;
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
            case NO_COMPOUNDING_SIMPLE_INTEREST:
                optionData = new EnumOptionData(SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST.getValue().longValue(),
                        codePrefix + SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST.getCode(),
                        "No Compounding - Simple Interest");
            break;
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
}