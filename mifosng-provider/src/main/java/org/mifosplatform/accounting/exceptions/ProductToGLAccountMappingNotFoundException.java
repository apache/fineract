package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.accounting.domain.PortfolioProductType;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class ProductToGLAccountMappingNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProductToGLAccountMappingNotFoundException(final PortfolioProductType type, final Long productId, String accountType) {
        super("error.msg.productTyAccountMapping.not.found", "Mapping for product of type" + type.toString() + " with Id " + productId
                + " does not exist for an account of type " + accountType, type.toString(), productId, accountType);
    }
}