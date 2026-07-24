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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Test;

public class LoanProductWithDownPaymentConfigurationTest extends FeignLoanTestBase {

    @Test
    public void loanProductCreationWithDownPaymentConfigurationTest() {
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        Long loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                enableAutoRepaymentForDownPayment);

        final GetLoanProductsProductIdResponse product = retrieveLoanProduct(loanProductId);
        assertNotNull(product);
        assertEquals(enableDownPayment, product.getEnableDownPayment());
        assertEquals(0, product.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, product.getEnableAutoRepaymentForDownPayment());
    }

    @Test
    public void loanProductUpdateWithEnableDownPaymentConfigurationTest() {
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        GetLoanProductsProductIdResponse product = createLoanProductWithoutDownPayment(delinquencyBucketId);
        assertNotNull(product);
        assertEquals(false, product.getEnableDownPayment());

        PutLoanProductsProductIdResponse modifyResponse = updateLoanProduct(product.getId(), new PutLoanProductsProductIdRequest()
                .enableDownPayment(true).disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0)).locale("en"));
        assertNotNull(modifyResponse);

        product = retrieveLoanProduct(modifyResponse.getResourceId());
        assertNotNull(product);
        assertEquals(true, product.getEnableDownPayment());
    }

    @Test
    public void loanProductEnableDownPaymentConfigurationValidationTests() {
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        Boolean enableDownPayment = true;

        ArrayList<HashMap<String, Object>> loanProductErrorData = getLoanProductError(
                Utils.convertToJson(
                        new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "0", false).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.less.than.min",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        loanProductErrorData = getLoanProductError(Utils.convertToJson(
                new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, "101", false).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.is.greater.than.max",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        loanProductErrorData = getLoanProductError(Utils.convertToJson(new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, "12.55555555", false).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.scale.is.greater.than.6",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        loanProductErrorData = getLoanProductError(
                Utils.convertToJson(
                        new LoanProductTestBuilder().withEnableDownPayment(false, "12.5", false).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.supported.only.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        loanProductErrorData = getLoanProductError(Utils.convertToJson(
                new LoanProductTestBuilder().withEnableDownPayment(enableDownPayment, null, false).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.disbursedAmountPercentageForDownPayment.required.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        loanProductErrorData = getLoanProductError(
                Utils.convertToJson(new LoanProductTestBuilder().withEnableDownPayment(false, null, true).build(null, delinquencyBucketId)),
                CommonConstants.RESPONSE_ERROR);
        assertNotNull(loanProductErrorData);
        assertEquals("validation.msg.loanproduct.enableAutoRepaymentForDownPayment.supported.only.for.enable.down.payment.true",
                loanProductErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    private GetLoanProductsProductIdResponse createLoanProductWithoutDownPayment(final Long delinquencyBucketId) {
        Long loanProductId = createLoanProductFromJson(Utils.convertToJson(new LoanProductTestBuilder().build(null, delinquencyBucketId)));
        return retrieveLoanProduct(loanProductId);
    }

    private Long createLoanProductWithDownPaymentConfiguration(final Long delinquencyBucketId, Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, Boolean enableAutoRepaymentForDownPayment) {
        return createLoanProductFromJson(Utils.convertToJson(new LoanProductTestBuilder()
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null, delinquencyBucketId)));
    }
}
