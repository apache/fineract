package org.mifosng.platform.client.service;

import java.util.Collection;

import org.mifosng.data.ClientData;

public interface ClientReadPlatformService {

	Collection<ClientData> retrieveAllIndividualClients();

	ClientData retrieveIndividualClient(Long clientId);
}