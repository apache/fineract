package org.mifosplatform.infrastructure.core.serialization;

/**
 * 
 */
public interface FromApiJsonDeserializer<T> {

    T commandFromApiJson(final String json);
}