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
package org.apache.fineract.organisation.monetary.data;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable data object representing currency.
 */
@Getter
@EqualsAndHashCode
public class CurrencyData implements Serializable {

    private final String code;
    private final String name;
    private final int decimalPlaces;
    private final Integer inMultiplesOf;
    private final String displaySymbol;
    private final String nameCode;
    private final String displayLabel;

    public static CurrencyData blank() {
        return new CurrencyData("", "", 0, 0, "", "");
    }

    public CurrencyData(String code) {
        this.code = code;
        this.name = null;
        this.decimalPlaces = 0;
        this.inMultiplesOf = null;
        this.displaySymbol = null;
        this.nameCode = null;
        this.displayLabel = null;
    }

    public CurrencyData(final String code, final String name, final int decimalPlaces, final Integer inMultiplesOf,
            final String displaySymbol, final String nameCode) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.inMultiplesOf = inMultiplesOf;
        this.displaySymbol = displaySymbol;
        this.nameCode = nameCode;
        this.displayLabel = generateDisplayLabel();
    }

    public CurrencyData(final String code, final int decimalPlaces, final Integer inMultiplesOf) {
        this.code = code;
        this.name = null;
        this.decimalPlaces = decimalPlaces;
        this.inMultiplesOf = inMultiplesOf;
        this.displaySymbol = null;
        this.nameCode = null;
        this.displayLabel = null;
    }

    private String generateDisplayLabel() {

        final StringBuilder builder = new StringBuilder(20);
        if (this.name != null) {
            builder.append(this.name).append(' ');
        }

        if (this.displaySymbol != null && !"".equalsIgnoreCase(this.displaySymbol.trim())) {
            builder.append('(').append(this.displaySymbol).append(')');
        } else {
            builder.append('[').append(this.code).append(']');
        }

        return builder.toString();
    }
}
