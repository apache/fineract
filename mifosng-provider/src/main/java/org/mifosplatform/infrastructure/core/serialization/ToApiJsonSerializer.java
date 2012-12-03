package org.mifosplatform.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

public interface ToApiJsonSerializer<T> {

    String serialize(final Object object);

    String serialize(final ApiRequestJsonSerializationSettings settings, Collection<T> collection, Set<String> supportedResponseParameters);

    String serialize(final ApiRequestJsonSerializationSettings settings, T single, Set<String> supportedResponseParameters);
}