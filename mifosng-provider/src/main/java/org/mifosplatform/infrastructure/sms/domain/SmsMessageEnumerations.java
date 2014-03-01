/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.domain;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class SmsMessageEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(SmsMessageStatusType.fromInt(statusId));
    }

    public static EnumOptionData status(final SmsMessageStatusType status) {
        EnumOptionData optionData = new EnumOptionData(SmsMessageStatusType.INVALID.getValue().longValue(),
                SmsMessageStatusType.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(SmsMessageStatusType.INVALID.getValue().longValue(),
                        SmsMessageStatusType.INVALID.getCode(), "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(SmsMessageStatusType.PENDING.getValue().longValue(),
                        SmsMessageStatusType.PENDING.getCode(), "Pending");
            break;
            case SENT:
                optionData = new EnumOptionData(SmsMessageStatusType.SENT.getValue().longValue(), SmsMessageStatusType.SENT.getCode(),
                        "Sent");
            break;
            case DELIVERED:
                optionData = new EnumOptionData(SmsMessageStatusType.DELIVERED.getValue().longValue(),
                        SmsMessageStatusType.DELIVERED.getCode(), "Delivered");
            break;
            case FAILED:
                optionData = new EnumOptionData(SmsMessageStatusType.FAILED.getValue().longValue(), SmsMessageStatusType.FAILED.getCode(),
                        "Failed");
            break;

        }

        return optionData;
    }
}