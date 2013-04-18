/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

/**
 * Enum representation of grouping type status states.
 */
public enum GroupingTypeStatus {

    INVALID(0, "groupingStatusType.invalid"), //
    PENDING(100, "groupingStatusType.pending"), //
    ACTIVE(300, "groupingStatusType.active"), //
    CLOSED(600, "groupingStatusType.closed");

    private final Integer value;
    private final String code;

    public static GroupingTypeStatus fromInt(final Integer statusValue) {

        GroupingTypeStatus enumeration = GroupingTypeStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = GroupingTypeStatus.PENDING;
            break;
            case 300:
                enumeration = GroupingTypeStatus.ACTIVE;
            break;
            case 600:
                enumeration = GroupingTypeStatus.CLOSED;
            break;
        }
        return enumeration;
    }

    private GroupingTypeStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final GroupingTypeStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public boolean isPending() {
        return this.value.equals(GroupingTypeStatus.PENDING.getValue());
    }

    public boolean isActive() {
        return this.value.equals(GroupingTypeStatus.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return this.value.equals(GroupingTypeStatus.CLOSED.getValue());
    }
}