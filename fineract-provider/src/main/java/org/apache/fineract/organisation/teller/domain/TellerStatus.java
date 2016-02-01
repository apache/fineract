/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.organisation.teller.domain;

/**
 * Enum representation of teller status states.
 */

public enum TellerStatus {
	
    INVALID(0, "tellerStatusType.invalid"),
    PENDING(100, "tellerStatusType.pending"),
    ACTIVE(300, "tellerStatusType.active"),
    INACTIVE(400, "tellerStatusType.inactive"),
    CLOSED(600, "tellerStatusType.closed");

    private final Integer value;
    private final String code;

    public static TellerStatus fromInt(final Integer statusValue) {

        TellerStatus status = TellerStatus.INVALID;
        switch (statusValue) {
            case 100:
            	status = TellerStatus.PENDING;
            break;
            case 300:
            	status = TellerStatus.ACTIVE;
            break;
            case 400:
            	status = TellerStatus.INACTIVE;
            break;
            case 600:
            	status = TellerStatus.CLOSED;
            break;
        }
        return status;
    }

    private TellerStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final TellerStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPending() {
        return this.value.equals(TellerStatus.PENDING.getValue());
    }

    public boolean isActive() {
        return this.value.equals(TellerStatus.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return this.value.equals(TellerStatus.CLOSED.getValue());
    }

    public boolean isInactive() {
        return this.value.equals(TellerStatus.INACTIVE.getValue());
    }
    
}
