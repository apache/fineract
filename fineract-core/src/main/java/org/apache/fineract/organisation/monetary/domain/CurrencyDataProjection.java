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

import static org.apache.fineract.query.QMCurrency.mCurrency;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

public final class CurrencyDataProjection extends MappingProjection<CurrencyData> {

    public CurrencyDataProjection() {
        super(CurrencyData.class, mCurrency.code, mCurrency.name, mCurrency.decimalPlaces, mCurrency.currencyMultiplesof,
                mCurrency.displaySymbol, mCurrency.internationalizedNameCode);
    }

    @Override
    protected CurrencyData map(Tuple row) {
        // decimal_places is NOT NULL, currency_multiplesof is nullable
        final int decimalPlaces = row.get(mCurrency.decimalPlaces);

        return new CurrencyData(row.get(mCurrency.code), row.get(mCurrency.name), decimalPlaces, row.get(mCurrency.currencyMultiplesof),
                row.get(mCurrency.displaySymbol), row.get(mCurrency.internationalizedNameCode));
    }
}
