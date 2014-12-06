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
