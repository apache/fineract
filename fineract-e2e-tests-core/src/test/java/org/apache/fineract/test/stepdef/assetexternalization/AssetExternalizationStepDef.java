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
package org.apache.fineract.test.stepdef.assetexternalization;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.JournalEntryData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.AssetExternalizationErrorMessage;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class AssetExternalizationStepDef extends AbstractStepDef {

    public static final String OWNER_EXTERNAL_ID_PREFIX = "TestOwner-";
    public static final String DATE_FORMAT_ASSET_EXT = "yyyy-MM-dd";
    public static final String DEFAULT_LOCALE = "en";
    public static final String TRANSACTION_TYPE_SALE = "sale";
    public static final String TRANSACTION_TYPE_BUYBACK = "buyback";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_ASSET_EXT);
    private static final Gson GSON = new JSON().getGson();

    @Autowired
    private ExternalAssetOwnersApi externalAssetOwnersApi;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @When("Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanIdUserGeneratedExtId(DataTable table) throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);
        if (transferExternalId == null) {
            transferExternalId = Utils.randomNameGenerator("TestTransferExtId_", 3);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED, transferExternalId);
        }

        createAssetExternalizationRequestByLoanId(table, transferExternalId);
    }

    @When("Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanIdSystemGeneratedExtId(DataTable table) throws IOException {
        createAssetExternalizationRequestByLoanId(table, null);
    }

    private void createAssetExternalizationRequestByLoanId(DataTable table, String transferExternalId) throws IOException {
        createAssetExternalizationRequestByLoanId(table, transferExternalId, true);
    }

    private void createAssetExternalizationRequestByLoanId(DataTable table, String transferExternalId, boolean regenerateOwner)
            throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostInitiateTransferRequest request = new PostInitiateTransferRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                    .transferRequestWithLoanId(loanId, request, transferData.get(0)).execute();
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.body().getResourceExternalId());
            ErrorHelper.checkSuccessfulApiCall(response);

        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId;
            if (regenerateOwner) {
                ownerExternalId = Utils.randomNameGenerator(OWNER_EXTERNAL_ID_PREFIX, 3);
            } else {
                ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
            }

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(transferExternalId)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                    .transferRequestWithLoanId(loanId, request, transferData.get(0)).execute();
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.body().getResourceExternalId());
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID, ownerExternalId);
            ErrorHelper.checkSuccessfulApiCall(response);

        } else {
            throw new IllegalStateException(String.format("%s is not supported Asset externalization transaction", transferData.get(0)));
        }
    }

    @When("Admin makes asset externalization BUYBACK request with ownerExternalId = null and settlement date {string} by Loan ID with system-generated transferExternalId")
    public void createAssetExternalizationBuybackRequestOwnerNullByLoanIdSystemGeneratedExtId(String settlementDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostInitiateTransferRequest request = new PostInitiateTransferRequest()//
                .settlementDate(settlementDate)//
                .ownerExternalId(null)//
                .transferExternalId(testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_FROM_RESPONSE))//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                .transferRequestWithLoanId(loanId, request, TRANSACTION_TYPE_BUYBACK).execute();
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                response.body().getResourceExternalId());
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @When("Admin makes asset externalization request by Loan external ID with unique ownerExternalId, user-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanExternalIdUserGeneratedExtId(DataTable table) throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);
        if (transferExternalId == null) {
            transferExternalId = Utils.randomNameGenerator("TestTransferExtId_", 3);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED, transferExternalId);
        }

        createAssetExternalizationRequestByLoanExternalId(table, transferExternalId);
    }

    @When("Admin makes asset externalization request by Loan external ID with unique ownerExternalId, system-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanExternalIdSystemGeneratedExtId(DataTable table) throws IOException {
        createAssetExternalizationRequestByLoanExternalId(table, null);
    }

    private void createAssetExternalizationRequestByLoanExternalId(DataTable table, String transferExternalId) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();

        PostInitiateTransferRequest request = new PostInitiateTransferRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                    .transferRequestWithLoanExternalId(loanExternalId, request, transferData.get(0)).execute();
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.body().getResourceExternalId());
            ErrorHelper.checkSuccessfulApiCall(response);

        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId = Utils.randomNameGenerator(OWNER_EXTERNAL_ID_PREFIX, 3);

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(transferExternalId)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                    .transferRequestWithLoanExternalId(loanExternalId, request, transferData.get(0)).execute();
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.body().getResourceExternalId());
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID, ownerExternalId);
            ErrorHelper.checkSuccessfulApiCall(response);

        } else {
            throw new IllegalStateException(String.format("%s is not supported Asset externalization transaction", transferData.get(0)));
        }
    }

    @Then("Asset externalization response has the correct Loan ID, transferExternalId")
    public void checkAssetExternalizationResponse() {
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        String transferExternalIdExpected = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostInitiateTransferResponse> response = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        PostInitiateTransferResponse body = response.body();
        Long loanIdActual = body.getSubResourceId();
        String transferExternalIdActual = body.getResourceExternalId();

        log.debug("loanId: {}", loanId);
        log.debug("ownerExternalIdStored: {}", ownerExternalIdStored);
        log.debug("transferExternalId generated by user: {}", transferExternalIdExpected);
        log.debug("transferExternalIdActual: {}", transferExternalIdActual);

        assertThat(loanIdActual).as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(loanIdActual, loanId)).isEqualTo(loanId);
        assertThat(body.getResourceId()).isNotNull();
        if (transferExternalIdExpected != null) {
            assertThat(transferExternalIdActual)
                    .as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(transferExternalIdActual, transferExternalIdExpected))
                    .isEqualTo(transferExternalIdExpected);
        } else {
            assertThat(transferExternalIdActual).isNotEmpty();
        }
    }

    @Then("Fetching Asset externalization details by loan id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanId(int numberOfElements, DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        checkExternalAssetDetails(loanId, null, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by loan external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanExternalId(int numberOfElements, DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();

        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, null, loanExternalId, null, null).execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        checkExternalAssetDetails(null, loanExternalId, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by transfer external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByTransferExternalId(int numberOfElements, DataTable table) throws IOException {
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);

        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(transferExternalId, null, null, null, null)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        checkExternalAssetDetails(null, null, response, numberOfElements, table);
    }

    @Then("Asset externalization details has the generated transferExternalId")
    public void checkGeneratedTransferExternalId() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostInitiateTransferResponse> assetExtResponse = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        String transferExternalIdExpected = assetExtResponse.body().getResourceExternalId();

        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        List<ExternalTransferData> content = response.body().getContent();

        content.forEach(e -> {
            assertThat(e.getTransferExternalId()).as(ErrorMessageHelper
                    .wrongDataInAssetExternalizationTransferExternalId(e.getTransferExternalId(), transferExternalIdExpected))
                    .isEqualTo(transferExternalIdExpected);
        });
    }

    private void checkExternalAssetDetails(Long loanId, String loanExternalId, Response<PageExternalTransferData> response,
            int numberOfElements, DataTable table) {
        PageExternalTransferData body = response.body();
        Integer numberOfElementsActual = body.getNumberOfElements();
        List<ExternalTransferData> content = body.getContent();

        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        String transferExternalId;

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);

            String transactionType = expectedValues.get(5);
            if (transactionType.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())) {
                transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
            } else {
                transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
            }

            expectedValues.add(ownerExternalIdStored);
            expectedValues.add(loanId == null ? null : String.valueOf(loanId));
            expectedValues.add(loanExternalId);
            expectedValues.add(transferExternalId);

            List<List<String>> actualValuesList = content.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getSettlementDate() == null ? null : FORMATTER.format(t.getSettlementDate()));
                actualValues.add(t.getPurchasePriceRatio() == null ? null : t.getPurchasePriceRatio());
                actualValues.add(t.getStatus() == null ? null : String.valueOf(t.getStatus()));
                actualValues.add(t.getEffectiveFrom() == null ? null : FORMATTER.format(t.getEffectiveFrom()));
                actualValues.add(t.getEffectiveTo() == null ? null : FORMATTER.format(t.getEffectiveTo()));
                actualValues.add(transactionType);
                actualValues.add(t.getOwner().getExternalId() == null ? null : t.getOwner().getExternalId());
                actualValues.add(loanId == null ? null : String.valueOf(t.getLoan().getLoanId()));
                actualValues.add(loanExternalId == null ? null : t.getLoan().getExternalId());
                actualValues.add(t.getTransferExternalId());
                return actualValues;
            }).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

            assertThat(numberOfElementsActual)
                    .as(ErrorMessageHelper.wrongTotalFilteredRecordsInAssetExternalizationDetails(numberOfElementsActual, numberOfElements))
                    .isEqualTo(numberOfElements);
            assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInExternalAssetDetails(i, actualValuesList, expectedValues))
                    .isTrue();
        }
    }

    @Then("BUYBACK transaction results a {int} error and proper error message when its settlementDate is earlier than the original settlementDate")
    public void buybackDateError(int errorCodeExpected, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);

        PostInitiateTransferRequest request = new PostInitiateTransferRequest()//
                .settlementDate(transferData.get(1))//
                .transferExternalId(transferExternalId)//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                .transferRequestWithLoanId(loanId, request, transferData.get(0)).execute();

        PageExternalTransferData transfers = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute().body();
        String settlementDateOriginal = FORMATTER.format(transfers.getContent().get(0).getSettlementDate());
        String errorMessageExpected = String.format(
                "This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                settlementDateOriginal);

        String errorToString = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getDeveloperMessage();
        int errorCodeActual = response.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("Asset externalization transaction with the following data results a {int} error and {string} error message")
    public void transactionError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostInitiateTransferRequest request = new PostInitiateTransferRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(null)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//
        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId = Utils.randomNameGenerator(OWNER_EXTERNAL_ID_PREFIX, 3);

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(null)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID, ownerExternalId);
        } else {
            throw new IllegalStateException(String.format("%s is not supported Asset externalization transaction", transferData.get(0)));
        }

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                .transferRequestWithLoanId(loanId, request, transferData.get(0)).execute();

        AssetExternalizationErrorMessage errorMsgType = AssetExternalizationErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = errorMsgType.getValue();

        String errorToString = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getDeveloperMessage();
        int errorCodeActual = response.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("Asset externalization SALES transaction with ownerExternalId = null and the following data results a {int} error and {string} error message")
    public void transactionErrorSalesOwnerNull(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostInitiateTransferRequest request = new PostInitiateTransferRequest()//
                .settlementDate(transferData.get(0))//
                .ownerExternalId(null)//
                .transferExternalId(null)//
                .purchasePriceRatio(transferData.get(1))//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi
                .transferRequestWithLoanId(loanId, request, TRANSACTION_TYPE_SALE).execute();

        AssetExternalizationErrorMessage errorMsgType = AssetExternalizationErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = errorMsgType.getValue();

        String errorToString = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getDeveloperMessage();
        int errorCodeActual = response.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("The latest asset externalization transaction with {string} status has the following TRANSFER Journal entries:")
    public void checkJournalEntriesTransaction(String status, DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Long lastTransferIdByStatus = getLastTransferIdByStatus(loanId, status);

        Response<ExternalOwnerTransferJournalEntryData> journalEntriesOfTransfer = externalAssetOwnersApi
                .getJournalEntriesOfTransfer(lastTransferIdByStatus, null, null).execute();
        List<JournalEntryData> content = journalEntriesOfTransfer.body().getJournalEntryData().getContent();

        List<List<String>> data = table.asLists();
        int linesExpected = data.size() - 1;

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            List<List<String>> actualValuesList = content.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getGlAccountType().getValue());
                actualValues.add(t.getGlAccountCode());
                actualValues.add(t.getGlAccountName());
                actualValues.add(t.getEntryType().getValue());
                actualValues.add(t.getAmount().setScale(2, RoundingMode.HALF_DOWN).toString());
                return actualValues;
            }).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInAssetExternalizationJournalEntry(i, actualValuesList, expectedValues))
                    .isTrue();

            int linesActual = journalEntriesOfTransfer.body().getJournalEntryData().getNumberOfElements();
            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInAssetExternalizationJournalEntry(linesActual, linesExpected))
                    .isEqualTo(linesExpected);
        }

        log.debug("loanId: {}", journalEntriesOfTransfer.body().getTransferData().getLoan().getLoanId());
        log.debug("ownerExternalId: {}", journalEntriesOfTransfer.body().getTransferData().getOwner().getExternalId());
        log.debug("transferId: {}", lastTransferIdByStatus);
        log.debug("transferExternalId: {}", journalEntriesOfTransfer.body().getTransferData().getTransferExternalId());
    }

    private Long getLastTransferIdByStatus(Long loanId, String status) throws IOException {
        Response<PageExternalTransferData> transfersResponse = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null)
                .execute();
        List<ExternalTransferData> content = transfersResponse.body().getContent();

        ExternalTransferData result = content.stream().filter(t -> status.equals(t.getStatus().getValue()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(String.format("No Journal entry found with status: %s", status)));

        return result.getTransferId();
    }

    private Long getLastTransferId(Long loanId) throws IOException {
        Response<PageExternalTransferData> transfersResponse = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null)
                .execute();
        List<ExternalTransferData> content = transfersResponse.body().getContent();
        ExternalTransferData result = content.stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("transfersResponse.body().getContent() is empty"));

        return result.getTransferId();
    }

    @Then("The asset external owner has the following OWNER Journal entries:")
    public void checkJournalEntriesOwner(DataTable table) throws IOException {
        String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        Response<ExternalOwnerJournalEntryData> journalEntriesOfOwner = externalAssetOwnersApi
                .getJournalEntriesOfOwner(ownerExternalId, null, null).execute();
        List<JournalEntryData> content = journalEntriesOfOwner.body().getJournalEntryData().getContent();

        List<List<String>> data = table.asLists();
        int linesExpected = data.size() - 1;

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            List<List<String>> actualValuesList = content.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getGlAccountType().getValue());
                actualValues.add(t.getGlAccountCode());
                actualValues.add(t.getGlAccountName());
                actualValues.add(t.getEntryType().getValue());
                actualValues.add(t.getAmount().setScale(2, RoundingMode.HALF_DOWN).toString());
                return actualValues;
            }).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInAssetExternalizationJournalEntry(i, actualValuesList, expectedValues))
                    .isTrue();

            int linesActual = journalEntriesOfOwner.body().getJournalEntryData().getNumberOfElements();
            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInAssetExternalizationJournalEntry(linesActual, linesExpected))
                    .isEqualTo(linesExpected);
        }

        log.debug("ownerExternalId: {}", journalEntriesOfOwner.body().getOwnerData().getExternalId());
    }

    @Then("LoanOwnershipTransferBusinessEvent is created")
    public void loanOwnershipTransferBusinessEventCheck() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanOwnershipTransferBusinessEventCheck(loanId, transferId);
    }

    @Then("LoanOwnershipTransferBusinessEvent with transfer status: {string} and transfer status reason {string} is created")
    public void loanOwnershipTransferBusinessEventCheck(String transferStatus, String transferStatusReason) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanOwnershipTransferBusinessEventWithStatusCheck(loanId, transferId, transferStatus, transferStatusReason);
    }

    @Then("LoanAccountSnapshotBusinessEvent is created")
    public void loanAccountSnapshotBusinessEventCheck() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanAccountSnapshotBusinessEventCheck(loanId, transferId);
    }

    @Then("Asset externalization response {string} has the correct Loan ID, transferExternalId")
    public void checkAssetExternalizationResponse(String type) {
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        String transferExternalIdExpected = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_PREFIX + "_" + type);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostInitiateTransferResponse> response = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        PostInitiateTransferResponse body = response.body();
        Long loanIdActual = body.getSubResourceId();
        String transferExternalIdActual = body.getResourceExternalId();

        log.debug("loanId: {}", loanId);
        log.debug("ownerExternalIdStored: {}", ownerExternalIdStored);
        log.debug("transferExternalId generated by user: {}", transferExternalIdExpected);
        log.debug("transferExternalIdActual: {}", transferExternalIdActual);

        assertThat(loanIdActual).as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(loanIdActual, loanId)).isEqualTo(loanId);
        assertThat(body.getResourceId()).isNotNull();
        if (transferExternalIdExpected != null) {
            assertThat(transferExternalIdActual)
                    .as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(transferExternalIdActual, transferExternalIdExpected))
                    .isEqualTo(transferExternalIdExpected);
        } else {
            assertThat(transferExternalIdActual).isNotEmpty();
        }
    }

    @When("Admin makes asset externalization request for type {string} by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanIdUserGeneratedExtId(String type, DataTable table) throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);
        if (transferExternalId == null) {
            transferExternalId = Utils.randomNameGenerator("TestTransferExtId_", 3);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type, transferExternalId);
        }

        createAssetExternalizationRequestByLoanId(table, transferExternalId);
    }

    @When("Admin send {string} command to the transaction type {string}")
    public void adminTransactionCommandTheWithType(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);
        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi.transferRequestWithId1(transferExternalId, command)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @When("Admin send {string} command to the transaction type {string} will throw error")
    public void adminTransactionCommandTheWithTypeThrowError(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);
        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi.transferRequestWithId1(transferExternalId, command)
                .execute();
        ErrorHelper.checkFailedApiCall(response, 403);
    }

    @Then("Fetching Asset externalization details by loan id gives numberOfElements: {int} with correct ownerExternalId, ignore transactionExternalId and contain the following data:")
    public void checkAssetExternalizationDetailsByLoanIdIgnoreTransactionExternalId(int numberOfElements, DataTable table)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        checkExternalAssetDetailsIgnoreTransferExternalId(loanId, null, response, numberOfElements, table);
    }

    private void checkExternalAssetDetailsIgnoreTransferExternalId(Long loanId, String loanExternalId,
            Response<PageExternalTransferData> response, int numberOfElements, DataTable table) {
        PageExternalTransferData body = response.body();
        Integer numberOfElementsActual = body.getNumberOfElements();
        List<ExternalTransferData> content = body.getContent();

        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            expectedValues.add(ownerExternalIdStored);
            expectedValues.add(loanId == null ? null : String.valueOf(loanId));
            expectedValues.add(loanExternalId);

            List<List<String>> actualValuesList = content.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getSettlementDate() == null ? null : FORMATTER.format(t.getSettlementDate()));
                actualValues.add(t.getPurchasePriceRatio() == null ? null : t.getPurchasePriceRatio());
                actualValues.add(t.getStatus() == null ? null : String.valueOf(t.getStatus()));
                actualValues.add(t.getEffectiveFrom() == null ? null : FORMATTER.format(t.getEffectiveFrom()));
                actualValues.add(t.getEffectiveTo() == null ? null : FORMATTER.format(t.getEffectiveTo()));
                actualValues.add(t.getOwner().getExternalId() == null ? null : t.getOwner().getExternalId());
                actualValues.add(loanId == null ? null : String.valueOf(t.getLoan().getLoanId()));
                actualValues.add(loanExternalId == null ? null : t.getLoan().getExternalId());
                return actualValues;
            }).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

            assertThat(numberOfElementsActual)
                    .as(ErrorMessageHelper.wrongTotalFilteredRecordsInAssetExternalizationDetails(numberOfElementsActual, numberOfElements))
                    .isEqualTo(numberOfElements);
            assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInExternalAssetDetails(i, actualValuesList, expectedValues))
                    .isTrue();
        }
    }

    @When("Admin send {string} command on {string} transaction it will throw an error")
    public void adminSendCommandAndItWillThrowError(String command, String transactionType) throws IOException {
        String transferExternalId;
        if (transactionType.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())) {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        }

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi.transferRequestWithId1(transferExternalId, command)
                .execute();
        ErrorHelper.checkFailedApiCall(response, 403);
    }

    @When("Admin send {string} command on {string} transaction")
    public void adminSendCommand(String command, String transactionType) throws IOException {
        String transferExternalId;
        if (transactionType.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())) {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        }

        Response<PostInitiateTransferResponse> response = externalAssetOwnersApi.transferRequestWithId1(transferExternalId, command)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @When("Admin makes asset externalization request for type {string} by Loan ID with unique ownerExternalId, force generated transferExternalId and without change test owner with following data:")
    public void createAssetExternalizationRequestByLoanIdUserGeneratedExtIdForceTransferIdNoTestOwner(String type, DataTable table)
            throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = Utils.randomNameGenerator("TestTransferExtId_", 3);
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type, transferExternalId);

        createAssetExternalizationRequestByLoanId(table, transferExternalId, false);
    }

}
