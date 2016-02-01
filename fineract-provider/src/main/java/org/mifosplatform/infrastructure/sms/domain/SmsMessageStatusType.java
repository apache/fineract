/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.domain;

public enum SmsMessageStatusType {

    INVALID(0, "smsMessageStatusType.invalid"), //
    PENDING(100, "smsMessageStatusType.pending"), //
    SENT(200, "smsMessageStatusType.sent"), //
    DELIVERED(300, "smsMessageStatusType.delivered"), //
    FAILED(400, "smsMessageStatusType.failed");

    private final Integer value;
    private final String code;

    public static SmsMessageStatusType fromInt(final Integer statusValue) {

        SmsMessageStatusType enumeration = SmsMessageStatusType.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = SmsMessageStatusType.PENDING;
            break;
            case 200:
                enumeration = SmsMessageStatusType.SENT;
            break;
            case 300:
                enumeration = SmsMessageStatusType.DELIVERED;
            break;
            case 400:
                enumeration = SmsMessageStatusType.FAILED;
            break;
        }
        return enumeration;
    }

    private SmsMessageStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
}