package org.mifosplatform.portfolio.client.domain;

/**
 * Responsible for generating unique account number based on some rules or patterns.
 * 
 * @see ZeroPaddedAccountNumberGenerator
 */
public interface AccountNumberGenerator {

    String generate();

}
