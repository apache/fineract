package org.mifosng.platform.client.service;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_ENROLL_NEW_CLIENT_ROLE')")
	Long enrollClient(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier updateClientDetails(ClientCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier addClientNote(NoteCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier updateNote(NoteCommand command);
}