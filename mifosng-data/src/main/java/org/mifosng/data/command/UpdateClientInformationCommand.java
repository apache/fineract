package org.mifosng.data.command;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.mifosng.data.EntityIdentifier;

@XmlRootElement
public class UpdateClientInformationCommand {

	private EntityIdentifier id;
	private List<KeyValueInformation> fields = new ArrayList<KeyValueInformation>(); 
	
	protected UpdateClientInformationCommand() {
		//
	}
	
	public UpdateClientInformationCommand(EntityIdentifier id, List<KeyValueInformation> fields) {
		this.id = id;
		this.fields = fields;
	}

	public List<KeyValueInformation> getFields() {
		return fields;
	}

	public void setFields(List<KeyValueInformation> fields) {
		this.fields = fields;
	}

	public EntityIdentifier getId() {
		return id;
	}

	public void setId(EntityIdentifier id) {
		this.id = id;
	}
}