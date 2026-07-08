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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientLoanChargeExternalIntegrationTest extends FeignLoanTestBase {

    private static final String NONE = "1";

    private static Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient("20 September 2011");
    }

    @Test
    public void checkNewClientLoanChargeSavesExternalId() {

        final Long loanProductId = createLoanProduct(false, NONE);

        final Long loanId = applyForLoanApplication(clientId, loanProductId, "12,000.00");
        approveLoan(loanId, approveLoanRequest(12000.0, "20 September 2011"));
        disburseLoanWithNetDisbursalAmount(loanId, "20 September 2011", "12,000.00");

        final Long chargeDefId = chargesHelper.createLoanSpecifiedDueDatePercentageOfInterestFee(1.0).getResourceId();

        final String externalId = "extId" + loanId.toString();
        PostLoansLoanIdChargesResponse chargeResponse = addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeDefId)
                .amount(1.0).dueDate("22 September 2011").externalId(externalId).dateFormat("dd MMMM yyyy").locale("en"));
        assertNotNull(chargeResponse);
        Long chargeId = chargeResponse.getResourceId();

        GetLoansLoanIdChargesChargeIdResponse loanCharge = getLoanCharge(loanId, chargeId);
        assertNotNull(loanCharge.getExternalId());
        assertEquals(externalId, loanCharge.getExternalId(), "Incorrect External Id Saved");
    }

    @Test
    public void checkNewClientLoanChargeFindsDuplicateExternalId() {

        final Long loanProductId = createLoanProduct(false, NONE);

        final Long loanId = applyForLoanApplication(clientId, loanProductId, "12,000.00");
        approveLoan(loanId, approveLoanRequest(12000.0, "20 September 2011"));
        disburseLoanWithNetDisbursalAmount(loanId, "20 September 2011", "12,000.00");

        final Long chargeDefId = chargesHelper.createLoanSpecifiedDueDatePercentageOfInterestFee(1.0).getResourceId();

        final String externalId = "extId" + loanId.toString();
        PostLoansLoanIdChargesResponse chargeResponse = addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeDefId)
                .amount(1.0).dueDate("22 September 2011").externalId(externalId).dateFormat("dd MMMM yyyy").locale("en"));
        assertNotNull(chargeResponse);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeDefId).amount(2.0)
                        .dueDate("23 September 2011").externalId(externalId).dateFormat("dd MMMM yyyy").locale("en")));
        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.loan.charge.duplicate.externalId");
    }

    private Long createLoanProduct(final boolean multiDisburseLoan, final String accountingRule) {
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, new org.apache.fineract.integrationtests.common.accounting.Account[0]);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return createLoanProductFromJson(loanProductJSON);
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, String principal) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .build(clientId.toString(), loanProductId.toString(), null);
        return applyForLoanFromJson(loanApplicationJSON);
    }
}
