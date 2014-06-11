/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class FinancialActivityAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

    public FinancialActivityAccountNotFoundException(final Long officeId, final String accountType) {
        super("error.msg.officeToAccountMapping.not.found", "Mapping for office with Id " + officeId
                + " does not exist for an account of type " + accountType, officeId, accountType);
    }

    public FinancialActivityAccountNotFoundException(final Long id) {
        super("error.msg.officeToAccountMapping.not.found", "Mapping for GL Account with Id " + id + " does not exist", id);
    }
}