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
	
	private String[] permissions;

	public RoleCommand() {
		//
	}

	public RoleCommand(final String name, final String description, final String[] permissionIds) {
		this.name = name;
		this.description = description;
		this.permissions = permissionIds;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}
}