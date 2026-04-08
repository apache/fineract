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

package org.apache.fineract.portfolio.delinquency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "m_delinquency_payment_rule")
public class DelinquencyMinimumPaymentPeriodAndRule extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Serial
    private static final long serialVersionUID = -9204385885041120403L;

    @OneToOne
    @JoinColumn(name = "bucket_id", nullable = false, unique = true)
    private DelinquencyBucket bucket;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private DelinquencyFrequencyType frequencyType;

    @Column(name = "minimum_payment", scale = 6, precision = 19, nullable = false)
    private BigDecimal minimumPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_payment_type", nullable = false)
    private DelinquencyMinimumPaymentType minimumPaymentType;
}
