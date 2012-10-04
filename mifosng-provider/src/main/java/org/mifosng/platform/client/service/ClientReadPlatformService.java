package org.mifosng.platform.client.service;

import java.util.Collection;

import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.ClientAccountSummaryCollectionData;
import org.mifosng.platform.api.data.ClientLookup;
import org.mifosng.platform.api.data.NoteData;

public interface ClientReadPlatformService {

	Collection<ClientData> retrieveAllIndividualClients(String extraCriteria);

	ClientData retrieveIndividualClient(Long clientId);

	ClientData retrieveNewClientDetails();

    Collection<ClientLookup> retrieveAllIndividualClientsForLookup(String extraCriteria);

	ClientAccountSummaryCollectionData retrieveClientAccountDetails(Long clientId);

	Collection<NoteData> retrieveAllClientNotes(Long clientId);

	NoteData retrieveClientNote(Long clientId, Long noteId);
}