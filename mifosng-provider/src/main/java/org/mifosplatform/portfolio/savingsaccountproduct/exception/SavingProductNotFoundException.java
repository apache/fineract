package org.mifosplatform.portfolio.savingsaccountproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SavingProductNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingProductNotFoundException(Long id) {
        super("error.msg.savingproduct.id.invalid", "Saving product with identifier " + id + " does not exist", id);
    }
}