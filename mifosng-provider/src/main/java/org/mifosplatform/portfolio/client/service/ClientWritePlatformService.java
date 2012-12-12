package org.mifosplatform.portfolio.client.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    EntityIdentifier createClient(JsonCommand command);

    EntityIdentifier updateClient(Long clientId, JsonCommand command);

    EntityIdentifier deleteClient(Long clientId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
    EntityIdentifier saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_CLIENTIMAGE')")
    EntityIdentifier saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_CLIENTIMAGE')")
    EntityIdentifier deleteClientImage(Long clientId);

    EntityIdentifier addClientNote(Long clientId, JsonCommand command);

    EntityIdentifier updateClientNote(Long clientId, Long noteId, JsonCommand command);

    EntityIdentifier addClientIdentifier(Long clientId, JsonCommand command);

    EntityIdentifier updateClientIdentifier(Long clientId, Long clientIdentifierId, JsonCommand command);

    EntityIdentifier deleteClientIdentifier(Long clientId, Long clientIdentifierId, Long commandId);
}