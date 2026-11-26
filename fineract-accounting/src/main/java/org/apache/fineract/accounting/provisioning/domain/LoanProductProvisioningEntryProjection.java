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
package org.apache.fineract.accounting.provisioning.domain;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.util.Objects;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.query.QAccGlAccount;
import org.apache.fineract.query.QMLoanproductProvisioningEntry;
import org.apache.fineract.query.QMOffice;
import org.apache.fineract.query.QMProductLoan;
import org.apache.fineract.query.QMProvisionCategory;

public final class LoanProductProvisioningEntryProjection extends MappingProjection<LoanProductProvisioningEntryData> {

    private final QMLoanproductProvisioningEntry entry;
    private final QMOffice office;
    private final QMProductLoan product;
    private final QMProvisionCategory category;
    private final QAccGlAccount liability;
    private final QAccGlAccount expense;
    private final boolean defaultNullNumbersToZero;

    public LoanProductProvisioningEntryProjection(final QMLoanproductProvisioningEntry entry, final QMOffice office,
            final QMProductLoan product, final QMProvisionCategory category, final QAccGlAccount liability, final QAccGlAccount expense) {
        this(entry, office, product, category, liability, expense, false);
    }

    public static LoanProductProvisioningEntryProjection withJdbcNullDefaults(final QMLoanproductProvisioningEntry entry,
            final QMOffice office, final QMProductLoan product, final QMProvisionCategory category, final QAccGlAccount liability,
            final QAccGlAccount expense) {
        return new LoanProductProvisioningEntryProjection(entry, office, product, category, liability, expense, true);
    }

    private LoanProductProvisioningEntryProjection(final QMLoanproductProvisioningEntry entry, final QMOffice office,
            final QMProductLoan product, final QMProvisionCategory category, final QAccGlAccount liability, final QAccGlAccount expense,
            final boolean defaultNullNumbersToZero) {
        super(LoanProductProvisioningEntryData.class, entry.historyId, entry.officeId, office.name, entry.currencyCode, entry.productId,
                product.name, entry.categoryId, category.categoryName, entry.overdueInDays, entry.reseveAmount, liability.id,
                liability.glCode, liability.name, expense.id, expense.glCode, expense.name, entry.criteriaId);
        this.entry = entry;
        this.office = office;
        this.product = product;
        this.category = category;
        this.liability = liability;
        this.expense = expense;
        this.defaultNullNumbersToZero = defaultNullNumbersToZero;
    }

    @Override
    protected LoanProductProvisioningEntryData map(final Tuple row) {
        return new LoanProductProvisioningEntryData().setHistoryId(row.get(entry.historyId)).setOfficeId(row.get(entry.officeId))
                .setOfficeName(row.get(office.name)).setCurrencyCode(row.get(entry.currencyCode)).setProductId(row.get(entry.productId))
                .setProductName(row.get(product.name)).setCategoryId(row.get(entry.categoryId))
                .setCategoryName(row.get(category.categoryName)).setOverdueInDays(withNumberDefault(row.get(entry.overdueInDays)))
                .setAmountreserved(row.get(entry.reseveAmount)).setLiablityAccount(withNumberDefault(row.get(liability.id)))
                .setLiabilityAccountCode(row.get(liability.glCode)).setLiabilityAccountName(row.get(liability.name))
                .setExpenseAccount(withNumberDefault(row.get(expense.id))).setExpenseAccountCode(row.get(expense.glCode))
                .setExpenseAccountName(row.get(expense.name)).setCriteriaId(row.get(entry.criteriaId));
    }

    private Long withNumberDefault(final Long value) {
        return defaultNullNumbersToZero ? Objects.requireNonNullElse(value, 0L) : value;
    }
}
