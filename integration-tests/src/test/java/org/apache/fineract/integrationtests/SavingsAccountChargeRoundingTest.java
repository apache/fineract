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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.savings.base.BaseSavingsIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class SavingsAccountChargeRoundingTest extends BaseSavingsIntegrationTest {

    private Long clientId;
    private ChargesHelper chargesHelper;
    private static final String DATE = "01 January 2026";

    @BeforeEach
    public void setup() {
        clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        chargesHelper = new ChargesHelper();
    }

    /** FLAT CHARGE **/
    @Test
    public void shouldApplyRoundingRules_forFlatCharge() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createFlatCharge(19.8);
            assertNotNull(charge.getResourceId());

            Long savingsChargeId = addFlatCharge(savingsId, charge.getResourceId(), 19.8, DATE);

            BigDecimal actualChargeAmount = getSavingsChargeAmount(savingsId, savingsChargeId);

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("19.8"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpFlatCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createFlatCharge(0.6);
            assertNotNull(charge.getResourceId());

            Long savingsChargeId = addFlatCharge(savingsId, charge.getResourceId(), 0.6, DATE);

            BigDecimal actualChargeAmount = getSavingsChargeAmount(savingsId, savingsChargeId);

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.6"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddFlatCharge_whenAmountRoundsToZero() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createFlatCharge(0.4);
            assertNotNull(charge.getResourceId());

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> addFlatCharge(savingsId, charge.getResourceId(), 0.4, DATE));

            assertEquals(400, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.savings.charge.amount.rounded.to.zero"));
            assertNoChargesPersisted(savingsId, charge.getResourceId());
        });
    }

    @Test
    public void shouldApplyRoundingRules_forPercentageOfWithdrawalCharge() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createPercentageWithdrawalCharge(2.5);

            addPercentageWithdrawalCharge(savingsId, charge.getResourceId(), 2.5);

            deposit(savingsId, DATE, BigDecimal.valueOf(1000));

            withdraw(savingsId, DATE, BigDecimal.valueOf(615));

            SavingsAccountData accountData = getSavingsAccount(savingsId);

            BigDecimal actualChargeFees = getActualChargeAmount(accountData);
            assertNotNull(actualChargeFees);

            BigDecimal actualBalance = getActualBalance(accountData);
            assertNotNull(actualBalance);

            BigDecimal expectedChargeFees = calculateExpectedPercentageCharge("615", "0.025", 0, 1);

            BigDecimal expectedBalance = calculateExpectedBalance("1000", "615", expectedChargeFees);

            assertBigDecimalEquals(expectedChargeFees, actualChargeFees);
            assertBigDecimalEquals(expectedBalance, actualBalance);
        });
    }

    @Test
    public void shouldRoundUpPercentageOfWithdrawalCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createPercentageWithdrawalCharge(2.5);

            addPercentageWithdrawalCharge(savingsId, charge.getResourceId(), 2.5);

            deposit(savingsId, DATE, BigDecimal.valueOf(1000));

            withdraw(savingsId, DATE, BigDecimal.valueOf(24));

            SavingsAccountData accountData = getSavingsAccount(savingsId);

            BigDecimal actualChargeFees = getActualChargeAmount(accountData);
            assertNotNull(actualChargeFees);

            BigDecimal actualBalance = getActualBalance(accountData);
            assertNotNull(actualBalance);

            BigDecimal expectedChargeFees = calculateExpectedPercentageCharge("24", "0.025", 0, 1);

            BigDecimal expectedBalance = calculateExpectedBalance("1000", "24", expectedChargeFees);

            assertBigDecimalEquals(expectedChargeFees, actualChargeFees);
            assertBigDecimalEquals(expectedBalance, actualBalance);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeFees);
        });
    }

    @Test
    public void shouldIgnorePercentageOfWithdrawalCharge_whenRoundedToZero() {
        runAt(DATE, () -> {

            Long productId = createSavingsProduct(0, 1);
            Long savingsId = createAndActivateSavingsAccount(productId, DATE);

            PostChargesResponse charge = createPercentageWithdrawalCharge(2.5);

            addPercentageWithdrawalCharge(savingsId, charge.getResourceId(), 2.5);

            deposit(savingsId, DATE, BigDecimal.valueOf(1000));

            withdraw(savingsId, DATE, BigDecimal.valueOf(20));

            SavingsAccountData accountData = getSavingsAccount(savingsId);

            BigDecimal actualChargeFees = getActualChargeAmount(accountData);
            assertNull(actualChargeFees);

            BigDecimal actualBalance = getActualBalance(accountData);
            assertNotNull(actualBalance);

            assertBigDecimalEquals(new BigDecimal("980"), actualBalance);
        });
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private Long createSavingsProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return createProduct(baseSavingsProduct(digitsAfterDecimal, inMultiplesOf)).getResourceId();
    }

    private PostSavingsProductsRequest baseSavingsProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return dailyInterestPostingProduct().digitsAfterDecimal(digitsAfterDecimal).inMultiplesOf(inMultiplesOf).currencyCode("USD");
    }

    private Long createAndActivateSavingsAccount(Long productId, String date) {
        Long savingsId = applySavingsAccount(applySavingsRequest(clientId, productId, date)).getSavingsId();
        approveSavingsAccount(savingsId, date);
        activateSavingsAccount(savingsId, date);
        return savingsId;
    }

    private PostChargesResponse createFlatCharge(double amount) {
        String uniqueChargeName = "Savings Account Flat Charge " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharges(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(2) // SAVINGS
                .chargeTimeType(2) // SPECIFIED DUE DATE
                .chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createPercentageWithdrawalCharge(double percentage) {
        String uniqueChargeName = "Savings Account Withdrawal Charge " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharges(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(2) // SAVINGS
                .chargeTimeType(5) // WITHDRAWAL
                .chargeCalculationType(2) // % OF AMOUNT
                .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private Long addFlatCharge(Long savingsId, Long chargeId, double amount, String date) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()
                .chargeId(chargeId).amount((float) amount).dateFormat(DATETIME_PATTERN).locale("en").dueDate(date);

        PostSavingsAccountsSavingsAccountIdChargesResponse response = ok(
                fineractClient().savingsAccountCharges.addSavingsAccountCharge(savingsId, request));

        return response.getResourceId();
    }

    private void addPercentageWithdrawalCharge(Long savingsId, Long chargeId, double amount) {
        PostSavingsAccountsSavingsAccountIdChargesRequest request = new PostSavingsAccountsSavingsAccountIdChargesRequest()
                .chargeId(chargeId).amount((float) amount).locale("en");

        ok(fineractClient().savingsAccountCharges.addSavingsAccountCharge(savingsId, request));
    }

    private PostSavingsAccountTransactionsResponse withdraw(Long savingsId, String date, BigDecimal amount) {
        PostSavingsAccountTransactionsRequest request = new PostSavingsAccountTransactionsRequest().dateFormat(DATETIME_PATTERN)
                .locale("en").paymentTypeId(1).transactionAmount(amount).transactionDate(date);

        return ok(fineractClient().savingsTransactions.createSavingsAccountTransaction(savingsId, request, "withdrawal"));
    }

    private GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse getSavingsAccountCharge(Long savingsId,
            Long savingsAccountChargeId) {
        return ok(fineractClient().savingsAccountCharges.retrieveSavingsAccountCharge(savingsId, savingsAccountChargeId));
    }

    private BigDecimal applyRoundingRules(BigDecimal amount, int digitsAfterDecimal, int inMultiplesOf) {
        BigDecimal scaled;

        if (digitsAfterDecimal == 0) {
            BigDecimal fractionPart = amount.remainder(BigDecimal.ONE);

            if (fractionPart.compareTo(new BigDecimal("0.5")) <= 0) {
                scaled = amount.setScale(0, RoundingMode.DOWN);
            } else {
                scaled = amount.setScale(0, RoundingMode.UP);
            }
        } else {
            scaled = amount.setScale(digitsAfterDecimal, RoundingMode.HALF_UP);
        }

        if (digitsAfterDecimal == 0 && inMultiplesOf > 0) {
            BigDecimal divisor = new BigDecimal(inMultiplesOf);
            BigDecimal remainder = scaled.remainder(divisor);

            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                scaled = scaled.add(divisor.subtract(remainder));
            }
        }
        return scaled;
    }

    private BigDecimal getSavingsChargeAmount(Long savingsId, Long savingsChargeId) {

        var chargeData = getSavingsAccountCharge(savingsId, savingsChargeId);

        assertNotNull(chargeData);
        assertNotNull(chargeData.getAmount());

        return BigDecimal.valueOf(chargeData.getAmount().doubleValue());
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    private BigDecimal getActualChargeAmount(SavingsAccountData accountData) {
        assertNotNull(accountData.getSummary());
        return accountData.getSummary().getTotalWithdrawalFees();
    }

    private BigDecimal getActualBalance(SavingsAccountData accountData) {
        assertNotNull(accountData.getSummary());
        return accountData.getSummary().getAccountBalance();
    }

    private BigDecimal calculateExpectedPercentageCharge(String baseAmount, String percentageAsDecimal, int digitsAfterDecimal,
            int inMultiplesOf) {
        BigDecimal base = new BigDecimal(baseAmount);
        BigDecimal percentage = new BigDecimal(percentageAsDecimal);
        BigDecimal rawCharge = base.multiply(percentage);
        return applyRoundingRules(rawCharge, digitsAfterDecimal, inMultiplesOf);
    }

    private BigDecimal calculateExpectedBalance(String deposit, String withdraw, BigDecimal expectedChargeFees) {
        return new BigDecimal(deposit).subtract(new BigDecimal(withdraw)).subtract(expectedChargeFees);
    }

    private List<GetSavingsAccountsSavingsAccountIdChargesResponse> getSavingsCharges(Long savingsId) {
        return ok(fineractClient().savingsAccountCharges.retrieveAllSavingsAccountCharges(savingsId, "all"));
    }

    private void assertNoChargesPersisted(Long savingsId, Long chargeId) {
        var charges = getSavingsCharges(savingsId);
        boolean chargeExists = charges.stream().anyMatch(c -> chargeId.equals(c.getChargeId()));
        assertFalse(chargeExists, "Expected charge not to persist since rounded amount becomes 0");
    }
}
