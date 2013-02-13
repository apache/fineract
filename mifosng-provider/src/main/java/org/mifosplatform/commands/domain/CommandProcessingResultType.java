/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.domain;

public enum CommandProcessingResultType {

    INVALID(0, "commandProcessingResultType.invalid"), //
    PROCESSED(1, "commandProcessingResultType.processed"), //
    AWAITING_APPROVAL(2, "commandProcessingResultType.awaiting.approval"), //
    REJECTED(3, "commandProcessingResultType.rejected");

    private final Integer value;
    private final String code;

    private CommandProcessingResultType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static CommandProcessingResultType fromInt(final Integer typeValue) {
        CommandProcessingResultType type = CommandProcessingResultType.INVALID;
        switch (typeValue) {
            case 1:
                type = PROCESSED;
            break;
            case 2:
                type = AWAITING_APPROVAL;
            break;
            case 3:
                type = REJECTED;
            break;
        }
        return type;
    }
}