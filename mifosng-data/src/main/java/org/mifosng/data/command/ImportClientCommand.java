package org.mifosng.data.command;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ImportClientCommand {

	private List<EnrollClientCommand> clients = new ArrayList<EnrollClientCommand>();

	public ImportClientCommand() {
		//
	}

	public List<EnrollClientCommand> getClients() {
		return clients;
	}

	public void setClients(List<EnrollClientCommand> clients) {
		this.clients = clients;
	}
}