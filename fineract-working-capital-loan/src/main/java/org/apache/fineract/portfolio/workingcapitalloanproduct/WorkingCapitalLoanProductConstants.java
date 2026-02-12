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
package org.apache.fineract.portfolio.workingcapitalloanproduct;

public final class WorkingCapitalLoanProductConstants {

    private WorkingCapitalLoanProductConstants() {
        // Prevent instantiation
    }

    // JSON property names
    public static final String nameParamName = "name";
    public static final String shortNameParamName = "shortName";
    public static final String descriptionParamName = "description";
    public static final String fundIdParamName = "fundId";
    public static final String startDateParamName = "startDate";
    public static final String closeDateParamName = "closeDate";
    public static final String externalIdParamName = "externalId";

    // Currency
    public static final String currencyCodeParamName = "currencyCode";
    public static final String digitsAfterDecimalParamName = "digitsAfterDecimal";
    public static final String inMultiplesOfParamName = "inMultiplesOf";

    // Settings
    public static final String amortizationTypeParamName = "amortizationType";
    public static final String flatPercentageAmountParamName = "flatPercentageAmount";
    public static final String delinquencyBucketIdParamName = "delinquencyBucketId";
    public static final String npvDayCountParamName = "npvDayCount";
    public static final String paymentAllocationParamName = "paymentAllocation";

    // Term
    public static final String minPrincipalParamName = "minPrincipal";
    public static final String principalParamName = "principal";
    public static final String maxPrincipalParamName = "maxPrincipal";
    public static final String minPeriodPaymentRateParamName = "minPeriodPaymentRate";
    public static final String periodPaymentRateParamName = "periodPaymentRate";
    public static final String maxPeriodPaymentRateParamName = "maxPeriodPaymentRate";
    public static final String discountParamName = "discount";
    public static final String repaymentEveryParamName = "repaymentEvery";
    public static final String repaymentFrequencyTypeParamName = "repaymentFrequencyType";

    // Configurable attributes (same as LoanProduct)
    public static final String allowAttributeOverridesParamName = "allowAttributeOverrides";
    public static final String flatPercentageAmountOverridableParamName = "flatPercentageAmount";
    public static final String delinquencyBucketClassificationOverridableParamName = "delinquencyBucketClassification";
    public static final String discountDefaultOverridableParamName = "discountDefault";
    public static final String periodPaymentFrequencyOverridableParamName = "periodPaymentFrequency";
    public static final String periodPaymentFrequencyTypeOverridableParamName = "periodPaymentFrequencyType";

    // Resource name for permissions
    public static final String RESOURCE_NAME = "WORKINGCAPITALLOANPRODUCT";
}
