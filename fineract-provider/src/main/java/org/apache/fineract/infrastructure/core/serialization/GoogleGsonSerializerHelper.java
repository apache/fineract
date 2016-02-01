/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.infrastructure.core.api.JodaDateTimeAdapter;
import org.apache.fineract.infrastructure.core.api.JodaLocalDateAdapter;
import org.apache.fineract.infrastructure.core.api.JodaMonthDayAdapter;
import org.apache.fineract.infrastructure.core.api.ParameterListExclusionStrategy;
import org.apache.fineract.infrastructure.core.api.ParameterListInclusionStrategy;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.stereotype.Service;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Helper class for serialization of java objects into JSON using google-gson.
 */
@Service
public final class GoogleGsonSerializerHelper {

    public Gson createGsonBuilder(final boolean prettyPrint) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
        builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
        builder.registerTypeAdapter(MonthDay.class, new JodaMonthDayAdapter());
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    public Gson createGsonBuilderForPartialResponseFiltering(final boolean prettyPrint, final Set<String> responseParameters) {

        final ExclusionStrategy strategy = new ParameterListInclusionStrategy(responseParameters);

        final GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
        builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
        builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
        builder.registerTypeAdapter(MonthDay.class, new JodaMonthDayAdapter());
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    public Gson createGsonBuilderWithParameterExclusionSerializationStrategy(final Set<String> supportedParameters,
            final boolean prettyPrint, final Set<String> responseParameters) {

        final Set<String> parameterNamesToSkip = new HashSet<>();

        if (!responseParameters.isEmpty()) {

            // strip out all known support parameters from expected response to
            // see if unsupported parameters requested for response.
            final Set<String> differentParametersDetectedSet = new HashSet<>(responseParameters);
            differentParametersDetectedSet.removeAll(supportedParameters);

            if (!differentParametersDetectedSet.isEmpty()) { throw new UnsupportedParameterException(new ArrayList<>(
                    differentParametersDetectedSet)); }

            parameterNamesToSkip.addAll(supportedParameters);
            parameterNamesToSkip.removeAll(responseParameters);
        }

        final ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);

        final GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
        builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
        builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
        builder.registerTypeAdapter(MonthDay.class, new JodaMonthDayAdapter());
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    public String serializedJsonFrom(final Gson serializer, final Object[] dataObjects) {
        return serializer.toJson(dataObjects);
    }

    public String serializedJsonFrom(final Gson serializer, final Object singleDataObject) {
        return serializer.toJson(singleDataObject);
    }
}