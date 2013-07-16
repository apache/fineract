package org.mifosplatform.portfolio.loanproduct.productmix.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when no product mixes found with the
 * productId.
 */
public class ProductMixNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProductMixNotFoundException(final Long productId) {
        super("error.msg.no.product.mixes.exists", "No product mixes are defined with the productId `" + productId + "`.", productId);
    }

}
