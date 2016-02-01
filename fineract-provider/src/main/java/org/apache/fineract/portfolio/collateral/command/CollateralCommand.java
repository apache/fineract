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
package org.apache.fineract.portfolio.collateral.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.collateral.api.CollateralApiConstants.COLLATERAL_JSON_INPUT_PARAMS;

/**
 * Immutable command for creating or updating details of a Collateral.
 */
public class CollateralCommand {

    private final Long collateralTypeId;
    private final BigDecimal value;
    private final String description;

    public CollateralCommand(final Long collateralTypeId, final BigDecimal value, final String description) {
        this.collateralTypeId = collateralTypeId;
        this.value = value;
        this.description = description;
    }

    public Long getCollateralTypeId() {
        return this.collateralTypeId;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }

    public void validateForCreate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collateral");

        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.COLLATERAL_TYPE_ID.getValue()).value(this.collateralTypeId)
                .notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.VALUE.getValue()).value(this.value).ignoreIfNull()
                .positiveAmount();
        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collateral");

        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.COLLATERAL_TYPE_ID.getValue()).value(this.collateralTypeId)
                .ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.VALUE.getValue()).value(this.value).ignoreIfNull()
                .positiveAmount();
        baseDataValidator.reset().parameter(COLLATERAL_JSON_INPUT_PARAMS.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}