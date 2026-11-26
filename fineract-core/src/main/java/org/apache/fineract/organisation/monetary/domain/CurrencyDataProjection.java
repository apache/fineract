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
import static org.apache.fineract.query.QMOrganisationCurrency.mOrganisationCurrency;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Maps the currency columns shared by {@code m_currency} and {@code m_organisation_currency} to {@link CurrencyData}.
 */
public final class CurrencyDataProjection extends MappingProjection<CurrencyData> {

    private final StringPath code;
    private final StringPath name;
    private final NumberPath<Integer> decimalPlaces;
    private final NumberPath<Integer> currencyMultiplesof;
    private final StringPath displaySymbol;
    private final StringPath internationalizedNameCode;

    /**
     * Projection over the platform currencies ({@code m_currency}).
     */
    public CurrencyDataProjection() {
        this(mCurrency.code, mCurrency.name, mCurrency.decimalPlaces, mCurrency.currencyMultiplesof, mCurrency.displaySymbol,
                mCurrency.internationalizedNameCode);
    }

    private CurrencyDataProjection(StringPath code, StringPath name, NumberPath<Integer> decimalPlaces,
            NumberPath<Integer> currencyMultiplesof, StringPath displaySymbol, StringPath internationalizedNameCode) {
        super(CurrencyData.class, code, name, decimalPlaces, currencyMultiplesof, displaySymbol, internationalizedNameCode);
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.currencyMultiplesof = currencyMultiplesof;
        this.displaySymbol = displaySymbol;
        this.internationalizedNameCode = internationalizedNameCode;
    }

    /**
     * Projection over the currencies allowed for the organisation ({@code m_organisation_currency}).
     */
    public static CurrencyDataProjection forOrganisationCurrency() {
        return new CurrencyDataProjection(mOrganisationCurrency.code, mOrganisationCurrency.name, mOrganisationCurrency.decimalPlaces,
                mOrganisationCurrency.currencyMultiplesof, mOrganisationCurrency.displaySymbol,
                mOrganisationCurrency.internationalizedNameCode);
    }

    @Override
    protected CurrencyData map(Tuple row) {
        // decimal_places is NOT NULL, currency_multiplesof is nullable
        final int decimalPlacesValue = row.get(decimalPlaces);

        return new CurrencyData(row.get(code), row.get(name), decimalPlacesValue, row.get(currencyMultiplesof), row.get(displaySymbol),
                row.get(internationalizedNameCode));
    }
}
