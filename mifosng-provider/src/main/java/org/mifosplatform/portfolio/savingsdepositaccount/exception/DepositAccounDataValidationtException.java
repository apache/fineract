/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccounDataValidationtException extends AbstractPlatformDomainRuleException {

    public DepositAccounDataValidationtException(BigDecimal preClosureInterestRate, BigDecimal maturityMinInterestRate) {
        super("error.msg.deposit.account.preClosureInterestRate.should.lessthanOrEqualTo.maturityMinInterestRate",
                "PreClosureInterestRate : " + preClosureInterestRate + "should lessthan or equal to maturityMinInterestRate : "
                        + maturityMinInterestRate, preClosureInterestRate, maturityMinInterestRate);
    }
}
