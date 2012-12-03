package org.mifosplatform.portfolio.savingsaccountproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SavingsProductNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingsProductNotFoundException(Long id) {
        super("error.msg.product.id.invalid", "Product with identifier " + id + " does not exist", id);
    }

}
