package org.mifosplatform.portfolio.client.domain;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGeneratorFactory {

    public AccountNumberGenerator determineClientAccountNoGenerator(final Long clientId) {
        return new ZeroPaddedAccountNumberGenerator(clientId, 9);
    }

    public AccountNumberGenerator determineLoanAccountNoGenerator(final Long loanId) {
        return new ZeroPaddedAccountNumberGenerator(loanId, 9);
    }
}