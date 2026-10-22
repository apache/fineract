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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetTaxesComponentsResponse;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesComponentsTaxComponentIdRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxComponentEditLockTest {

    private FineractFeignClient fineractClient;

    @BeforeEach
    public void setup() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    }

    @Test
    void unlinkedComponent_percentageAndAccountsCanBeChanged() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Account creditAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
            final Long taxComponentId = createTaxComponent(creditAccount);
            assertTrue(retrieve(taxComponentId).getEditable());

            final Account newCreditAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
            ok(() -> fineractClient.taxComponents().updateTaxComponent(taxComponentId,
                    new PutTaxesComponentsTaxComponentIdRequest().percentage(7.0f)
                            .creditAccountType(Integer.valueOf(newCreditAccount.getAccountType().toString()))
                            .creditAccountId(newCreditAccount.getAccountID().longValue()).locale(Utils.LOCALE)));

            final GetTaxesComponentsResponse taxComponent = retrieve(taxComponentId);
            assertEquals(7.0f, taxComponent.getPercentage());
            assertEquals(newCreditAccount.getAccountID().longValue(), taxComponent.getCreditAccount().getId());
        });
    }

    @Test
    void linkedComponent_onlyTheNameCanBeChanged() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Account creditAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
            final Long taxComponentId = createTaxComponent(creditAccount);
            linkToCharge(createTaxGroup(taxComponentId));
            assertFalse(retrieve(taxComponentId).getEditable());

            final String newName = Utils.randomStringGenerator("TAX_CMP_", 6);
            ok(() -> fineractClient.taxComponents().updateTaxComponent(taxComponentId,
                    wholeForm(creditAccount).name(newName).percentage(5.0f)));
            assertEquals(newName, retrieve(taxComponentId).getName());

            final CallFailedRuntimeException percentageChange = fail(
                    () -> fineractClient.taxComponents().updateTaxComponent(taxComponentId, wholeForm(creditAccount).percentage(7.0f)));
            assertEquals(400, percentageChange.getStatus());
            assertTrue(percentageChange.getMessage().contains("percentage.cannot.be.modified.for.linked.tax.component"));

            final Account otherAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
            final CallFailedRuntimeException accountChange = fail(() -> fineractClient.taxComponents().updateTaxComponent(taxComponentId,
                    wholeForm(creditAccount).percentage(5.0f).creditAccountId(otherAccount.getAccountID().longValue())));
            assertEquals(400, accountChange.getStatus());
            assertTrue(accountChange.getMessage().contains("creditAccountId.cannot.be.modified.for.linked.tax.component"));

            final GetTaxesComponentsResponse taxComponent = retrieve(taxComponentId);
            assertEquals(5.0f, taxComponent.getPercentage());
            assertEquals(creditAccount.getAccountID().longValue(), taxComponent.getCreditAccount().getId());
        });
    }

    @Test
    void template_isEditable() {
        assertTrue(ok(() -> fineractClient.taxComponents().retrieveTemplateTaxComponent()).getEditable());
    }

    private PutTaxesComponentsTaxComponentIdRequest wholeForm(final Account creditAccount) {
        return new PutTaxesComponentsTaxComponentIdRequest().startDate("01 January 2023")
                .creditAccountType(Integer.valueOf(creditAccount.getAccountType().toString()))
                .creditAccountId(creditAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);
    }

    private GetTaxesComponentsResponse retrieve(final Long taxComponentId) {
        return ok(() -> fineractClient.taxComponents().retrieveOneTaxComponent(taxComponentId));
    }

    private Long createTaxComponent(final Account creditAccount) {
        final PostTaxesComponentsRequest request = new PostTaxesComponentsRequest().name(Utils.randomStringGenerator("TAX_CMP_", 6))
                .percentage(5.0f).startDate("01 January 2023").creditAccountType(Integer.valueOf(creditAccount.getAccountType().toString()))
                .creditAccountId(creditAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);
        return ok(() -> fineractClient.taxComponents().createTaxComponent(request)).getResourceId();
    }

    private Long createTaxGroup(final Long taxComponentId) {
        final PostTaxesGroupRequest request = new PostTaxesGroupRequest().name(Utils.randomStringGenerator("TAX_GRP_", 6))
                .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate("01 January 2023")))
                .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);
        return ok(() -> fineractClient.taxGroup().createTaxGroup(request)).getResourceId();
    }

    private void linkToCharge(final Long taxGroupId) {
        new FeignChargesHelper(fineractClient).createCharge(new ChargeRequest().penalty(false).amount(10.0)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.uniqueRandomStringGenerator("CHARGE_", 6)).chargeAppliesTo(1).locale(Utils.LOCALE).active(true)
                .taxGroupId(taxGroupId));
    }
}
