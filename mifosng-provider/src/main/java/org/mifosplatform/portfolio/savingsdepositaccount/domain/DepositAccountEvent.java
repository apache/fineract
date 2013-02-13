/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.domain;

public enum DepositAccountEvent {

    DEPOSIT_CREATED, DEPOSIT_REJECTED, DEPOSIT_APPROVED, DEPOSIT_APPROVAL_UNDO, DEPOSIT_WITHDRAWN, DEPOSIT_MATURED, DEPOSIT_CLOSED, DEPOSIT_PRECLOSED;
}