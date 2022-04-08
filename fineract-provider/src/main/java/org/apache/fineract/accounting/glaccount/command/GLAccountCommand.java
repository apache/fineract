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
package org.apache.fineract.accounting.glaccount.command;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.api.GLAccountJsonInputParams;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for adding a general Ledger Account
 */
@RequiredArgsConstructor
@Getter
public class GLAccountCommand {

    private final Long id;
    private final String name;
    private final Long parentId;
    private final String glCode;
    private final Boolean disabled;
    private final Boolean manualEntriesAllowed;
    private final Integer type;
    private final Integer usage;
    private final String description;
    private final Long tagId;

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

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
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

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
        baseDataValidator.reset().parameter(GLAccountJsonInputParams.TAGID.getValue()).value(this.tagId).ignoreIfNull()
                .longGreaterThanZero();
    }

    public boolean isHeaderAccount() {
        return GLAccountUsage.HEADER.getValue().equals(this.usage);
    }

}
