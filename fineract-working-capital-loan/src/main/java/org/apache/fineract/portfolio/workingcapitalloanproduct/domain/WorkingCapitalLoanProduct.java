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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.fund.domain.Fund;

/**
 * Working Capital Loan Product entity. This is a separate entity from the standard LoanProduct to provide flexibility
 * for configuring Working Capital loan products without impacting existing loan products.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "m_wc_loan_product", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_wc_loan_product_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "unq_wc_loan_product_external_id"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "unq_wc_loan_product_short_name") })
public class WorkingCapitalLoanProduct extends AbstractPersistableCustom<Long> {

    // Details category
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "external_id", length = 100)
    private ExternalId externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delinquency_bucket_classification_id")
    private DelinquencyBucket delinquencyBucket;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "description")
    private String description;

    // Currency (MonetaryCurrency is @Embeddable)
    @Embedded
    private MonetaryCurrency currency;

    // Core product parameters
    @Embedded
    private WorkingCapitalLoanProductRelatedDetail relatedDetail;

    // Min/max constraints
    @Embedded
    private WorkingCapitalLoanProductMinMaxConstraints minMaxConstraints;

    // Payment allocation rules
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wcProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkingCapitalLoanProductPaymentAllocationRule> paymentAllocationRules = new ArrayList<>();

    // Configurable attributes
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "wcProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private WorkingCapitalLoanProductConfigurableAttributes configurableAttributes;

    public WorkingCapitalLoanProduct(String name, String shortName, ExternalId externalId, Fund fund, DelinquencyBucket delinquencyBucket,
            LocalDate startDate, LocalDate closeDate, String description, MonetaryCurrency currency,
            WorkingCapitalLoanProductRelatedDetail relatedDetail, WorkingCapitalLoanProductMinMaxConstraints minMaxConstraints,
            List<WorkingCapitalLoanProductPaymentAllocationRule> paymentAllocationRules,
            WorkingCapitalLoanProductConfigurableAttributes configurableAttributes) {
        this.name = name;
        this.shortName = shortName;
        this.externalId = externalId;
        this.fund = fund;
        this.delinquencyBucket = delinquencyBucket;
        this.startDate = startDate;
        this.closeDate = closeDate;
        this.description = description;
        this.currency = currency;
        this.relatedDetail = relatedDetail;
        this.minMaxConstraints = minMaxConstraints;
        this.paymentAllocationRules = paymentAllocationRules;
        if (this.paymentAllocationRules != null) {
            for (WorkingCapitalLoanProductPaymentAllocationRule rule : this.paymentAllocationRules) {
                rule.setWcProduct(this);
            }
        }
        this.configurableAttributes = configurableAttributes;
        if (this.configurableAttributes != null) {
            this.configurableAttributes.setWcProduct(this);
        }
    }

    public void updatePaymentAllocationRules(final List<WorkingCapitalLoanProductPaymentAllocationRule> newRules) {
        if (newRules != null) {
            this.paymentAllocationRules.clear();
            this.paymentAllocationRules.addAll(newRules);
        }
    }

}
