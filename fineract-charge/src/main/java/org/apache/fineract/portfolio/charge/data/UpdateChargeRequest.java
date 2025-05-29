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

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.charge.validation.UpdateChargeValidation;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@UpdateChargeValidation
public class UpdateChargeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long id;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.name.size}")
    private String name;

    @Size(max = 3, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.currencyCode.size}")
    private String currencyCode;

    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.amount}")
    private BigDecimal amount;

    private Integer chargeAppliesTo;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.chargeCalculationType.min}")
    @Max(value = 5, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.chargeCalculationType.max}")
    private Integer chargeCalculationType;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.feeFrequency.min}")
    private Integer feeInterval;

    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.feeFrequency.min}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.feeFrequency.max}")
    private Integer feeFrequency;
    private Boolean enableFreeWithdrawalCharge;

    private Integer freeWithdrawalFrequency;

    private Integer restartCountFrequency;

    private Integer countFrequencyType;

    private Boolean enablePaymentType;
    private Long paymentTypeId;

    private Boolean penalty;

    private Boolean active;

    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.minCap}")
    private BigDecimal minCap;

    @DecimalMin(value = "0.0", inclusive = false, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.maxCap}")
    private BigDecimal maxCap;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.taxGroupId.min}")
    private Long taxGroupId;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.incomeAccountId}")
    private Long incomeAccountId;

    private String locale;

    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.chargePaymentMode.min}")
    @Max(value = 1, message = "{org.apache.fineract.portfolio.charge.data.update.charge.request.chargePaymentMode.max}")
    private Integer chargePaymentMode;

    private String feeOnMonthDay;
    private String monthDayFormat;
    private Integer chargeTimeType;

}
