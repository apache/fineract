/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;

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
