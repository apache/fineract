/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Exception thrown when an attempt is made update the parent of a root office.
 */
public class DepositProductValueOutsideRangeException extends AbstractPlatformDomainRuleException {

    public DepositProductValueOutsideRangeException(final String actualValue, final String rangeMin, final String rangeMax, final String fieldCode) {
        super("error.msg." + fieldCode + ".outside.of.allowed.range", actualValue + " is outside of allowed range of [" + rangeMin + ", "
                + rangeMax + "].", actualValue, rangeMin, rangeMax);
    }
}