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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.test.data.accounttype.AccountTypeResolver;
import org.apache.fineract.test.data.accounttype.DefaultAccountType;
import org.apache.fineract.test.data.codevalue.CodeValueResolver;
import org.apache.fineract.test.data.codevalue.DefaultCodeValue;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.helper.ParallelExecutionHelper;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkingCapitalInitializerStep implements FineractGlobalInitializerStep {

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalRequestFactory workingCapitalRequestFactory;
    private final PaymentTypeResolver paymentTypeResolver;
    private final AccountTypeResolver accountTypeResolver;
    private final CodeValueResolver codeValueResolver;
    private final CodeHelper codeHelper;

    @Override
    public void initialize() throws Exception {
        PostAllowAttributeOverrides allowAttributeOverridesDisabled = new PostAllowAttributeOverrides()
                .delinquencyBucketClassification(false).discountDefault(false).periodPaymentFrequencyType(false)
                .periodPaymentFrequency(false).breach(false);

        PostAllowAttributeOverrides allowAttributeOverrides = new PostAllowAttributeOverrides().delinquencyBucketClassification(true)
                .breach(true).discountDefault(true).periodPaymentFrequencyType(true).periodPaymentFrequency(true);

        // Retrieve code IDs for charge-off and write-off reasons
        final Long chargeOffReasonCodeId = codeHelper.retrieveCodeByName("ChargeOffReasons").getId();
        final Long writeOffReasonCodeId = codeHelper.retrieveCodeByName("WriteOffReasons").getId();

        List<Runnable> items = List.of(
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISCOUNT,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_DISCOUNT.getName()).discount(new BigDecimal(50)))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DISALLOW_ATTRIBUTES_OVERRIDE.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISCOUNT_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE.getName())
                                .discount(new BigDecimal(50)).allowAttributeOverrides(allowAttributeOverridesDisabled))),
                () -> {
                    PostWorkingCapitalLoanProductsRequest req = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                            .name(DefaultWorkingCapitalLoanProduct.WCLP_FOR_UPDATE.getName());
                    PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProductIdempotent(req);
                    TestContext.GLOBAL.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST_FOR_UPDATE_WCLP, req);
                    TestContext.GLOBAL.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_FOR_UPDATE_WCLP, response);
                },
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DELINQUENCY_RESCHEDULE,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_DELINQUENCY_RESCHEDULE.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_NEAR_BREACH,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachNearBreachRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_NEAR_BREACH.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductBreachRequest().allowAttributeOverrides(allowAttributeOverridesDisabled)
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_NEAR_BREACH_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachNearBreachRequest()
                                        .allowAttributeOverrides(allowAttributeOverridesDisabled)
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_NEAR_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE
                                                .getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_ADVANCED_ACCOUNTING,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_ADVANCED_ACCOUNTING.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new org.apache.fineract.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES))))
                                .chargeOffReasonToExpenseAccountMappings(List.of(
                                        new org.apache.fineract.client.models.WorkingCapitalPostChargeOffReasonToExpenseAccountMappings()
                                                .chargeOffReasonCodeValueId(
                                                        codeValueResolver.resolve(chargeOffReasonCodeId, DefaultCodeValue.valueOf("FRAUD")))
                                                .expenseAccountId(
                                                        accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))))
                                .writeOffReasonsToExpenseMappings(List
                                        .of(new org.apache.fineract.client.models.WorkingCapitalPostWriteOffReasonToExpenseAccountMappings()
                                                .writeOffReasonCodeValueId(codeValueResolver.resolve(writeOffReasonCodeId,
                                                        DefaultCodeValue.valueOf("BAD_DEBT")))
                                                .expenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT)))))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_ACC_DEF_REV_AM,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_ACC_DEF_REV_AM.getName())
                                        .allowAttributeOverrides(allowAttributeOverrides).overpaymentLiabilityAccountId(
                                                accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY)))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_PERIOD_PAYMENT_RATE,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest().minPeriodPaymentRate(new BigDecimal(1))
                                .maxPeriodPaymentRate(new BigDecimal(95)).periodPaymentRate(new BigDecimal(10))
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_PERIOD_PAYMENT_RATE.getName()))));
        ParallelExecutionHelper.runInParallel(items);
    }

    private PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProductIdempotent(
            PostWorkingCapitalLoanProductsRequest workingCapitalProductRequest) {
        String workingCapitalProductName = workingCapitalProductRequest.getName();
        log.debug("Attempting to create working capital product: {}", workingCapitalProductName);
        try {
            List<GetWorkingCapitalLoanProductsResponse> existingWorkingCapitalProducts = fineractClient.workingCapitalLoanProducts()
                    .retrieveAllWorkingCapitalLoanProducts(Map.of());
            GetWorkingCapitalLoanProductsResponse existingWorkingCapitalProduct = existingWorkingCapitalProducts.stream()
                    .filter(p -> workingCapitalProductName.equals(p.getName())).findFirst().orElse(null);

            if (existingWorkingCapitalProduct != null) {
                log.debug("Working capital product '{}' already exists with ID: {}", workingCapitalProductName,
                        existingWorkingCapitalProduct.getId());
                PostWorkingCapitalLoanProductsResponse response = new PostWorkingCapitalLoanProductsResponse();
                response.setResourceId(existingWorkingCapitalProduct.getId());
                return response;
            }
        } catch (Exception e) {
            log.warn("Error checking if working capital product '{}' exists", workingCapitalProductName, e);
        }

        log.debug("Creating new working capital product: {}", workingCapitalProductName);
        try {
            PostWorkingCapitalLoanProductsResponse response = ok(() -> fineractClient.workingCapitalLoanProducts()
                    .createWorkingCapitalLoanProduct(workingCapitalProductRequest, Map.of()));
            log.debug("Successfully created working capital product '{}' with ID: {}", workingCapitalProductName, response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("FAILED to create working capital product '{}'", workingCapitalProductName, e);
            throw e;
        }
    }

}
