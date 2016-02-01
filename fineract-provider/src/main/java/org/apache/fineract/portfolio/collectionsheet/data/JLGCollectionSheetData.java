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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.joda.time.LocalDate;

/**
 * Immutable data object for join liability group's collection sheet.
 */
public class JLGCollectionSheetData {

    private final LocalDate dueDate;
    private final Collection<LoanProductData> loanProducts;
    @SuppressWarnings("unused")
    private final Collection<SavingsProductData> savingsProducts;
    private final Collection<JLGGroupData> groups;
    private final List<EnumOptionData> attendanceTypeOptions;
    private final Collection<PaymentTypeData> paymentTypeOptions;

    public static JLGCollectionSheetData instance(final LocalDate date, final Collection<LoanProductData> loanProducts,
            final Collection<JLGGroupData> groups, final List<EnumOptionData> attendanceTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new JLGCollectionSheetData(date, loanProducts, null, groups, attendanceTypeOptions, paymentTypeOptions);
    }

    public static JLGCollectionSheetData withSavingsProducts(final JLGCollectionSheetData data,
            final Collection<SavingsProductData> savingsProducts) {

        return new JLGCollectionSheetData(data.dueDate, data.loanProducts, savingsProducts, data.groups, data.attendanceTypeOptions,
                data.paymentTypeOptions);
    }

    private JLGCollectionSheetData(LocalDate dueDate, Collection<LoanProductData> loanProducts,
            Collection<SavingsProductData> savingsProducts, Collection<JLGGroupData> groups, List<EnumOptionData> attendanceTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        this.dueDate = dueDate;
        this.loanProducts = loanProducts;
        this.savingsProducts = savingsProducts;
        this.groups = groups;
        this.attendanceTypeOptions = attendanceTypeOptions;
        this.paymentTypeOptions = paymentTypeOptions;
    }

    public LocalDate getDate() {
        return this.dueDate;
    }

    public Collection<JLGGroupData> getGroups() {
        return this.groups;
    }

    public Collection<LoanProductData> getLoanProducts() {
        return this.loanProducts;
    }

}