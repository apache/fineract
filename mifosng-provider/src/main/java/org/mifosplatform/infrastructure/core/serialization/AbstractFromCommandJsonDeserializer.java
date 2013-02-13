/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

/**
 * Abstract implementation of {@link FromCommandJsonDeserializer} that can be
 * extended for specific commands.
 */
public abstract class AbstractFromCommandJsonDeserializer<T> implements FromCommandJsonDeserializer<T> {

    @Override
    public T commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public T commandFromCommandJson(final Long codeId, final String commandAsJson) {
        return commandFromCommandJson(codeId, commandAsJson, false);
    }
}