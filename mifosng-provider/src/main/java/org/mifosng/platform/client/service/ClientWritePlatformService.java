package org.mifosng.platform.client.service;

import java.io.InputStream;

import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.ClientIdentifierCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.common.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_ENROLL_NEW_CLIENT_ROLE')")
	Long enrollClient(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier updateClientDetails(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier deleteClientImage(Long clientId);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier addClientNote(NoteCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier updateNote(NoteCommand command);

	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'CAN_DELETE_CLIENT_ROLE')")
	EntityIdentifier deleteClient(Long clientId);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	Long addClientIdentifier(ClientIdentifierCommand clientIdentifierCommand);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier updateClientIdentifier(ClientIdentifierCommand clientIdentifierCommand);
	
	@PreAuthorize(value = "hasAnyRole('PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE')")
	EntityIdentifier deleteClientIdentifier(Long clientIdentifierId);
}