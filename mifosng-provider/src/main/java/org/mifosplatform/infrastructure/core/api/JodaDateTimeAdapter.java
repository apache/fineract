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
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {

        JsonElement element = null;
        if (src != null) {
            element = new JsonPrimitive(src.getMillis());
        }

        return element;
    }
}
