/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.data;

public class SavingAccountForLookup {
	
	private final Long id;
	
	public SavingAccountForLookup(final Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

}
