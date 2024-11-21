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

import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.CANCELLED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.PENDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PagedRequestExternalAssetOwnerSearchRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

@Slf4j
public class SearchExternalAssetOwnerTransferTest extends ExternalAssetOwnerTransferTest {

    @Test
    public void saleActiveLoanToExternalAssetOwnerWithSearching() {
        final String baseDate = "2020-02-29";
        LocalDate baseLocalDate = Utils.getDateAsLocalDate("29 February 2020");

        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.of(2020, 2, 29));
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID, "29 February 2020");
            addPenaltyForLoan(loanID, "10");

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanID, baseDate);
            validateResponse(saleTransferResponse, loanID);

            // LookUp by ExternalId
            String externalId = saleTransferResponse.getResourceExternalId();
            PagedRequestExternalAssetOwnerSearchRequest searchRequest = EXTERNAL_ASSET_OWNER_HELPER
                    .buildExternalAssetOwnerSearchRequest(externalId, "", null, null, null, null);
            PageExternalTransferData response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);

            validateExternalAssetOwnerTransfer(response,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            // LookUp by Effective Date
            searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest("", "settlement", baseLocalDate, null, null,
                    null);
            response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);

            validateExternalAssetOwnerTransfer(response,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            // Cancel the External Asset Transfer
            EXTERNAL_ASSET_OWNER_HELPER.cancelTransferByTransferExternalId(saleTransferResponse.getResourceExternalId());
            searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(externalId, "", null, null, null, null);
            response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);

            validateExternalAssetOwnerTransfer(response,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            baseDate, false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            baseDate, false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")));

            // LookUp by Effective Date
            // LookUp by Effective Date
            searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest("", "effective", baseLocalDate, baseLocalDate,
                    null, null);
            response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);

            validateExternalAssetOwnerTransfer(response,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            baseDate, false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), baseDate, baseDate,
                            baseDate, false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")));

        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void initialSearchExternalAssetOwnerTransferUsingTextTest() {
        String textToSearch = UUID.randomUUID().toString();
        PagedRequestExternalAssetOwnerSearchRequest searchRequest = EXTERNAL_ASSET_OWNER_HELPER
                .buildExternalAssetOwnerSearchRequest(textToSearch, "", null, null, 0, 10);
        PageExternalTransferData response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        assertNotNull(response);
        assertEquals("Expecting none result", 0, response.getContent().size());

        // Search over the current Asset Transfers and get just the first five
        textToSearch = "";
        searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(textToSearch, "", null, null, 0, 5);
        response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        assertNotNull(response);
        assertEquals("Expecting first five results", 5, response.getContent().size());

        textToSearch = response.getContent().iterator().next().getOwner().getExternalId();
        searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(textToSearch, "", null, null, 0, 5);
        response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        assertNotNull(response);
        assertTrue("Expecting only two results", response.getContent().size() >= 2);
        assertEquals("External Id is different", textToSearch, response.getContent().iterator().next().getOwner().getExternalId());
    }

    @Test
    public void initialSearchExternalAssetOwnerTransferUsingEffectiveDateTest() {
        final String attribute = "effective";
        LocalDate fromDate = Utils.getDateAsLocalDate("01 March 2023");
        LocalDate toDate = fromDate.plusMonths(3);
        PagedRequestExternalAssetOwnerSearchRequest searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(null,
                attribute, fromDate, toDate, 0, null);
        PageExternalTransferData response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 0);

        fromDate = Utils.getDateAsLocalDate("01 January 2020");
        toDate = fromDate.plusMonths(6);
        searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(null, attribute, fromDate, toDate, 0, null);
        response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 1);
        assertTrue("Transfers were not found", response.getContent().size() > 0);
    }

    @Test
    public void initialSearchExternalAssetOwnerTransferUsingSubmittedDateTest() {
        final String attribute = "settlement";
        LocalDate fromDate = Utils.getDateAsLocalDate("01 March 2023");
        LocalDate toDate = fromDate.plusMonths(3);
        PagedRequestExternalAssetOwnerSearchRequest searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(null,
                attribute, fromDate, toDate, 0, null);
        PageExternalTransferData response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 0);

        fromDate = Utils.getDateAsLocalDate("01 February 2020");
        toDate = fromDate.plusMonths(3);
        searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(null, attribute, fromDate, toDate, 0, null);
        response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 1);
        assertTrue("Transfers were not found", response.getContent().size() > 0);
    }

    @Test
    public void initialSearchExternalAssetOwnerTransferUsingTextAndDatesTest() {
        final String textToSearch = UUID.randomUUID().toString();
        final String attribute = "settlement";
        LocalDate fromDate = Utils.getDateAsLocalDate("01 March 2023");
        LocalDate toDate = fromDate.plusMonths(3);
        PagedRequestExternalAssetOwnerSearchRequest searchRequest = EXTERNAL_ASSET_OWNER_HELPER
                .buildExternalAssetOwnerSearchRequest(textToSearch, attribute, fromDate, toDate, 0, null);
        PageExternalTransferData response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 0);

        fromDate = Utils.getDateAsLocalDate("01 February 2020");
        toDate = fromDate.plusMonths(3);
        searchRequest = EXTERNAL_ASSET_OWNER_HELPER.buildExternalAssetOwnerSearchRequest(textToSearch, attribute, fromDate, toDate, 0,
                null);
        response = EXTERNAL_ASSET_OWNER_HELPER.searchExternalAssetOwnerTransfer(searchRequest);
        validateResponse(response, 0);
    }

    private void validateResponse(PageExternalTransferData response, final Integer size) {
        assertNotNull(response);
        final boolean isEmpty = (size == 0);
        assertEquals(isEmpty, response.getEmpty());
        assertEquals(true, response.getFirst());
        if (isEmpty) {
            assertTrue("Transfers size difference", response.getContent().size() == size);
            assertTrue("Total pages difference", response.getTotalPages() == 0);
        } else {
            assertTrue("Total pages difference", response.getTotalPages() > 0);
            assertTrue("Total number of elements difference", response.getNumberOfElements() > 0);
        }
        assertEquals(true, response.getFirst());
    }

}
