/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class NoProvisioningCriteriaDefinitionFound extends AbstractPlatformResourceNotFoundException {

    public NoProvisioningCriteriaDefinitionFound() {
        super("error.msg.no.provisioning.criteria.definitions.found", "No Provisioning Criteria Definitions are found");
    }
}
