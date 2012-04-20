package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * For creating and updating roles. When updating the id is expected to be populated.
 */
@XmlRootElement
public class RoleCommand {

	private Long id;
	private String name;
	private String description;
	private String[] permissionIds;

	protected RoleCommand() {
		//
	}

	public RoleCommand(final String name, final String description, final String[] permissionIds) {
		this.name = name;
		this.description = description;
		this.permissionIds = permissionIds;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String[] getPermissionIds() {
		return this.permissionIds;
	}

	public void setPermissionIds(final String[] permissionIds) {
		this.permissionIds = permissionIds;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}