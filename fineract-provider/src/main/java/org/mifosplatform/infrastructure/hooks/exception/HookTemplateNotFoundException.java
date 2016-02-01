/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class HookTemplateNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public HookTemplateNotFoundException(final String name) {
		super("error.msg.template.not.found", "Template with name `" + name
				+ "` does not exist", name);
	}

	public HookTemplateNotFoundException(final Long templateId) {
		super("error.msg.template.identifier.not.found",
				"Template with identifier `" + templateId + "` does not exist",
				templateId);
	}

}
