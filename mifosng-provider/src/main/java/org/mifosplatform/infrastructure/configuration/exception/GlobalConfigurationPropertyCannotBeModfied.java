/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class GlobalConfigurationPropertyCannotBeModfied extends AbstractPlatformDomainRuleException{
    
    public GlobalConfigurationPropertyCannotBeModfied(final Long configId) {
        super("error.msg.configuration.id.not.modifiable", "Configuration identifier `" + configId + "` cannot be modified", new Object[] {configId});
    }

}
