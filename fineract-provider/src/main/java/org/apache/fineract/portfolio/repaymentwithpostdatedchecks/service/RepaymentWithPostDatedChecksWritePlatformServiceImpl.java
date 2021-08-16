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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.exception.PostDatedCheckNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepaymentWithPostDatedChecksWritePlatformServiceImpl implements RepaymentWithPostDatedChecksWritePlatformService {

    private final PostDatedChecksRepository postDatedChecksRepository;
    private final FromJsonHelper fromApiJsonHelper;
    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParametersForUpdate = new HashSet<>(Arrays.asList("name", "amount", "accountNo", "locale"));
    private final Set<String> supportedParametersForBounce = new HashSet<>(
            Arrays.asList("name", "amount", "accountNo", "checkNo", "locale"));

    @Autowired
    public RepaymentWithPostDatedChecksWritePlatformServiceImpl(final PostDatedChecksRepository postDatedChecksRepository,
            final FromJsonHelper fromApiJsonHelper) {
        this.postDatedChecksRepository = postDatedChecksRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Transactional
    @Override
    public CommandProcessingResult addPostDatedChecks(JsonCommand command) {
        return CommandProcessingResult.empty();
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
        final PostDatedChecks postDatedChecks = this.postDatedChecksRepository.findById(command.entityId())
                .orElseThrow(() -> new PostDatedCheckNotFoundException(command.entityId()));
        Map<String, Object> changes = postDatedChecks.bouncePostDatedChecks(command);
        this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId()).with(changes)
                .build();
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

        // if (command.parameterExists("repaymentDate")) {
        // final LocalDate repaymentDate = this.fromApiJsonHelper.extractLocalDateNamed("repaymentDate", jsonElement);
        // baseDataValidator.reset().parameter("repaymentDate").value(repaymentDate).notNull();
        // }

        if (command.parameterExists("accountNo")) {
            final Long accountNo = this.fromApiJsonHelper.extractLongNamed("accountNo", jsonElement);
            baseDataValidator.reset().parameter("accountNo").value(accountNo).notNull().positiveAmount();
        }

        if (command.parameterExists("name")) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", jsonElement);
            baseDataValidator.reset().parameter("name").value(name).notNull();
        }

        // if (command.parameterExists("dateFormat")) {
        // final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(jsonElement.getAsJsonObject());
        // baseDataValidator.reset().parameter("dateFormat").value(dateFormat).notNull();
        // }

        // if (command.parameterExists("date")) {
        // final LocalDate date = this.fromApiJsonHelper.extractLocalDateNamed("date", jsonElement);
        // baseDataValidator.reset().parameter("date").value(date).notNull();
        // }

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
