/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;

public interface LoanProductReadPlatformService {

    Collection<LoanProductData> retrieveAllLoanProducts();

    Collection<LoanProductData> retrieveAllLoanProductsForLookup();

    Collection<LoanProductData> retrieveAllLoanProductsForLookup(boolean activeOnly);

    LoanProductData retrieveLoanProduct(Long productId);

    LoanProductData retrieveNewLoanProductDetails();

    Collection<LoanProductData> retrieveAllLoanProductsForCurrency(String currencyCode);

    Collection<LoanProductData> retrieveAvailableLoanProductsForMix();

    Collection<LoanProductData> retrieveRestrictedProductsForMix(Long productId);

    Collection<LoanProductData> retrieveAllowedProductsForMix(Long productId);

    Collection<LoanProductBorrowerCycleVariationData> retrieveLoanProductBorrowerCycleVariations(Long loanProductId);
}