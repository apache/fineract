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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanUndoDisbursementTest extends BaseLoanIntegrationTest {

    @Test
    public void testProgressiveLoanUndoDisbursementWithChargeAtDisbursement() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        final PostChargesResponse disbCharge = chargesHelper.createCharges(new ChargeRequest().active(true).chargeAppliesTo(1)
                .chargeCalculationType(1).chargePaymentMode(0).chargeTimeType(1).currencyCode("EUR").amount(10.0d)
                .name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5)).locale("en"));

        runAt("01 January 2025", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "01 January 2025",
                    430.0, 7.0, 6, l -> l.setCharges(
                            List.of(new PostLoansRequestChargeData().chargeId(disbCharge.getResourceId()).amount(new BigDecimal("10.0")))));

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "01 January 2025");
            verifyRepaymentSchedule(loanId, installment(430.0, 0.0, 10.0, 0, null, "01 January 2025"), //
                    installment(70.63, 2.51, 73.14, false, "01 February 2025"), //
                    installment(71.04, 2.1, 73.14, false, "01 March 2025"), //
                    installment(71.46, 1.68, 73.14, false, "01 April 2025"), //
                    installment(71.87, 1.27, 73.14, false, "01 May 2025"), //
                    installment(72.29, 0.85, 73.14, false, "01 June 2025"), //
                    installment(72.71, 0.42, 73.13, false, "01 July 2025"));
            Assertions.assertDoesNotThrow(() -> loanTransactionHelper.undoDisbursalLoan(loanId, new PostLoansLoanIdRequest()));
        });
    }
}
