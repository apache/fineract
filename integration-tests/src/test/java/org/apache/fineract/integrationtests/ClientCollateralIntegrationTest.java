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

import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.DEFAULT_ACTIVATION_DATE;

import java.math.BigDecimal;
import org.apache.fineract.client.models.ClientCollateralUpdateResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCollateralIntegrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCollateralIntegrationTest.class);

    private FeignClientHelper clientHelper;
    private FeignCollateralHelper collateralHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        collateralHelper = new FeignCollateralHelper(fineractClient());
    }

    @Test
    public void createClientCollateralTest() {
        LOG.info("-------------------------Creating Client Collateral---------------------------");
        final Long clientID = clientHelper.createClient(DEFAULT_ACTIVATION_DATE);
        Assertions.assertEquals(clientID, clientHelper.getClient(clientID).getId());

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
    }

    @Test
    public void updateClientCollateral() {
        LOG.info("-------------------------Updating Client Collateral---------------------------");
        final Long clientID = clientHelper.createClient(DEFAULT_ACTIVATION_DATE);
        Assertions.assertEquals(clientID, clientHelper.getClient(clientID).getId());

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);

        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        final ClientCollateralUpdateResponse response = collateralHelper.updateClientCollateralQuantity(clientID, clientCollateralId,
                BigDecimal.ONE);
        Assertions.assertEquals(0, BigDecimal.ONE.compareTo(response.getChanges().getQuantity()),
                () -> "Expected quantity 1 but was " + response.getChanges().getQuantity());
    }

}
