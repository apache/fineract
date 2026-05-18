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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.TaxComponentCreateRequest;
import org.apache.fineract.client.models.TaxGroupComponentData;
import org.apache.fineract.client.models.TaxGroupCreateRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxesTest {

    private FeignTaxComponentHelper taxComponentHelper;
    private FeignTaxGroupHelper taxGroupHelper;

    @BeforeEach
    public void setup() {
        taxComponentHelper = new FeignTaxComponentHelper(FineractFeignClientHelper.getFineractFeignClient());
        taxGroupHelper = new FeignTaxGroupHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void createTaxComponentTest() {
        Long taxComponentId = createTaxComponentWithLiabilityToCredit("taxComponent");

        var taxComponentDetails = taxComponentHelper.retrieveTaxComponent(taxComponentId);
        Assertions.assertNotNull(taxComponentDetails);
        Assertions.assertNotNull(taxComponentDetails.getId());
        Assertions.assertEquals(taxComponentId, taxComponentDetails.getId());

        taxComponentId = createTaxComponentWithLiabilityToDebit("taxComponent");

        taxComponentDetails = taxComponentHelper.retrieveTaxComponent(taxComponentId);
        Assertions.assertNotNull(taxComponentDetails);
        Assertions.assertNotNull(taxComponentDetails.getId());
        Assertions.assertEquals(taxComponentId, taxComponentDetails.getId());
    }

    @Test
    public void createTaxGroupTest() {
        var allTaxGroups = taxGroupHelper.retrieveAllTaxGroups();
        Assertions.assertNotNull(allTaxGroups);

        final Long taxComponentId = createTaxComponentWithLiabilityToCredit("taxComponent");

        final Set<TaxGroupComponentData> taxComponentsSet = new HashSet<>();
        taxComponentsSet.add(new TaxGroupComponentData().taxComponentId(taxComponentId).startDate("01 January 2023"));
        var taxGroupRequest = new TaxGroupCreateRequest().name(Utils.randomStringGenerator("TAX_GRP_", 4)).taxComponents(taxComponentsSet)
                .dateFormat("dd MMMM yyyy").locale("en");
        var taxGroupResponse = taxGroupHelper.createTaxGroup(taxGroupRequest);
        Assertions.assertNotNull(taxGroupResponse);
        Assertions.assertNotNull(taxGroupResponse.getResourceId());

        var taxGroupDetails = taxGroupHelper.retrieveTaxGroup(taxGroupResponse.getResourceId());
        Assertions.assertNotNull(taxGroupDetails);
        Assertions.assertEquals(taxGroupResponse.getResourceId(), taxGroupDetails.getId());
        Assertions.assertFalse(taxGroupDetails.getTaxAssociations().isEmpty());
        var taxAssociation = taxGroupDetails.getTaxAssociations().iterator().next();
        Assertions.assertNotNull(taxAssociation);
        Assertions.assertEquals(taxComponentId, taxAssociation.getTaxComponent().getId());

        allTaxGroups = taxGroupHelper.retrieveAllTaxGroups();
        Assertions.assertNotNull(allTaxGroups);
        Assertions.assertTrue(allTaxGroups.size() > 0);
    }

    private Long createTaxComponentWithLiabilityToCredit(final String taxComponentPrefix) {
        final Account taxComponentGlAccount = AccountHelper.createLiabilityGlAccount(taxComponentPrefix);

        var taxComponentRequest = new TaxComponentCreateRequest().name(Utils.randomStringGenerator(taxComponentPrefix, 4))
                .percentage(BigDecimal.valueOf(12.0f)).startDate("01 January 2023")
                .creditAccountType(Integer.valueOf(taxComponentGlAccount.getAccountType().toString()))
                .creditAccountId(taxComponentGlAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);

        var taxComponentRespose = taxComponentHelper.createTaxComponent(taxComponentRequest);
        Assertions.assertNotNull(taxComponentRespose);
        Assertions.assertNotNull(taxComponentRespose.getResourceId());

        return taxComponentRespose.getResourceId();
    }

    private Long createTaxComponentWithLiabilityToDebit(final String taxComponentPrefix) {
        final Account taxComponentGlAccount = AccountHelper.createLiabilityGlAccount(taxComponentPrefix);

        var taxComponentRequest = new TaxComponentCreateRequest().name(Utils.randomStringGenerator(taxComponentPrefix, 4))
                .percentage(BigDecimal.valueOf(12.0f)).startDate("01 January 2023")
                .debitAccountType(Integer.valueOf(taxComponentGlAccount.getAccountType().toString()))
                .debitAccountId(taxComponentGlAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);

        var taxComponentRespose = taxComponentHelper.createTaxComponent(taxComponentRequest);
        Assertions.assertNotNull(taxComponentRespose);
        Assertions.assertNotNull(taxComponentRespose.getResourceId());

        return taxComponentRespose.getResourceId();
    }

    @Test
    void retrieveTaxGroupWithNonExistentId_shouldReturn404() {
        final Long nonExistentTaxGroupId = 99999L;

        CallFailedRuntimeException exception = taxGroupHelper.retrieveTaxGroupExpectingError(nonExistentTaxGroupId);

        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.group.id.invalid"),
                "Response should contain the error code for tax group not found");
    }

    @Test
    void retrieveTaxComponentWithNonExistentId_shouldReturn404() {
        final Long nonExistentTaxComponentId = 99999L;

        CallFailedRuntimeException exception = taxComponentHelper.retrieveTaxComponentExpectingError(nonExistentTaxComponentId);

        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.component.id.invalid"),
                "Response should contain the error code for tax component not found");
    }

}
