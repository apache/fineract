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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollateralManagementProductUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.quality.not-empty}")
    private String quality;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.basePrice.not-empty}")
    @PositiveOrZero(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.basePrice.positive}")
    private BigDecimal basePrice;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.pctToBase.not-empty}")
    @PositiveOrZero(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.pctToBase.positive}")
    private BigDecimal pctToBase;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.unitType.not-empty}")
    private String unitType;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.name.not-empty}")
    private String name;

    @Schema(example = "USD")
    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.currency.not-empty}")
    private String currency;

    @NotEmpty(message = "{org.apache.fineract.portfolio.collateralmanagement.data.product-update-request.locale.not-empty}")
    private String locale;

    @Hidden
    private Long collateralId;
}
