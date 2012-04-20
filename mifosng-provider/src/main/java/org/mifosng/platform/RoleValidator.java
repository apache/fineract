package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.springframework.util.ObjectUtils;

public class RoleValidator {

	private final RoleCommand command;

	public RoleValidator(RoleCommand command) {
		this.command = command;
	}

	public void validateForCreate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		validate(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}
	
	public void validateForUpdate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (command.getId() == null || command.getId() < 1) {
			ErrorResponse error = new ErrorResponse("validation.msg.role.id.not.provided", "id");
			dataValidationErrors.add(error);
		}
		
		validate(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

	private void validate(List<ErrorResponse> dataValidationErrors) {
		if (StringUtils.isBlank(command.getName())) {
			ErrorResponse error = new ErrorResponse("validation.msg.role.name.cannot.be.blank", "name");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getDescription())) {
			ErrorResponse error = new ErrorResponse("validation.msg.role.description.cannot.be.blank", "description");
			dataValidationErrors.add(error);
		} else {
			if (command.getDescription().trim().length() > 500) {
				ErrorResponse error = new ErrorResponse("validation.msg.role.description.exceeds.max.length", "description");
				dataValidationErrors.add(error);
			}
		}
		
		if (ObjectUtils.isEmpty(command.getPermissionIds())) {
			ErrorResponse error = new ErrorResponse("validation.msg.role.permissions.cannot.be.empty", "selectedItems");
			dataValidationErrors.add(error);
		}
	}
}
