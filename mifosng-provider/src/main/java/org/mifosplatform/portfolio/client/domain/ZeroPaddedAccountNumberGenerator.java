package org.mifosplatform.portfolio.client.domain;

/**
 * Example {@link AccountNumberGenerator} for clients that takes an entities
 * auto generated database id and zero fills it ensuring the identifier is
 * always of a given <code>maxLength</code>.
 */
public class ZeroPaddedAccountNumberGenerator implements AccountNumberGenerator {

    private final Long id;
    private final int maxLength;

    public ZeroPaddedAccountNumberGenerator(final Long id, int maxLength) {
        this.id = id;
        this.maxLength = maxLength;
    }

    @Override
    public String generate() {
        return String.format("%0" + maxLength + "d", id);
    }
}