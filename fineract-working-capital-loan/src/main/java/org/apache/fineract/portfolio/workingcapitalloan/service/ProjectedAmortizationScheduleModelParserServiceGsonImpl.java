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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.ToNumberPolicy;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExcludeAnnotationBasedExclusionStrategy;
import org.apache.fineract.infrastructure.core.serialization.gson.LocalDateAdapter;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.serialization.MoneyDeserializer;
import org.apache.fineract.organisation.monetary.serialization.MoneySerializer;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProjectedAmortizationScheduleModelParserServiceGsonImpl implements ProjectedAmortizationScheduleModelParserService {

    private final Gson serializer = createSerializer();

    private static Gson createSerializer() {
        return new GsonBuilder() //
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe()) //
                .registerTypeAdapter(Money.class, new MoneySerializer()) //
                .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL) //
                .addSerializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()) //
                .addDeserializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()) //
                .create();
    }

    private static Gson createDeserializer(final MathContext mc, final MonetaryCurrency currency) {
        return new GsonBuilder() //
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe()) //
                .registerTypeAdapter(Money.class, new MoneyDeserializer(mc, currency)) //
                .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL) //
                .registerTypeAdapter(ProjectedAmortizationScheduleModel.class,
                        (InstanceCreator<ProjectedAmortizationScheduleModel>) type -> ProjectedAmortizationScheduleModel
                                .forDeserialization(mc, currency)) //
                .addSerializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()) //
                .addDeserializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()) //
                .create();
    }

    @Override
    public String toJson(@NonNull final ProjectedAmortizationScheduleModel model) {
        return serializer.toJson(model);
    }

    @Override
    @Nullable
    public ProjectedAmortizationScheduleModel fromJson(@Nullable final String json, @NonNull final MathContext mc,
            @NonNull final MonetaryCurrency currency) {
        if (json == null) {
            return null;
        }
        try {
            return createDeserializer(mc, currency).fromJson(json, ProjectedAmortizationScheduleModel.class);
        } catch (Exception e) {
            log.warn("Failed to parse ProjectedAmortizationScheduleModel JSON. Falling back to null.", e);
            return null;
        }
    }
}
