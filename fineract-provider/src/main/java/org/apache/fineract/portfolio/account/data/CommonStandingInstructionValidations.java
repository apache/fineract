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
package org.apache.fineract.portfolio.account.data;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.monthDayFormatParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.nameParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public abstract class CommonStandingInstructionValidations implements StandingInstructionValidator {
    protected final StandingInstructionHelper standingInstructionHelper;
    protected final StandingInstruction standingInstruction;
    protected final DataValidatorBuilder baseDataValidator;

    protected CommonStandingInstructionValidations(final StandingInstructionHelper standingInstructionHelper, final StandingInstruction standingInstruction, final DataValidatorBuilder baseDataValidator) {
        this.standingInstructionHelper = standingInstructionHelper;
        this.standingInstruction = standingInstruction;
        this.baseDataValidator = baseDataValidator;
    }

    @Override
    public final void validate() {
        validateCommonFields();
        validateSpecificFields();
    }

    private void validateCommonFields() {
        final Integer transferType = this.standingInstruction.getTransferType();
        this.baseDataValidator.reset().parameter(transferTypeParamName).value(transferType).notNull().inMinMaxRange(1, 3);

        final String name = this.standingInstruction.getName();
        this.baseDataValidator.reset().parameter(nameParamName).value(name).notBlank();

        final Integer priority = this.standingInstruction.getPriority();
        this.baseDataValidator.reset().parameter(priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);

        final Integer instructionType = this.standingInstruction.getInstructionType();
        this.baseDataValidator.reset().parameter(instructionTypeParamName).value(instructionType).notNull().inMinMaxRange(1, 2);

        final Integer status = this.standingInstruction.getStatus();
        this.baseDataValidator.reset().parameter(statusParamName).value(status).notNull().inMinMaxRange(1, 2);

        final LocalDate validFrom = this.standingInstruction.getValidFrom();
        this.baseDataValidator.reset().parameter(validFromParamName).value(validFrom).notNull();

        final LocalDate validTill = this.standingInstruction.getValidTill();
        this.baseDataValidator.reset().parameter(validTillParamName).value(validTill).validateDateAfter(validFrom);

        final Integer recurrenceType = this.standingInstruction.getRecurrenceType();
        this.baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2); 
    }
    
    protected abstract void validateSpecificFields();

    protected boolean isValidAccountTransfer(final Integer fromAccountType, final Integer toAccountType) {
        return this.standingInstructionHelper.isSavingsAccount(fromAccountType) && 
                this.standingInstructionHelper.isSavingsAccount(toAccountType);
    }

    protected boolean isValidLoanRepayment(final Integer fromAccountType, final Integer toAccountType) {
        return this.standingInstructionHelper.isSavingsAccount(fromAccountType) && 
                this.standingInstructionHelper.isLoanAccount(toAccountType);
    }

    protected boolean isSelfAccountTransfer(final Long fromOfficeId, final Long toOfficeId, final Long fromAccountId, final Long toAccountId) {
        if (fromOfficeId == null || toOfficeId == null || fromAccountId == null || toAccountId == null) {
            return false;
        }

        return Objects.equals(fromOfficeId, toOfficeId) && Objects.equals(fromAccountId, toAccountId);
    }

    protected void validatePeriodicFields() {
        final Integer recurrenceFrequency = this.standingInstruction.getRecurrenceFrequency();
        this.baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).notNull().inMinMaxRange(0, 3);

        final Integer recurrenceInterval = this.standingInstruction.getRecurrenceInterval();
        this.baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).notNull().integerGreaterThanZero();

        if (!isValidFrequencyData(recurrenceFrequency, recurrenceInterval)) {
            return;
        }

        MonthDay monthDay = null;
        if (isMonthlyOrYearlyFrequency(recurrenceFrequency)) {
            monthDay = this.standingInstruction.getMonthDay();
        }

        final LocalDate validFrom = this.standingInstruction.getValidFrom();
        if (!areValidDates(validFrom)) {
            return;
        }

        final LocalDate firstExecutionDate = getFirstExecutionDate(validFrom, recurrenceFrequency, recurrenceInterval, monthDay);

        final LocalDate validTill = this.standingInstruction.getValidTill();
        if (isValidTillBeforeFirstExecution(validFrom, firstExecutionDate, validTill)) {
            this.baseDataValidator.reset().parameter(validTillParamName).value(validTill)
                    .failWithCode(StandingInstructionApiConstants.BEFORE_FIRST_EXECUTION_DATE_ERROR_CODE);
        }
    }

    private boolean isValidFrequencyData(final Integer recurrenceFrequency, final Integer recurrenceInterval) {
        if (recurrenceFrequency == null || recurrenceInterval == null) {
            return false;
        }
        if (recurrenceFrequency < 0 || recurrenceFrequency > 4) {
            return false;
        }
        if (recurrenceInterval < 1) {
            return false;
        }

        return true;
    }

    private boolean isMonthlyOrYearlyFrequency(final Integer recurrenceFrequency) {
        final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
        return frequencyType != null && (frequencyType.isMonthly() || frequencyType.isYearly());
    }

    private boolean areValidDates(LocalDate... dates) {
        return Arrays
                .stream(dates)
                .allMatch(Objects::nonNull);
    }

    private LocalDate getFirstExecutionDate(final LocalDate validFrom, final Integer recurrenceFrequency, final Integer recurrenceInterval, final MonthDay monthDay) {
        final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);

        if (frequencyType == null) {
            return null;
        }

        if (frequencyType.isDaily()) {
            return validFrom.plusDays(recurrenceInterval);
        }

        if (frequencyType.isWeekly()) {
            return validFrom.plusWeeks(recurrenceInterval);
        }

        if (monthDay == null) {
            return null;
        }

        LocalDate date = calculateBaseDate(frequencyType, validFrom, monthDay);

        if (areValidDates(date) && !date.isAfter(validFrom)) {
            date = advanceToNextCycle(frequencyType, date);
        }

        return date;
    }

    private boolean isValidTillBeforeFirstExecution(final LocalDate validFrom, final LocalDate firstExecutionDate, final LocalDate validTill) {
        if (!areValidDates(firstExecutionDate, validTill)) {
            return false;
        }

        return validTill.isBefore(firstExecutionDate);
    }

    private LocalDate calculateBaseDate(final PeriodFrequencyType frequencyType, final LocalDate validFrom, final MonthDay monthDay) {
        if (frequencyType.isMonthly()) {
            return LocalDate.of(validFrom.getYear(), validFrom.getMonth(), monthDay.getDayOfMonth());
        }
        if (frequencyType.isYearly()) {
            return monthDay.atYear(validFrom.getYear());
        }
        return null;
    }

    private LocalDate advanceToNextCycle(final PeriodFrequencyType frequencyType, final LocalDate date) {
        if (frequencyType.isMonthly()) {
            return date.plusMonths(1);
        }
        if (frequencyType.isYearly()) {
            return date.plusYears(1);
        }
        return date;
    }

    protected void validateAmountForFixedInstructionType() {
        final BigDecimal amount = this.standingInstruction.getAmount();
        this.baseDataValidator.reset().parameter(amountParamName).value(amount).notNull().positiveAmount();
    }

    protected void validateAmountForDuesInstructionType() {
        final BigDecimal amount = this.standingInstruction.getAmount();

        if (amount != null) {
            this.baseDataValidator.reset().parameter(amountParamName)
                    .failWithCode(StandingInstructionApiConstants.AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE);
        }
    }
}