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

import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanProductWithRepaymentDueEventConfigurationTest extends FeignLoanTestBase {

    private static final Integer DUE_DAYS_FOR_REPAYMENT_EVENT = 1;
    private static final Integer OVER_DUE_DAYS_FOR_REPAYMENT_EVENT = 2;

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanProductCreationWithDueDaysConfigurationForRepaymentEventTest() {
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        Long loanProductId = createLoanProductWithDueDaysForRepaymentEvent(delinquencyBucketId, DUE_DAYS_FOR_REPAYMENT_EVENT,
                OVER_DUE_DAYS_FOR_REPAYMENT_EVENT);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertNotNull(getLoanProductsProductResponse.getDueDaysForRepaymentEvent());
        assertNotNull(getLoanProductsProductResponse.getOverDueDaysForRepaymentEvent());
        assertEquals(DUE_DAYS_FOR_REPAYMENT_EVENT, getLoanProductsProductResponse.getDueDaysForRepaymentEvent());
        assertEquals(OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, getLoanProductsProductResponse.getOverDueDaysForRepaymentEvent());
    }

    @Test
    public void loanProductUpdateWithDueDaysConfigurationForRepaymentEventTest() {
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createDefaultLoanProduct(delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        PutLoanProductsProductIdResponse loanProductModifyResponse = updateDueDaysForRepaymentEvent(getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);
    }

    private PutLoanProductsProductIdResponse updateDueDaysForRepaymentEvent(Long id) {
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .dueDaysForRepaymentEvent(DUE_DAYS_FOR_REPAYMENT_EVENT).overDueDaysForRepaymentEvent(OVER_DUE_DAYS_FOR_REPAYMENT_EVENT)
                .locale("en");
        return updateLoanProduct(id, requestModifyLoan);
    }

    private GetLoanProductsProductIdResponse createDefaultLoanProduct(final Long delinquencyBucketId) {
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
        return retrieveLoanProduct(loanProductId);
    }

    private Long createLoanProductWithDueDaysForRepaymentEvent(final Long delinquencyBucketId, Integer dueDaysForRepaymentEvent,
            Integer overDueDaysForRepaymentEvent) {
        return createLoanProduct(new LoanProductTestBuilder().withDueDaysForRepaymentEvent(dueDaysForRepaymentEvent)
                .withOverDueDaysForRepaymentEvent(overDueDaysForRepaymentEvent).buildRequest(null, delinquencyBucketId));
    }

}
