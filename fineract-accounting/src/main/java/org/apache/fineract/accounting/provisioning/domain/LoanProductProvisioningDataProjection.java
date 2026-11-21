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

import static java.util.Objects.requireNonNullElse;
import static org.apache.fineract.query.QMLoan.mLoan;
import static org.apache.fineract.query.QMLoanRepaymentSchedule.mLoanRepaymentSchedule;
import static org.apache.fineract.query.QMProvisioningCriteriaDefinition.mProvisioningCriteriaDefinition;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.NumberExpression;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;

public final class LoanProductProvisioningDataProjection extends MappingProjection<LoanProductProvisioningEntryData> {

    private final NumberExpression<Long> officeId;
    private final NumberExpression<Long> overdueDays;

    public LoanProductProvisioningDataProjection(final NumberExpression<Long> officeId, final NumberExpression<Long> overdueDays) {
        super(LoanProductProvisioningEntryData.class, officeId, mLoan.loanTypeEnum, mProvisioningCriteriaDefinition.criteriaId,
                mLoan.productId, mLoan.currencyCode, overdueDays, mLoanRepaymentSchedule.duedate,
                mProvisioningCriteriaDefinition.categoryId, mProvisioningCriteriaDefinition.provisionPercentage,
                mLoan.totalOutstandingDerived, mProvisioningCriteriaDefinition.liabilityAccount,
                mProvisioningCriteriaDefinition.expenseAccount);
        this.officeId = officeId;
        this.overdueDays = overdueDays;
    }

    @Override
    protected LoanProductProvisioningEntryData map(final Tuple row) {
        return new LoanProductProvisioningEntryData().setOfficeId(requireNonNullElse(row.get(officeId), 0L))
                .setProductId(requireNonNullElse(row.get(mLoan.productId), 0L)).setCurrencyCode(row.get(mLoan.currencyCode))
                .setOverdueInDays(requireNonNullElse(row.get(overdueDays), 0L))
                .setCategoryId(requireNonNullElse(row.get(mProvisioningCriteriaDefinition.categoryId), 0L))
                .setPercentage(row.get(mProvisioningCriteriaDefinition.provisionPercentage))
                .setBalance(row.get(mLoan.totalOutstandingDerived))
                .setLiablityAccount(requireNonNullElse(row.get(mProvisioningCriteriaDefinition.liabilityAccount), 0L))
                .setExpenseAccount(requireNonNullElse(row.get(mProvisioningCriteriaDefinition.expenseAccount), 0L))
                .setCriteriaId(requireNonNullElse(row.get(mProvisioningCriteriaDefinition.criteriaId), 0L));
    }
}
