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
package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectedAmortizationSchedulePaymentData {

    private final int paymentNo;
    private final LocalDate paymentDate;
    private final long count;
    private final long paymentsLeft;
    private final BigDecimal expectedPaymentAmount;
    private final BigDecimal forecastPaymentAmount;
    private final BigDecimal discountFactor;
    private final BigDecimal npvValue;
    private final BigDecimal balance;
    private final BigDecimal expectedAmortizationAmount;
    private final BigDecimal netAmortizationAmount;
    private final BigDecimal actualPaymentAmount;
    private final BigDecimal actualAmortizationAmount;
    private final BigDecimal incomeModification;
    private final BigDecimal deferredBalance;

}
