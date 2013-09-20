/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class GroupingTypeEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(GroupingTypeStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final GroupingTypeStatus status) {
        EnumOptionData optionData = new EnumOptionData(GroupingTypeStatus.INVALID.getValue().longValue(),
                GroupingTypeStatus.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(GroupingTypeStatus.INVALID.getValue().longValue(), GroupingTypeStatus.INVALID.getCode(),
                        "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(GroupingTypeStatus.PENDING.getValue().longValue(), GroupingTypeStatus.PENDING.getCode(),
                        "Pending");
            break;
            case ACTIVE:
                optionData = new EnumOptionData(GroupingTypeStatus.ACTIVE.getValue().longValue(), GroupingTypeStatus.ACTIVE.getCode(),
                        "Active");
            break;
            case CLOSED:
                optionData = new EnumOptionData(GroupingTypeStatus.CLOSED.getValue().longValue(), GroupingTypeStatus.CLOSED.getCode(),
                        "Closed");
            break;
            case TRANSFER_IN_PROGRESS:
                optionData = new EnumOptionData(GroupingTypeStatus.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        GroupingTypeStatus.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress");
            break;
            case TRANSFER_ON_HOLD:
                optionData = new EnumOptionData(GroupingTypeStatus.TRANSFER_ON_HOLD.getValue().longValue(),
                        GroupingTypeStatus.TRANSFER_ON_HOLD.getCode(), "Transfer on hold");
            break;
        }

        return optionData;
    }
}