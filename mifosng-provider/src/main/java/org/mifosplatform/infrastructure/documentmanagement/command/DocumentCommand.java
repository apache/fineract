package org.mifosplatform.infrastructure.documentmanagement.command;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a client identifier.
 */
public class DocumentCommand {

    private final Long id;
    private final String parentEntityType;
    private final Long parentEntityId;
    private final String name;
    private final String description;

    private String fileName;
    private Long size;
    private String type;
    private String location;

    private final Set<String> modifiedParameters;

    public DocumentCommand(final Set<String> modifiedParameters, final Long id, final String parentEntityType, final Long parentEntityId,
            final String name, final String fileName, Long size, String type, final String description, String location) {
        this.modifiedParameters = modifiedParameters;
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

    public Set<String> getModifiedParameters() {
        return modifiedParameters;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isFileNameChanged() {
        return this.modifiedParameters.contains("fileName");
    }

    public boolean isSizeChanged() {
        return this.modifiedParameters.contains("size");
    }

    public boolean isFileTypeChanged() {
        return this.modifiedParameters.contains("type");
    }

    public boolean isDescriptionChanged() {
        return this.modifiedParameters.contains("description");
    }

    public boolean isLocationChanged() {
        return this.modifiedParameters.contains("location");
    }

}