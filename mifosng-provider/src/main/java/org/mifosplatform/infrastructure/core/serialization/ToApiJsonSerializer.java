/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.infrastructure.core.service.Page;

public interface ToApiJsonSerializer<T> {

    String serialize(Object object);

    String serializePretty(boolean prettyOn, Object object);

    String serializeResult(Object object);

    String serialize(ApiRequestJsonSerializationSettings settings, Collection<T> collection, Set<String> supportedResponseParameters);

    String serialize(ApiRequestJsonSerializationSettings settings, T single, Set<String> supportedResponseParameters);

    String serialize(ApiRequestJsonSerializationSettings settings, Page<T> singleObject, Set<String> supportedResponseParameters);
}