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
package org.apache.fineract.portfolio.collateralmanagement.data;

import java.math.BigDecimal;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;

public final class CollateralManagementData {

    private String quality;

    private BigDecimal basePrice;

    private String unitType;

    private BigDecimal pctToBase;

    private String currency;

    private String name;

    private Long id;

    private CollateralManagementData(final String quality, final BigDecimal basePrice, final String unitType, final BigDecimal pctToBase,
            final String currency, final String name, final Long id) {
        this.basePrice = basePrice;
        this.pctToBase = pctToBase;
        this.quality = quality;
        this.unitType = unitType;
        this.currency = currency;
        this.name = name;
        this.id = id;
    }

    public static CollateralManagementData createNew(final CollateralManagementDomain collateralManagementDomain) {
        return new CollateralManagementData(collateralManagementDomain.getQuality(), collateralManagementDomain.getBasePrice(),
                collateralManagementDomain.getUnitType(), collateralManagementDomain.getPctToBase(),
                collateralManagementDomain.getCurrency().getCode(), collateralManagementDomain.getName(),
                collateralManagementDomain.getId());
    }

}
