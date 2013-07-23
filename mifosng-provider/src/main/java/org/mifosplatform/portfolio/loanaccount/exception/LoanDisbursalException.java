package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanDisbursalException extends AbstractPlatformDomainRuleException {

    public LoanDisbursalException(final String cureentProduct, final String restrictedProduct) {
        super("error.msg.loan.disbursal.failed", "This loan could not be disbursed as `" + cureentProduct + "` and `" + restrictedProduct
                + "` are not allowed to co-exist", new Object[] { cureentProduct, restrictedProduct });
    }

}
