/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class FloatingRateNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public FloatingRateNotFoundException(final Long id) {
		super("error.msg.floatingrate.id.invalid",
				"Floating Rate with identifier " + id + " does not exist", id);
	}

	public FloatingRateNotFoundException(final String globalisationMessageCode) {
		super(globalisationMessageCode, "Floating Rate does not exist");
	}
}
