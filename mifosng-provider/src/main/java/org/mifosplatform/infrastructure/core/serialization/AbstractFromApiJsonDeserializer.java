/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

/**
 * Abstract implementation of {@link FromApiJsonDeserializer} that can be
 * extended for specific commands.
 */
public abstract class AbstractFromApiJsonDeserializer<T> implements FromApiJsonDeserializer<T> {

    @Override
    public abstract T commandFromApiJson(final String json);
}