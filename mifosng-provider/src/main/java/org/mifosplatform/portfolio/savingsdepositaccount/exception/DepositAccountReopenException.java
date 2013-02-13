/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccountReopenException extends AbstractPlatformDomainRuleException {

    public DepositAccountReopenException(LocalDate maturedDate) {
        super("error.msg.depositaccount.cannot.reopen.before.mature", "You cannot renew the account that is not matured:" + maturedDate);
    }
}
