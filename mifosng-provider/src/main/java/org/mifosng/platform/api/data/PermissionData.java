package org.mifosng.platform.api.data;

/**
 * Immutable data object for permission data.
 */
public class PermissionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;
    private final String code;

    public PermissionData(final Long id, final String name, final String description, final String code) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        PermissionData data = (PermissionData) obj;
        return this.code.equalsIgnoreCase(data.code);
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }
}