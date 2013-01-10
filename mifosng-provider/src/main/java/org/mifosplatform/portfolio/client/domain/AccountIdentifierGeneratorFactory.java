package org.mifosplatform.portfolio.client.domain;

import org.springframework.stereotype.Component;

@Component
public class AccountIdentifierGeneratorFactory {

    public AccountNumberGenerator determineClientAccountNoGenerator(final Long clientId) {
        return new ClientAccountNumberGenerator(clientId, 9);
    }

}
