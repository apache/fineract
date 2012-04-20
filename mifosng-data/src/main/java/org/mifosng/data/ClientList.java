package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClientList {

	private Collection<ClientData> clients = new ArrayList<ClientData>();

	protected ClientList() {
		//
	}

	public ClientList(final Collection<ClientData> clients) {
		this.clients = clients;
	}

	public Collection<ClientData> getClients() {
		return this.clients;
	}

	public void setClients(final Collection<ClientData> clients) {
		this.clients = clients;
	}
}