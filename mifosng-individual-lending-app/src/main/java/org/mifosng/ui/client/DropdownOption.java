package org.mifosng.ui.client;

public class DropdownOption {

	private String label;
	private String value;

	public static DropdownOption of(String label, String value) {
		DropdownOption option = new DropdownOption();
		option.setLabel(label);
		option.setValue(value);
		return option;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}