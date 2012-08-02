package org.mifosng.platform.group.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

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
        
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

}
