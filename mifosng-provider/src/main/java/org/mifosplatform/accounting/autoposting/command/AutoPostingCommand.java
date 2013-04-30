/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for adding an accounting closure
 */
public class AutoPostingCommand {

    @SuppressWarnings("unused")
    private final Long id;
    private final Long officeId;
    private final LocalDate closingDate;
    private final String comments;

    public AutoPostingCommand(final Long id, final Long officeId, final LocalDate closingDate, final String comments) {
        this.id = id;
        this.officeId = officeId;
        this.closingDate = closingDate;
        this.comments = comments;
    }

    public void validateForCreate() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter(GLClosureJsonInputParams.CLOSING_DATE.getValue()).value(this.closingDate).notBlank();
        baseDataValidator.reset().parameter(GLClosureJsonInputParams.OFFICE_ID.getValue()).value(this.officeId).notNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter(GLClosureJsonInputParams.COMMENTS.getValue()).value(this.comments).ignoreIfNull()
                .notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter(GLClosureJsonInputParams.COMMENTS.getValue()).value(this.comments).ignoreIfNull()
                .notExceedingLengthOf(500);
        baseDataValidator.reset().anyOfNotNull(this.comments);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}