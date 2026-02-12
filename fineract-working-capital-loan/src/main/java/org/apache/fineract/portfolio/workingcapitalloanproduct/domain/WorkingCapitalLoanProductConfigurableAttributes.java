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
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Configurable attributes for Working Capital Loan Product. Fields that can be overridden during loan creation.
 */
@Entity
@Table(name = "m_wc_loan_product_configurable_attributes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingCapitalLoanProductConfigurableAttributes extends AbstractPersistableCustom<Long> {

    @OneToOne
    @JoinColumn(name = "wc_loan_product_id", nullable = false)
    private WorkingCapitalLoanProduct wcProduct;

    @Column(name = "flat_percentage_amount_overridable")
    private Boolean flatPercentageAmount;

    @Column(name = "delinquency_bucket_classification_overridable")
    private Boolean delinquencyBucketClassification;

    @Column(name = "discount_default_overridable")
    private Boolean discountDefault;

    @Column(name = "period_payment_frequency_overridable")
    private Boolean periodPaymentFrequency;

    @Column(name = "period_payment_frequency_type_overridable")
    private Boolean periodPaymentFrequencyType;
}
