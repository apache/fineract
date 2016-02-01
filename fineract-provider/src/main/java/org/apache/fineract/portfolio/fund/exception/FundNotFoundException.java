/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.fund.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when fund resources are not found.
 */
public class FundNotFoundException extends AbstractPlatformResourceNotFoundException {

    public FundNotFoundException(final Long id) {
        super("error.msg.fund.id.invalid", "Fund with identifier " + id + " does not exist", id);
    }
}