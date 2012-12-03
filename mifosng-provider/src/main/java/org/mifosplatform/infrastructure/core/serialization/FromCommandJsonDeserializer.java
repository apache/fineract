package org.mifosplatform.infrastructure.core.serialization;

/**
 * 
 */
public interface FromCommandJsonDeserializer<T> {

    T commandFromCommandJson(final String json);

    T commandFromCommandJson(final Long resourceId, final String json);
    
    T commandFromCommandJson(final Long resourceId, final String json, final boolean makerCheckerApproval);
}