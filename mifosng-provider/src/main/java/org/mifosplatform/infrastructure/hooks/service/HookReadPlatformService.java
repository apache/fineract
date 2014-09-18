/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.hooks.data.HookData;

public interface HookReadPlatformService {

	Collection<HookData> retrieveAllHooks();

	HookData retrieveHook(Long hookId);

	HookData retrieveNewHookDetails(String templateName);
}
