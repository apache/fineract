/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class DuplicateOfficeToGLAccountMappingFoundException extends AbstractPlatformDomainRuleException {

    public DuplicateOfficeToGLAccountMappingFoundException(final Long officeId, final Integer accountType) {
        super("error.msg.officeToAccountMapping.exists.for.office", "Mapping for office with Id " + officeId
                + " exist for an account of type " + accountType, officeId, accountType);
    }

}