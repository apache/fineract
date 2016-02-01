/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.data;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class HookTemplateData implements Serializable {

	private final Long id;
	private final String name;

	// associations
	private final List<Field> schema;

	public static HookTemplateData instance(final Long id, final String name,
			final List<Field> schema) {
		return new HookTemplateData(id, name, schema);
	}

	private HookTemplateData(final Long id, final String name,
			final List<Field> schema) {
		this.id = id;
		this.name = name;

		// associations
		this.schema = schema;
	}

	public Long getServiceId() {
		return this.id;
	}

}
