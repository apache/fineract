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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Represents currencies allowed for this MFI/organisation.
 */
@Entity
@Table(name = "m_organisation_currency")
public class OrganisationCurrency extends AbstractPersistableCustom {

    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @Column(name = "decimal_places", nullable = false)
    private Integer decimalPlaces;

    @Column(name = "currency_multiplesof")
    private Integer inMultiplesOf;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "internationalized_name_code", nullable = false, length = 50)
    private String nameCode;

    @Column(name = "display_symbol", nullable = true, length = 10)
    private String displaySymbol;

    protected OrganisationCurrency() {
        this.code = null;
        this.name = null;
        this.decimalPlaces = null;
        this.inMultiplesOf = null;
        this.nameCode = null;
        this.displaySymbol = null;
    }

    public OrganisationCurrency(final String code, final String name, final int decimalPlaces, final Integer inMultiplesOf,
            final String nameCode, final String displaySymbol) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.inMultiplesOf = inMultiplesOf;
        this.nameCode = nameCode;
        this.displaySymbol = displaySymbol;
    }

    public final String getCode() {
        return code;
    }

    public final MonetaryCurrency toMonetaryCurrency() {
        return new MonetaryCurrency(this.code, this.decimalPlaces, this.inMultiplesOf);
    }
}
