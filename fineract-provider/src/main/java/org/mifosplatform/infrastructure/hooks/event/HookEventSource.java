/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.event;

import java.io.Serializable;

public class HookEventSource implements Serializable {

	private final String entityName;

	private final String actionName;

	public HookEventSource(final String entityName, final String actionName) {
		super();
		this.entityName = entityName;
		this.actionName = actionName;
	}

	public String getEntityName() {
		return this.entityName;
	}

	public String getActionName() {
		return this.actionName;
	}

}
