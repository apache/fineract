package org.mifosng.platform.loan.domain;

public class InvalidLoanTimelineDate extends RuntimeException {

	private final String errorCode;

	public InvalidLoanTimelineDate(final String message, final String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}
}