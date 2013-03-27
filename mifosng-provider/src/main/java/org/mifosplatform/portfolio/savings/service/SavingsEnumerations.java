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
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsPeriodFrequencyType;

public class SavingsEnumerations {

    public static EnumOptionData interestRatePeriodFrequencyType(final int id) {
        return interestRatePeriodFrequencyType(SavingsPeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRatePeriodFrequencyType(final SavingsPeriodFrequencyType type) {
        final String codePrefix = "savings.interest.rate.";
        EnumOptionData optionData = new EnumOptionData(SavingsPeriodFrequencyType.INVALID.getValue().longValue(),
                SavingsPeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.DAYS.getCode(), "Per day");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.WEEKS.getCode(), "Per week");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.MONTHS.getCode(), "Per month");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsPeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SavingsPeriodFrequencyType.YEARS.getCode(), "Per year");
            break;
        }
        return optionData;
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

    public static EnumOptionData interestPeriodType(final Integer type) {
        return interestPeriodType(SavingsInterestPeriodType.fromInt(type));
    }

    public static EnumOptionData interestPeriodType(final SavingsInterestPeriodType type) {

        final String codePrefix = "savings.interest.period.";
        EnumOptionData optionData = new EnumOptionData(SavingsInterestPeriodType.INVALID.getValue().longValue(),
                SavingsInterestPeriodType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAILY:
                optionData = new EnumOptionData(SavingsInterestPeriodType.DAILY.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.DAILY.getCode(), "Daily");
            break;
            case WEEKLY:
                optionData = new EnumOptionData(SavingsInterestPeriodType.WEEKLY.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.WEEKLY.getCode(), "Weekly");
            break;
            case BIWEEKLY:
                optionData = new EnumOptionData(SavingsInterestPeriodType.BIWEEKLY.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.BIWEEKLY.getCode(), "Bi-Weekly");
            break;
            case MONTHLY:
                optionData = new EnumOptionData(SavingsInterestPeriodType.MONTHLY.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.MONTHLY.getCode(), "Monthly");
            break;
            case QUATERLY:
                optionData = new EnumOptionData(SavingsInterestPeriodType.QUATERLY.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.QUATERLY.getCode(), "Quarterly");
            break;
            case SEMIANNUAL:
                optionData = new EnumOptionData(SavingsInterestPeriodType.SEMIANNUAL.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.SEMIANNUAL.getCode(), "Semi-Annual");
            break;
            case ANNUAL:
                optionData = new EnumOptionData(SavingsInterestPeriodType.ANNUAL.getValue().longValue(), codePrefix
                        + SavingsInterestPeriodType.ANNUAL.getCode(), "Annually");
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