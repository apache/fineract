/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_hook_registered_events")
public class HookResource extends AbstractPersistable<Long> {

	@ManyToOne(optional = false)
	@JoinColumn(name = "hook_id", referencedColumnName = "id", nullable = false)
	private Hook hook;

	@Column(name = "entity_name", nullable = false, length = 45)
	private String entityName;

	@Column(name = "action_name", nullable = false, length = 45)
	private String actionName;

	protected HookResource() {
		//
	}

	public static HookResource createNewWithoutHook(final String entityName,
			final String actionName) {
		return new HookResource(null, entityName, actionName);
	}

	private HookResource(final Hook hook, final String entityName,
			final String actionName) {
		this.hook = hook;
		this.entityName = entityName;
		this.actionName = actionName;
	}

	public void update(final Hook hook) {
		this.hook = hook;
	}
}
