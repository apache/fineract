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
package org.apache.fineract.portfolio.loanaccount.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.ToNumberPolicy;
import jakarta.validation.constraints.NotNull;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExcludeAnnotationBasedExclusionStrategy;
import org.apache.fineract.infrastructure.core.serialization.gson.LocalDateAdapter;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.serialization.MoneyDeserializer;
import org.apache.fineract.organisation.monetary.serialization.MoneySerializer;
import org.apache.fineract.portfolio.loanproduct.calc.data.InterestPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

@Slf4j
@RequiredArgsConstructor
public class ProgressiveLoanInterestScheduleModelParserServiceGsonImpl implements ProgressiveLoanInterestScheduleModelParserService {

    private final Gson gsonSerializer = createSerializer();

    private Gson createSerializer() {
        return new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
                .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL).registerTypeAdapter(Money.class, new MoneySerializer())
                .addDeserializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()).create();
    }

    private Gson createDeserializer(LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, MathContext mc,
            Integer installmentAmountInMultipliesOf) {
        InterestScheduleModelServiceGsonContext ctx = new InterestScheduleModelServiceGsonContext(
                new MonetaryCurrency(loanProductRelatedDetail.getCurrencyData()), mc, loanProductRelatedDetail,
                installmentAmountInMultipliesOf);
        return new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
                .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL)
                .registerTypeAdapter(Money.class, new MoneyDeserializer(ctx.getMc(), ctx.getCurrency()))
                .registerTypeAdapter(InterestPeriod.class, (InstanceCreator<InterestPeriod>) ctx::createInterestPeriodInstance)
                .registerTypeAdapter(ProgressiveLoanInterestScheduleModel.class,
                        (InstanceCreator<ProgressiveLoanInterestScheduleModel>) ctx::createProgressiveLoanInterestScheduleModelInstance)
                .registerTypeAdapter(RepaymentPeriod.class, (InstanceCreator<RepaymentPeriod>) ctx::createRepaymentPeriodInstance)
                .addDeserializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new JsonExcludeAnnotationBasedExclusionStrategy()).create();
    }

    @Override
    public String toJson(@NotNull ProgressiveLoanInterestScheduleModel model) {
        return gsonSerializer.toJson(model);
    }

    @Override
    public ProgressiveLoanInterestScheduleModel fromJson(String s,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, @NotNull MathContext mc,
            Integer installmentAmountInMultipliesOf) {
        if (s == null) {
            return null;
        }
        try {
            Gson gson = createDeserializer(loanProductRelatedDetail, mc, installmentAmountInMultipliesOf);
            return gson.fromJson(s, ProgressiveLoanInterestScheduleModel.class);
        } catch (Exception e) {
            log.warn("Failed to parse ProgressiveLoanInterestScheduleModel json. Falling back to default value.", e);
            return null;
        }
    }
}
