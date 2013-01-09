package org.mifosplatform.portfolio.client.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    CommandProcessingResult createClient(JsonCommand command);

    CommandProcessingResult updateClient(Long clientId, JsonCommand command);

    CommandProcessingResult deleteClient(Long clientId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_CLIENTIMAGE')")
    CommandProcessingResult deleteClientImage(Long clientId);

    CommandProcessingResult addClientNote(Long clientId, JsonCommand command);

    CommandProcessingResult updateClientNote(Long clientId, Long noteId, JsonCommand command);

    CommandProcessingResult addClientIdentifier(Long clientId, JsonCommand command);

    CommandProcessingResult updateClientIdentifier(Long clientId, Long clientIdentifierId, JsonCommand command);

    CommandProcessingResult deleteClientIdentifier(Long clientId, Long clientIdentifierId, Long commandId);
}