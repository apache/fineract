package org.mifosng.platform.client.service;

import java.io.InputStream;

import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.ClientIdentifierCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.common.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENT')")
	Long createClient(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_CLIENT')")
	EntityIdentifier updateClientDetails(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_CLIENT')")
	EntityIdentifier deleteClient(ClientCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
	EntityIdentifier saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
	EntityIdentifier saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_CLIENTIMAGE')")
	EntityIdentifier deleteClientImage(Long clientId);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTNOTE')")
	EntityIdentifier addClientNote(NoteCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_CLIENTNOTE')")
	EntityIdentifier updateNote(NoteCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIDENTIFIER')")
	Long addClientIdentifier(ClientIdentifierCommand clientIdentifierCommand);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_CLIENTIDENTIFIER')")
	EntityIdentifier updateClientIdentifier(ClientIdentifierCommand clientIdentifierCommand);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_CLIENTIDENTIFIER')")
	EntityIdentifier deleteClientIdentifier(Long clientIdentifierId);
}