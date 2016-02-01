/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class XBRLMappingInvalidException extends AbstractPlatformDomainRuleException {

    public XBRLMappingInvalidException(final String msg) {
        super("error.msg.xbrl.report.mapping.invalid.id", "Mapping does not exist", msg);
    }

}
