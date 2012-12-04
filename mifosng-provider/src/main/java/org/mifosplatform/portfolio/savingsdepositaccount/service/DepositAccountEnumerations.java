package org.mifosplatform.portfolio.savingsdepositaccount.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;

public class DepositAccountEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(DepositAccountStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final DepositAccountStatus status) {
        EnumOptionData optionData = null;
        switch (status) {
            case SUBMITED_AND_PENDING_APPROVAL:
                optionData = new EnumOptionData(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL.getValue().longValue(),
                        DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
            break;
            /*
             * case APPROVED: optionData = new
             * EnumOptionData(DepositAccountStatus
             * .APPROVED.getValue().longValue(),
             * DepositAccountStatus.APPROVED.getCode(), "Approved"); break;
             */
            case ACTIVE:
                optionData = new EnumOptionData(DepositAccountStatus.ACTIVE.getValue().longValue(), DepositAccountStatus.ACTIVE.getCode(),
                        "Active");
            break;
            case REJECTED:
                optionData = new EnumOptionData(DepositAccountStatus.REJECTED.getValue().longValue(),
                        DepositAccountStatus.REJECTED.getCode(), "Rejected");
            break;
            case WITHDRAWN_BY_CLIENT:
                optionData = new EnumOptionData(DepositAccountStatus.WITHDRAWN_BY_CLIENT.getValue().longValue(),
                        DepositAccountStatus.WITHDRAWN_BY_CLIENT.getCode(), "Withdrawn by applicant");
            break;
            case MATURED:
                optionData = new EnumOptionData(DepositAccountStatus.MATURED.getValue().longValue(),
                        DepositAccountStatus.MATURED.getCode(), "Matured");
            break;
            case CLOSED:
                optionData = new EnumOptionData(DepositAccountStatus.CLOSED.getValue().longValue(), DepositAccountStatus.CLOSED.getCode(),
                        "Closed");
            break;
            case PRECLOSED:
                optionData = new EnumOptionData(DepositAccountStatus.PRECLOSED.getValue().longValue(),
                        DepositAccountStatus.PRECLOSED.getCode(), "Preclosed");
            break;
            default:
                optionData = new EnumOptionData(DepositAccountStatus.INVALID.getValue().longValue(),
                        DepositAccountStatus.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }
}