package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when loan collateral does
 * not exist.
 */
public class LoanCollateralNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanCollateralNotFoundException(final Long id) {
        super("error.msg.loanCollateral.id.invalid", "Loan collateral with identifier " + id + " does not exist", id);
    }

    public LoanCollateralNotFoundException(final Long id, final Long loanId) {
        super("error.msg.loanCollateral.id.invalid.for.given.loan", "Loan collateral with identifier " + id + " does not exist for loan "
                + loanId, id, loanId);
    }
}