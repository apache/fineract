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
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;

/**
 * WCLoanProductRelatedDetail encapsulates all the details of a {@link WorkingCapitalLoanProduct} that are also used and
 * persisted by a {@link WorkingCapitalLoan}.
 */
@Embeddable
@Getter
@Setter
public class WorkingCapitalLoanProductRelatedDetails {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "principal_amount", scale = 6, precision = 19)
    private BigDecimal principal;

    @Column(name = "period_payment_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal periodPaymentRate;

    @Column(name = "repayment_every", nullable = false)
    private Integer repaymentEvery;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_frequency_enum", nullable = false)
    private WorkingCapitalLoanPeriodFrequencyType repaymentFrequencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "amortization_type", nullable = false)
    private WorkingCapitalAmortizationType amortizationType;

    @Column(name = "npv_day_count", nullable = false)
    private Integer npvDayCount;

    @Column(name = "discount", scale = 6, precision = 19)
    private BigDecimal discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delinquency_bucket_classification_id")
    private DelinquencyBucket delinquencyBucket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breach_id")
    private WorkingCapitalBreach breach;

    @Column(name = "delinquency_grace_days", nullable = false)
    private Integer delinquencyGraceDays = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "delinquency_start_type", nullable = false)
    private WorkingCapitalLoanDelinquencyStartType delinquencyStartType;
}
