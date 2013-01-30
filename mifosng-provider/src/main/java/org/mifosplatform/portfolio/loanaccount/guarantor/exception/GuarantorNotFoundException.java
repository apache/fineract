package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when guarantor resources are not found.
 */
public class GuarantorNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GuarantorNotFoundException(Long id) {
        super("error.msg.loan.guarantor.", "Guarantor with identifier " + id + " does not exist", id);
    }
}