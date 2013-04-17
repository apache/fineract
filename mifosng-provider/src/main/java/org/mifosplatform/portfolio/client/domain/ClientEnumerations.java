/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class ClientEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(ClientStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final ClientStatus status) {
        EnumOptionData optionData = new EnumOptionData(ClientStatus.INVALID.getValue().longValue(), ClientStatus.INVALID.getCode(),
                "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(ClientStatus.INVALID.getValue().longValue(), ClientStatus.INVALID.getCode(), "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(ClientStatus.PENDING.getValue().longValue(), ClientStatus.PENDING.getCode(), "Pending");
            break;
            case ACTIVE:
                optionData = new EnumOptionData(ClientStatus.ACTIVE.getValue().longValue(), ClientStatus.ACTIVE.getCode(), "Active");
            break;
            case CLOSED:
                optionData = new EnumOptionData(ClientStatus.CLOSED.getValue().longValue(), ClientStatus.CLOSED.getCode(), "Closed");
            break;
        }

        return optionData;
    }
}