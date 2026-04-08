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
package org.apache.fineract.portfolio.workingcapitalloanbreach.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;

@Entity
@Table(name = "m_wc_breach")
@Getter
@Setter
@NoArgsConstructor
public class WorkingCapitalBreach extends AbstractPersistableCustom<Long> {

    @Column(name = "breach_frequency")
    private Integer breachFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_frequency_type")
    private WorkingCapitalLoanPeriodFrequencyType breachFrequencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_amount_calculation_type")
    private WorkingCapitalBreachAmountCalculationType breachAmountCalculationType;

    @Column(name = "breach_amount", scale = 6, precision = 19)
    private BigDecimal breachAmount;
}
