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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.MonthDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.charge.validation.ValidChargeUpdate;

/**
 * Payload for updating an existing charge definition. It is intentionally a distinct type from
 * {@link ChargeCreateRequest} (and does NOT extend it) so the {@code CommandDispatcher} can route create vs. update by
 * payload type. All fields are optional (PATCH semantics): only the values that are provided (non-null) are applied.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidChargeUpdate
public class ChargeUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // set from the path parameter, never part of the request body
    @Hidden
    private Long id;
    private Integer chargeAppliesTo;
    @Pattern(regexp = ".*\\S.*", message = "{org.apache.fineract.portfolio.charge.name.not-blank}")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.charge.name.max}")
    private String name;
    @Pattern(regexp = ".*\\S.*", message = "{org.apache.fineract.portfolio.charge.currency-code.not-blank}")
    @Size(max = 3, message = "{org.apache.fineract.portfolio.charge.currency-code.max}")
    private String currencyCode;
    private Integer chargeTimeType;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.charge.charge-calculation-type.range}")
    @Max(value = 5, message = "{org.apache.fineract.portfolio.charge.charge-calculation-type.range}")
    private Integer chargeCalculationType;
    @Positive(message = "{org.apache.fineract.portfolio.charge.amount.positive}")
    private Double amount;
    private Boolean active;
    private Boolean penalty;
    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.charge-payment-mode.range}")
    @Max(value = 1, message = "{org.apache.fineract.portfolio.charge.charge-payment-mode.range}")
    private Integer chargePaymentMode;
    private String monthDayFormat;
    private String locale;
    private String feeOnMonthDay;
    @Positive(message = "{org.apache.fineract.portfolio.charge.fee-interval.positive}")
    private Integer feeInterval;
    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.fee-frequency.range}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.charge.fee-frequency.range}")
    private Integer feeFrequency;
    private Boolean enableFreeWithdrawalCharge;
    @Positive(message = "{org.apache.fineract.portfolio.charge.free-withdrawal-frequency.positive}")
    private Integer freeWithdrawalFrequency;
    @Positive(message = "{org.apache.fineract.portfolio.charge.restart-count-frequency.positive}")
    private Integer restartCountFrequency;
    private Integer countFrequencyType;
    @Positive(message = "{org.apache.fineract.portfolio.charge.payment-type-id.positive}")
    private Long paymentTypeId;
    private Boolean enablePaymentType;
    @Positive(message = "{org.apache.fineract.portfolio.charge.min-cap.positive}")
    private BigDecimal minCap;
    @Positive(message = "{org.apache.fineract.portfolio.charge.max-cap.positive}")
    private BigDecimal maxCap;
    @Positive(message = "{org.apache.fineract.portfolio.charge.income-account-id.positive}")
    private Long incomeAccountId;
    @Positive(message = "{org.apache.fineract.portfolio.charge.tax-group-id.positive}")
    private Long taxGroupId;

    public MonthDay feeOnMonthDayAsMonthDay() {
        return DateUtils.parseMonthDay(this.feeOnMonthDay, this.monthDayFormat, this.locale);
    }

}
