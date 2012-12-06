package org.mifosplatform.portfolio.client.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.ClientNoteCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    Long createClient(JsonCommand command);

    EntityIdentifier updateClientDetails(Long clientId, JsonCommand command);

    EntityIdentifier deleteClient(Long clientId, JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
    EntityIdentifier saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
    EntityIdentifier saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_CLIENTIMAGE')")
    EntityIdentifier deleteClientImage(Long clientId);

    EntityIdentifier addClientNote(ClientNoteCommand command);

    EntityIdentifier updateNote(ClientNoteCommand command);

    Long addClientIdentifier(ClientIdentifierCommand command);

    EntityIdentifier updateClientIdentifier(ClientIdentifierCommand command);

    EntityIdentifier deleteClientIdentifier(ClientIdentifierCommand command);
}