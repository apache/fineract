/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.hooks.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_hook_registered_events")
public class HookResource extends AbstractPersistableCustom<Long> {

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
