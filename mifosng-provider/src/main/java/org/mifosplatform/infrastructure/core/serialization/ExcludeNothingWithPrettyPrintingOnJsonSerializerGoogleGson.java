/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.api.JodaDateTimeAdapter;
import org.mifosplatform.infrastructure.core.api.JodaLocalDateAdapter;
import org.mifosplatform.infrastructure.core.api.JodaMonthDayAdapter;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * A google gson implementation of {@link ExcludeNothingJsonSerializer}
 * contract.
 * </p>
 * 
 * <p>
 * It serializes all fields of any Java {@link Object} passed to it.
 * </p>
 */
@Component
public final class ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson {

    private final Gson gson;

    public ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson() {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
        builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
        builder.registerTypeAdapter(MonthDay.class, new JodaMonthDayAdapter());
        builder.setPrettyPrinting();

        this.gson = builder.create();
    }

    public String serialize(final Object result) {
        return this.gson.toJson(result);
    }
}