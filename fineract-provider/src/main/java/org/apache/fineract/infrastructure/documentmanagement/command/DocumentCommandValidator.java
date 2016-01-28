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
package org.apache.fineract.infrastructure.documentmanagement.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

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
