package org.mifosng.data.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrganisationCurrencyCommand implements Serializable {

	private Collection<String> notSelectedItems = new ArrayList<String>();
	private Collection<String> selectedItems = new ArrayList<String>();

	public OrganisationCurrencyCommand() {
		//
	}

	public OrganisationCurrencyCommand(final List<String> selectedCurrencyCodes) {
		this.selectedItems = selectedCurrencyCodes;
	}

	public Collection<String> getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(Collection<String> selectedItems) {
		this.selectedItems = selectedItems;
	}

	public Collection<String> getNotSelectedItems() {
		return notSelectedItems;
	}

	public void setNotSelectedItems(Collection<String> notSelectedItems) {
		this.notSelectedItems = notSelectedItems;
	}
}