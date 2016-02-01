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
package org.apache.fineract.portfolio.loanproduct.productmix.data;

import java.util.Collection;

import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

public class ProductMixData {

    private final Long productId;
    private final String productName;
    private final Collection<LoanProductData> restrictedProducts;
    private final Collection<LoanProductData> allowedProducts;
    @SuppressWarnings("unused")
    private final Collection<LoanProductData> productOptions;

    public ProductMixData(final Long productId, final String productName, final Collection<LoanProductData> restrictedProducts,
            final Collection<LoanProductData> allowedProducts, final Collection<LoanProductData> productOptions) {
        this.productId = productId;
        this.productName = productName;
        this.restrictedProducts = restrictedProducts;
        this.allowedProducts = allowedProducts;
        this.productOptions = productOptions;
    }

    public static ProductMixData template(final Collection<LoanProductData> productOptions) {
        return new ProductMixData(null, null, null, null, productOptions);
    }

    public static ProductMixData withTemplateOptions(final ProductMixData productMixData, final Collection<LoanProductData> productOptions) {
        return new ProductMixData(productMixData.productId, productMixData.productName, productMixData.restrictedProducts,
                productMixData.allowedProducts, productOptions);
    }

    public static ProductMixData withDetails(final Long productId, final String productName,
            final Collection<LoanProductData> restrictedProducts, final Collection<LoanProductData> allowedProducts) {
        return new ProductMixData(productId, productName, restrictedProducts, allowedProducts, null);
    }

    public static ProductMixData withRestrictedOptions(final Collection<LoanProductData> restrictedProducts,
            final Collection<LoanProductData> allowedProducts) {
        return new ProductMixData(null, null, restrictedProducts, allowedProducts, null);
    }

}
