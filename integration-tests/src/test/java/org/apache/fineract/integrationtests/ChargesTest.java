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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.ChargeCreateResponse;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeUpdateRequest;
import org.apache.fineract.client.models.GLAccountData;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsResponse;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupResponse;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChargesTest extends FeignIntegrationTest {

    private static final double CHARGE_AMOUNT = 100.0;
    private static final double MODIFIED_AMOUNT = 200.0;
    private static final String LOCALE = "en";
    private static final int FEE_FREQUENCY_MONTHS = 2;
    private static final int FEE_FREQUENCY_YEARS = 3;
    private static final int FEE_INTERVAL = 2;
    private static final String FEE_ON_MONTH_DAY = "04 March";
    private static final String MONTH_DAY_FORMAT = "dd MMM";

    private FeignChargesHelper chargesHelper;
    private FeignTaxComponentHelper taxComponentHelper;
    private FeignTaxGroupHelper taxGroupHelper;
    private FeignAccountHelper accountHelper;

    @BeforeAll
    public void setup() {
        chargesHelper = new FeignChargesHelper(fineractClient());
        taxComponentHelper = new FeignTaxComponentHelper(fineractClient());
        taxGroupHelper = new FeignTaxGroupHelper(fineractClient());
        accountHelper = new FeignAccountHelper(fineractClient());
    }

    @Test
    public void testClientChargeKeepsIncomeAccountOnCreateAndUpdate() {
        Long incomeAccountId = accountHelper.createIncomeAccount().getAccountID().longValue();
        Long otherIncomeAccountId = accountHelper.createIncomeAccount().getAccountID().longValue();

        Long chargeId = chargesHelper
                .createCharge(ChargeRequestBuilders.clientSpecifiedDueDateFee(CHARGE_AMOUNT).incomeAccountId(incomeAccountId))
                .getResourceId();

        ChargeData editTemplate = chargesHelper.getChargeWithTemplate(chargeId);
        Assertions.assertNotNull(editTemplate.getIncomeOrLiabilityAccount(), "Income account must be stored on create");
        Assertions.assertEquals(incomeAccountId, editTemplate.getIncomeOrLiabilityAccount().getId());

        ChargeData addTemplate = chargesHelper.getChargeTemplate(ChargeAppliesTo.CLIENT.getValue().longValue(), null);
        Assertions.assertEquals(accountIds(addTemplate.getIncomeOrLiabilityAccountOptions().get("incomeAccountOptions")),
                accountIds(editTemplate.getIncomeOrLiabilityAccountOptions().get("incomeAccountOptions")));
        Assertions.assertTrue(
                accountIds(editTemplate.getIncomeOrLiabilityAccountOptions().get("incomeAccountOptions")).contains(incomeAccountId));

        chargesHelper.updateCharge(chargeId, new ChargeUpdateRequest().locale(LOCALE).incomeAccountId(otherIncomeAccountId));
        Assertions.assertEquals(otherIncomeAccountId, chargesHelper.getChargeWithTemplate(chargeId).getIncomeOrLiabilityAccount().getId(),
                "Income account must be changed on update");

        verifyDeletion(chargeId);
    }

    @Test
    public void testSavingsWithdrawalFeeKeepsFreeWithdrawalSettingsOnCreate() {
        Long chargeId = chargesHelper
                .createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge().enableFreeWithdrawalCharge(true)
                        .freeWithdrawalFrequency(3).restartCountFrequency(1).countFrequencyType(PeriodFrequencyType.MONTHS.getValue()))
                .getResourceId();

        ChargeData charge = chargesHelper.getCharge(chargeId);
        Assertions.assertEquals(Boolean.TRUE, charge.getFreeWithdrawal(), "Free withdrawals must be stored on create");
        Assertions.assertEquals(3, charge.getFreeWithdrawalChargeFrequency());
        Assertions.assertEquals(1, charge.getRestartFrequency());
        Assertions.assertEquals(PeriodFrequencyType.MONTHS.getValue(), charge.getRestartFrequencyEnum());

        verifyDeletion(chargeId);
    }

    @Test
    public void testFreeWithdrawalsWithoutFrequenciesAreRejected() {
        Assertions.assertEquals(400,
                chargesHelper
                        .createChargeExpectingError(SavingsRequestBuilders.savingsWithdrawalFeeCharge().enableFreeWithdrawalCharge(true))
                        .getStatus());
    }

    private static List<Long> accountIds(List<GLAccountData> accounts) {
        return accounts.stream().map(GLAccountData::getId).sorted().toList();
    }

    @Test
    public void testChargesForLoans() {

        // Retrieving all Charges
        List<ChargeData> allChargesData = chargesHelper.getAllCharges();
        Assertions.assertNotNull(allChargesData);

        // Testing Creation, Updation and Deletion of Disbursement Charge
        final Long disbursementChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanDisbursementFee(CHARGE_AMOUNT))
                .getResourceId();
        Assertions.assertNotNull(disbursementChargeId);
        verifyLoanChargeModifications(disbursementChargeId);
        verifyDeletion(disbursementChargeId);

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Long specifiedDueDateChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePenalty(CHARGE_AMOUNT))
                .getResourceId();
        Assertions.assertNotNull(specifiedDueDateChargeId);
        verifyLoanChargeModifications(specifiedDueDateChargeId);
        verifyDeletion(specifiedDueDateChargeId);

        // Testing Creation, Updation and Deletion of Installment Fee Charge
        final Long installmentFeeChargeId = chargesHelper
                .createCharge(ChargeRequestBuilders.loanInstallmentCharge(ChargeCalculationType.FLAT, CHARGE_AMOUNT, true)).getResourceId();
        verifyLoanChargeModifications(installmentFeeChargeId);
        verifyDeletion(installmentFeeChargeId);

        // Testing Creation, Updation and Deletion of Overdue Installment Fee
        // Charge
        final Long overdueFeeChargeId = chargesHelper
                .createCharge(ChargeRequestBuilders.loanOverdueFee(CHARGE_AMOUNT).feeFrequency(FEE_FREQUENCY_MONTHS)
                        .feeOnMonthDay(FEE_ON_MONTH_DAY).feeInterval(FEE_INTERVAL).monthDayFormat(MONTH_DAY_FORMAT))
                .getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);
        verifyLoanChargeModifications(overdueFeeChargeId);

        Map<String, Object> changes = chargesHelper.updateCharge(overdueFeeChargeId,
                new ChargeUpdateRequest().locale(LOCALE).feeFrequency(FEE_FREQUENCY_YEARS).feeInterval(FEE_INTERVAL)).getChanges();
        ChargeData chargeDataAfterChanges = chargesHelper.getCharge(overdueFeeChargeId);
        Assertions.assertEquals(chargeDataAfterChanges.getFeeFrequency().getId(), longChange(changes, "feeFrequency"),
                "Verifying Charge after Modification");

        verifyDeletion(overdueFeeChargeId);
    }

    @Test
    public void testChargeCanBeDeactivated() {
        final Long chargeId = chargesHelper.createLoanSpecifiedDueDateCharge(CHARGE_AMOUNT).getResourceId();
        Assertions.assertNotNull(chargeId);
        Assertions.assertTrue(chargesHelper.getCharge(chargeId).getActive(), "Charge must be active after creation.");
        chargesHelper.updateCharge(chargeId, new ChargeUpdateRequest().locale(LOCALE).active(false));
        Assertions.assertFalse(chargesHelper.getCharge(chargeId).getActive(), "Charge must be inactive after update.");
        verifyDeletion(chargeId);
    }

    @Test
    public void testChargesForSavings() {

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Long specifiedDueDateChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsSpecifiedDueDateCharge())
                .getResourceId();
        Assertions.assertNotNull(specifiedDueDateChargeId);
        verifyAmountModification(specifiedDueDateChargeId);
        verifyDeletion(specifiedDueDateChargeId);

        // Testing Creation, Updation and Deletion of Savings Activation Charge
        final Long savingsActivationChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsActivationFeeCharge())
                .getResourceId();
        Assertions.assertNotNull(savingsActivationChargeId);
        verifyAmountModification(savingsActivationChargeId);
        verifyDeletion(savingsActivationChargeId);

        // Testing Creation, Updation and Deletion of Charge for Withdrawal Fee
        final Long withdrawalFeeChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge()).getResourceId();
        Assertions.assertNotNull(withdrawalFeeChargeId);

        // Updating Charge-Calculation-Type to Withdrawal-Fee
        Map<String, Object> changes = chargesHelper
                .updateCharge(withdrawalFeeChargeId,
                        new ChargeUpdateRequest().locale(LOCALE).chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue()))
                .getChanges();
        ChargeData chargeDataAfterChanges = chargesHelper.getCharge(withdrawalFeeChargeId);
        Assertions.assertEquals(chargeDataAfterChanges.getChargeCalculationType().getId(), longChange(changes, "chargeCalculationType"),
                "Verifying Charge after Modification");
        verifyDeletion(withdrawalFeeChargeId);

        // Testing Creation, Updation and Deletion of Charge for Annual Fee
        final Long annualFeeChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsAnnualFeeCharge()).getResourceId();
        Assertions.assertNotNull(annualFeeChargeId);
        verifyAmountModification(annualFeeChargeId);
        verifyDeletion(annualFeeChargeId);

        // Testing Creation, Updation and Deletion of Charge for Monthly Fee
        final Long monthlyFeeChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsMonthlyFeeCharge()).getResourceId();
        Assertions.assertNotNull(monthlyFeeChargeId);
        verifyAmountModification(monthlyFeeChargeId);
        verifyDeletion(monthlyFeeChargeId);

        // Testing Creation, Updation and Deletion of Charge for Overdraft Fee
        final Long overdraftFeeChargeId = chargesHelper
                .createCharge(SavingsRequestBuilders.savingsCharge(ChargeTimeType.OVERDRAFT_FEE.getValue())).getResourceId();
        Assertions.assertNotNull(overdraftFeeChargeId);
        verifyAmountModification(overdraftFeeChargeId);
        verifyDeletion(overdraftFeeChargeId);
    }

    @Test
    public void testChargeUsingPercentageCalculationWithMinAndMaxGapValues() {
        final BigDecimal minCapVal = BigDecimal.valueOf(23);
        final BigDecimal maxCapVal = BigDecimal.valueOf(45);

        final ChargeCreateResponse feeCharge = chargesHelper.createCharge(new ChargeCreateRequest().penalty(false).amount(9.0)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue())
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                .currencyCode("USD").name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5))
                .chargeAppliesTo(1).locale("en").active(true).minCap(minCapVal).maxCap(maxCapVal));

        Assertions.assertNotNull(feeCharge);
        final Long chargeId = feeCharge.getResourceId();
        Assertions.assertNotNull(chargeId);

        final ChargeData chargeResponseData = chargesHelper.getCharge(chargeId);
        Assertions.assertNotNull(chargeResponseData);
        Assertions.assertEquals(minCapVal.stripTrailingZeros(), chargeResponseData.getMinCap().stripTrailingZeros());
        Assertions.assertEquals(maxCapVal.stripTrailingZeros(), chargeResponseData.getMaxCap().stripTrailingZeros());
    }

    @Test
    public void testChargeCreationWithTaxGroup() {
        final PostTaxesComponentsRequest taxComponentRequest = new PostTaxesComponentsRequest()
                .name(Utils.randomStringGenerator("TAX_COM_", 4)).percentage(12.0f).startDate("01 January 2023").dateFormat("dd MMMM yyyy")
                .locale("en");

        final PostTaxesComponentsResponse taxComponentRespose = taxComponentHelper.createTaxComponent(taxComponentRequest);
        Assertions.assertNotNull(taxComponentRequest);

        final Set<PostTaxesGroupTaxComponents> taxComponentsSet = new HashSet<>();
        taxComponentsSet
                .add(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentRespose.getResourceId()).startDate("01 January 2023"));
        final PostTaxesGroupRequest taxGroupRequest = new PostTaxesGroupRequest().name(Utils.randomStringGenerator("TAX_GRP_", 4))
                .taxComponents(taxComponentsSet).dateFormat("dd MMMM yyyy").locale("en");
        final PostTaxesGroupResponse taxGroupResponse = taxGroupHelper.createTaxGroup(taxGroupRequest);
        Assertions.assertNotNull(taxGroupResponse);

        final ChargeCreateResponse feeCharge = chargesHelper.createCharge(new ChargeCreateRequest().penalty(false).amount(9.0)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue())
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                .currencyCode("USD").name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5))
                .chargeAppliesTo(1).locale("en").active(true).taxGroupId(taxGroupResponse.getResourceId()));

        Assertions.assertNotNull(feeCharge);
        final Long chargeId = feeCharge.getResourceId();
        Assertions.assertNotNull(chargeId);

        final ChargeData chargeResponseData = chargesHelper.getCharge(chargeId);
        Assertions.assertNotNull(chargeResponseData);
        Assertions.assertNotNull(chargeResponseData.getTaxGroup());
        Assertions.assertEquals(chargeResponseData.getTaxGroup().getId(), taxGroupResponse.getResourceId());
    }

    private void verifyLoanChargeModifications(Long chargeId) {
        verifyAmountModification(chargeId);

        Map<String, Object> changes = chargesHelper.updateCharge(chargeId, percentageUpdate(ChargeCalculationType.PERCENT_OF_AMOUNT))
                .getChanges();
        ChargeData chargeDataAfterChanges = chargesHelper.getCharge(chargeId);
        Assertions.assertEquals(chargeDataAfterChanges.getChargePaymentMode().getId(), longChange(changes, "chargePaymentMode"),
                "Verifying Charge after Modification");
        Assertions.assertEquals(chargeDataAfterChanges.getChargeCalculationType().getId(), longChange(changes, "chargeCalculationType"),
                "Verifying Charge after Modification");

        verifyCalculationTypeModification(chargeId, ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST);
        verifyCalculationTypeModification(chargeId, ChargeCalculationType.PERCENT_OF_INTEREST);
    }

    private void verifyCalculationTypeModification(Long chargeId, ChargeCalculationType chargeCalculationType) {
        Map<String, Object> changes = chargesHelper.updateCharge(chargeId, percentageUpdate(chargeCalculationType)).getChanges();
        ChargeData chargeDataAfterChanges = chargesHelper.getCharge(chargeId);
        Assertions.assertEquals(chargeDataAfterChanges.getChargeCalculationType().getId(), longChange(changes, "chargeCalculationType"),
                "Verifying Charge after Modification");
    }

    private void verifyAmountModification(Long chargeId) {
        Map<String, Object> changes = chargesHelper.updateCharge(chargeId, new ChargeUpdateRequest().locale(LOCALE).amount(MODIFIED_AMOUNT))
                .getChanges();
        ChargeData chargeDataAfterChanges = chargesHelper.getCharge(chargeId);
        Assertions.assertEquals(0, chargeDataAfterChanges.getAmount().compareTo(new BigDecimal(String.valueOf(changes.get("amount")))),
                "Verifying Charge after Modification");
    }

    private void verifyDeletion(Long chargeId) {
        Long chargeIdAfterDeletion = chargesHelper.deleteCharge(chargeId).getResourceId();
        Assertions.assertEquals(chargeId, chargeIdAfterDeletion, "Verifying Charge ID after deletion");
    }

    private static Long longChange(Map<String, Object> changes, String parameterName) {
        return ((Number) changes.get(parameterName)).longValue();
    }

    private static ChargeUpdateRequest percentageUpdate(ChargeCalculationType chargeCalculationType) {
        return new ChargeUpdateRequest().locale(LOCALE).chargeCalculationType(chargeCalculationType.getValue())
                .chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }
}
