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
package org.apache.fineract.organisation.monetary.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class MonetaryCurrency {

    @Column(name = "currency_code", length = 3, nullable = false)
    private String code;

    @Column(name = "currency_digits", nullable = false)
    private int digitsAfterDecimal;

    @Column(name = "currency_multiplesof")
    private Integer inMultiplesOf;

    public MonetaryCurrency copy() {
        return new MonetaryCurrency().setCode(this.code).setDigitsAfterDecimal(this.digitsAfterDecimal)
                .setInMultiplesOf(this.inMultiplesOf);
    }

    public static MonetaryCurrency fromApplicationCurrency(ApplicationCurrency applicationCurrency) {
        return new MonetaryCurrency().setCode(applicationCurrency.getCode()).setDigitsAfterDecimal(applicationCurrency.getDecimalPlaces())
                .setInMultiplesOf(applicationCurrency.getInMultiplesOf());

    }

    public static MonetaryCurrency fromCurrencyData(final CurrencyData currencyData) {
        return new MonetaryCurrency().setCode(currencyData.getCode()).setDigitsAfterDecimal(currencyData.getDecimalPlaces())
                .setInMultiplesOf(currencyData.getInMultiplesOf());
    }
}
