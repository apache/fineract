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
package org.apache.fineract.test.factory;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.PostSavingsCharges;
import org.apache.fineract.client.models.PostSavingsProductsRequest;

public final class SavingsProductRequestFactory {

    public static final String DEFAULT_SAVINGS_PRODUCT_NAME = "CEUR";

    public static final String DEFAULT_SAVINGS_PRODUCT_SHORT_NAME = "CEU";
    public static final String DEFAULT_SAVINGS_PRODUCT_DESCRIPTION = "";
    public static final String DEFAULT_SAVINGS_PRODUCT_CURRENCY_CODE = "EUR";
    public static final Integer DEFAULT_SAVINGS_PRODUCT_DIGITS_AFTER_DECIMAL = 2;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_IN_MULTIPLES_OF = 0;
    public static final Double DEFAULT_SAVINGS_PRODUCT_NOMINAL_ANNUAL_INTEREST_RATE = 0.0;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_INTEREST_COMPOUNDING_PERIOD_TIME = 1;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_INTEREST_POSTING_PERIOD_TIME = 4;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_INTEREST_CALCULATION_TYPE = 1;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_INTEREST_CALCULATION_DAYS_IN_YEAR_TYPE = 365;
    public static final Integer DEFAULT_SAVINGS_PRODUCT_ACCOUNTING_RULE = 1;
    public static final String LOCALE_EN = "en";

    private SavingsProductRequestFactory() {}

    public static PostSavingsProductsRequest defaultSavingsProductRequest() {
        List<PostSavingsCharges> charges = new ArrayList<>();

        return new PostSavingsProductsRequest().name(DEFAULT_SAVINGS_PRODUCT_NAME)//
                .shortName(DEFAULT_SAVINGS_PRODUCT_SHORT_NAME)//
                .description(DEFAULT_SAVINGS_PRODUCT_DESCRIPTION)//
                .currencyCode(DEFAULT_SAVINGS_PRODUCT_CURRENCY_CODE)//
                .digitsAfterDecimal(DEFAULT_SAVINGS_PRODUCT_DIGITS_AFTER_DECIMAL)//
                .inMultiplesOf(DEFAULT_SAVINGS_PRODUCT_IN_MULTIPLES_OF)//
                .nominalAnnualInterestRate(DEFAULT_SAVINGS_PRODUCT_NOMINAL_ANNUAL_INTEREST_RATE)//
                .interestCompoundingPeriodType(DEFAULT_SAVINGS_PRODUCT_INTEREST_COMPOUNDING_PERIOD_TIME)//
                .interestPostingPeriodType(DEFAULT_SAVINGS_PRODUCT_INTEREST_POSTING_PERIOD_TIME)//
                .interestCalculationType(DEFAULT_SAVINGS_PRODUCT_INTEREST_CALCULATION_TYPE)//
                .interestCalculationDaysInYearType(DEFAULT_SAVINGS_PRODUCT_INTEREST_CALCULATION_DAYS_IN_YEAR_TYPE)//
                .charges(charges)//
                .accountingRule(DEFAULT_SAVINGS_PRODUCT_ACCOUNTING_RULE)//
                .locale(LOCALE_EN);//
    }
}
