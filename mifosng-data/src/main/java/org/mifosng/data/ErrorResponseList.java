package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorResponseList {

	private List<ErrorResponse> errors = new ArrayList<ErrorResponse>();

	public ErrorResponseList() {
		//
	}

	public ErrorResponseList(final List<ErrorResponse> errors) {
		this.errors = errors;
	}

	@XmlElementWrapper(name="errors")
	@XmlElement(name="errorResponse")
	public List<ErrorResponse> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorResponse> errors) {
		this.errors = errors;
	}
}