/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.event;

import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.context.ApplicationEvent;

public class HookEvent extends ApplicationEvent {

	private final String payload;

	private final String tenantIdentifier;

	private final AppUser appUser;

	private final String authToken;

	public HookEvent(final HookEventSource source, final String payload,
			final String tenantIdentifier, final AppUser appUser,
			final String authToken) {
		super(source);
		this.payload = payload;
		this.tenantIdentifier = tenantIdentifier;
		this.appUser = appUser;
		this.authToken = authToken;
	}

	public String getPayload() {
		return this.payload;
	}

	@Override
	public HookEventSource getSource() {
		return (HookEventSource) super.source;
	}

	public String getTenantIdentifier() {
		return this.tenantIdentifier;
	}

	public AppUser getAppUser() {
		return this.appUser;
	}

	public String getAuthToken() {
		return this.authToken;
	}

}
