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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;

public class CurrencyDomain implements Comparable<CurrencyDomain> {

    public static class Builder {

        private String code;
        private String name;
        private int decimalPlaces;
        private String displaySymbol;
        private String nameCode;
        private String displayLabel;

        private Builder(final String code, final String name, final int decimalPlaces, final String displaySymbol, final String nameCode,
                final String displayLabel) {
            this.code = code;
            this.name = name;
            this.decimalPlaces = decimalPlaces;
            this.displaySymbol = displaySymbol;
            this.nameCode = nameCode;
            this.displayLabel = displayLabel;
        }

        public CurrencyDomain build() {
            return new CurrencyDomain(this.code, this.name, this.decimalPlaces, this.displaySymbol, this.nameCode, this.displayLabel);
        }
    }

    private String code;
    private String name;
    private int decimalPlaces;
    private String displaySymbol;
    private String nameCode;
    private String displayLabel;

    CurrencyDomain() {
        super();
    }

    private CurrencyDomain(final String code, final String name, final int decimalPlaces, final String displaySymbol,
            final String nameCode, final String displayLabel) {
        super();
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.displaySymbol = displaySymbol;
        this.nameCode = nameCode;
        this.displayLabel = displayLabel;
    }

    public String getCode() {
        return this.code;
    }

    public int getDecimalPlaces() {
        return this.decimalPlaces;
    }

    public String getDisplaySymbol() {
        return this.displaySymbol;
    }

    public String getNameCode() {
        return this.nameCode;
    }

    public String getDisplayLabel() {
        return this.displayLabel;
    }

    public String getName() {
        return this.name;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static CurrencyDomain fromJSON(final String jsonData) {
        return new Gson().fromJson(jsonData, CurrencyDomain.class);
    }

    public static Builder create(final String code, final String name, final int decimalPlaces, final String displaySymbol,
            final String nameCode, final String displayLabel) {
        return new Builder(code, name, decimalPlaces, displaySymbol, nameCode, displayLabel);
    }

    @Override
    public int hashCode() {
        int hash = 1;

        if (this.name != null) hash += this.name.hashCode();
        if (this.code != null) hash += this.code.hashCode();
        if (this.decimalPlaces >= 0) hash += this.decimalPlaces;
        if (this.displaySymbol != null) hash += this.displaySymbol.hashCode();
        if (this.nameCode != null) hash += this.nameCode.hashCode();
        if (this.displayLabel != null) hash += this.displayLabel.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (!(obj instanceof CurrencyDomain)) return false;

        CurrencyDomain cd = (CurrencyDomain) obj;

        if (this.name.equals(cd.name) && this.code.equals(cd.code) && this.decimalPlaces == cd.decimalPlaces
                && this.displaySymbol.equals(cd.displaySymbol) && this.nameCode.equals(cd.nameCode)
                && this.displayLabel.equals(cd.displayLabel)) return true;
        return false;
    }

    @Override
    public int compareTo(CurrencyDomain cd) {
        return this.name.compareTo(cd.getName());
    }
}
