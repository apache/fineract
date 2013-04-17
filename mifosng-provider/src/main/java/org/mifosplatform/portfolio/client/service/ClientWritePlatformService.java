/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    CommandProcessingResult createClient(JsonCommand command);

    CommandProcessingResult updateClient(Long clientId, JsonCommand command);

    CommandProcessingResult activateClient(Long clientId, JsonCommand command);

    CommandProcessingResult deleteClient(Long clientId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_CLIENTIMAGE')")
    CommandProcessingResult deleteClientImage(Long clientId);
}