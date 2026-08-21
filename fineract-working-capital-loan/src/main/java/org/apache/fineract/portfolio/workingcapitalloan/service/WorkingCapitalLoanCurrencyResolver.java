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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;

public final class WorkingCapitalLoanCurrencyResolver {

    private WorkingCapitalLoanCurrencyResolver() {}

    public static CurrencyData resolveCurrency(final WorkingCapitalLoan loan) {
        final MonetaryCurrency currency = loan.getCurrency();
        if (currency != null) {
            return currency.toData();
        }
        if (loan.getLoanProduct() != null && loan.getLoanProduct().getCurrency() != null) {
            return loan.getLoanProduct().getCurrency().toData();
        }
        throw new IllegalStateException("No currency found for loan " + loan.getId());
    }
}
