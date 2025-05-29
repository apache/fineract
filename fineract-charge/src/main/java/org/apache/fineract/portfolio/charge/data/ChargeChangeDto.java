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

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeChangeDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String currencyCode;
    private BigDecimal amount;
    private Integer chargeTimeType;
    private Integer chargeCalculationType;
    private Integer chargePaymentMode;
    private Integer feeOnMonthDay;
    private Integer feeOnMonth;
    private Integer feeInterval;
    private Boolean penalty;
    private Boolean active;
    private BigDecimal minCap;
    private BigDecimal maxCap;
    private Integer feeFrequency;
    private Boolean enableFreeWithdrawal;
    private Integer freeWithdrawalFrequency;
    private Integer restartFrequency;
    private Integer restartFrequencyEnum;
    private Boolean enablePaymentType;
    private Long paymentTypeId;
    private Long incomeAccountId;
    private Long taxGroupId;
    private String locale;
    private String monthDayFormat;
}
