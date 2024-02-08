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
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;

public final class LoanCollateralTemplateData {

    private Long collateralId;

    private BigDecimal basePrice;

    private BigDecimal pctToBase;

    private BigDecimal quantity;

    private String name;

    private LoanCollateralTemplateData(final Long collateralId, final BigDecimal basePrice, final BigDecimal pctToBase,
            final BigDecimal quantity, final String name) {
        this.quantity = quantity;
        this.collateralId = collateralId;
        this.basePrice = basePrice;
        this.pctToBase = pctToBase;
        this.name = name;
    }

    public static LoanCollateralTemplateData instanceOf(final ClientCollateralManagement clientCollateralManagement) {
        return new LoanCollateralTemplateData(clientCollateralManagement.getId(),
                clientCollateralManagement.getCollaterals().getBasePrice(), clientCollateralManagement.getCollaterals().getPctToBase(),
                clientCollateralManagement.getQuantity(), clientCollateralManagement.getCollaterals().getName());
    }

}
