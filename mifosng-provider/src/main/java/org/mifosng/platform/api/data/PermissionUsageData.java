package org.mifosng.platform.api.data;

public class PermissionUsageData {

	private final String grouping;
	private final String code;
	private final String entityName;
	private final String actionName;
	private final Boolean selected;

	public PermissionUsageData(final String grouping, final String code,
			final String entityName, final String actionName,
			final Boolean selected) {
		this.grouping = grouping;
		this.code = code;
		this.entityName = entityName;
		this.actionName = actionName;
		this.selected = selected;
	}

	public String getGrouping() {
		return grouping;
	}

	public String getCode() {
		return code;
	}

	public String getEntityName() {
		return entityName;
	}

	public String getActionName() {
		return actionName;
	}

	public Boolean getSelected() {
		return selected;
	}

}