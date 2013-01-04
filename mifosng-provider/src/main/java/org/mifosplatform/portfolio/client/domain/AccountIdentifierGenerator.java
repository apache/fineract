package org.mifosplatform.portfolio.client.domain;

/**
 * Responsible for generating unique account identifiers based some rules or patterns.
 * 
 * @see ClientAccountIdentifierGenerator
 */
public interface AccountIdentifierGenerator {

    String generate();

}
