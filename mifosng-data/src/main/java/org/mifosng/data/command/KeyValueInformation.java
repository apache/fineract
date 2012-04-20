package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class KeyValueInformation {

	private String key;
	private String value;
	
	protected KeyValueInformation() {
		//
	}
	
	public KeyValueInformation(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
