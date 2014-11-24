/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

/**
 * Enum representation of {@link Guarantor} status states.
 */
public enum GuarantorFundStatusType {

    INVALID(0, "guarantorFundStatusType.invalid"), //
    ACTIVE(100, "guarantorFundStatusType.active"), //
    COMPLETED(200, "guarantorFundStatusType.completed"), //
    WITHDRAWN(300, "guarantorFundStatusType.withdrawn"), //
    DELETED(400, "guarantorFundStatusType.deleted");

    private final Integer value;
    private final String code;

    public static GuarantorFundStatusType fromInt(final Integer type) {

        GuarantorFundStatusType enumeration = GuarantorFundStatusType.INVALID;
        switch (type) {
            case 100:
                enumeration = GuarantorFundStatusType.ACTIVE;
            break;
            case 200:
                enumeration = GuarantorFundStatusType.COMPLETED;
            break;
            case 300:
                enumeration = GuarantorFundStatusType.WITHDRAWN;
            break;
            case 400:
                enumeration = GuarantorFundStatusType.DELETED;
            break;
        }
        return enumeration;
    }

    private GuarantorFundStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final GuarantorFundStatusType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isActive() {
        return this.value.equals(GuarantorFundStatusType.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return isWithdrawn() || isCompleted() || isDeleted();
    }

    public boolean isWithdrawn() {
        return this.value.equals(GuarantorFundStatusType.WITHDRAWN.getValue());
    }

    public boolean isCompleted() {
        return this.value.equals(GuarantorFundStatusType.COMPLETED.getValue());
    }

    public boolean isDeleted() {
        return this.value.equals(GuarantorFundStatusType.DELETED.getValue());
    }
}