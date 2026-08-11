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
import java.util.List;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.RateData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRateHelper;
import org.apache.fineract.integrationtests.client.feign.modules.RateRequestBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RatesTest extends FeignIntegrationTest {

    private static final String PERCENTAGE_CHANGE = "percentage";

    private FeignRateHelper rateHelper;

    @BeforeAll
    public void setup() {
        rateHelper = new FeignRateHelper(fineractClient());
    }

    @Test
    public void testRatesForLoans() {

        final CommandProcessingResult createResponse = rateHelper.createRate(RateRequestBuilders.loanRate());
        final Long loanRateId = createResponse.getResourceId();
        Assertions.assertNotNull(loanRateId);

        final List<RateData> allRates = rateHelper.retrieveAllRates();
        Assertions.assertTrue(allRates.stream().anyMatch(rate -> loanRateId.equals(rate.getId())),
                "Rate " + loanRateId + " is missing from the rate listing");

        final CommandProcessingResult updateResponse = rateHelper.updateRate(loanRateId, RateRequestBuilders.modifyRatePercentage());
        Assertions.assertEquals(loanRateId, updateResponse.getResourceId());

        final BigDecimal changedPercentage = new BigDecimal(updateResponse.getChanges().get(PERCENTAGE_CHANGE).toString());
        Assertions.assertEquals(0, changedPercentage.compareTo(RateRequestBuilders.MODIFIED_PERCENTAGE),
                "Verifying the percentage the update command reported as changed");

        final RateData rateAfterUpdate = rateHelper.retrieveRate(loanRateId);
        Assertions.assertEquals(0, rateAfterUpdate.getPercentage().compareTo(changedPercentage), "Verifying Rate after modification");

    }

}
