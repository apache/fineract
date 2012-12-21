package org.mifosplatform.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

public interface ToApiJsonSerializer<T> {

    String serialize(Object object);
    
    String serializePretty(boolean prettyOn, Object object);
    
    String serializeResult(Object object);

    String serialize(ApiRequestJsonSerializationSettings settings, Collection<T> collection, Set<String> supportedResponseParameters);

    String serialize(ApiRequestJsonSerializationSettings settings, T single, Set<String> supportedResponseParameters);
}