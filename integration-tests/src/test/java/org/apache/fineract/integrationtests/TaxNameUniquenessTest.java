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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutTaxesComponentsTaxComponentIdRequest;
import org.apache.fineract.client.models.PutTaxesGroupTaxGroupIdRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxNameUniquenessTest {

    private FineractFeignClient fineractClient;

    @BeforeEach
    public void setup() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    }

    @Test
    void createTaxComponentWithExistingName_shouldBeRejected() {
        final String name = Utils.randomStringGenerator("TAX_CMP_", 6);
        assertNotNull(createTaxComponent(name));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.taxComponents().createTaxComponent(taxComponentRequest(name)));

        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.component.duplicate.name"));
    }

    @Test
    void renameTaxComponentToExistingName_shouldBeRejected() {
        final String name = Utils.randomStringGenerator("TAX_CMP_", 6);
        createTaxComponent(name);
        final Long otherId = createTaxComponent(Utils.randomStringGenerator("TAX_CMP_", 6));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.taxComponents().updateTaxComponent(otherId, new PutTaxesComponentsTaxComponentIdRequest().name(name)));

        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.component.duplicate.name"));
    }

    @Test
    void createTaxGroupWithExistingName_shouldBeRejected() {
        final String name = Utils.randomStringGenerator("TAX_GRP_", 6);
        final Long taxComponentId = createTaxComponent(Utils.randomStringGenerator("TAX_CMP_", 6));
        assertNotNull(createTaxGroup(name, taxComponentId));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.taxGroup().createTaxGroup(taxGroupRequest(name, taxComponentId)));

        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.group.duplicate.name"));
    }

    @Test
    void renameTaxGroupToExistingName_shouldBeRejected() {
        final String name = Utils.randomStringGenerator("TAX_GRP_", 6);
        final Long taxComponentId = createTaxComponent(Utils.randomStringGenerator("TAX_CMP_", 6));
        createTaxGroup(name, taxComponentId);
        final Long otherGroupId = createTaxGroup(Utils.randomStringGenerator("TAX_GRP_", 6), taxComponentId);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.taxGroup().updateTaxGroup(otherGroupId,
                new PutTaxesGroupTaxGroupIdRequest().name(name).taxComponents(new HashSet<>())));

        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.group.duplicate.name"));
    }

    private Long createTaxComponent(final String name) {
        return ok(() -> fineractClient.taxComponents().createTaxComponent(taxComponentRequest(name))).getResourceId();
    }

    private PostTaxesComponentsRequest taxComponentRequest(final String name) {
        final Account glAccount = AccountHelper.createLiabilityGlAccount("taxComponent");
        return new PostTaxesComponentsRequest().name(name).percentage(5.0f).startDate("01 January 2023")
                .creditAccountType(Integer.valueOf(glAccount.getAccountType().toString()))
                .creditAccountId(glAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);
    }

    private Long createTaxGroup(final String name, final Long taxComponentId) {
        return ok(() -> fineractClient.taxGroup().createTaxGroup(taxGroupRequest(name, taxComponentId))).getResourceId();
    }

    private PostTaxesGroupRequest taxGroupRequest(final String name, final Long taxComponentId) {
        return new PostTaxesGroupRequest().name(name)
                .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate("01 January 2023")))
                .dateFormat("dd MMMM yyyy").locale("en");
    }
}
