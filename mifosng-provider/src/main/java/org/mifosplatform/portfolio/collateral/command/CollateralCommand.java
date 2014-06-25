/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.collateral.api.CollateralApiConstants.COLLATERAL_JSON_INPUT_PARAMS;

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