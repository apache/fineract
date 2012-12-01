package org.mifosplatform.infrastructure.core.api;

/**
 * Service for serializing commands into another format.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see PortfolioCommandSerializerServiceJson
 */
public interface PortfolioCommandSerializerService {

    String serializeCommandToJson(Object command);
}