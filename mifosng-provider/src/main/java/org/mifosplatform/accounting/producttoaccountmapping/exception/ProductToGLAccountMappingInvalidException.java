/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class ProductToGLAccountMappingInvalidException extends AbstractPlatformDomainRuleException {

    public ProductToGLAccountMappingInvalidException(final String paramName, final String accountName, final Long accountId,
            final String actualAccountCategory, final String expectedAccountCategory) {
        super("error.msg." + paramName + ".invalid.account.type", "Passed in GLAccount " + paramName + " with Id " + accountId
                + "maps to the account " + accountName + " of type " + actualAccountCategory + ", the expected account type was "
                + expectedAccountCategory, paramName, accountId, accountName, actualAccountCategory, expectedAccountCategory);
    }
}