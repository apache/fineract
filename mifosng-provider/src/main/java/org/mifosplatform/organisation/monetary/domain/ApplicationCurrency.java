/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.domain.OrganisationCurrency;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_currency")
public class ApplicationCurrency extends AbstractPersistable<Long> {

    @Column(name = "code", nullable = false, length = 3)
    private final String code;

    @Column(name = "decimal_places", nullable = false)
    private final Integer decimalPlaces;

    @Column(name = "name", nullable = false, length = 50)
    private final String name;

    @Column(name = "internationalized_name_code", nullable = false, length = 50)
    private final String nameCode;

    @Column(name = "display_symbol", nullable = true, length = 10)
    private final String displaySymbol;

    protected ApplicationCurrency() {
        this.code = null;
        this.name = null;
        this.decimalPlaces = null;
        this.nameCode = null;
        this.displaySymbol = null;
    }

    public static ApplicationCurrency from(final ApplicationCurrency currency, final int decimalPlaces) {
        return new ApplicationCurrency(currency.code, currency.name, decimalPlaces, currency.nameCode, currency.displaySymbol);
    }

    private ApplicationCurrency(final String code, final String name, final int decimalPlaces, final String nameCode,
            final String displaySymbol) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.nameCode = nameCode;
        this.displaySymbol = displaySymbol;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public Integer getDecimalPlaces() {
        return this.decimalPlaces;
    }

    public String getNameCode() {
        return nameCode;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }

    public CurrencyData toData() {
        return new CurrencyData(this.code, this.name, this.decimalPlaces, this.displaySymbol, this.nameCode);
    }

    public OrganisationCurrency toOrganisationCurrency() {
        return new OrganisationCurrency(this.code, this.name, this.decimalPlaces, this.nameCode, this.displaySymbol);
    }
}