package org.mifosng.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "permission")
public class PermissionData implements Serializable {

	private Long id;
	private Long orgId;
	private String name;
	private String description;
	private String code;
	private int groupType;

	public PermissionData() {
		//
	}

	public PermissionData(final Long id, final Long orgId,
			final String name, final String description, final String code, int groupType) {
		this.id = id;
		this.orgId = orgId;
		this.name = name;
		this.description = description;
		this.code = code;
		this.groupType = groupType;
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

	public Long getId() {
		return this.id;
	}

	public Long getOrgId() {
		return this.orgId;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getCode() {
		return this.code;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setOrgId(final Long orgId) {
		this.orgId = orgId;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}
}