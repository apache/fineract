/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

public class MifosEntity {
	private Long entityId;
	private MifosEntityType type;
	
	@SuppressWarnings("unused")
	private MifosEntity() {
	}
	
	public MifosEntity(Long entityId, MifosEntityType type) {
		this.entityId = entityId;
		this.type = type;
	}
	
	public Long getId () {
		return this.entityId;
	}
	
	public MifosEntityType getType () {
		return this.type;
	}
}
