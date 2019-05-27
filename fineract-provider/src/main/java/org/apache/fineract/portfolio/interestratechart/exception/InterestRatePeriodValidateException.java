package org.apache.fineract.portfolio.interestratechart.exception;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class InterestRatePeriodValidateException extends AbstractPlatformResourceNotFoundException {
	public InterestRatePeriodValidateException() {
		super("error.msg.interest.rate.chart.slab.not.blank", "Interest rate chart slab shouldn't be blank");
	}
}
