/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

public class LoanStatusMapper {

    private final Integer statusId;

    public LoanStatusMapper(final Integer statusId) {
        this.statusId = statusId;
    }

    public boolean isPendingApproval() {
        return Integer.valueOf(100).equals(this.statusId);
    }

    public boolean isAwaitingDisbursal() {
        return Integer.valueOf(200).equals(this.statusId);
    }

    public boolean isOpen() {
        return Integer.valueOf(300).equals(this.statusId);
    }

    public boolean isWithdrawnByClient() {
        return Integer.valueOf(400).equals(this.statusId);
    }

    public boolean isRejected() {
        return Integer.valueOf(500).equals(this.statusId);
    }

    public boolean isClosed() {
        return Integer.valueOf(600).equals(this.statusId) || isWithdrawnByClient() || isRejected();
    }
    
    public boolean isOverpaid() {
        return Integer.valueOf(700).equals(this.statusId);
    }
}