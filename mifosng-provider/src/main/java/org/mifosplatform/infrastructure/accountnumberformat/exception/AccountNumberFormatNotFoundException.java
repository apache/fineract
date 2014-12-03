/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.exception;

import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class AccountNumberFormatNotFoundException extends AbstractPlatformResourceNotFoundException {

    public AccountNumberFormatNotFoundException(final Long id) {
        super(AccountNumberFormatConstants.EXCEPTION_ACCOUNT_NUMBER_FORMAT_NOT_FOUND, "AccountNumber format with identifier " + id
                + " does not exist", id);
    }

}
