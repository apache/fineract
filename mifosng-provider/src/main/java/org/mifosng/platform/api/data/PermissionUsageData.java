package org.mifosng.platform.api.data;

/**
 * Immutable representation of permissions
 */
public class PermissionUsageData {

    @SuppressWarnings("unused")
    private final String grouping;
    @SuppressWarnings("unused")
    private final String code;
    @SuppressWarnings("unused")
    private final String entityName;
    @SuppressWarnings("unused")
    private final String actionName;
    @SuppressWarnings("unused")
    private final Boolean selected;

    public static PermissionUsageData from(final String permissionCode, final boolean isSelected) {
        return new PermissionUsageData(null, permissionCode, null, null, isSelected);
    }

    public PermissionUsageData(final String grouping, final String code, final String entityName, final String actionName,
            final Boolean selected) {
        this.grouping = grouping;
        this.code = code;
        this.entityName = entityName;
        this.actionName = actionName;
        this.selected = selected;
    }
}