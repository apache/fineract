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
    public static final String breachIdParamName = "breachId";
    public static final String nearBreachIdParamName = "nearBreachId";

    // Configurable attributes (same as LoanProduct)
    public static final String allowAttributeOverridesParamName = "allowAttributeOverrides";
    public static final String delinquencyBucketClassificationOverridableParamName = "delinquencyBucketClassification";
    public static final String breachOverridableParamName = "breach";
    public static final String discountDefaultOverridableParamName = "discountDefault";
    public static final String periodPaymentFrequencyOverridableParamName = "periodPaymentFrequency";
    public static final String periodPaymentFrequencyTypeOverridableParamName = "periodPaymentFrequencyType";

    // Delinquency grace
    public static final String delinquencyGraceDaysParamName = "delinquencyGraceDays";
    public static final String delinquencyStartTypeParamName = "delinquencyStartType";

    // Accounting
    public static final String accountingRuleParamName = "accountingRule";
    public static final String fundSourceAccountIdParamName = "fundSourceAccountId";
    public static final String loanPortfolioAccountIdParamName = "loanPortfolioAccountId";
    public static final String transfersInSuspenseAccountIdParamName = "transfersInSuspenseAccountId";
    public static final String deferredIncomeLiabilityAccountIdParamName = "deferredIncomeLiabilityAccountId";
    public static final String incomeFromDiscountFeeAccountIdParamName = "incomeFromDiscountFeeAccountId";
    public static final String incomeFromFeeAccountIdParamName = "incomeFromFeeAccountId";
    public static final String incomeFromPenaltyAccountIdParamName = "incomeFromPenaltyAccountId";
    public static final String incomeFromRecoveryAccountIdParamName = "incomeFromRecoveryAccountId";
    public static final String writeOffAccountIdParamName = "writeOffAccountId";
    public static final String overpaymentLiabilityAccountIdParamName = "overpaymentLiabilityAccountId";
    public static final String incomeFromChargeOffInterestAccountIdParamName = "incomeFromChargeOffInterestAccountId";
    public static final String incomeFromChargeOffFeesAccountIdParamName = "incomeFromChargeOffFeesAccountId";
    public static final String incomeFromChargeOffPenaltyAccountIdParamName = "incomeFromChargeOffPenaltyAccountId";
    public static final String incomeFromGoodwillCreditInterestAccountIdParamName = "incomeFromGoodwillCreditInterestAccountId";
    public static final String incomeFromGoodwillCreditFeesAccountIdParamName = "incomeFromGoodwillCreditFeesAccountId";
    public static final String incomeFromGoodwillCreditPenaltyAccountIdParamName = "incomeFromGoodwillCreditPenaltyAccountId";
    public static final String goodwillCreditAccountIdParamName = "goodwillCreditAccountId";
    public static final String chargeOffExpenseAccountIdParamName = "chargeOffExpenseAccountId";
    public static final String chargeOffFraudExpenseAccountIdParamName = "chargeOffFraudExpenseAccountId";

    // Near Breach
    public static final String nearBreachNameParamName = "nearBreachName";
    public static final String nearBreachFrequencyParamName = "nearBreachFrequency";
    public static final String nearBreachFrequencyTypeParamName = "nearBreachFrequencyType";
    public static final String nearBreachThresholdParamName = "nearBreachThreshold";

    // Resource name for permissions
    public static final String WCLP_RESOURCE_NAME = "WORKINGCAPITALLOANPRODUCT";
}
