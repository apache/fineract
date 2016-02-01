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
package org.apache.fineract.infrastructure.hooks.event;

import org.apache.fineract.useradministration.domain.AppUser;
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
