/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_product_recurring_detail")
public class DepositProductRecurringDetail extends AbstractPersistable<Long> {

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