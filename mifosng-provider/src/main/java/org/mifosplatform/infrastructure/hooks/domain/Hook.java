/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.domain;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.configParamName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.displayNameParamName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.eventsParamName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.isActiveParamName;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Entity
@Table(name = "m_hook")
public class Hook extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "hook", orphanRemoval = true)
	private Set<HookResource> events = new HashSet<>();

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "hook", orphanRemoval = true)
	private Set<HookConfiguration> config = new HashSet<>();

	@ManyToOne(optional = true)
	@JoinColumn(name = "template_id", referencedColumnName = "id", nullable = false)
	private HookTemplate template;

	protected Hook() {
		//
	}

	public static Hook fromJson(final JsonCommand command,
			final HookTemplate template, final Set<HookConfiguration> config,
			final Set<HookResource> events) {
		final String displayName = command
				.stringValueOfParameterNamed(displayNameParamName);
		Boolean isActive = command
				.booleanObjectValueOfParameterNamed(isActiveParamName);
        if(isActive == null)
            isActive = false;
		return new Hook(template, displayName, isActive, config, events);
	}

	private Hook(final HookTemplate template, final String displayName,
			final Boolean isActive, final Set<HookConfiguration> config,
			final Set<HookResource> events) {

		this.template = template;

		if (StringUtils.isNotBlank(displayName)) {
			this.name = displayName.trim();
		} else {
			this.name = template.getName();
		}
		this.isActive = isActive;
		if (!CollectionUtils.isEmpty(config)) {
			this.config = associateConfigWithThisHook(config);
		}
		if (!CollectionUtils.isEmpty(events)) {
			this.events = associateEventsWithThisHook(events);
		}

	}

	private Set<HookConfiguration> associateConfigWithThisHook(
			final Set<HookConfiguration> config) {
		for (final HookConfiguration hookConfiguration : config) {
			hookConfiguration.update(this);
		}
		return config;
	}

	private Set<HookResource> associateEventsWithThisHook(
			final Set<HookResource> events) {
		for (final HookResource hookResource : events) {
			hookResource.update(this);
		}
		return events;
	}

	public HookTemplate getHookTemplate() {
		return this.template;
	}

	public Set<HookConfiguration> getHookConfig() {
		return this.config;
	}

	public Map<String, Object> update(final JsonCommand command) {

		final Map<String, Object> actualChanges = new LinkedHashMap<>(4);

		if (command.isChangeInStringParameterNamed(displayNameParamName,
				this.name)) {
			final String newValue = command
					.stringValueOfParameterNamed(displayNameParamName);
			actualChanges.put(displayNameParamName, newValue);
			this.name = newValue;
		}

		if (command.isChangeInBooleanParameterNamed(isActiveParamName,
				this.isActive)) {
			final Boolean newValue = command
					.booleanObjectValueOfParameterNamed(isActiveParamName);
			actualChanges.put(isActiveParamName, newValue);
			this.isActive = newValue;
		}

		// events
		if (command.hasParameter(eventsParamName)) {
			final JsonArray jsonArray = command
					.arrayOfParameterNamed(eventsParamName);
			if (jsonArray != null) {
				actualChanges.put(eventsParamName, jsonArray);
			}
		}

		// config
		if (command.hasParameter(configParamName)) {
			final JsonElement element = command.parsedJson().getAsJsonObject()
					.get(configParamName);
			if (element != null) {
				actualChanges.put(configParamName, element);
			}
		}

		return actualChanges;
	}

	public boolean updateEvents(final Set<HookResource> newHookEvents) {
		if (newHookEvents == null) {
			return false;
		}

		if (this.events == null) {
			this.events = new HashSet<>();
		}
		this.events.clear();
		this.events.addAll(associateEventsWithThisHook(newHookEvents));
		return true;
	}

	public boolean updateConfig(final Set<HookConfiguration> newHookConfig) {
		if (newHookConfig == null) {
			return false;
		}

		if (this.config == null) {
			this.config = new HashSet<>();
		}
		this.config.clear();
		this.config.addAll(associateConfigWithThisHook(newHookConfig));
		return true;
	}

}
