package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "identifier")
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
