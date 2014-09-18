/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class HookNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public HookNotFoundException(final String name) {
		super("error.msg.hook.not.found", "Hook with name `" + name
				+ "` does not exist", name);
	}

	public HookNotFoundException(final Long hookId) {
		super("error.msg.hook.identifier.not.found", "Hook with identifier `"
				+ hookId + "` does not exist", hookId);
	}
}
