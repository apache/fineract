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
package org.apache.fineract.portfolio.savings.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_deposit_product_recurring_detail")
public class DepositProductRecurringDetail extends AbstractPersistableCustom<Long> {

    @Embedded
    private DepositRecurringDetail recurringDetail;

    @OneToOne
    @JoinColumn(name = "savings_product_id", nullable = false)
    private RecurringDepositProduct product;

    protected DepositProductRecurringDetail() {
        super();
    }

    public static DepositProductRecurringDetail createNew(DepositRecurringDetail recurringDetail, SavingsProduct product) {

        return new DepositProductRecurringDetail(recurringDetail, product);
    }

    private DepositProductRecurringDetail(DepositRecurringDetail recurringDetail, SavingsProduct product) {
        this.recurringDetail = recurringDetail;
        this.product = (RecurringDepositProduct) product;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
        if (this.recurringDetail != null) {
            actualChanges.putAll(this.recurringDetail.update(command));
        }
        return actualChanges;
    }

    public DepositRecurringDetail recurringDetail() {
        return this.recurringDetail;
    }

    public void updateProductReference(final SavingsProduct product) {
        this.product = (RecurringDepositProduct) product;
    }
}