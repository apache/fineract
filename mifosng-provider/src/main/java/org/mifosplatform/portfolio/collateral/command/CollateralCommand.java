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
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collateral");

        baseDataValidator.reset().parameter("collateralTypeId").value(this.collateralTypeId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("value").value(this.value).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("description").value(this.description).ignoreIfNull().notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collateral");

        baseDataValidator.reset().parameter("collateralTypeId").value(this.collateralTypeId).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("value").value(this.value).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("description").value(this.description).ignoreIfNull().notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}