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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "m_wc_loan_delinquency_action")
public class WorkingCapitalLoanDelinquencyAction extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false)
    private WorkingCapitalLoan workingCapitalLoan;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private DelinquencyAction action;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "minimum_payment", scale = 6, precision = 19)
    private BigDecimal minimumPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_payment_type")
    private DelinquencyMinimumPaymentType minimumPaymentType;

    @Column(name = "frequency")
    private Integer frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type")
    private DelinquencyFrequencyType frequencyType;

}
