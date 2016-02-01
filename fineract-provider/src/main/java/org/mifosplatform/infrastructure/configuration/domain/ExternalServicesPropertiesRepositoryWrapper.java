/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.mifosplatform.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalServicesPropertiesRepositoryWrapper {

    private final ExternalServicesPropertiesRepository repository;

    @Autowired
    public ExternalServicesPropertiesRepositoryWrapper(final ExternalServicesPropertiesRepository repository) {
        this.repository = repository;
    }

    public ExternalServicesProperties findOneByIdAndName(Long id, String name, String externalServiceName) {
        final ExternalServicesProperties externalServicesProperties = this.repository
                .findOneByExternalServicePropertiesPK(new ExternalServicePropertiesPK(id, name));
        if (externalServicesProperties == null) throw new ExternalServiceConfigurationNotFoundException(externalServiceName, name);
        return externalServicesProperties;
    }
}
