package org.mifosng.platform.api.data;


public class DocumentData {

	private final Long id;
	private final String parentEntityType;
	private final Long parentEntityId;
	private final String name;
	private final String fileName;
	private final Long size;
	private final String type;
	private final String description;
	private final String location;

	public DocumentData(final Long id, final String parentEntityType,
			final Long parentEntityId, final String name,
			final String fileName, final Long size, final String type,
			final String description, final String location) {
		this.id = id;
		this.parentEntityType = parentEntityType;
		this.parentEntityId = parentEntityId;
		this.name = name;
		this.fileName = fileName;
		this.size = size;
		this.type = type;
		this.description = description;
		this.location = location;
	}

	public Long getId() {
		return id;
	}

	public String getParentEntityType() {
		return parentEntityType;
	}

	public Long getParentEntityId() {
		return parentEntityId;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getSize() {
		return size;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

}