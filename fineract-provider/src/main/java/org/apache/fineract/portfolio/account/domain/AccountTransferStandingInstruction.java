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
package org.apache.fineract.portfolio.account.domain;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_account_transfer_standing_instructions", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name") })
public class AccountTransferStandingInstruction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "instruction_type")
    private Integer instructionType;

    @Column(name = "status")
    private Integer status;

    @Column(name = "amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amount;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_from")
    private Date validFrom;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_till")
    private Date validTill;

    @Column(name = "recurrence_type")
    private Integer recurrenceType;

    @Column(name = "recurrence_frequency")
    private Integer recurrenceFrequency;

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval;

    @Column(name = "recurrence_on_day")
    private Integer recurrenceOnDay;

    @Column(name = "recurrence_on_month")
    private Integer recurrenceOnMonth;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_run_date")
    private Date latsRunDate;

    protected AccountTransferStandingInstruction() {

    }

    public static AccountTransferStandingInstruction create(final AccountTransferDetails accountTransferDetails, final String name,
            final Integer priority, final Integer instructionType, final Integer status, final BigDecimal amount,
            final LocalDate validFrom, final LocalDate validTill, final Integer recurrenceType, final Integer recurrenceFrequency,
            final Integer recurrenceInterval, final MonthDay recurrenceOnMonthDay) {
        Integer recurrenceOnDay = null;
        Integer recurrenceOnMonth = null;
        if (recurrenceOnMonthDay != null) {
            recurrenceOnDay = recurrenceOnMonthDay.getDayOfMonth();
            recurrenceOnMonth = recurrenceOnMonthDay.getMonthOfYear();
        }
        return new AccountTransferStandingInstruction(accountTransferDetails, name, priority, instructionType, status, amount, validFrom,
                validTill, recurrenceType, recurrenceFrequency, recurrenceInterval, recurrenceOnDay, recurrenceOnMonth);
    }

    private AccountTransferStandingInstruction(final AccountTransferDetails accountTransferDetails, final String name,
            final Integer priority, final Integer instructionType, final Integer status, final BigDecimal amount,
            final LocalDate validFrom, final LocalDate validTill, final Integer recurrenceType, final Integer recurrenceFrequency,
            final Integer recurrenceInterval, final Integer recurrenceOnDay, final Integer recurrenceOnMonth) {
        this.accountTransferDetails = accountTransferDetails;
        this.name = name;
        this.priority = priority;
        this.instructionType = instructionType;
        this.status = status;
        this.amount = amount;
        if (validFrom != null) {
            this.validFrom = validFrom.toDate();
        }
        if (validTill == null) {
            this.validTill = null;
        } else {
            this.validTill = validTill.toDate();
        }
        this.recurrenceType = recurrenceType;
        this.recurrenceFrequency = recurrenceFrequency;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceOnDay = recurrenceOnDay;
        this.recurrenceOnMonth = recurrenceOnMonth;
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);

        validateDependencies(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public Map<String, Object> update(JsonCommand command) {
        final Map<String, Object> actualChanges = new HashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);

        if (StandingInstructionStatus.fromInt(this.status).isDeleted()) {
            baseDataValidator.reset().parameter(statusParamName).failWithCode("can.not.modify.once.deleted");
        }

        if (command.isChangeInDateParameterNamed(validFromParamName, this.validFrom)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(validFromParamName);
            actualChanges.put(validFromParamName, newValue);
            this.validFrom = newValue.toDate();
        }

        if (command.isChangeInDateParameterNamed(validTillParamName, this.validTill)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(validTillParamName);
            actualChanges.put(validTillParamName, newValue);
            this.validTill = newValue.toDate();
        }

        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            actualChanges.put(amountParamName, newValue);
            this.amount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(statusParamName, this.status)) {
            final Integer newValue = command.integerValueOfParameterNamed(statusParamName);
            actualChanges.put(statusParamName, newValue);
            this.status = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(priorityParamName, this.priority)) {
            final Integer newValue = command.integerValueOfParameterNamed(priorityParamName);
            actualChanges.put(priorityParamName, newValue);
            this.priority = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(instructionTypeParamName, this.instructionType)) {
            final Integer newValue = command.integerValueOfParameterNamed(instructionTypeParamName);
            actualChanges.put(instructionTypeParamName, newValue);
            this.instructionType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(recurrenceTypeParamName, this.recurrenceType)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurrenceTypeParamName);
            actualChanges.put(recurrenceTypeParamName, newValue);
            this.recurrenceType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(recurrenceFrequencyParamName, this.recurrenceFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurrenceFrequencyParamName);
            actualChanges.put(recurrenceFrequencyParamName, newValue);
            this.recurrenceFrequency = newValue;
        }

        if (command.hasParameter(recurrenceOnMonthDayParamName)) {
            final MonthDay monthDay = command.extractMonthDayNamed(recurrenceOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(recurrenceOnMonthDayParamName);
            final Integer dayOfMonthValue = monthDay.getDayOfMonth();
            if (this.recurrenceOnDay != dayOfMonthValue) {
                actualChanges.put(recurrenceOnMonthDayParamName, actualValueEntered);
                this.recurrenceOnDay = dayOfMonthValue;
            }

            final Integer monthOfYear = monthDay.getMonthOfYear();
            if (this.recurrenceOnMonth != monthOfYear) {
                actualChanges.put(recurrenceOnMonthDayParamName, actualValueEntered);
                this.recurrenceOnMonth = monthOfYear;
            }
        }

        if (command.isChangeInIntegerParameterNamed(recurrenceIntervalParamName, this.recurrenceInterval)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurrenceIntervalParamName);
            actualChanges.put(recurrenceIntervalParamName, newValue);
            this.recurrenceInterval = newValue;
        }
        validateDependencies(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        return actualChanges;
    }

    private void validateDependencies(final DataValidatorBuilder baseDataValidator) {

        if (this.validTill != null && this.validFrom != null) {
            baseDataValidator.reset().parameter(validTillParamName).value(LocalDate.fromDateFields(this.validTill))
                    .validateDateAfter(LocalDate.fromDateFields(this.validFrom));
        }

        if (AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence()) {
            baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(this.recurrenceFrequency).notNull();
            baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(this.recurrenceInterval).notNull();
            if (this.recurrenceFrequency != null) {
                PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(this.recurrenceFrequency);
                if (frequencyType.isMonthly()) {
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(this.recurrenceOnDay).notNull();
                } else if (frequencyType.isYearly()) {
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(this.recurrenceOnDay).notNull();
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(this.recurrenceOnMonth).notNull();
                }
            }
        }

        if (this.accountTransferDetails.toSavingsAccount() != null) {
            baseDataValidator.reset().parameter(instructionTypeParamName).value(this.instructionType).notNull().inMinMaxRange(1, 1);
            baseDataValidator.reset().parameter(recurrenceTypeParamName).value(this.recurrenceType).notNull().inMinMaxRange(1, 1);
        }

        if (StandingInstructionType.fromInt(this.instructionType).isFixedAmoutTransfer()) {
            baseDataValidator.reset().parameter(amountParamName).value(this.amount).notNull();
        }

        String errorCode = null;
        if (this.accountTransferDetails.transferType().isAccountTransfer()
                && (this.accountTransferDetails.fromSavingsAccount() == null || this.accountTransferDetails.toSavingsAccount() == null)) {
            errorCode = "not.account.transfer";
        } else if (this.accountTransferDetails.transferType().isLoanRepayment()
                && (this.accountTransferDetails.fromSavingsAccount() == null || this.accountTransferDetails.toLoanAccount() == null)) {
            errorCode = "not.loan.repayment";
        }
        if (errorCode != null) {
            baseDataValidator.reset().parameter(transferTypeParamName).failWithCode(errorCode);
        }

    }

    public void updateLatsRunDate(Date latsRunDate) {
        this.latsRunDate = latsRunDate;
    }
    
    public void updateStatus(Integer status){
        this.status = status;
    }
    
    /** 
     * delete the standing instruction by setting the status to 3 and appending "_deleted_" and the id to the name
     **/
     public void delete() {
         this.status = StandingInstructionStatus.DELETED.getValue();
         this.name = this.name + "_deleted_" + this.getId();
     }
}
