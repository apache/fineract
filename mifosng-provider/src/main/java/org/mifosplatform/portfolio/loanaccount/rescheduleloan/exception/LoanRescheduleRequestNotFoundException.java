/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan reschedule request resources are not found.
 **/
public class LoanRescheduleRequestNotFoundException extends AbstractPlatformResourceNotFoundException {

	/** 
	 * LoanRescheduleRequestNotFoundException constructor 
	 * 
	 * @param requestId the loan reschedule request identifier
	 * @return void
	 **/
	public LoanRescheduleRequestNotFoundException(final Long requestId) {
		super("error.msg.loan.reschedule.request.id.invalid", 
				"Loan reschedule request with identifier " + requestId + " does not exist", requestId);
	}

}
