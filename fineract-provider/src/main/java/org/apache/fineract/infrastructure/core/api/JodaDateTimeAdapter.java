/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.api;

import java.lang.reflect.Type;

import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for joda time {@link DateTime} that returns date as long to match
 * previous functionality.
 */
public class JodaDateTimeAdapter implements JsonSerializer<DateTime> {

    @SuppressWarnings("unused")
    @Override
    public JsonElement serialize(final DateTime src, final Type typeOfSrc, final JsonSerializationContext context) {

        JsonElement element = null;
        if (src != null) {
            element = new JsonPrimitive(src.getMillis());
        }

        return element;
    }
}
