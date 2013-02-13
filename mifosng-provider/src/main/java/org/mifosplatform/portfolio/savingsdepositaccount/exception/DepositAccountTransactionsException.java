/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccountTransactionsException extends AbstractPlatformDomainRuleException {

    public DepositAccountTransactionsException(String errorcode, String remainInterestForWithdrawal) {
        super(errorcode, "You can Withdraw " + remainInterestForWithdrawal + " only", remainInterestForWithdrawal);
    }
}
