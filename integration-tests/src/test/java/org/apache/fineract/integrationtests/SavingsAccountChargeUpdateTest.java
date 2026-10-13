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

import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.DATETIME_PATTERN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage, at the REST-API level, for updating a savings account charge (PUT
 * {@code /savingsaccounts/{accountId}/charges/{chargeId}}).
 *
 * <p>
 * Before the fix, updating the amount of a {@code PERCENT_OF_AMOUNT} withdrawal-fee charge unconditionally threw a
 * {@code NullPointerException} (HTTP 500) — see {@code SavingsAccountCharge#update(JsonCommand)}. This class exercises
 * that path end to end and confirms the update both succeeds and takes effect (the new percentage is actually applied),
 * while also checking that the {@code FLAT} path — the other calculation type savings charges support — kept working.
 *
 * <p>
 * The other half of the fix — rejecting calculation types outside {@code FLAT}/{@code PERCENT_OF_AMOUNT} with HTTP 400
 * instead of silently ignoring the update — is <em>not</em> exercised here: such a charge can only exist through
 * stale/legacy data (direct DB writes), never through the API, because charge-definition creation/update already
 * enforces {@code ChargeCalculationType.validValuesForSavings()} for {@code appliesTo=SAVINGS}. That guard is covered
 * at the unit level in {@code SavingsAccountChargeTest} (fineract-savings), which can construct such an entity via
 * reflection.
 */
public class SavingsAccountChargeUpdateTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2026";

    private Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient();
    }

    @Test
    public void updatePercentOfAmountWithdrawalCharge_succeedsAndAppliesNewPercentage() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct();
            Long savingsId = createApproveActivateSavings(clientId, productId, DATE);

            PostChargesResponse charge = createPercentageWithdrawalCharge(2.5);
            Long savingsChargeId = addPercentageWithdrawalCharge(savingsId, charge.getResourceId(), 2.5);

            PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()
                    .amount(5.0f).locale("en");

            // Before the fix this threw a NullPointerException (HTTP 500) instead of completing.
            assertDoesNotThrow(() -> updateSavingsAccountCharge(savingsId, savingsChargeId, request));

            GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse updatedCharge = getSavingsAccountCharge(savingsId,
                    savingsChargeId);

            assertNotNull(updatedCharge.getPercentage());
            assertBigDecimalEquals(new BigDecimal("5"), BigDecimal.valueOf(updatedCharge.getPercentage()));
        });
    }

    @Test
    public void updateFlatCharge_succeedsAndAppliesNewAmount() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct();
            Long savingsId = createApproveActivateSavings(clientId, productId, DATE);

            PostChargesResponse charge = createFlatCharge(19.8);
            Long savingsChargeId = addFlatCharge(savingsId, charge.getResourceId(), 19.8, DATE);

            PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest request = new PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest()
                    .amount(30.0f).locale("en");

            assertDoesNotThrow(() -> updateSavingsAccountCharge(savingsId, savingsChargeId, request));

            GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse updatedCharge = getSavingsAccountCharge(savingsId,
                    savingsChargeId);

            assertNotNull(updatedCharge.getAmount());
            assertBigDecimalEquals(new BigDecimal("30"), BigDecimal.valueOf(updatedCharge.getAmount()));
        });
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private Long createSavingsProduct() {
        PostSavingsProductsRequest request = new PostSavingsProductsRequest().locale("en")
                .name(Utils.uniqueRandomStringGenerator("DAILY_INTEREST", 6)).shortName(Utils.uniqueRandomStringGenerator("", 4))
                .description("Daily interest posting product").nominalAnnualInterestRate(10.0).digitsAfterDecimal(2).inMultiplesOf(1)
                .currencyCode("USD").accountingRule(1).interestCalculationDaysInYearType(365).interestCompoundingPeriodType(1)
                .interestCalculationType(2).interestPostingPeriodType(1).withdrawalFeeForTransfers(false).enforceMinRequiredBalance(false)
                .allowOverdraft(false).withHoldTax(false).isDormancyTrackingActive(false);
        return createSavingsProduct(request).getResourceId();
    }

    private PostChargesResponse createFlatCharge(double amount) {
        String uniqueChargeName = "Savings Account Flat Charge " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(2) // SAVINGS
                .chargeTimeType(2) // SPECIFIED DUE DATE
                .chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createPercentageWithdrawalCharge(double percentage) {
        String uniqueChargeName = "Savings Account Withdrawal Charge " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(2) // SAVINGS
                .chargeTimeType(5) // WITHDRAWAL
                .chargeCalculationType(2) // % OF AMOUNT
                .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private Long addFlatCharge(Long savingsId, Long chargeId, double amount, String date) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()
                .chargeId(chargeId).amount((float) amount).dateFormat(DATETIME_PATTERN).locale("en").dueDate(date);

        return addSavingsAccountCharge(savingsId, request).getResourceId();
    }

    private Long addPercentageWithdrawalCharge(Long savingsId, Long chargeId, double amount) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()
                .chargeId(chargeId).amount((float) amount).locale("en");

        return addSavingsAccountCharge(savingsId, request).getResourceId();
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }
}
