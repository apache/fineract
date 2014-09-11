/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;

public class LoanRescheduleRequestEnumerations {
	
	public static EnumOptionData status(final LoanRescheduleRequestStatusEnumData status) {
		Long id = status.id();
        String code = status.code();
        String value = status.value();

        return new EnumOptionData(id, code, value);
	}

	public static LoanRescheduleRequestStatusEnumData status(final Integer statusId) {
		return status(LoanStatus.fromInt(statusId));
	}
	
	public static LoanRescheduleRequestStatusEnumData status(final LoanStatus status) {
		LoanRescheduleRequestStatusEnumData enumData = new LoanRescheduleRequestStatusEnumData(
				LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
				LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
		
		switch(status) {
			case SUBMITTED_AND_PENDING_APPROVAL:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
						LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
				break;
				
			case APPROVED:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.APPROVED.getValue().longValue(),
						LoanStatus.APPROVED.getCode(), "Approved");
				break;
				
			case REJECTED:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.REJECTED.getValue().longValue(),
						LoanStatus.REJECTED.getCode(), "Rejected");
				break;
				
			default:
				break;
		}
		
		return enumData;
	}
}
