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
package org.apache.fineract.portfolio.charge.data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.charge.validation.CreateChargeValidation;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@CreateChargeValidation
public class CreateChargeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.name.notblank}")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.name.size}")
    private String name;

    @NotBlank(message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.currencyCode.notblank}")
    @Size(max = 3, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.currencyCode.size}")
    private String currencyCode;

    @NotNull(message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.amount.notnull}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.amount.decimalmin}")
    private BigDecimal amount;

    @NotNull(message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.chargeAppliesTo.notnull}")
    private Integer chargeAppliesTo;

    @NotNull(message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.chargeCalculationType.notnull}")
    private Integer chargeCalculationType;

    private Integer feeInterval;

    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.feeFrequency.min}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.feeFrequency.max}")
    private Integer feeFrequency;

    private Boolean enableFreeWithdrawalCharge;

    private Integer freeWithdrawalFrequency;
    private Integer restartCountFrequency;

    private Integer countFrequencyType;

    private Boolean enablePaymentType;
    private Long paymentTypeId;

    private Boolean penalty;

    private Boolean active;

    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.minCap.decimalmin}")
    private BigDecimal minCap;

    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.maxCap.decimalmin}")
    private BigDecimal maxCap;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.data.create.charge.request.taxGroupId.min}")
    private Long taxGroupId;

    private Long incomeAccountId;

    private String locale;

    private Integer chargePaymentMode;

    private String feeOnMonthDay;
    private String monthDayFormat;

    private Integer chargeTimeType;

}
