package org.mifosplatform.portfolio.client.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.NoteCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    Long createClient(ClientCommand command);

    EntityIdentifier updateClientDetails(ClientCommand command);

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

    Long addClientIdentifier(ClientIdentifierCommand command);

    EntityIdentifier updateClientIdentifier(ClientIdentifierCommand command);

    EntityIdentifier deleteClientIdentifier(ClientIdentifierCommand command);
}