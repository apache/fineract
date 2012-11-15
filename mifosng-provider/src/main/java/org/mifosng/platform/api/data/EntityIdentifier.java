package org.mifosng.platform.api.data;

/**
 * Represents the successful result of an REST API call.
 */
public class EntityIdentifier {

	private Long entityId;
	@SuppressWarnings("unused")
	private Long makerCheckerId;

	public static EntityIdentifier makerChecker(final Long makerCheckerId) {
		return new EntityIdentifier(null, makerCheckerId);
	}
	
	public static EntityIdentifier makerChecker(final Long resourceId, final Long makerCheckerId) {
		return new EntityIdentifier(resourceId, makerCheckerId);
	}
	
	public EntityIdentifier() {
		//
	}

	public EntityIdentifier(final Long entityId) {
		this.entityId = entityId;
	}
	
	private EntityIdentifier(final Long entityId, final Long makerCheckerId) {
		this.entityId = entityId;
		this.makerCheckerId = makerCheckerId;
	}

	public Long getEntityId() {
		return this.entityId;
	}

	public void setEntityId(final Long entityId) {
		this.entityId = entityId;
	}
}
