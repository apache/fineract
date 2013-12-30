/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.mifosplatform.portfolio.savings.DepositAccountType;

public class DepositAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

    public DepositAccountNotFoundException(final DepositAccountType accountType, final Long id) {
        super("error.msg." + accountType.getCode().toLowerCase() + ".id.invalid", StringUtils.capitalize(accountType.toString()
                .toLowerCase()) + " account with identifier " + id + " does not exist", id);
    }
}