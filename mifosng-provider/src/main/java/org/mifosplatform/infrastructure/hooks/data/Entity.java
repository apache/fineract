/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.data;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class Entity implements Serializable {

	private String name;

	private List<String> actions;

	public void setName(final String name) {
		this.name = name;
	}

	public void setActions(final List<String> actions) {
		this.actions = actions;
	}

	public String getName() {
		return this.name;
	}

}
