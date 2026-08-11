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
package org.apache.fineract.portfolio.account.service;

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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionDeleteRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdateRequest;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionCreateResponse;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionDeleteResponse;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionUpdateResponse;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.exception.StandingInstructionNotFoundException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class StandingInstructionWriteServiceImpl implements StandingInstructionWriteService {

    private final Validator validator;
    private final AccountTransferDetailAssembler accountTransferDetailAssembler;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final StandingInstructionRepository standingInstructionRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final LoanAssembler loanAccountAssembler;

    @Transactional
    @Override
    public StandingInstructionCreateResponse create(final StandingInstructionCreationRequest request) {
        validate(request);

        final PortfolioAccountType fromType = PortfolioAccountType.fromInt(request.getFromAccountType());
        final PortfolioAccountType toType = PortfolioAccountType.fromInt(request.getToAccountType());
        final String name = request.getName();

        Long standingInstructionId = null;
        try {
            final AccountTransferDetails details = assembleAccountTransferDetails(request.getFromAccountId(), request.getToAccountId(),
                    request.getTransferType(), fromType, toType);

            BigDecimal transferAmount = request.getAmount();
            if (transferAmount != null && details.fromSavingsAccount() != null) {
                transferAmount = Money.of(details.fromSavingsAccount().getCurrency(), transferAmount).getAmount();
            }

            final AccountTransferStandingInstruction standingInstruction = AccountTransferStandingInstruction.create(details, name,
                    request.getPriority(), request.getInstructionType(), request.getStatus(), transferAmount, request.validFromAsDate(),
                    request.validTillAsDate(), request.getRecurrenceType(), request.getRecurrenceFrequency(),
                    request.getRecurrenceInterval(), request.recurrenceOnMonthDayAsMonthDay());
            validateDependencies(standingInstruction);
            details.updateAccountTransferStandingInstruction(standingInstruction);

            this.accountTransferDetailRepository.saveAndFlush(details);
            standingInstructionId = details.accountTransferStandingInstruction().getId();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(name, dve.getMostSpecificCause(), dve);
        }

        return StandingInstructionCreateResponse.builder().resourceId(standingInstructionId).clientId(request.getFromClientId()).build();
    }

    @Transactional
    @Override
    public StandingInstructionUpdateResponse update(final StandingInstructionUpdateRequest request) {
        validate(request);

        final AccountTransferStandingInstruction standingInstruction = this.standingInstructionRepository.findById(request.getId())
                .orElseThrow(() -> new StandingInstructionNotFoundException(request.getId()));

        final Map<String, Object> changes = applyChanges(standingInstruction, request);
        validateDependencies(standingInstruction);

        if (!changes.isEmpty()) {
            this.standingInstructionRepository.save(standingInstruction);
        }

        return StandingInstructionUpdateResponse.builder().resourceId(request.getId()).changes(changes).build();
    }

    @Transactional
    @Override
    public StandingInstructionDeleteResponse delete(final StandingInstructionDeleteRequest request) {
        final AccountTransferStandingInstruction standingInstruction = this.standingInstructionRepository.findById(request.getId())
                .orElseThrow(() -> new StandingInstructionNotFoundException(request.getId()));
        standingInstruction.delete();
        this.standingInstructionRepository.save(standingInstruction);
        return StandingInstructionDeleteResponse.builder().resourceId(request.getId()).build();
    }

    private <T> void validate(final T request) {
        final Set<ConstraintViolation<T>> violations = this.validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    /**
     * Applies the (partial) update request to the standing instruction, recording every value that actually changed.
     * Only the values that are present in the request are touched.
     */
    private Map<String, Object> applyChanges(final AccountTransferStandingInstruction instruction,
            final StandingInstructionUpdateRequest request) {
        if (StandingInstructionStatus.fromInt(instruction.getStatus()).isDeleted()) {
            final List<ApiParameterError> errors = new ArrayList<>();
            new DataValidatorBuilder(errors).resource(STANDING_INSTRUCTION_RESOURCE_NAME).reset().parameter(statusParamName)
                    .failWithCode("can.not.modify.once.deleted");
            throw new PlatformApiDataValidationException(errors);
        }

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LocalDate validFrom = request.validFromAsDate();
        if (isChanged(validFrom, instruction.getValidFrom())) {
            instruction.setValidFrom(validFrom);
            actualChanges.put(validFromParamName, validFrom);
        }

        final LocalDate validTill = request.validTillAsDate();
        if (isChanged(validTill, instruction.getValidTill())) {
            instruction.setValidTill(validTill);
            actualChanges.put(validTillParamName, validTill);
        }

        final BigDecimal amount = request.getAmount();
        if (amount != null && (instruction.getAmount() == null || instruction.getAmount().compareTo(amount) != 0)) {
            instruction.setAmount(amount);
            actualChanges.put(amountParamName, amount);
        }

        if (isChanged(request.getStatus(), instruction.getStatus())) {
            instruction.setStatus(request.getStatus());
            actualChanges.put(statusParamName, request.getStatus());
        }

        if (isChanged(request.getPriority(), instruction.getPriority())) {
            instruction.setPriority(request.getPriority());
            actualChanges.put(priorityParamName, request.getPriority());
        }

        if (isChanged(request.getInstructionType(), instruction.getInstructionType())) {
            instruction.setInstructionType(request.getInstructionType());
            actualChanges.put(instructionTypeParamName, request.getInstructionType());
        }

        if (isChanged(request.getRecurrenceType(), instruction.getRecurrenceType())) {
            instruction.setRecurrenceType(request.getRecurrenceType());
            actualChanges.put(recurrenceTypeParamName, request.getRecurrenceType());
        }

        if (isChanged(request.getRecurrenceFrequency(), instruction.getRecurrenceFrequency())) {
            instruction.setRecurrenceFrequency(request.getRecurrenceFrequency());
            actualChanges.put(recurrenceFrequencyParamName, request.getRecurrenceFrequency());
        }

        final MonthDay recurrenceOnMonthDay = request.recurrenceOnMonthDayAsMonthDay();
        if (recurrenceOnMonthDay != null) {
            if (isChanged(recurrenceOnMonthDay.getDayOfMonth(), instruction.getRecurrenceOnDay())) {
                instruction.setRecurrenceOnDay(recurrenceOnMonthDay.getDayOfMonth());
                actualChanges.put(recurrenceOnMonthDayParamName, recurrenceOnMonthDay.toString());
            }
            if (isChanged(recurrenceOnMonthDay.getMonthValue(), instruction.getRecurrenceOnMonth())) {
                instruction.setRecurrenceOnMonth(recurrenceOnMonthDay.getMonthValue());
                actualChanges.put(recurrenceOnMonthDayParamName, recurrenceOnMonthDay.toString());
            }
        }

        if (isChanged(request.getRecurrenceInterval(), instruction.getRecurrenceInterval())) {
            instruction.setRecurrenceInterval(request.getRecurrenceInterval());
            actualChanges.put(recurrenceIntervalParamName, request.getRecurrenceInterval());
        }

        return actualChanges;
    }

    private static boolean isChanged(final Object requestValue, final Object currentValue) {
        return requestValue != null && !Objects.equals(requestValue, currentValue);
    }

    /**
     * The rules that can only be checked once the instruction is assembled, because they depend on the linked accounts
     * rather than on the request alone. Used for both create and update, so an update is validated against the merged
     * (post-update) state.
     */
    private void validateDependencies(final AccountTransferStandingInstruction instruction) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);

        final AccountTransferDetails details = instruction.getAccountTransferDetails();

        if (instruction.getValidTill() != null && instruction.getValidFrom() != null) {
            baseDataValidator.reset().parameter(validTillParamName).value(instruction.getValidTill())
                    .validateDateAfter(instruction.getValidFrom());
        }

        if (AccountTransferRecurrenceType.fromInt(instruction.getRecurrenceType()).isPeriodicRecurrence()) {
            baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(instruction.getRecurrenceFrequency()).notNull();
            baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(instruction.getRecurrenceInterval()).notNull();
            if (instruction.getRecurrenceFrequency() != null) {
                final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(instruction.getRecurrenceFrequency());
                if (frequencyType.isMonthly()) {
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(instruction.getRecurrenceOnDay()).notNull();
                } else if (frequencyType.isYearly()) {
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(instruction.getRecurrenceOnDay()).notNull();
                    baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(instruction.getRecurrenceOnMonth()).notNull();
                }
            }
        }

        if (details.toSavingsAccount() != null) {
            baseDataValidator.reset().parameter(instructionTypeParamName).value(instruction.getInstructionType()).notNull().inMinMaxRange(1,
                    1);
            baseDataValidator.reset().parameter(recurrenceTypeParamName).value(instruction.getRecurrenceType()).notNull().inMinMaxRange(1,
                    1);
        }

        if (StandingInstructionType.fromInt(instruction.getInstructionType()).isFixedAmoutTransfer()) {
            baseDataValidator.reset().parameter(amountParamName).value(instruction.getAmount()).notNull();
        }

        String errorCode = null;
        if (details.transferType().isAccountTransfer() && (details.fromSavingsAccount() == null || details.toSavingsAccount() == null)) {
            errorCode = "not.account.transfer";
        } else if (details.transferType().isLoanRepayment() && (details.fromSavingsAccount() == null || details.toLoanAccount() == null)) {
            errorCode = "not.loan.repayment";
        }
        if (errorCode != null) {
            baseDataValidator.reset().parameter(transferTypeParamName).failWithCode(errorCode);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private AccountTransferDetails assembleAccountTransferDetails(final Long fromAccountId, final Long toAccountId,
            final Integer transferType, final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        if (PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType)) {
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromAccountId, false);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toAccountId, false);
            return this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(fromSavingsAccount, toSavingsAccount, transferType);
        } else if (PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.LOAN.equals(toAccountType)) {
            final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromAccountId, false);
            final Loan toLoanAccount = this.loanAccountAssembler.assembleFrom(toAccountId);
            return this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(fromSavingsAccount, toLoanAccount, transferType);
        } else if (PortfolioAccountType.LOAN.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType)) {
            final Loan fromLoanAccount = this.loanAccountAssembler.assembleFrom(fromAccountId);
            final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toAccountId, false);
            return this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(fromLoanAccount, toSavingsAccount, transferType);
        }
        throw new PlatformDataIntegrityException("error.msg.standinginstruction.transfer.type.not.supported",
                "Transfer between the given account types is not supported for standing instructions");
    }

    private void handleDataIntegrityIssues(final String name, final Throwable realCause, final NonTransientDataAccessException dve) {
        if (realCause.getMessage() != null && realCause.getMessage().contains("name")) {
            throw new PlatformDataIntegrityException("error.msg.standinginstruction.duplicate.name",
                    "Standinginstruction with name `" + name + "` already exists", "name", name);
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
