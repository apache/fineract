package org.mifosplatform.useradministration.data;

/**
 * Immutable representation of permissions
 */
public class PermissionData {

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

    public static PermissionData from(final String permissionCode, final boolean isSelected) {
        return new PermissionData(null, permissionCode, null, null, isSelected);
    }

    public static PermissionData instance(final String grouping, final String code, final String entityName, final String actionName,
            final Boolean selected) {
        return new PermissionData(grouping, code, entityName, actionName, selected);
    }

    private PermissionData(final String grouping, final String code, final String entityName, final String actionName,
            final Boolean selected) {
        this.grouping = grouping;
        this.code = code;
        this.entityName = entityName;
        this.actionName = actionName;
        this.selected = selected;
    }
}