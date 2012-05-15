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
	
	private String[] notSelectedItems;
	private String[] selectedItems;

	protected RoleCommand() {
		//
	}

	public RoleCommand(final String name, final String description, final String[] permissionIds) {
		this.name = name;
		this.description = description;
		this.selectedItems = permissionIds;
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

	public String[] getNotSelectedItems() {
		return notSelectedItems;
	}

	public void setNotSelectedItems(String... notSelectedItems) {
		this.notSelectedItems = notSelectedItems;
	}

	public String[] getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(String... selectedItems) {
		this.selectedItems = selectedItems;
	}
}