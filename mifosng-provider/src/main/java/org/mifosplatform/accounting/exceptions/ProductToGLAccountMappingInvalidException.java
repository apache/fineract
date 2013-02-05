package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class ProductToGLAccountMappingInvalidException extends AbstractPlatformDomainRuleException {

    public ProductToGLAccountMappingInvalidException(final String paramName, String accountName, Long accountId,
            String actualAccountCategory, String expectedAccountCategory) {
        super("error.msg." + paramName + ".invalid.account.type", "Passed in GLAccount " + paramName + " with Id " + accountId
                + "maps to the account " + accountName + " of type " + actualAccountCategory + ", the expected account type was "
                + expectedAccountCategory, paramName, accountId, accountName, actualAccountCategory, expectedAccountCategory);
    }
}