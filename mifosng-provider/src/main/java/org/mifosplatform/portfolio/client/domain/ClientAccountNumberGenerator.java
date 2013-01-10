package org.mifosplatform.portfolio.client.domain;

/**
 * Example {@link AccountNumberGenerator} for clients that takes clients
 * auto generated database id and zero fills it ensuring the identifier is
 * always of a given <code>maxLength</code>.
 */
public class ClientAccountNumberGenerator implements AccountNumberGenerator {

    private final Long clientId;
    private final int maxLength;

    public ClientAccountNumberGenerator(final Long clientId, int maxLength) {
        this.clientId = clientId;
        this.maxLength = maxLength;}

    @Override
    public String generate() {
        return String.format("%0" + maxLength + "d", clientId);
    }
}