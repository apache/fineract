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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.apache.fineract.portfolio.charge.validation.ValidChargeCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidChargeCreate
public class ChargeCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.portfolio.charge.charge-applies-to.not-null}")
    private Integer chargeAppliesTo;
    @NotBlank(message = "{org.apache.fineract.portfolio.charge.name.not-blank}")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.charge.name.max}")
    private String name;
    @NotBlank(message = "{org.apache.fineract.portfolio.charge.currency-code.not-blank}")
    @Size(max = 3, message = "{org.apache.fineract.portfolio.charge.currency-code.max}")
    private String currencyCode;
    private Integer chargeTimeType;
    @NotNull(message = "{org.apache.fineract.portfolio.charge.charge-calculation-type.not-null}")
    private Integer chargeCalculationType;
    @NotNull(message = "{org.apache.fineract.portfolio.charge.amount.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.charge.amount.positive}")
    private Double amount;
    private Boolean active;
    private Boolean penalty;
    private Integer chargePaymentMode;
    private String monthDayFormat;
    private String locale;
    private String feeOnMonthDay;
    @Positive(message = "{org.apache.fineract.portfolio.charge.fee-interval.positive}")
    private Integer feeInterval;
    @Min(value = 0, message = "{org.apache.fineract.portfolio.charge.fee-frequency.range}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.charge.fee-frequency.range}")
    private Integer feeFrequency;
    @Positive(message = "{org.apache.fineract.portfolio.charge.payment-type-id.positive}")
    private Long paymentTypeId;
    private Boolean enablePaymentType;
    @Positive(message = "{org.apache.fineract.portfolio.charge.min-cap.positive}")
    private BigDecimal minCap;
    @Positive(message = "{org.apache.fineract.portfolio.charge.max-cap.positive}")
    private BigDecimal maxCap;
    @Positive(message = "{org.apache.fineract.portfolio.charge.tax-group-id.positive}")
    private Long taxGroupId;

    public MonthDay feeOnMonthDayAsMonthDay() {
        return DateUtils.parseMonthDay(this.feeOnMonthDay, this.monthDayFormat, this.locale);
    }

}
