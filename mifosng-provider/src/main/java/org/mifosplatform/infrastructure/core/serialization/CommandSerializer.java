package org.mifosplatform.infrastructure.core.serialization;


/**
 * Service for serializing commands into another format.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see CommandSerializerDefaultToJson
 */
public interface CommandSerializer {

    String serializeCommandToJson(Object command);
}