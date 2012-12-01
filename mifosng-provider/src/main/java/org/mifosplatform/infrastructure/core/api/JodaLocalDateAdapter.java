package org.mifosplatform.infrastructure.core.api;

import java.lang.reflect.Type;

import org.joda.time.LocalDate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for joda time {@link LocalDate} that returns date in array format
 * to match previous jackson functionality.
 */
public class JodaLocalDateAdapter implements JsonSerializer<LocalDate> {

    @SuppressWarnings("unused")
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {

        JsonArray array = null;
        if (src != null) {
            array = new JsonArray();
            array.add(new JsonPrimitive(src.getYearOfEra()));
            array.add(new JsonPrimitive(src.getMonthOfYear()));
            array.add(new JsonPrimitive(src.getDayOfMonth()));
        }

        return array;
    }
}