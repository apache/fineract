/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class GroupCommandValidator {

    private final GroupCommand command;

    public GroupCommandValidator(GroupCommand command) {
        this.command = command;
    }
    
    public void validateForCreate() {
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
    
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        
        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("levelId").value(command.getLevelId()).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    public void validateForUpdate() {
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        
        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);

        if (command.isOfficeIdChanged()) {
            baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
    
   public void validateForLoanOfficerUpdate() {
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        
        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();

        if (command.isLoanOfficerChanged()) {
            baseDataValidator.reset().parameter("loanOfficerId").value(command.getLoanOfficeId()).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
    
}
