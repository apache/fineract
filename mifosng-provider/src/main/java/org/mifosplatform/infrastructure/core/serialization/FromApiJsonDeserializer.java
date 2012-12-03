package org.mifosplatform.infrastructure.core.serialization;

/**
 * 
 */
public interface FromApiJsonDeserializer<T> {

    T commandFromApiJson(final String json);

    T commandFromApiJson(final Long resourceId, final String json);
    
    String serializedCommandJsonFromApiJson(final String json);
    
    String serializedCommandJsonFromApiJson(final Long resourceId, final String json);
}