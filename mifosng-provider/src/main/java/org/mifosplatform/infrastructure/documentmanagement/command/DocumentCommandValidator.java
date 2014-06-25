/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DocumentCommandValidator {

    private final DocumentCommand command;

    public DocumentCommandValidator(final DocumentCommand command) {
        this.command = command;
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("document");

        baseDataValidator.reset().parameter("name").value(this.command.getName()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("size").value(this.command.getSize()).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("fileName").value(this.command.getFileName()).ignoreIfNull().notBlank()
                .notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("location").value(this.command.getLocation()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(this.command.getName()).ignoreIfNull().notExceedingLengthOf(250);

        baseDataValidator.reset().anyOfNotNull(this.command.getName(), this.command.getFileName(), this.command.getDescription(),
                this.command.getLocation(), this.command.getSize());

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForCreate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("document");
        baseDataValidator.reset().parameter("parentEntityType").value(this.command.getParentEntityType()).notBlank()
                .notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("parentEntityId").value(this.command.getParentEntityId()).integerGreaterThanZero();
        baseDataValidator.reset().parameter("name").value(this.command.getName()).notBlank().notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("size").value(this.command.getSize()).integerGreaterThanZero();
        baseDataValidator.reset().parameter("fileName").value(this.command.getFileName()).notBlank().notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("description").value(this.command.getName()).notExceedingLengthOf(250);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
