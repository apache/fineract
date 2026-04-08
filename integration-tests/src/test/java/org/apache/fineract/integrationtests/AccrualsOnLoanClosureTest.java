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

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class AccrualsOnLoanClosureTest extends BaseLoanIntegrationTest {

    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    private static final String startDate = "22 April 2025";
    private static final String disbursementDate = "22 April 2024";
    private static final String repaymentDate = "25 April 2024";
    private static final Double disbursementAmount = 800.0;
    private static final Double repaymentAmount = 820.0;
    private static final Double chargeAmount = 20.0;

    private static Long loanId;
    private static PostChargesResponse penaltyResponse;
    private static final String penaltyCharge1AddedDate = "24 April 2024";

    @Test
    public void testAccrualCreatedOnLoanClosureWithSubmittedDate() {
        PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        PostLoanProductsResponse loanProduct = loanProductHelper
                .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

        loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), disbursementDate, disbursementAmount);
        Assertions.assertNotNull(loanId);
        disburseLoan(loanId, BigDecimal.valueOf(disbursementAmount), disbursementDate);

        penaltyResponse = chargesHelper.createCharges(
                new ChargeRequest().active(true).chargeTimeType(2).chargeAppliesTo(1).chargeCalculationType(1).penalty(true).amount(20.0)
                        .currencyCode("USD").locale("en").chargePaymentMode(0).name(Utils.randomStringGenerator("PENALTY_", 6)));
        runAt(startDate, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));

            loanTransactionHelper.addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().dateFormat("dd MMMM yyyy").locale("en")
                    .chargeId(penaltyResponse.getResourceId()).amount(chargeAmount).dueDate(penaltyCharge1AddedDate));

            loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                    .transactionDate(repaymentDate).locale("en").transactionAmount(repaymentAmount));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(800.0, null, "22 April 2024"), //
                    installment(800.0, 0.0, 0.0, 20.0, 0.0, true, "22 May 2024"));

            verifyTransactions(loanId, //
                    transaction(800.0, "Disbursement", "22 April 2024", 800.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(820.0, "Repayment", "25 April 2024", 0.0, 800.0, 0.0, 0.0, 20.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "25 April 2024", 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 0.0));

            globalConfigurationHelper.updateGlobalConfiguration(CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        });
    }
}
