package org.mifosng.platform.api.commands;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ImportClientCommand {

	private List<ClientCommand> clients = new ArrayList<ClientCommand>();

	public ImportClientCommand() {
		//
	}

	public List<ClientCommand> getClients() {
		return clients;
	}

	public void setClients(List<ClientCommand> clients) {
		this.clients = clients;
	}
}