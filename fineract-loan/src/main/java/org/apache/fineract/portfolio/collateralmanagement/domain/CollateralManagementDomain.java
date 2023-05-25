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
package org.apache.fineract.portfolio.collateralmanagement.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.portfolio.collateralmanagement.api.CollateralManagementJsonInputParams;

@Entity
@Table(name = "m_collateral_management")
public class CollateralManagementDomain extends AbstractPersistableCustom {

    @Column(name = "name", length = 20, columnDefinition = " ")
    private String name;

    @Column(name = "quality", nullable = false, length = 40)
    private String quality;

    @Column(name = "base_price", nullable = false, scale = 5, precision = 20)
    private BigDecimal basePrice;

    @Column(name = "unit_type", nullable = false, length = 10)
    private String unitType;

    @Column(name = "pct_to_base", nullable = false, scale = 5, precision = 20)
    private BigDecimal pctToBase;

    @ManyToOne
    @JoinColumn(name = "currency")
    private ApplicationCurrency currency;

    @OneToMany(mappedBy = "collateral", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ClientCollateralManagement> clientCollateralManagements = new HashSet<>();

    public CollateralManagementDomain() {

    }

    private CollateralManagementDomain(final String quality, final BigDecimal basePrice, final String unitType, final BigDecimal pctToBase,
            final ApplicationCurrency currency, final String name) {
        this.basePrice = basePrice;
        this.currency = currency;
        this.pctToBase = pctToBase;
        this.unitType = unitType;
        this.quality = quality;
        this.name = name;
    }

    public static CollateralManagementDomain createNew(JsonCommand jsonCommand, final ApplicationCurrency applicationCurrency) {
        String quality = jsonCommand.stringValueOfParameterNamed("quality");
        BigDecimal basePrice = jsonCommand.bigDecimalValueOfParameterNamed("basePrice");
        BigDecimal pctToBase = jsonCommand.bigDecimalValueOfParameterNamedDefaultToNullIfZero("pctToBase");
        String unitType = jsonCommand.stringValueOfParameterNamed("unitType");
        String name = jsonCommand.stringValueOfParameterNamed("name");
        return new CollateralManagementDomain(quality, basePrice, unitType, pctToBase, applicationCurrency, name);
    }

    public Map<String, Object> update(final JsonCommand command, final ApplicationCurrency applicationCurrency) {
        final Map<String, Object> changes = new LinkedHashMap<>(5);
        final String nameParamName = CollateralManagementJsonInputParams.NAME.getValue();

        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
            changes.put(nameParamName, this.name);
        }

        final String qualityParamName = CollateralManagementJsonInputParams.QUALITY.getValue();
        if (command.isChangeInStringParameterNamed(qualityParamName, this.quality)) {
            final String newValue = command.stringValueOfParameterNamed(qualityParamName);
            this.quality = newValue;
            changes.put(qualityParamName, this.quality);
        }

        final String unitTypeParamName = CollateralManagementJsonInputParams.UNIT_TYPE.getValue();
        if (command.isChangeInStringParameterNamed(unitTypeParamName, this.unitType)) {
            final String newValue = command.stringValueOfParameterNamed(unitTypeParamName);
            this.unitType = newValue;
            changes.put(unitTypeParamName, this.unitType);
        }

        this.currency = applicationCurrency;

        final String basePriceParamName = CollateralManagementJsonInputParams.BASE_PRICE.getValue();
        if (command.isChangeInBigDecimalParameterNamed(basePriceParamName, this.basePrice)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(basePriceParamName);
            this.basePrice = newValue;
            changes.put(basePriceParamName, this.basePrice);
        }

        final String pctToBaseParamName = CollateralManagementJsonInputParams.PCT_TO_BASE.getValue();
        if (command.isChangeInBigDecimalParameterNamed(pctToBaseParamName, this.pctToBase)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(pctToBaseParamName);
            this.pctToBase = newValue;
            changes.put(pctToBaseParamName, this.pctToBase);
        }

        return changes;
    }

    public String getQuality() {
        return this.quality;
    }

    public String getUnitType() {
        return this.unitType;
    }

    public ApplicationCurrency getCurrency() {
        return this.currency;
    }

    public BigDecimal getBasePrice() {
        return this.basePrice;
    }

    public BigDecimal getPctToBase() {
        return this.pctToBase;
    }

    public String getName() {
        return this.name;
    }

    public Set<ClientCollateralManagement> getClientCollateralManagements() {
        return this.clientCollateralManagements;
    }

}
