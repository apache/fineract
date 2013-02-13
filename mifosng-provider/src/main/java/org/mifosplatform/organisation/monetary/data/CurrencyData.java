/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.data;

/**
 * Immutable data object representing currency.
 */
public class CurrencyData {

    private final String code;
    private final String name;
    private final int decimalPlaces;
    private final String displaySymbol;
    @SuppressWarnings("unused")
    private final String nameCode;
    @SuppressWarnings("unused")
    private final String displayLabel;

    public static CurrencyData blank() {
        return new CurrencyData("", "", 0, "", "");
    }

    public CurrencyData(final String code, final String name, final int decimalPlaces, final String displaySymbol, final String nameCode) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.displaySymbol = displaySymbol;
        this.nameCode = nameCode;
        this.displayLabel = generateDisplayLabel();
    }

    public String code() {
        return this.code;
    }

    public int decimalPlaces() {
        return this.decimalPlaces;
    }
    
    @Override
    public boolean equals(Object obj) {
        CurrencyData currencyData = (CurrencyData) obj;
        return currencyData.code.equals(this.code);
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    private String generateDisplayLabel() {

        StringBuilder builder = new StringBuilder(this.name).append(' ');

        if (this.displaySymbol != null && !"".equalsIgnoreCase(displaySymbol.trim())) {
            builder.append('(').append(this.displaySymbol).append(')');
        } else {
            builder.append('[').append(this.code).append(']');
        }

        return builder.toString();
    }
}