package org.mifosng.platform.api.data;

public class PermissionUsageData {

	private final String grouping;
	private final String code;
	private final Boolean selected;

	public PermissionUsageData(final String grouping, final String code,
			final Boolean selected) {
		this.grouping = grouping;
		this.code = code;
		this.selected = selected;
	}

	public String getGrouping() {
		return grouping;
	}

	public String getCode() {
		return code;
	}

	public Boolean getSelected() {
		return selected;
	}

}