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
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.rates.RatesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RatesTest {

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    public void testRatesForLoans() {

        // Retrieving all Rates
        List<RateData> allRatesData = RatesHelper.getRates();
        Assertions.assertNotNull(allRatesData);

        // Testing Creation and Update of Loan Rate
        final CommandProcessingResult createResponse = RatesHelper.createRates(RatesHelper.getLoanRateRequest());
        final Long loanRateId = createResponse.getResourceId();
        Assertions.assertNotNull(loanRateId);

        // Update Rate percentage
        final CommandProcessingResult changes = RatesHelper.updateRates(loanRateId, RatesHelper.getModifyRateRequest());

        final RateData rateDataAfterChanges = RatesHelper.getRateById(loanRateId);
        final BigDecimal changedPercentage = new BigDecimal(changes.getChanges().get("percentage").toString());
        Assertions.assertEquals(0, rateDataAfterChanges.getPercentage().compareTo(changedPercentage), "Verifying Rate after modification");

    }

}
