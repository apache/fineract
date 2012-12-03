package org.mifosplatform.infrastructure.core.serialization;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JodaDateTimeAdapter;
import org.mifosplatform.infrastructure.core.api.JodaLocalDateAdapter;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>A google gson implementation of {@link ExcludeNothingJsonSerializer} contract.</p>
 * 
 * <p>It serializes all fields of any Java {@link Object} passed to it.</p>
 */
@Component
public final class ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson {

    private final Gson gson;

    public ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
        builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
        builder.setPrettyPrinting();
        
        this.gson = builder.create();
    }

    public String serialize(final Object result) {
        return this.gson.toJson(result);
    }
}