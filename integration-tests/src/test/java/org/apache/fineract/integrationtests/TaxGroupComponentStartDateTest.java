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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetTaxesGroupResponse;
import org.apache.fineract.client.models.GetTaxesGroupTaxAssociations;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesGroupTaxGroupIdRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.TaxGroupTemplateApi;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxGroupComponentStartDateTest {

    private final FeignAccountHelper accountHelper = new FeignAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
    private FineractFeignClient fineractClient;
    private TaxGroupTemplateApi taxGroupTemplateApi;

    @BeforeEach
    public void setup() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        taxGroupTemplateApi = fineractClient.create(TaxGroupTemplateApi.class);
    }

    @Test
    void updateTaxGroup_changesStartDateOfComponentNotYetEffective() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 March 2024");
            final Long mappingId = association(taxGroupId, taxComponentId).getId();

            ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId, updateRequest(
                    new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).startDate("15 March 2024"))));

            final GetTaxesGroupTaxAssociations association = association(taxGroupId, taxComponentId);
            assertEquals(mappingId, association.getId());
            assertEquals(LocalDate.of(2024, 3, 15), association.getStartDate());
        });
    }

    @Test
    void updateTaxGroup_rejectsStartDateChangeOfEffectiveComponent() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 January 2023");
            final Long mappingId = association(taxGroupId, taxComponentId).getId();

            final CallFailedRuntimeException exception = fail(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId, updateRequest(
                    new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).startDate("15 March 2024"))));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("cannot.be.modified.after.activation"));
            assertEquals(LocalDate.of(2023, 1, 1), association(taxGroupId, taxComponentId).getStartDate());
        });
    }

    @Test
    void updateTaxGroup_rejectsStartDateOnOrAfterEndDate() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 March 2024");
            final Long mappingId = association(taxGroupId, taxComponentId).getId();
            ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId,
                    updateRequest(new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).endDate("01 June 2024"))));

            final CallFailedRuntimeException exception = fail(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId, updateRequest(
                    new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).startDate("15 June 2024"))));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("must.be.before.end.date"));
            final GetTaxesGroupTaxAssociations association = association(taxGroupId, taxComponentId);
            assertEquals(LocalDate.of(2024, 3, 1), association.getStartDate());
            assertEquals(LocalDate.of(2024, 6, 1), association.getEndDate());
        });
    }

    @Test
    void updateTaxGroup_rejectsStartDateChangeOfLinkedGroup() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 March 2024");
            final Long mappingId = association(taxGroupId, taxComponentId).getId();
            linkToCharge(taxGroupId);

            final CallFailedRuntimeException exception = fail(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId, updateRequest(
                    new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).startDate("15 March 2024"))));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("cannot.be.modified.for.linked.tax.group"));
            assertEquals(LocalDate.of(2024, 3, 1), association(taxGroupId, taxComponentId).getStartDate());
        });
    }

    @Test
    void updateTaxGroup_addsNewComponentWithPastStartDate() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 March 2024");
            final Long newTaxComponentId = createTaxComponent();

            ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId,
                    updateRequest(new PutTaxesGroupTaxComponents().taxComponentId(newTaxComponentId).startDate("01 June 2023"))));

            assertEquals(LocalDate.of(2023, 6, 1), association(taxGroupId, newTaxComponentId).getStartDate());
        });
    }

    @Test
    void retrieveTaxGroupTemplate_unlinkedGroupFlagsComponentsByStartDate() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long futureComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(futureComponentId, "01 March 2024");
            final Long effectiveComponentId = createTaxComponent();
            ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId,
                    updateRequest(new PutTaxesGroupTaxComponents().taxComponentId(effectiveComponentId).startDate("01 June 2023"))));

            final GetTaxesGroupResponse template = ok(() -> taxGroupTemplateApi.retrieveTaxGroupWithTemplate(taxGroupId));

            assertTrue(template.getGroupEditable());
            assertTrue(association(template, futureComponentId).getComponentEditable());
            assertFalse(association(template, effectiveComponentId).getComponentEditable());
        });
    }

    @Test
    void retrieveTaxGroupTemplate_linkedGroupIsNotEditable() {
        BusinessDateHelper.runAt("01 January 2024", () -> {
            final Long taxComponentId = createTaxComponent();
            final Long taxGroupId = createTaxGroup(taxComponentId, "01 March 2024");
            linkToCharge(taxGroupId);

            final GetTaxesGroupResponse template = ok(() -> taxGroupTemplateApi.retrieveTaxGroupWithTemplate(taxGroupId));

            assertFalse(template.getGroupEditable());
            assertFalse(association(template, taxComponentId).getComponentEditable());
        });
    }

    private Long createTaxComponent() {
        final Account glAccount = accountHelper.createLiabilityAccount("taxComponent");
        final PostTaxesComponentsRequest request = new PostTaxesComponentsRequest().name(Utils.randomStringGenerator("TAX_CMP_", 6))
                .percentage(5.0f).startDate("01 January 2023").creditAccountType(Integer.valueOf(glAccount.getAccountType().toString()))
                .creditAccountId(glAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);
        return ok(() -> fineractClient.taxComponents().createTaxComponent(request)).getResourceId();
    }

    private Long createTaxGroup(final Long taxComponentId, final String startDate) {
        final PostTaxesGroupRequest request = new PostTaxesGroupRequest().name(Utils.randomStringGenerator("TAX_GRP_", 6))
                .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate(startDate)))
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

    private GetTaxesGroupTaxAssociations association(final Long taxGroupId, final Long taxComponentId) {
        return association(ok(() -> fineractClient.taxGroup().retrieveOneTaxGroup(taxGroupId)), taxComponentId);
    }

    private GetTaxesGroupTaxAssociations association(final GetTaxesGroupResponse taxGroup, final Long taxComponentId) {
        return taxGroup.getTaxAssociations().stream().filter(a -> taxComponentId.equals(a.getTaxComponent().getId())).findFirst()
                .orElseThrow();
    }

    private PutTaxesGroupTaxGroupIdRequest updateRequest(final PutTaxesGroupTaxComponents... taxComponents) {
        return new PutTaxesGroupTaxGroupIdRequest().taxComponents(new HashSet<>(Arrays.asList(taxComponents))).dateFormat(Utils.DATE_FORMAT)
                .locale(Utils.LOCALE);
    }
}
