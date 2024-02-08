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
import lombok.RequiredArgsConstructor;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RepaymentWithPostDatedChecksWritePlatformServiceImpl implements RepaymentWithPostDatedChecksWritePlatformService {

    public static final String NAME = "name";
    public static final String AMOUNT = "amount";
    public static final String ACCOUNT_NO = "accountNo";
    public static final String LOCALE = "locale";
    public static final String CHECK_NO = "checkNo";
    public static final String INSTALLMENT_ID = "installmentId";
    public static final String REPAYMENT_WITH_POST_DATED_CHECKS = "repayment-with-post-dated-checks";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(NAME, AMOUNT, ACCOUNT_NO, LOCALE));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_BOUNCE = new HashSet<>(
            Arrays.asList(NAME, AMOUNT, ACCOUNT_NO, CHECK_NO, LOCALE, INSTALLMENT_ID));
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;

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

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, jsonObject);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(AMOUNT, jsonObject, locale);

        final Integer installmentId = this.fromApiJsonHelper.extractIntegerNamed(INSTALLMENT_ID, jsonObject, locale);

        final Long accountNo = this.fromApiJsonHelper.extractLongNamed(ACCOUNT_NO, jsonObject);
        final Long checkNo = this.fromApiJsonHelper.extractLongNamed(CHECK_NO, jsonObject);

        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();

        final List<LoanRepaymentScheduleInstallment> installmentList = loanRepaymentScheduleInstallments.stream()
                .filter(repayment -> repayment.getInstallmentNumber().equals(installmentId)).toList();

        final PostDatedChecks postDatedChecks = PostDatedChecks.instanceOf(accountNo, name, amount, installmentList.get(0), loan, checkNo);
        try {
            this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("postdatedChecks");
            if (realCause.getMessage().toLowerCase().contains("transaction has been rolled back")) {
                baseDataValidator.reset().parameter(CHECK_NO).failWithCode("value.must.be.unique");
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

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), SUPPORTED_PARAMETERS_FOR_BOUNCE);

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(REPAYMENT_WITH_POST_DATED_CHECKS);

        if (!command.parameterExists(LOCALE)) {
            baseDataValidator.reset().parameter(LOCALE).notNull().failWithCode("locale.not.exists");
        } else {
            final String locale = this.fromApiJsonHelper.extractStringNamed(LOCALE, jsonElement);
            baseDataValidator.reset().parameter(LOCALE).value(locale).notNull();
        }

        if (command.parameterExists(AMOUNT)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT, jsonElement);
            baseDataValidator.reset().parameter(AMOUNT).value(amount).notNull().positiveAmount();
        }

        if (command.parameterExists(ACCOUNT_NO)) {
            final Long accountNo = this.fromApiJsonHelper.extractLongNamed(ACCOUNT_NO, jsonElement);
            baseDataValidator.reset().parameter(ACCOUNT_NO).value(accountNo).notNull().positiveAmount();
        }

        if (command.parameterExists(NAME)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, jsonElement);
            baseDataValidator.reset().parameter(NAME).value(name).notNull();
        }

        if (command.parameterExists(CHECK_NO)) {
            final Long checkNo = this.fromApiJsonHelper.extractLongNamed(CHECK_NO, jsonElement);
            baseDataValidator.reset().parameter(CHECK_NO).value(checkNo).notNull().longGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    private void validateForUpdate(JsonCommand command) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), SUPPORTED_PARAMETERS_FOR_UPDATE);

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(REPAYMENT_WITH_POST_DATED_CHECKS);

        if (!command.parameterExists(LOCALE)) {
            baseDataValidator.reset().parameter(LOCALE).notNull().failWithCode("locale.not.exists");
        } else {
            final String locale = this.fromApiJsonHelper.extractStringNamed(LOCALE, jsonElement);
            baseDataValidator.reset().parameter(LOCALE).value(locale).notNull();
        }

        if (command.parameterExists(AMOUNT)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT, jsonElement);
            baseDataValidator.reset().parameter(AMOUNT).value(amount).notNull().positiveAmount();
        }

        if (command.parameterExists(ACCOUNT_NO)) {
            final Long accountNo = this.fromApiJsonHelper.extractLongNamed(ACCOUNT_NO, jsonElement);
            baseDataValidator.reset().parameter(ACCOUNT_NO).value(accountNo).notNull().positiveAmount();
        }

        if (command.parameterExists(NAME)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, jsonElement);
            baseDataValidator.reset().parameter(NAME).value(name).notNull();
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
