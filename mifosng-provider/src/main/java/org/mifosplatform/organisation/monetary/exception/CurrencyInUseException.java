/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to delete a currency that is
 * still in use.
 */
public class CurrencyInUseException extends AbstractPlatformDomainRuleException {

	public CurrencyInUseException(final String currencyCode) {
		super("error.msg.currency.currencyCode.inUse",
				"Cannot remove currency with identifier " + currencyCode
						+ " while it is still in use", currencyCode);
	}

}
