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
package org.apache.fineract.portfolio.loanproduct.service;

import java.util.Collection;

import org.apache.fineract.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

public interface LoanProductReadPlatformService {

    Collection<LoanProductData> retrieveAllLoanProducts();

    Collection<LoanProductData> retrieveAllLoanProductsForLookup(String inClass);
    
    Collection<LoanProductData> retrieveAllLoanProductsForLookup();

    Collection<LoanProductData> retrieveAllLoanProductsForLookup(boolean activeOnly);

    LoanProductData retrieveLoanProduct(Long productId);

    LoanProductData retrieveNewLoanProductDetails();

    Collection<LoanProductData> retrieveAllLoanProductsForCurrency(String currencyCode);

    Collection<LoanProductData> retrieveAvailableLoanProductsForMix();

    Collection<LoanProductData> retrieveRestrictedProductsForMix(Long productId);

    Collection<LoanProductData> retrieveAllowedProductsForMix(Long productId);

    Collection<LoanProductBorrowerCycleVariationData> retrieveLoanProductBorrowerCycleVariations(Long loanProductId);

    LoanProductData retrieveLoanProductFloatingDetails(Long loanProductId);
}