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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetTaxesGroupResponse;
import org.apache.fineract.client.models.GetTaxesGroupTaxAssociations;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesGroupTaxGroupIdRequest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxGroupEditAfterComponentEndedTest {

    private FineractFeignClient fineractClient;

    @BeforeEach
    public void setup() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    }

    @Test
    void updateTaxGroup_allowsEditingGroupWhoseComponentHasEnded() {
        final Long taxComponentId = createTaxComponent();
        final Long taxGroupId = createTaxGroup(taxComponentId, "01 January 2023");
        final Long mappingId = association(taxGroupId, taxComponentId).getId();
        BusinessDateHelper.runAt("01 January 2024", () -> ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId,
                updateRequest(new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).endDate("15 January 2024")))));

        BusinessDateHelper.runAt("01 February 2024", () -> {
            final String newName = Utils.randomStringGenerator("TAX_GRP_", 6);
            ok(() -> fineractClient.taxGroup().updateTaxGroup(taxGroupId,
                    updateRequest(new PutTaxesGroupTaxComponents().id(mappingId).taxComponentId(taxComponentId).endDate("15 January 2024"))
                            .name(newName)));

            final GetTaxesGroupResponse taxGroup = ok(() -> fineractClient.taxGroup().retrieveOneTaxGroup(taxGroupId));
            assertEquals(newName, taxGroup.getName());
            assertEquals(1, taxGroup.getTaxAssociations().size());
        });
    }

    private Long createTaxComponent() {
        final Account glAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
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

    private GetTaxesGroupTaxAssociations association(final Long taxGroupId, final Long taxComponentId) {
        final GetTaxesGroupResponse taxGroup = ok(() -> fineractClient.taxGroup().retrieveOneTaxGroup(taxGroupId));
        return taxGroup.getTaxAssociations().stream().filter(a -> taxComponentId.equals(a.getTaxComponent().getId())).findFirst()
                .orElseThrow();
    }

    private PutTaxesGroupTaxGroupIdRequest updateRequest(final PutTaxesGroupTaxComponents... taxComponents) {
        return new PutTaxesGroupTaxGroupIdRequest().taxComponents(new HashSet<>(Arrays.asList(taxComponents))).dateFormat(Utils.DATE_FORMAT)
                .locale(Utils.LOCALE);
    }
}
