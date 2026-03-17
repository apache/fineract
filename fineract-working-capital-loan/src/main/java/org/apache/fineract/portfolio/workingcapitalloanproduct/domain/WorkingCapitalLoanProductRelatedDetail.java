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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WorkingCapitalLoanProductRelatedDetail encapsulates the core product parameters of a
 * {@link WorkingCapitalLoanProduct} that define repayment and amortization behaviour (aligned with
 * {@link org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail} by functionality).
 */
@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkingCapitalLoanProductRelatedDetail {

    @Enumerated(EnumType.STRING)
    @Column(name = "amortization_type", nullable = false)
    private WorkingCapitalAmortizationType amortizationType;

    @Column(name = "flat_percentage_amount", scale = 6, precision = 19)
    private BigDecimal flatPercentageAmount;

    @Column(name = "npv_day_count", nullable = false)
    private Integer npvDayCount;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal principal;

    @Column(name = "period_payment_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal periodPaymentRate;

    @Column(name = "repayment_every", nullable = false)
    private Integer repaymentEvery;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_frequency_enum", nullable = false)
    private WorkingCapitalLoanPeriodFrequencyType repaymentFrequencyType;

    @Column(name = "discount", scale = 6, precision = 19)
    private BigDecimal discount;
}
