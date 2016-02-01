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
package org.apache.fineract.portfolio.search.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

public class AdHocSearchQueryData {

    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final String loanProductName;
    @SuppressWarnings("unused")
    private final Integer count;
    @SuppressWarnings("unused")
    private final BigDecimal loanOutStanding;
    @SuppressWarnings("unused")
    private final Double percentage;

    @SuppressWarnings("unused")
    private final Collection<LoanProductData> loanProducts;
    @SuppressWarnings("unused")
    private final Collection<OfficeData> offices;

    public static AdHocSearchQueryData template(final Collection<LoanProductData> loanProducts, final Collection<OfficeData> offices) {
        final String officeName = null;
        final String loanProductName = null;
        final Integer count = null;
        final BigDecimal loanOutStanding = null;
        final Double percentage = null;
        return new AdHocSearchQueryData(officeName, loanProductName, count, loanOutStanding, percentage, loanProducts, offices);
    }

    public static AdHocSearchQueryData matchedResult(final String officeName, final String loanProductName, final Integer count,
            final BigDecimal loanOutStanding, final Double percentage) {

        final Collection<LoanProductData> loanProducts = null;
        final Collection<OfficeData> offices = null;
        return new AdHocSearchQueryData(officeName, loanProductName, count, loanOutStanding, percentage, loanProducts, offices);
    }

    private AdHocSearchQueryData(final String officeName, final String loanProductName, final Integer count,
            final BigDecimal loanOutStanding, final Double percentage, final Collection<LoanProductData> loanProducts,
            final Collection<OfficeData> offices) {

        this.officeName = officeName;
        this.loanProductName = loanProductName;
        this.count = count;
        this.loanOutStanding = loanOutStanding;
        this.percentage = percentage;

        this.loanProducts = loanProducts;
        this.offices = offices;
    }
}
