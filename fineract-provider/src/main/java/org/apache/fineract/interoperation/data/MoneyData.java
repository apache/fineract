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
package org.apache.fineract.interoperation.data;

import static org.apache.fineract.interoperation.util.InteropUtil.DEFAULT_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_AMOUNT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_CURRENCY;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_LOCALE;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;

public class MoneyData {

    public static final List<String> PARAMS = List.copyOf(Arrays.asList(PARAM_AMOUNT, PARAM_CURRENCY, PARAM_LOCALE));

    @NotNull
    private final BigDecimal amount;
    @NotNull
    private final String currency;

    MoneyData(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static MoneyData build(BigDecimal amount, String currency) {
        return new MoneyData(amount, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void normalizeAmount(@NotNull MonetaryCurrency currency) {
        if (!currency.getCode().equals(this.currency)) {
            throw new UnsupportedOperationException("Internal error: Invalid currency " + currency.getCode());
        }
        MathUtil.normalizeAmount(amount, currency);
    }

    public static MoneyData validateAndParse(DataValidatorBuilder dataValidator, JsonObject element, FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element, PARAMS);

        String locale = jsonHelper.extractStringNamed(PARAM_LOCALE, element);
        BigDecimal amount = locale == null ? jsonHelper.extractBigDecimalNamed(PARAM_AMOUNT, element, DEFAULT_LOCALE)
                : jsonHelper.extractBigDecimalWithLocaleNamed(PARAM_AMOUNT, element);
        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_AMOUNT).value(amount).notBlank()
                .zeroOrPositiveAmount();

        String currency = jsonHelper.extractStringNamed(PARAM_CURRENCY, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_CURRENCY).value(currency).notBlank().notExceedingLengthOf(3);

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new MoneyData(amount, currency);
    }
}
