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
@Table(name = "m_hook_configuration")
public class HookConfiguration extends AbstractPersistable<Long> {

	@ManyToOne(optional = false)
	@JoinColumn(name = "hook_id", referencedColumnName = "id", nullable = false)
	private Hook hook;

	@Column(name = "field_type", nullable = false, length = 20)
	private String fieldType;

	@Column(name = "field_name", nullable = false, length = 100)
	private String fieldName;

	@Column(name = "field_value", nullable = false, length = 100)
	private String fieldValue;

	public static HookConfiguration createNewWithoutHook(
			final String fieldType, final String fieldName,
			final String fieldValue) {
		return new HookConfiguration(null, fieldType, fieldName, fieldValue);
	}
	
	public static HookConfiguration createNew(final Hook hook, final String fieldType,
			final String fieldName, final String fieldValue) {
		return new HookConfiguration(hook, fieldType, fieldName, fieldValue);
	}

	protected HookConfiguration() {
		//
	}

	private HookConfiguration(final Hook hook, final String fieldType,
			final String fieldName, final String fieldValue) {
		this.hook = hook;
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getFieldType() {
		return this.fieldType;
	}

	public String getFieldValue() {
		return this.fieldValue;
	}

	public void update(final Hook hook) {
		this.hook = hook;
	}

}
