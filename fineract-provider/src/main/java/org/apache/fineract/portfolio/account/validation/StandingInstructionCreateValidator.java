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
package org.apache.fineract.portfolio.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.MonthDay;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

/**
 * The cross-field half of the create validation; the single-field rules live as constraints on
 * {@link StandingInstructionCreationRequest} itself.
 */
public class StandingInstructionCreateValidator
        implements ConstraintValidator<ValidStandingInstructionCreate, StandingInstructionCreationRequest> {

    private static final String MSG_PREFIX = "{org.apache.fineract.portfolio.account.standinginstruction.";

    private static final String VALID_FROM = "validFrom";
    private static final String VALID_TILL = "validTill";
    private static final String AMOUNT = "amount";
    private static final String TRANSFER_TYPE = "transferType";
    private static final String INSTRUCTION_TYPE = "instructionType";
    private static final String RECURRENCE_TYPE = "recurrenceType";
    private static final String RECURRENCE_FREQUENCY = "recurrenceFrequency";
    private static final String RECURRENCE_INTERVAL = "recurrenceInterval";
    private static final String RECURRENCE_ON_MONTH_DAY = "recurrenceOnMonthDay";

    @Override
    public boolean isValid(final StandingInstructionCreationRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();

        boolean valid = checkDates(request, context);
        valid &= checkRecurrence(request, context);
        valid &= checkInstructionTypeAgainstAmount(request, context);
        valid &= checkSavingsTargetRestrictions(request, context);
        valid &= checkTransferTypeAgainstAccountTypes(request, context);
        return valid;
    }

    private boolean checkDates(final StandingInstructionCreationRequest request, final ConstraintValidatorContext context) {
        final LocalDate validFrom = request.validFromAsDate();
        if (validFrom == null) {
            return violation(context, MSG_PREFIX + "valid-from.not-null}", VALID_FROM);
        }
        if (StringUtils.isBlank(request.getValidTill())) {
            return true;
        }
        final LocalDate validTill = request.validTillAsDate();
        if (validTill == null) {
            return violation(context, MSG_PREFIX + "valid-till.invalid}", VALID_TILL);
        }
        return validTill.isAfter(validFrom) || violation(context, MSG_PREFIX + "valid-till.not-after-valid-from}", VALID_TILL);
    }

    /**
     * A periodic recurrence needs an interval and a frequency, and a monthly or yearly frequency additionally needs the
     * day/month it recurs on.
     */
    private boolean checkRecurrence(final StandingInstructionCreationRequest request, final ConstraintValidatorContext context) {
        boolean valid = true;
        final Integer recurrenceType = request.getRecurrenceType();
        final Integer recurrenceFrequency = request.getRecurrenceFrequency();

        if (recurrenceType != null && AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence()) {
            if (request.getRecurrenceInterval() == null) {
                valid = violation(context, MSG_PREFIX + "recurrence-interval.not-null}", RECURRENCE_INTERVAL);
            }
            if (recurrenceFrequency == null) {
                valid = violation(context, MSG_PREFIX + "recurrence-frequency.not-null}", RECURRENCE_FREQUENCY);
            }
        }

        if (recurrenceFrequency != null) {
            final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
            if ((frequencyType.isMonthly() || frequencyType.isYearly()) && monthDayOf(request) == null) {
                valid = violation(context, MSG_PREFIX + "recurrence-on-month-day.not-null}", RECURRENCE_ON_MONTH_DAY);
            }
        }
        return valid;
    }

    private boolean checkInstructionTypeAgainstAmount(final StandingInstructionCreationRequest request,
            final ConstraintValidatorContext context) {
        final Integer instructionType = request.getInstructionType();
        if (instructionType == null) {
            return true;
        }
        final StandingInstructionType type = StandingInstructionType.fromInt(instructionType);
        if (type.isFixedAmoutTransfer() && request.getAmount() == null) {
            return violation(context, MSG_PREFIX + "amount.not-null}", AMOUNT);
        }
        if (type.isDuesAmoutTransfer() && request.getAmount() != null) {
            return violation(context, MSG_PREFIX + "amount.not-supported-for-dues}", AMOUNT);
        }
        return true;
    }

    /**
     * Transfers into a savings account only support a fixed amount on a periodic recurrence.
     */
    private boolean checkSavingsTargetRestrictions(final StandingInstructionCreationRequest request,
            final ConstraintValidatorContext context) {
        final Integer toAccountType = request.getToAccountType();
        if (toAccountType == null || !PortfolioAccountType.SAVINGS.equals(PortfolioAccountType.fromInt(toAccountType))) {
            return true;
        }
        boolean valid = true;
        if (!Integer.valueOf(1).equals(request.getInstructionType())) {
            valid = violation(context, MSG_PREFIX + "instruction-type.invalid-for-savings-target}", INSTRUCTION_TYPE);
        }
        if (!Integer.valueOf(1).equals(request.getRecurrenceType())) {
            valid = violation(context, MSG_PREFIX + "recurrence-type.invalid-for-savings-target}", RECURRENCE_TYPE);
        }
        return valid;
    }

    private boolean checkTransferTypeAgainstAccountTypes(final StandingInstructionCreationRequest request,
            final ConstraintValidatorContext context) {
        final Integer transferType = request.getTransferType();
        final Integer fromAccountType = request.getFromAccountType();
        final Integer toAccountType = request.getToAccountType();
        if (transferType == null || fromAccountType == null || toAccountType == null) {
            return true;
        }

        final AccountTransferType accountTransferType = AccountTransferType.fromInt(transferType);
        final PortfolioAccountType from = PortfolioAccountType.fromInt(fromAccountType);
        final PortfolioAccountType to = PortfolioAccountType.fromInt(toAccountType);

        if (accountTransferType.isAccountTransfer() && (PortfolioAccountType.LOAN.equals(from) || PortfolioAccountType.LOAN.equals(to))) {
            return violation(context, MSG_PREFIX + "transfer-type.not-account-transfer}", TRANSFER_TYPE);
        }
        if (accountTransferType.isLoanRepayment() && (PortfolioAccountType.LOAN.equals(from) || PortfolioAccountType.SAVINGS.equals(to))) {
            return violation(context, MSG_PREFIX + "transfer-type.not-loan-repayment}", TRANSFER_TYPE);
        }
        return true;
    }

    private MonthDay monthDayOf(final StandingInstructionCreationRequest request) {
        return request.recurrenceOnMonthDayAsMonthDay();
    }

    /**
     * Records a constraint violation on {@code property} and always returns {@code false}, so callers can write
     * {@code condition || violation(...)}.
     */
    private static boolean violation(final ConstraintValidatorContext context, final String template, final String property) {
        context.buildConstraintViolationWithTemplate(template).addPropertyNode(property).addConstraintViolation();
        return false;
    }
}
