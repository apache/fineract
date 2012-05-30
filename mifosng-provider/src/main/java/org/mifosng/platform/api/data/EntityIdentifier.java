package org.mifosng.platform.api.data;


public class EntityIdentifier {

	private Long entityId;

	public EntityIdentifier() {
		//
	}

	public EntityIdentifier(final Long entityId) {
		this.entityId = entityId;
	}

	public Long getEntityId() {
		return this.entityId;
	}

	public void setEntityId(final Long entityId) {
		this.entityId = entityId;
	}
}
