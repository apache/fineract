/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.glaccount.api.GLAccountJsonInputParams;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.domain.GLAccountUsage;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for adding a general Ledger Account
 */
public class GLAccountCommand {

    @SuppressWarnings("unused")
    private final Long id;
    private final String name;
    private final Long parentId;
    private final String glCode;
    private final Boolean disabled;
    private final Boolean manualEntriesAllowed;
    private final Integer usage;
    private final Integer type;
    private final String description;
    private final Long tagId;

    public GLAccountCommand(final Long id, final String name, final Long parentId, final String glCode, final Boolean disabled,
            final Boolean manualEntriesAllowed, final Integer type, final Integer usage, final String description, final Long tagId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.disabled = disabled;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.type = type;
        this.usage = usage;
        this.description = description;
        this.tagId = tagId;
    }

    public void validateForCreate() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLAccount");

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.NAME.getValue()).value(this.name).notBlank().notExceedingLengthOf(200);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.GL_CODE.getValue()).value(this.glCode).notBlank()
                .notExceedingLengthOf(45);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.PARENT_ID.getValue()).value(this.parentId).ignoreIfNull()
                .integerGreaterThanZero();

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.TYPE.getValue()).value(this.type).notNull()
                .inMinMaxRange(GLAccountType.getMinValue(), GLAccountType.getMaxValue());

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.USAGE.getValue()).value(this.usage)
                .inMinMaxRange(GLAccountUsage.getMinValue(), GLAccountUsage.getMaxValue());

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notExceedingLengthOf(500);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED.getValue()).value(this.manualEntriesAllowed)
                .notBlank();

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.TAGID.getValue()).value(this.tagId).ignoreIfNull()
                .longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLAccount");

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.NAME.getValue()).value(this.name).ignoreIfNull().notBlank()
                .notExceedingLengthOf(200);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.GL_CODE.getValue()).ignoreIfNull().value(this.glCode).notBlank()
                .notExceedingLengthOf(45);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.PARENT_ID.getValue()).value(this.parentId).ignoreIfNull()
                .integerGreaterThanZero();

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.TYPE.getValue()).value(this.type).ignoreIfNull()
                .inMinMaxRange(GLAccountType.getMinValue(), GLAccountType.getMaxValue());
        baseDataValidator.reset().parameter(GLAccountJsonInputParams.USAGE.getValue()).value(this.usage).ignoreIfNull()
                .inMinMaxRange(GLAccountUsage.getMinValue(), GLAccountUsage.getMaxValue());

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notBlank().notExceedingLengthOf(500);

        baseDataValidator.reset().parameter(GLAccountJsonInputParams.DISABLED.getValue()).value(this.disabled).ignoreIfNull();

        baseDataValidator.reset().anyOfNotNull(this.name, this.glCode, this.parentId, this.type, this.description, this.disabled);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
        baseDataValidator.reset().parameter(GLAccountJsonInputParams.TAGID.getValue()).value(this.tagId).ignoreIfNull()
                .longGreaterThanZero();
    }

    public boolean isHeaderAccount() {
        return GLAccountUsage.HEADER.getValue().equals(this.usage);
    }

}