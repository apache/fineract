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
package org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksStatus;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.exception.PostDatedCheckNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepaymentWithPostDatedChecksWritePlatformServiceImpl implements RepaymentWithPostDatedChecksWritePlatformService {

    private final PostDatedChecksRepository postDatedChecksRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;
    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParametersForUpdate = new HashSet<>(Arrays.asList("name", "amount", "accountNo", "locale"));
    private final Set<String> supportedParametersForBounce = new HashSet<>(
            Arrays.asList("name", "amount", "accountNo", "checkNo", "locale", "installmentId"));

    @Autowired
    public RepaymentWithPostDatedChecksWritePlatformServiceImpl(final PostDatedChecksRepository postDatedChecksRepository,
            final FromJsonHelper fromApiJsonHelper, final LoanRepository loanRepository) {
        this.postDatedChecksRepository = postDatedChecksRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanRepository = loanRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult updatePostDatedChecks(JsonCommand command) {
        validateForUpdate(command);
        final PostDatedChecks postDatedChecks = this.postDatedChecksRepository.findById(command.entityId())
                .orElseThrow(() -> new PostDatedCheckNotFoundException(command.entityId()));
        Map<String, Object> changes = postDatedChecks.updatePostDatedChecks(command);
        this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId()).with(changes)
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult bouncePostDatedChecks(JsonCommand command) {
        validateForBounce(command);

        final PostDatedChecks postDatedCheck = this.postDatedChecksRepository.findById(command.entityId())
                .orElseThrow(() -> new PostDatedCheckNotFoundException(command.entityId()));
        postDatedCheck.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_BOUNCED);

        final Loan loan = this.loanRepository.findById(command.getLoanId())
                .orElseThrow(() -> new LoanNotFoundException(command.getLoanId()));

        final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonObject);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", jsonObject);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", jsonObject, locale);

        final Integer installmentId = this.fromApiJsonHelper.extractIntegerNamed("installmentId", jsonObject, locale);

        final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", jsonObject);
        final Long checkNo = this.fromApiJsonHelper.extractLongNamed("checkNo", jsonObject);

        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();

        final List<LoanRepaymentScheduleInstallment> installmentList = loanRepaymentScheduleInstallments.stream()
                .filter(repayment -> repayment.getInstallmentNumber().equals(installmentId)).collect(Collectors.toList());

        final PostDatedChecks postDatedChecks = PostDatedChecks.instanceOf(accountNo, name, amount, installmentList.get(0), loan, checkNo);
        try {
            this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("postdatedChecks");
            if (realCause.getMessage().toLowerCase().contains("transaction has been rolled back")) {
                baseDataValidator.reset().parameter("checkNo").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(Integer.toUnsignedLong(installmentId))
                .withLoanId(postDatedChecks.getLoan().getId()).build();
    }

    private void validateForBounce(JsonCommand command) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), this.supportedParametersForBounce);

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("repayment-with-post-dated-checks");

        if (!command.parameterExists("locale")) {
            baseDataValidator.reset().parameter("locale").notNull().failWithCode("locale.not.exists");
        } else {
            final String locale = this.fromApiJsonHelper.extractStringNamed("locale", jsonElement);
            baseDataValidator.reset().parameter("locale").value(locale).notNull();
        }

        if (command.parameterExists("amount")) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", jsonElement);
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();
        }

        if (command.parameterExists("accountNo")) {
            final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", jsonElement);
            baseDataValidator.reset().parameter("accountNo").value(accountNo).notNull().positiveAmount();
        }

        if (command.parameterExists("name")) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", jsonElement);
            baseDataValidator.reset().parameter("name").value(name).notNull();
        }

        if (command.parameterExists("checkNo")) {
            final Long checkNo = this.fromApiJsonHelper.extractLongNamed("checkNo", jsonElement);
            baseDataValidator.reset().parameter("checkNo").value(checkNo).notNull().longGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    private void validateForUpdate(JsonCommand command) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), this.supportedParametersForUpdate);

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("repayment-with-post-dated-checks");

        if (!command.parameterExists("locale")) {
            baseDataValidator.reset().parameter("locale").notNull().failWithCode("locale.not.exists");
        } else {
            final String locale = this.fromApiJsonHelper.extractStringNamed("locale", jsonElement);
            baseDataValidator.reset().parameter("locale").value(locale).notNull();
        }

        if (command.parameterExists("amount")) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", jsonElement);
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();
        }

        if (command.parameterExists("accountNo")) {
            final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", jsonElement);
            baseDataValidator.reset().parameter("accountNo").value(accountNo).notNull().positiveAmount();
        }

        if (command.parameterExists("name")) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", jsonElement);
            baseDataValidator.reset().parameter("name").value(name).notNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult deletePostDatedChecks(final JsonCommand command) {
        this.postDatedChecksRepository.deleteById(command.entityId());
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withLoanId(command.getLoanId())
                .withEntityId(command.entityId()).build();
    }
}
