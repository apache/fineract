package org.mifosng.platform.documentmanagement.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_document")
public class Document extends AbstractAuditableCustom<AppUser, Long> {

	@SuppressWarnings("unused")
	@Column(name = "parent_entity_type", length = 50)
	private String parentEntityType;

	@SuppressWarnings("unused")
	@Column(name = "parent_entity_id", length = 1000)
	private Long parentEntityId;

	@SuppressWarnings("unused")
	@Column(name = "name", length = 250)
	private String name;

	@SuppressWarnings("unused")
	@Column(name = "file_name", length = 250)
	private String fileName;

	@SuppressWarnings("unused")
	@Column(name = "size")
	private Long size;

	@SuppressWarnings("unused")
	@Column(name = "type", length = 50)
	private String type;

	@SuppressWarnings("unused")
	@Column(name = "description", length = 1000)
	private String description;

	@SuppressWarnings("unused")
	@Column(name = "location", length = 500)
	private String location;

	public Document() {
	}

	public static Document createNew(String parentEntityType,
			Long parentEntityId, String name, String fileName, Long size,
			String type, String description, String location) {
		return new Document(parentEntityType, parentEntityId, name, fileName,
				size, type, description, location);
	}

	private Document(String parentEntityType, Long parentEntityId, String name,
			String fileName, Long size, String type, String description,
			String location) {
		this.parentEntityType = StringUtils.defaultIfEmpty(parentEntityType,
				null);
		this.parentEntityId = parentEntityId;
		this.name = StringUtils.defaultIfEmpty(name, null);
		this.fileName = StringUtils.defaultIfEmpty(fileName, null);
		this.size = size;
		this.type = StringUtils.defaultIfEmpty(type, null);
		this.description = StringUtils.defaultIfEmpty(description, null);
		this.location = StringUtils.defaultIfEmpty(location, null);
	}

	public void update(final DocumentCommand command) {
		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}
		if (command.isFileNameChanged()) {
			this.fileName = command.getFileName();
		}
		if (command.isFileTypeChanged()) {
			this.type = command.getType();
		}
		if (command.isLocationChanged()) {
			this.location = command.getLocation();
		}
		if (command.isNameChanged()) {
			this.name = command.getName();
		}
		if (command.isSizeChanged()) {
			this.size = command.getSize();
		}

	}

}