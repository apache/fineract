/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.service;

public enum TransferEventType {
    PROPOSAL(1, "transferEvent.proposal"), ACCEPTANCE(2, "transferEvent.acceptance"), WITHDRAWAL(3, "transferEvent.withdrawal"), REJECTION(
            4, "transferEvent.rejection");

    private final Integer value;
    private final String code;

    private TransferEventType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isAcceptance() {
        return value.equals(TransferEventType.ACCEPTANCE.value);
    }

}
