package org.mifosplatform.portfolio.client.domain;

/**
 * Responsible for generating unique account number based on some rules or patterns.
 * 
 * @see ClientAccountNumberGenerator
 */
public interface AccountNumberGenerator {

    String generate();

}
