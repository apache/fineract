package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.saving.domain.DepositStatus;

public class DepositAccountEnumerations {

	public static EnumOptionData status(final Integer statusId) {
		return status(DepositStatus.fromInt(statusId));
	}

	public static EnumOptionData status(final DepositStatus status) {
		EnumOptionData optionData = null;
		switch (status) {
		case SUBMITED_AND_PENDING_APPROVAL:
			optionData = new EnumOptionData(DepositStatus.SUBMITED_AND_PENDING_APPROVAL.getValue().longValue(), DepositStatus.SUBMITED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
			break;
		case APPROVED:
			optionData = new EnumOptionData(DepositStatus.APPROVED.getValue().longValue(), DepositStatus.APPROVED.getCode(), "Approved");
			break;
		case ACTIVE:
			optionData = new EnumOptionData(DepositStatus.ACTIVE.getValue().longValue(), DepositStatus.ACTIVE.getCode(), "Active");
			break;
		case REJECTED:
			optionData = new EnumOptionData(DepositStatus.REJECTED.getValue().longValue(), DepositStatus.REJECTED.getCode(), "Rejected");
			break;
		case WITHDRAWN_BY_CLIENT:
			optionData = new EnumOptionData(DepositStatus.WITHDRAWN_BY_CLIENT.getValue().longValue(), DepositStatus.WITHDRAWN_BY_CLIENT.getCode(), "Withdrawn by applicant");
			break;
		case MATURED:
			optionData = new EnumOptionData(DepositStatus.MATURED.getValue().longValue(), DepositStatus.MATURED.getCode(), "Matured");
			break;
		case CLOSED:
			optionData = new EnumOptionData(DepositStatus.CLOSED.getValue().longValue(), DepositStatus.CLOSED.getCode(), "Closed");
			break;
		default:
			optionData = new EnumOptionData(DepositStatus.INVALID.getValue().longValue(), DepositStatus.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
}