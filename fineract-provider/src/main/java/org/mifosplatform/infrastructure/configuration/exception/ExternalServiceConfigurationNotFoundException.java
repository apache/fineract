/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ExternalServiceConfigurationNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ExternalServiceConfigurationNotFoundException(final String serviceName) {
        super("error.msg.externalservice.servicename.invalid", "Service Name`" + serviceName + "` does not exist", serviceName);
    }

    public ExternalServiceConfigurationNotFoundException(final String externalServiceName, final String name) {
        super("error.msg.externalservice.property.invalid",
                "Parameter`" + name + "` does not exist for the ServiceName `" + externalServiceName + "`", name, externalServiceName);
    }
}
