/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.mifosplatform.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GlobalConfigurationRepository} that adds NULL checking and
 * Error handling capabilities
 * </p>
 */
@Service
public class GlobalConfigurationRepositoryWrapper {

    private final GlobalConfigurationRepository repository;

    @Autowired
    public GlobalConfigurationRepositoryWrapper(final GlobalConfigurationRepository repository) {
        this.repository = repository;
    }

    public GlobalConfigurationProperty findOneByNameWithNotFoundDetection(final String propertyName) {
        final GlobalConfigurationProperty property = this.repository.findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property;
    }

    public GlobalConfigurationProperty findOneWithNotFoundDetection(final Long configId) {
        final GlobalConfigurationProperty property = this.repository.findOne(configId);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(configId); }
        return property;
    }

    public void save(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.save(globalConfigurationProperty);
    }

    public void saveAndFlush(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.saveAndFlush(globalConfigurationProperty);
    }

    public void delete(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.delete(globalConfigurationProperty);
    }

}