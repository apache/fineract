/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan transactions processing strategy
 * resources are not found.
 */
public class LoanTransactionProcessingStrategyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanTransactionProcessingStrategyNotFoundException(final Long id) {
        super("error.msg.transactions.processing.strategy.id.invalid", "Loan transaction processing strategy with identifier " + id
                + " does not exist", id);
    }
}