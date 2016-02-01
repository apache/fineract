/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.domain;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.nameParamName;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_hook_templates")
public class HookTemplate extends AbstractPersistable<Long> {

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "template", orphanRemoval = true)
	private final Set<Schema> fields = new HashSet<>();

	private HookTemplate(final String name) {

		if (StringUtils.isNotBlank(name)) {
			this.name = name.trim();
		} else {
			this.name = null;
		}
	}

	protected HookTemplate() {

	}

	public static HookTemplate fromJson(final JsonCommand command) {
		final String name = command.stringValueOfParameterNamed(nameParamName);
		return new HookTemplate(name);
	}

	public String getName() {
		return this.name;
	}

	public Set<Schema> getSchema() {
		return this.fields;
	}
}
