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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ExternalTransferOwnerData;
import org.apache.fineract.client.models.PostExternalAssetOwnerRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.investor.InvestorHelper;
import org.junit.jupiter.api.Test;

@Slf4j
public class ExternalAssetOwnerTest extends BaseLoanIntegrationTest {

    @Test
    public void testCreateExternalAssetOwnerSuccessfully() {
        final String ownerExternalId = Utils.uniqueRandomStringGenerator("eao", 20);
        final PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(ownerExternalId);

        final PostExternalAssetOwnerResponse response = InvestorHelper.createExternalAssetOwner(request);

        assertNotNull(response);
        assertNotNull(response.getResourceId());

        List<ExternalTransferOwnerData> externalAssetOwners = InvestorHelper.retrieveExternalAssetOwners();
        assertTrue(externalAssetOwners.size() > 0);
        Optional<ExternalTransferOwnerData> optExternalTransferOwnerData = externalAssetOwners.stream()
                .filter(eao -> eao.getExternalId().equals(ownerExternalId)).findFirst();
        assertTrue(optExternalTransferOwnerData.isPresent());
    }

    @Test
    public void testCreateExternalAssetOwnerFailsWhenOwnerExternalIdIsNull() {
        final PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> InvestorHelper.createExternalAssetOwner(request));
        assertEquals(400, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("validation.msg.externalAssetOwner.ownerExternalId.cannot.be.blank"));
    }

    @Test
    public void testCreateExternalAssetOwnerFailsWhenOwnerExternalIdIsDuplicate() {
        final String ownerExternalId = Utils.uniqueRandomStringGenerator("eao", 20);
        final PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(ownerExternalId);

        final PostExternalAssetOwnerResponse firstResponse = InvestorHelper.createExternalAssetOwner(request);
        assertNotNull(firstResponse);
        assertNotNull(firstResponse.getResourceId());

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> InvestorHelper.createExternalAssetOwner(request));
        assertEquals(403, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.provided.external.id.already.exists"));
    }
}
