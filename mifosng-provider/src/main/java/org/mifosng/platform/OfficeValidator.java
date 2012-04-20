package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.ErrorResponse;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class OfficeValidator {

	private final String name;
	private final Long parentId;
	private final LocalDate openingDate;
	private final String externalId;

	public OfficeValidator(String name, Long parentId, LocalDate openingDate, String externalId) {
		this.name = name;
		this.parentId = parentId;
		this.openingDate = openingDate;
		this.externalId = externalId;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (StringUtils.isBlank(this.name)) {
			ErrorResponse error = new ErrorResponse("validation.msg.office.name.cannot.be.blank", "name");
			dataValidationErrors.add(error);
		}
		
		if (this.parentId == null || parentId.equals(Long.valueOf(-1))) {
			ErrorResponse error = new ErrorResponse("validation.msg.office.parent.cannot.be.blank", "parentId");
			dataValidationErrors.add(error);
		}
		
		if (this.openingDate == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.office.opening.date.cannot.be.blank", "openingDate");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(this.externalId)) {
			
			if (this.externalId.trim().length() > 100) {
				ErrorResponse error = new ErrorResponse("validation.msg.office.externalId.exceeds.max.length", "externalId");
				dataValidationErrors.add(error);
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

}
