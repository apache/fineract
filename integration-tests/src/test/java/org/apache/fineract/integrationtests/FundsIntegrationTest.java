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

import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.FundData;
import org.apache.fineract.client.models.FundRequest;
import org.apache.fineract.client.models.PutFundsFundIdRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFundHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Funds Integration Test for checking Funds Application.
 */
public class FundsIntegrationTest extends FeignIntegrationTest {

    private FeignFundHelper fundHelper;

    @BeforeAll
    public void setup() {
        fundHelper = new FeignFundHelper(fineractClient());
    }

    @Test
    public void testCreateFund() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithEmptyName() {
        CallFailedRuntimeException exception = fundHelper
                .createFundExpectingError(new FundRequest().externalId(UUID.randomUUID().toString()));
        Assertions.assertEquals(400, exception.getStatus());
    }

    @Test
    public void testCreateFundWithEmptyExternalId() {
        final Long fundID = createFund(new FundRequest().name(Utils.uniqueRandomStringGenerator("", 10)));
        Assertions.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithDuplicateName() {
        FundRequest fund = validFund();
        final Long fundID = createFund(fund);
        Assertions.assertNotNull(fundID);

        CallFailedRuntimeException exception = fundHelper
                .createFundExpectingError(new FundRequest().name(fund.getName()).externalId(UUID.randomUUID().toString()));
        Assertions.assertEquals(403, exception.getStatus());
    }

    @Test
    public void testCreateFundWithDuplicateExternalId() {
        FundRequest fund = validFund();
        final Long fundID = createFund(fund);
        Assertions.assertNotNull(fundID);

        CallFailedRuntimeException exception = fundHelper.createFundExpectingError(
                new FundRequest().name(Utils.uniqueRandomStringGenerator("", 10)).externalId(fund.getExternalId()));
        Assertions.assertEquals(403, exception.getStatus());
    }

    @Test
    public void testCreateFundWithInvalidName() {
        CallFailedRuntimeException exception = fundHelper.createFundExpectingError(
                new FundRequest().name(Utils.randomStringGenerator("", 120)).externalId(UUID.randomUUID().toString()));
        Assertions.assertEquals(400, exception.getStatus());
    }

    @Test
    public void testCreateFundWithInvalidExternalId() {
        CallFailedRuntimeException exception = fundHelper.createFundExpectingError(
                new FundRequest().name(Utils.uniqueRandomStringGenerator("", 10)).externalId(Utils.randomStringGenerator("fund-", 120)));
        Assertions.assertEquals(400, exception.getStatus());
    }

    @Test
    public void testRetrieveFund() {
        FundRequest fund = validFund();
        final Long fundID = createFund(fund);
        Assertions.assertNotNull(fundID);

        FundData retrieved = fundHelper.retrieveFund(fundID);

        Assertions.assertEquals(fund.getName(), retrieved.getName());
    }

    @Test
    public void testRetrieveAllFunds() {
        FundRequest fund = validFund();
        final Long fundID = createFund(fund);
        Assertions.assertNotNull(fundID);

        List<FundData> funds = fundHelper.retrieveAllFunds();

        Assertions.assertNotNull(funds);
        Assertions.assertFalse(funds.isEmpty());
        Assertions.assertTrue(funds.stream().anyMatch(retrieved -> fundID.equals(retrieved.getId())
                && fund.getName().equals(retrieved.getName()) && fund.getExternalId().equals(retrieved.getExternalId())));
    }

    @Test
    public void testRetrieveUnknownFund() {
        CallFailedRuntimeException exception = fundHelper.retrieveFundExpectingError(Long.MAX_VALUE);
        Assertions.assertEquals(404, exception.getStatus());
        Assertions.assertEquals("error.msg.resource.not.found", FeignErrors.errorGlobalisationCode(exception));
    }

    @Test
    public void testUpdateFund() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);

        String newName = Utils.uniqueRandomStringGenerator("", 10);
        String newExternalId = UUID.randomUUID().toString();
        PutFundsFundIdRequest changes = fundHelper.updateFund(fundID, new FundRequest().name(newName).externalId(newExternalId))
                .getChanges();

        Assertions.assertEquals(newName, changes.getName());
        Assertions.assertEquals(newExternalId, changes.getExternalId());
    }

    @Test
    public void testUpdateUnknownFund() {
        String newName = Utils.uniqueRandomStringGenerator("", 10);
        String newExternalId = UUID.randomUUID().toString();
        CallFailedRuntimeException exception = fundHelper.updateFundExpectingError(Long.MAX_VALUE,
                new FundRequest().name(newName).externalId(newExternalId));
        Assertions.assertEquals(404, exception.getStatus());
    }

    @Test
    public void testUpdateFundWithInvalidNewName() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);

        String newName = Utils.randomStringGenerator("", 120);
        String newExternalId = UUID.randomUUID().toString();
        CallFailedRuntimeException exception = fundHelper.updateFundExpectingError(fundID,
                new FundRequest().name(newName).externalId(newExternalId));

        Assertions.assertEquals(400, exception.getStatus());
    }

    @Test
    public void testUpdateFundWithNewExternalId() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);

        String newExternalId = UUID.randomUUID().toString();
        PutFundsFundIdRequest changes = fundHelper.updateFund(fundID, new FundRequest().externalId(newExternalId)).getChanges();

        Assertions.assertEquals(newExternalId, changes.getExternalId());
    }

    @Test
    public void testUpdateFundWithInvalidNewExternalId() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);

        String newName = Utils.uniqueRandomStringGenerator("", 10);
        String newExternalId = Utils.randomStringGenerator("fund-", 120);
        CallFailedRuntimeException exception = fundHelper.updateFundExpectingError(fundID,
                new FundRequest().name(newName).externalId(newExternalId));

        Assertions.assertEquals(400, exception.getStatus());
    }

    @Test
    public void testUpdateFundWithNewName() {
        final Long fundID = createFund(validFund());
        Assertions.assertNotNull(fundID);

        String newName = Utils.uniqueRandomStringGenerator("", 10);
        PutFundsFundIdRequest changes = fundHelper.updateFund(fundID, new FundRequest().name(newName)).getChanges();

        Assertions.assertEquals(newName, changes.getName());
    }

    @Test
    public void testUpdateFundWithEmptyParams() {
        FundRequest fund = validFund();
        final Long fundID = createFund(fund);
        Assertions.assertNotNull(fundID);

        PutFundsFundIdRequest changes = fundHelper.updateFund(fundID, new FundRequest()).getChanges();

        Assertions.assertNull(changes.getName());
        Assertions.assertNull(changes.getExternalId());

        // assert that there was no change in
        // the name and external ID of the fund
        FundData retrieved = fundHelper.retrieveFund(fundID);

        Assertions.assertEquals(fund.getName(), retrieved.getName());
        Assertions.assertEquals(fund.getExternalId(), retrieved.getExternalId());
    }

    private Long createFund(FundRequest request) {
        return fundHelper.createFund(request).getResourceId();
    }

    private static FundRequest validFund() {
        return new FundRequest().name(Utils.uniqueRandomStringGenerator("", 10)).externalId(UUID.randomUUID().toString());
    }
}
