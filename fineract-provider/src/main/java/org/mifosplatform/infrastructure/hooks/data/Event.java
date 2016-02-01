/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.data;

import java.io.Serializable;

public class Event implements Serializable {

	private final String actionName;
	private final String entityName;

	public static Event instance(final String actionName,
			final String entityName) {
		return new Event(actionName, entityName);
	}

	private Event(final String actionName, final String entityName) {
		this.actionName = actionName;
		this.entityName = entityName;
	}

	public String getActionName() {
		return this.actionName;
	}

	public String getEntityName() {
		return this.entityName;
	}

}
