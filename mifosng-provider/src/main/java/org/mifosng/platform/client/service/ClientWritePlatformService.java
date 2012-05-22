package org.mifosng.platform.client.service;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.ClientCommand;
import org.mifosng.data.command.NoteCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_ENROLL_NEW_CLIENT_ROLE')")
	Long enrollClient(ClientCommand command);
	
	EntityIdentifier updateClientDetails(ClientCommand command);
	
	// FIXME - add permission for adding and updating client notes.
	EntityIdentifier addClientNote(NoteCommand command);

	EntityIdentifier updateNote(NoteCommand command);

	
}