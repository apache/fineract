/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.account.data;

import java.util.Collection;

@SuppressWarnings("unused")
public class SelfAccountTransferData {

	private final Collection<SelfAccountTemplateData> accountOptions;

	public SelfAccountTransferData(
			final Collection<SelfAccountTemplateData> accountOptions) {
		this.accountOptions = accountOptions;
	}

}
