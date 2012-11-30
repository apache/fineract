package org.mifosng.platform.api.infrastructure;

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