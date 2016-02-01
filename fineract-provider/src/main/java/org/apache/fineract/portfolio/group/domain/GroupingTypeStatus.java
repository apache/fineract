/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.domain;

/**
 * Enum representation of grouping type status states.
 */
public enum GroupingTypeStatus {

    INVALID(0, "groupingStatusType.invalid"), //
    PENDING(100, "groupingStatusType.pending"), //
    ACTIVE(300, "groupingStatusType.active"), //
    TRANSFER_IN_PROGRESS(303, "clientStatusType.transfer.in.progress"), //
    TRANSFER_ON_HOLD(304, "clientStatusType.transfer.on.hold"), //
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
        return this.code;
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

    public boolean isTransferInProgress() {
        return isTransferInProgress();
    }

    public boolean isTransferOnHold() {
        return isTransferOnHold();
    }

    public boolean isUnderTransfer() {
        return isTransferInProgress() || isTransferOnHold();
    }
}