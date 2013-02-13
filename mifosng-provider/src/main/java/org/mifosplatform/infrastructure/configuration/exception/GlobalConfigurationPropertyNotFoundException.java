/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when global configuration properties are
 * not found.
 */
public class GlobalConfigurationPropertyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GlobalConfigurationPropertyNotFoundException(final String propertyName) {
        super("error.msg.configuration.property.invalid", "Configuration property `" + propertyName + "` does not exist", propertyName);
    }
}