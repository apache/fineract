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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.ExternalAssetOwnerLoanProductAttributesApi;
import org.apache.fineract.client.feign.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.feign.services.LoanProductsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesTemplateData;
import org.apache.fineract.client.models.ExternalTransferOwnerData;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.JournalEntryData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerResponse;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.data.AssetExternalizationErrorMessage;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanOwnershipTransferEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class AssetExternalizationStepDef extends AbstractStepDef {

    public static final String OWNER_EXTERNAL_ID_PREFIX = "TestOwner-";
    public static final String DATE_FORMAT_ASSET_EXT = "yyyy-MM-dd";
    public static final String DEFAULT_LOCALE = "en";
    public static final String TRANSACTION_TYPE_SALE = "sale";
    public static final String TRANSACTION_TYPE_BUYBACK = "buyback";
    public static final String TRANSACTION_TYPE_INTERMEDIARY_SALE = "intermediarySale";
    public static final String COMMAND = "command";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_ASSET_EXT);
    private static final long NON_EXISTING_LOAN_PRODUCT_ID = 999_999_999L;

    private final FineractFeignClient fineractFeignClient;
    private final EventCheckHelper eventCheckHelper;
    private final EventAssertion eventAssertion;
    private final FineractClientConfiguration fineractClientConfiguration;
    private final LoanProductsRequestFactory loanProductsRequestFactory;

    private ExternalAssetOwnersApi externalAssetOwnersApi() {
        return fineractFeignClient.externalAssetOwners();
    }

    private LoanProductsApi loanProductsApi() {
        return fineractFeignClient.loanProducts();
    }

    private ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributesApi() {
        return fineractFeignClient.externalAssetOwnerLoanProductAttributes();
    }

    @When("Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanIdUserGeneratedExtId(DataTable table) throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);
        if (transferExternalId == null) {
            transferExternalId = Utils.randomStringGenerator("TestTransferExtId_", 10);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED, transferExternalId);
        }

        createAssetExternalizationRequestByLoanId(table, transferExternalId);
    }

    @When("Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanIdSystemGeneratedExtId(DataTable table) {
        createAssetExternalizationRequestByLoanId(table, null);
    }

    private void createAssetExternalizationRequestByLoanId(DataTable table, String transferExternalId) {
        createAssetExternalizationRequestByLoanId(table, transferExternalId, true);
    }

    private void createAssetExternalizationRequestByLoanId(DataTable table, String transferExternalId, boolean regenerateOwner) {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanId(loanId, request,
                    Map.of(COMMAND, transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());

        } else if ((transferData.get(0).equals(TRANSACTION_TYPE_SALE) || transferData.get(0).equals(TRANSACTION_TYPE_INTERMEDIARY_SALE))) {
            String ownerExternalId;
            if (regenerateOwner) {
                // For owner-to-owner transfers: preserve the current owner as previous owner
                String currentOwner = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
                if (currentOwner != null && !transferData.get(0).equals(TRANSACTION_TYPE_INTERMEDIARY_SALE)) {
                    testContext().set(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID, currentOwner);
                }
                ownerExternalId = Utils.randomStringGenerator(OWNER_EXTERNAL_ID_PREFIX, 10);
            } else {
                ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
            }

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(transferExternalId)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanId(loanId, request,
                    Map.of(COMMAND, transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID, ownerExternalId);
            if (transferData.get(0).equals(TRANSACTION_TYPE_INTERMEDIARY_SALE)) {
                assertThat(ownerExternalId).isNotNull();
                testContext().set(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID, ownerExternalId);
                testContext().set(TestContextKey.ASSET_EXTERNALIZATION_INTERMEDIARY_SALE_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                        response.getResourceExternalId());
            }
        } else {
            throw new IllegalStateException(String.format("%s is not supported Asset externalization transaction", transferData.get(0)));
        }
    }

    @When("Admin makes asset externalization BUYBACK request with ownerExternalId = null and settlement date {string} by Loan ID with system-generated transferExternalId")
    public void createAssetExternalizationBuybackRequestOwnerNullByLoanIdSystemGeneratedExtId(String settlementDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest()//
                .settlementDate(settlementDate)//
                .ownerExternalId(null)//
                .transferExternalId(testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_FROM_RESPONSE))//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanId(loanId, request,
                Map.of(COMMAND, TRANSACTION_TYPE_BUYBACK));
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                response.getResourceExternalId());
    }

    @When("Admin makes asset externalization request by Loan external ID with unique ownerExternalId, user-generated transferExternalId and the following data:")
    public void createAssetExternalizationRequestByLoanExternalIdUserGeneratedExtId(DataTable table) throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);
        if (transferExternalId == null) {
            transferExternalId = Utils.randomStringGenerator("TestTransferExtId_", 10);
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

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanExternalId(loanExternalId, request,
                    Map.of(COMMAND, transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());
        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId = Utils.randomStringGenerator(OWNER_EXTERNAL_ID_PREFIX, 10);

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(transferExternalId)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanExternalId(loanExternalId, request,
                    Map.of(COMMAND, transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID, ownerExternalId);
        } else {
            throw new IllegalStateException(String.format("%s is not supported Asset externalization transaction", transferData.get(0)));
        }
    }

    @Then("Asset externalization response has the correct Loan ID, transferExternalId")
    public void checkAssetExternalizationResponse() {
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        String transferExternalIdExpected = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostInitiateTransferResponse response = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        Long loanIdActual = response.getSubResourceId();
        String transferExternalIdActual = response.getResourceExternalId();

        logAssetExternalizationResponseDetails(loanId, ownerExternalIdStored, transferExternalIdExpected, transferExternalIdActual);

        assertThat(loanIdActual).as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(loanIdActual, loanId)).isEqualTo(loanId);
        assertThat(response.getResourceId()).isNotNull();
        if (transferExternalIdExpected != null) {
            assertThat(transferExternalIdActual)
                    .as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(transferExternalIdActual, transferExternalIdExpected))
                    .isEqualTo(transferExternalIdExpected);
        } else {
            assertThat(transferExternalIdActual).isNotEmpty();
        }
    }

    private void logAssetExternalizationResponseDetails(long loanId, String ownerExternalIdStored, String transferExternalIdExpected,
            String transferExternalIdActual) {
        log.debug("loanId: {}", loanId);
        log.debug("ownerExternalIdStored: {}", ownerExternalIdStored);
        log.debug("transferExternalId generated by user: {}", transferExternalIdExpected);
        log.debug("transferExternalIdActual: {}", transferExternalIdActual);
    }

    @Then("Fetching Asset externalization details by loan id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanId(int numberOfElements, DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));

        checkExternalAssetDetails(loanId, null, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by loan external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanExternalId(int numberOfElements, DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanExternalId", loanExternalId));
        checkExternalAssetDetails(null, loanExternalId, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by transfer external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByTransferExternalId(int numberOfElements, DataTable table) {
        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("transferExternalId", transferExternalId),
                Map.of());
        checkExternalAssetDetails(null, null, response, numberOfElements, table);
    }

    @Then("Asset externalization details has the generated transferExternalId")
    public void checkGeneratedTransferExternalId() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostInitiateTransferResponse assetExtResponse = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        String transferExternalIdExpected = assetExtResponse.getResourceExternalId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        List<ExternalTransferData> content = response.getContent();

        content.forEach(e -> {
            assertThat(e.getTransferExternalId()).as(ErrorMessageHelper
                    .wrongDataInAssetExternalizationTransferExternalId(e.getTransferExternalId(), transferExternalIdExpected))
                    .isEqualTo(transferExternalIdExpected);
        });
    }

    private void checkExternalAssetDetails(Long loanId, String loanExternalId, PageExternalTransferData response, int numberOfElements,
            DataTable table) {
        Integer numberOfElementsActual = response.getNumberOfElements();
        List<ExternalTransferData> content = response.getContent();

        String transferExternalId;
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        String ownerExternalId;
        String previousAssetOwner;
        String intermediarySaleAssetOwner = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);

            String transactionType = expectedValues.get(5);
            String status = expectedValues.get(2);

            // in case transfer has no previous intermediarySale transfer
            if (intermediarySaleAssetOwner == null) {
                if (transactionType.equalsIgnoreCase(TRANSACTION_TYPE_BUYBACK)
                        && status.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())) {
                    previousAssetOwner = ownerExternalIdStored;
                    ownerExternalId = ownerExternalIdStored;
                    transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                } else { // in case of sale or intermediarySale transfer
                    ownerExternalId = ownerExternalIdStored;
                    previousAssetOwner = null;
                    transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                }
            } else { // in case transfer has previous intermediarySale or owner-to-owner transfer
                if (transactionType.equalsIgnoreCase(TRANSACTION_TYPE_SALE)
                        && (status.equals(ExternalTransferData.StatusEnum.ACTIVE.getValue())
                                || status.equals(ExternalTransferData.StatusEnum.PENDING.getValue()))) {
                    ownerExternalId = ownerExternalIdStored;
                    previousAssetOwner = intermediarySaleAssetOwner;
                    transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                } else if (transactionType.equalsIgnoreCase(TRANSACTION_TYPE_SALE)
                        && (status.equals(ExternalTransferData.StatusEnum.DECLINED.getValue())
                                || status.equals(ExternalTransferData.StatusEnum.CANCELLED.getValue()))) {
                    // DECLINED and CANCELLED records have previousOwner = null in the API response
                    ownerExternalId = ownerExternalIdStored;
                    previousAssetOwner = null;
                    transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                } else if (transactionType.equalsIgnoreCase(TRANSACTION_TYPE_BUYBACK)
                        && (status.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())
                                || status.equals(ExternalTransferData.StatusEnum.BUYBACK_INTERMEDIATE.getValue()))) {
                    ownerExternalId = ownerExternalIdStored;
                    previousAssetOwner = ownerExternalIdStored;
                    transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                } else {
                    ownerExternalId = intermediarySaleAssetOwner;
                    previousAssetOwner = null;
                    transferExternalId = testContext()
                            .get(TestContextKey.ASSET_EXTERNALIZATION_INTERMEDIARY_SALE_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
                }
            }

            List<List<String>> actualValuesList = content.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getSettlementDate() == null ? null : FORMATTER.format(t.getSettlementDate()));
                actualValues.add(t.getPurchasePriceRatio() == null ? null : t.getPurchasePriceRatio());
                actualValues.add(t.getStatus() == null ? null : String.valueOf(t.getStatus()));
                actualValues.add(t.getEffectiveFrom() == null ? null : FORMATTER.format(t.getEffectiveFrom()));
                actualValues.add(t.getEffectiveTo() == null ? null : FORMATTER.format(t.getEffectiveTo()));
                actualValues.add(transactionType);
                if (expectedValues.size() > 6) {
                    actualValues.add(
                            t.getDetails() != null ? t.getDetails().getTotalOutstanding().setScale(2, RoundingMode.HALF_DOWN).toString()
                                    : null);
                }
                if (expectedValues.size() > 7) {
                    actualValues.add(t.getDetails() != null
                            ? t.getDetails().getTotalPrincipalOutstanding().setScale(2, RoundingMode.HALF_DOWN).toString()
                            : null);
                }
                if (expectedValues.size() > 8) {
                    actualValues.add(t.getDetails() != null
                            ? t.getDetails().getTotalInterestOutstanding().setScale(2, RoundingMode.HALF_DOWN).toString()
                            : null);
                }
                if (expectedValues.size() > 9) {
                    actualValues.add(t.getDetails() != null
                            ? t.getDetails().getTotalFeeChargesOutstanding().setScale(2, RoundingMode.HALF_DOWN).toString()
                            : null);
                }
                if (expectedValues.size() > 10) {
                    actualValues.add(t.getDetails() != null
                            ? t.getDetails().getTotalPenaltyChargesOutstanding().setScale(2, RoundingMode.HALF_DOWN).toString()
                            : null);
                }
                actualValues.add(t.getOwner().getExternalId() == null ? null : t.getOwner().getExternalId());
                actualValues.add(t.getPreviousOwner() == null ? null : t.getPreviousOwner().getExternalId());
                actualValues.add(loanId == null ? null : String.valueOf(t.getLoan().getLoanId()));
                actualValues.add(loanExternalId == null ? null : t.getLoan().getExternalId());
                actualValues.add(t.getTransferExternalId());
                return actualValues;
            }).collect(Collectors.toList());

            expectedValues.add(ownerExternalId);
            expectedValues.add(previousAssetOwner);
            expectedValues.add(loanId == null ? null : String.valueOf(loanId));
            expectedValues.add(loanExternalId);
            expectedValues.add(transferExternalId);

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

            assertThat(numberOfElementsActual)
                    .as(ErrorMessageHelper.wrongTotalFilteredRecordsInAssetExternalizationDetails(numberOfElementsActual, numberOfElements))
                    .isEqualTo(numberOfElements);
            assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInExternalAssetDetails(i, actualValuesList, expectedValues))
                    .isTrue();
        }
    }

    private void logErrorDetails(int errorCodeActual, String errorMessageActual) {
        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("BUYBACK transaction results a {int} error and proper error message when its settlementDate is earlier than the original settlementDate")
    public void buybackDateError(int errorCodeExpected, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        String transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);

        PageExternalTransferData transfers = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        String settlementDateOriginal = FORMATTER.format(transfers.getContent().get(0).getSettlementDate());
        String errorMessageExpected = String.format(
                "This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                settlementDateOriginal);

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest()//
                .settlementDate(transferData.get(1))//
                .transferExternalId(transferExternalId)//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of(COMMAND, transferData.get(0))));

        int errorCodeActual = exception.getStatus();
        String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .contains(errorMessageExpected);

        logErrorDetails(errorCodeActual, errorMessageActual);
    }

    @Then("Asset externalization transaction with the following data results a {int} error and {string} error message")
    public void transactionError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(null)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//
        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId = Utils.randomStringGenerator(OWNER_EXTERNAL_ID_PREFIX, 10);

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

        AssetExternalizationErrorMessage errorMsgType = AssetExternalizationErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = errorMsgType.getValue();

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of(COMMAND, transferData.get(0))));

        int errorCodeActual = exception.getStatus();
        String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        if (errorMessageType.equals("INVALID_REQUEST")) {
            assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                    .containsAnyOf("Validation errors:", errorMessageExpected);
        } else {
            assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                    .contains(errorMessageExpected);
        }

        logErrorDetails(errorCodeActual, errorMessageActual);
    }

    @Then("Asset externalization SALES transaction with ownerExternalId = null and the following data results a {int} error and {string} error message")
    public void transactionErrorSalesOwnerNull(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest()//
                .settlementDate(transferData.get(0))//
                .ownerExternalId(null)//
                .transferExternalId(null)//
                .purchasePriceRatio(transferData.get(1))//
                .dateFormat(DATE_FORMAT_ASSET_EXT)//
                .locale(DEFAULT_LOCALE);//

        AssetExternalizationErrorMessage errorMsgType = AssetExternalizationErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = errorMsgType.getValue();

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of(COMMAND, TRANSACTION_TYPE_SALE)));

        int errorCodeActual = exception.getStatus();
        String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        if (errorMessageType.equals("INVALID_REQUEST")) {
            assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                    .containsAnyOf("Validation errors:", errorMessageExpected);
        } else {
            assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                    .contains(errorMessageExpected);
        }

        logErrorDetails(errorCodeActual, errorMessageActual);
    }

    @Then("The latest asset externalization transaction with {string} status has the following TRANSFER Journal entries:")
    public void checkJournalEntriesTransaction(String status, DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        Long lastTransferIdByStatus = getLastTransferIdByStatus(loanId, status);

        ExternalOwnerTransferJournalEntryData journalEntriesOfTransfer = externalAssetOwnersApi()
                .getJournalEntriesOfTransfer(lastTransferIdByStatus, Map.of());
        List<JournalEntryData> content = journalEntriesOfTransfer.getJournalEntryData().getContent();

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
        }

        int linesActual = journalEntriesOfTransfer.getJournalEntryData().getNumberOfElements();
        assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInAssetExternalizationJournalEntry(linesActual, linesExpected))
                .isEqualTo(linesExpected);

        log.debug("loanId: {}", journalEntriesOfTransfer.getTransferData().getLoan().getLoanId());
        log.debug("ownerExternalId: {}", journalEntriesOfTransfer.getTransferData().getOwner().getExternalId());
        log.debug("transferId: {}", lastTransferIdByStatus);
        log.debug("transferExternalId: {}", journalEntriesOfTransfer.getTransferData().getTransferExternalId());
    }

    private Long getLastTransferIdByStatus(Long loanId, String status) throws IOException {
        PageExternalTransferData transfersResponse = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        List<ExternalTransferData> content = transfersResponse.getContent();

        ExternalTransferData result = content.stream().filter(t -> status.equals(t.getStatus().getValue()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(String.format("No Journal entry found with status: %s", status)));

        return result.getTransferId();
    }

    private Long getLastTransferId(Long loanId) throws IOException {
        PageExternalTransferData transfersResponse = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        List<ExternalTransferData> content = transfersResponse.getContent();
        ExternalTransferData result = content.stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("transfersResponse.getContent() is empty"));

        return result.getTransferId();
    }

    private ExternalTransferData getLastTransferByTransferType(Long loanId, String transferType) throws IOException {
        String transferExternalId;
        if (transferType.equalsIgnoreCase(TRANSACTION_TYPE_BUYBACK)) {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else if (transferType.equalsIgnoreCase(TRANSACTION_TYPE_SALE)) {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else if (transferType.equalsIgnoreCase(TRANSACTION_TYPE_INTERMEDIARY_SALE)) {
            transferExternalId = testContext()
                    .get(TestContextKey.ASSET_EXTERNALIZATION_INTERMEDIARY_SALE_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else {
            transferExternalId = null;
        }

        PageExternalTransferData transfersResponse = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        List<ExternalTransferData> content = transfersResponse.getContent();
        return content.stream().filter(bizEvent -> bizEvent.getTransferExternalId().equals(transferExternalId)).toList().stream()
                .reduce((first, second) -> second).orElseThrow(() -> new IllegalStateException("transfersResponse.getContent() is empty"));
    }

    @Then("The asset external owner has the following OWNER Journal entries:")
    public void checkJournalEntriesOwner(final DataTable table) throws IOException {
        final String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        assertOwnerJournalEntries(ownerExternalId, table, true);
    }

    @Then("The asset external owner has the following OWNER Journal entries containing:")
    public void checkJournalEntriesOwnerContaining(final DataTable table) throws IOException {
        final String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        assertOwnerJournalEntries(ownerExternalId, table, false);
    }

    @Then("The previous asset external owner has the following OWNER Journal entries:")
    public void checkJournalEntriesPreviousOwner(final DataTable table) throws IOException {
        final String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);
        assertOwnerJournalEntries(ownerExternalId, table, false);
    }

    @Then("The previous asset external owner has owner-tagged journal entries")
    public void checkPreviousOwnerHasOwnerTaggedJournalEntries() throws IOException {
        final String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);
        ExternalOwnerJournalEntryData journalEntriesOfOwner = externalAssetOwnersApi().getJournalEntriesOfOwner(ownerExternalId, Map.of());
        assertThat(journalEntriesOfOwner.getJournalEntryData()).as("Previous asset external owner must have owner-tagged journal entries")
                .isNotNull();
        assertThat(journalEntriesOfOwner.getJournalEntryData().getContent()).as("Previous asset external owner must have journal entries")
                .isNotEmpty();
    }

    private void assertOwnerJournalEntries(final String ownerExternalId, final DataTable table, final boolean assertExactCount) {
        ExternalOwnerJournalEntryData journalEntriesOfOwner = externalAssetOwnersApi().getJournalEntriesOfOwner(ownerExternalId, Map.of());
        assert journalEntriesOfOwner.getJournalEntryData() != null;
        List<JournalEntryData> content = journalEntriesOfOwner.getJournalEntryData().getContent();

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
        }

        if (assertExactCount) {
            int linesActual = journalEntriesOfOwner.getJournalEntryData().getNumberOfElements();
            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInAssetExternalizationJournalEntry(linesActual, linesExpected))
                    .isEqualTo(linesExpected);
        }

        log.debug("ownerExternalId: {}", journalEntriesOfOwner.getOwnerData().getExternalId());
    }

    @Then("LoanOwnershipTransferBusinessEvent is created")
    public void loanOwnershipTransferBusinessEventCheck() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanOwnershipTransferBusinessEventCheck(loanId, transferId);
    }

    @Then("LoanOwnershipTransferBusinessEvent with transfer status: {string} and transfer status reason {string} is created")
    public void loanOwnershipTransferBusinessEventCheck(String transferStatus, String transferStatusReason) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanOwnershipTransferBusinessEventWithStatusCheck(loanId, transferId, transferStatus, transferStatusReason);
    }

    public String getPreviousAssetOwner(ExternalTransferData transferData, String transferType, boolean isIntermediarySaleTransfer) {
        String previousAssetOwner;
        String intermediarySaleAssetOwner = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);
        String assetOwner = transferData.getOwner() == null ? null : transferData.getOwner().getExternalId();

        if ((transferType.equalsIgnoreCase(TRANSACTION_TYPE_SALE) || transferType.equalsIgnoreCase(TRANSACTION_TYPE_BUYBACK))
                && isIntermediarySaleTransfer) {
            previousAssetOwner = intermediarySaleAssetOwner;
        } else if (transferType.equalsIgnoreCase(TRANSACTION_TYPE_BUYBACK)) {
            previousAssetOwner = assetOwner;
        } else {
            // in case - transferType is sale(has no intermediarySale before) or intermediarySale
            previousAssetOwner = null;
        }
        return previousAssetOwner;
    }

    @Then("LoanOwnershipTransferBusinessEvent with transfer type: {string} and transfer asset owner is created")
    public void loanOwnershipTransferBusinessEventCheck(String transferType) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        ExternalTransferData transferData = getLastTransferByTransferType(loanId, transferType);
        String previousAssetOwner = getPreviousAssetOwner(transferData, transferType, false);

        eventCheckHelper.loanOwnershipTransferBusinessEventWithTypeCheck(loanId, transferData, transferType, previousAssetOwner);
    }

    @Then("LoanOwnershipTransferBusinessEvent with transfer type: {string} and transfer asset owner based on intermediarySale is created")
    public void loanOwnershipTransferBusinessEventCheckBasedOnIntermediarySale(String transferType) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        ExternalTransferData transferData = getLastTransferByTransferType(loanId, transferType);
        String previousAssetOwner = getPreviousAssetOwner(transferData, transferType, true);

        eventCheckHelper.loanOwnershipTransferBusinessEventWithTypeCheck(loanId, transferData, transferType, previousAssetOwner);
    }

    @Then("LoanOwnershipTransferBusinessEvent is not created on {string}")
    public void loanOwnershipTransferBusinessEventIsNotRaised(String date) throws IOException {
        eventAssertion.assertEventNotRaised(LoanOwnershipTransferEvent.class, em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    @Then("LoanAccountSnapshotBusinessEvent is created")
    public void loanAccountSnapshotBusinessEventCheck() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        Long transferId = getLastTransferId(loanId);

        eventCheckHelper.loanAccountSnapshotBusinessEventCheck(loanId, transferId);
    }

    @Then("Asset externalization response {string} has the correct Loan ID, transferExternalId")
    public void checkAssetExternalizationResponse(String type) {
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        String transferExternalIdExpected = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_PREFIX + "_" + type);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostInitiateTransferResponse response = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE);
        Long loanIdActual = response.getSubResourceId();
        String transferExternalIdActual = response.getResourceExternalId();

        logAssetExternalizationResponseDetails(loanId, ownerExternalIdStored, transferExternalIdExpected, transferExternalIdActual);

        assertThat(loanIdActual).as(ErrorMessageHelper.wrongDataInAssetExternalizationResponse(loanIdActual, loanId)).isEqualTo(loanId);
        assertThat(response.getResourceId()).isNotNull();
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
            transferExternalId = Utils.randomStringGenerator("TestTransferExtId_", 10);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type, transferExternalId);
        }

        createAssetExternalizationRequestByLoanId(table, transferExternalId);
    }

    @When("Admin send {string} command to the transaction type {string}")
    public void adminTransactionCommandTheWithType(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);

        externalAssetOwnersApi().transferRequestWithIdByExternalId(transferExternalId, Map.of(COMMAND, command));
    }

    @When("Admin send {string} command to the transaction type {string} will throw error")
    public void adminTransactionCommandTheWithTypeThrowError(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithIdByExternalId(transferExternalId, Map.of(COMMAND, command)));

        assertThat(exception.getStatus()).as("Expected status code: 403").isEqualTo(403);
    }

    @Then("Fetching Asset externalization details by loan id gives numberOfElements: {int} with correct ownerExternalId, ignore transactionExternalId and contain the following data:")
    public void checkAssetExternalizationDetailsByLoanIdIgnoreTransactionExternalId(int numberOfElements, DataTable table)
            throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));
        checkExternalAssetDetailsIgnoreTransferExternalId(loanId, null, response, numberOfElements, table);
    }

    private void checkExternalAssetDetailsIgnoreTransferExternalId(Long loanId, String loanExternalId, PageExternalTransferData response,
            int numberOfElements, DataTable table) {
        Integer numberOfElementsActual = response.getNumberOfElements();
        List<ExternalTransferData> content = response.getContent();

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

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithIdByExternalId(transferExternalId, Map.of(COMMAND, command)));

        assertThat(exception.getStatus()).as("Expected status code: 403").isEqualTo(403);
    }

    @When("Admin send {string} command on {string} transaction")
    public void adminSendCommand(String command, String transactionType) throws IOException {
        String transferExternalId;
        if (transactionType.equals(ExternalTransferData.StatusEnum.BUYBACK.getValue())) {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        } else {
            transferExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE);
        }

        externalAssetOwnersApi().transferRequestWithIdByExternalId(transferExternalId, Map.of(COMMAND, command));
    }

    @When("Admin set external asset owner loan product attribute {string} value {string} for loan product {string}")
    public void setAExternalAssetOwnerLoanProductAttribute(String externalAssetOwnerLoanProductAttributeKey,
            String externalAssetOwnerLoanProductAttributeValue, String loanProductName) throws IOException {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        long loanProductId = loanProducts.stream().filter(loanProduct -> loanProduct.getName().equals(loanProductName)).findFirst()
                .orElseThrow(() -> new RuntimeException("No loan product is found!")).getId();

        PageExternalTransferLoanProductAttributesData getExternalAssetOwnerLoanProductAttribute = externalAssetOwnerLoanProductAttributesApi()
                .retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId, externalAssetOwnerLoanProductAttributeKey);

        if (getExternalAssetOwnerLoanProductAttribute.getTotalFilteredRecords() == 0) {
            PostExternalAssetOwnerLoanProductAttributeRequest setLoanProductAttributeRequest = new PostExternalAssetOwnerLoanProductAttributeRequest()
                    .attributeKey(externalAssetOwnerLoanProductAttributeKey).attributeValue(externalAssetOwnerLoanProductAttributeValue);
            externalAssetOwnerLoanProductAttributesApi().createExternalAssetOwnerLoanProductAttribute(loanProductId,
                    setLoanProductAttributeRequest);
        } else {
            List<ExternalTransferLoanProductAttributesData> attributes = getExternalAssetOwnerLoanProductAttribute.getPageItems();
            assert attributes != null;
            long attributeId = attributes.stream()
                    .filter(attribute -> attribute.getAttributeKey().equals(externalAssetOwnerLoanProductAttributeKey)).findFirst()
                    .orElseThrow(() -> new RuntimeException(ErrorMessageHelper
                            .wrongDataInExternalAssetOwnerLoanProductAttribute(externalAssetOwnerLoanProductAttributeKey, loanProductId)))
                    .getAttributeId();
            PutExternalAssetOwnerLoanProductAttributeRequest setLoanProductAttributeRequest = new PutExternalAssetOwnerLoanProductAttributeRequest()
                    .attributeKey(externalAssetOwnerLoanProductAttributeKey).attributeValue(externalAssetOwnerLoanProductAttributeValue);
            externalAssetOwnerLoanProductAttributesApi().updateExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId,
                    setLoanProductAttributeRequest);
        }
    }

    private long resolveLoanProductIdByName(String loanProductName) {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        Long id = loanProducts.stream().filter(loanProduct -> loanProduct.getName().equals(loanProductName)).findFirst()
                .orElseThrow(() -> new RuntimeException("No loan product is found!")).getId();
        return Objects.requireNonNull(id, "Loan product '" + loanProductName + "' has no id");
    }

    @Given("Loan product {string} exists dedicated to this feature")
    public void ensureDedicatedLoanProductExists(String loanProductName) {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        boolean alreadyExists = loanProducts.stream().anyMatch(loanProduct -> loanProduct.getName().equals(loanProductName));
        if (!alreadyExists) {
            PostLoanProductsRequest request = loanProductsRequestFactory.defaultLoanProductsRequestLP1().name(loanProductName)
                    .dueDaysForRepaymentEvent(3).overDueDaysForRepaymentEvent(3);
            PostLoanProductsResponse response = ok(() -> loanProductsApi().createLoanProduct(request));
            log.debug("Created dedicated loan product '{}' with id {} for ExternalAssetOwnerLoanProductAttributes.feature", loanProductName,
                    response.getResourceId());
        }
    }

    @Given("Loan product {string} exists dedicated to this feature with buy down fees")
    public void ensureDedicatedBuyDownFeeLoanProductExists(String loanProductName) {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        boolean alreadyExists = loanProducts.stream().anyMatch(loanProduct -> loanProduct.getName().equals(loanProductName));
        if (!alreadyExists) {
            PostLoanProductsRequest request = loanProductsRequestFactory.defaultLoanProductsRequestLP2BuyDownFees()
                    .paymentAllocation(List.of(createDefaultPaymentAllocation())).name(loanProductName);
            PostLoanProductsResponse response = ok(() -> loanProductsApi().createLoanProduct(request));
            log.debug("Created dedicated loan product '{}' with id {} for ExternalAssetOwnerExcludedTransactionTypes.feature",
                    loanProductName, response.getResourceId());
        }
    }

    @Given("Loan product {string} exists dedicated to this feature with buy down fees and charge off reasons")
    public void ensureDedicatedBuyDownFeeWithChargeOffReasonsLoanProductExists(String loanProductName) {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        boolean alreadyExists = loanProducts.stream().anyMatch(loanProduct -> loanProduct.getName().equals(loanProductName));
        if (!alreadyExists) {
            PostLoanProductsRequest request = loanProductsRequestFactory
                    .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappingsWithBuyDownFee()
                    .paymentAllocation(List.of(createDefaultPaymentAllocation())).name(loanProductName);
            PostLoanProductsResponse response = ok(() -> loanProductsApi().createLoanProduct(request));
            log.debug("Created dedicated loan product '{}' with id {} for ExternalAssetOwnerExcludedTransactionTypes.feature",
                    loanProductName, response.getResourceId());
        }
    }

    private AdvancedPaymentData createDefaultPaymentAllocation() {
        return new AdvancedPaymentData()//
                .transactionType("DEFAULT")//
                .futureInstallmentAllocationRule("NEXT_INSTALLMENT")//
                .paymentAllocationOrder(List.of(//
                        new PaymentAllocationOrder().order(1).paymentAllocationRule("PAST_DUE_PENALTY"),
                        new PaymentAllocationOrder().order(2).paymentAllocationRule("PAST_DUE_FEE"),
                        new PaymentAllocationOrder().order(3).paymentAllocationRule("PAST_DUE_INTEREST"),
                        new PaymentAllocationOrder().order(4).paymentAllocationRule("PAST_DUE_PRINCIPAL"),
                        new PaymentAllocationOrder().order(5).paymentAllocationRule("DUE_PENALTY"),
                        new PaymentAllocationOrder().order(6).paymentAllocationRule("DUE_FEE"),
                        new PaymentAllocationOrder().order(7).paymentAllocationRule("DUE_INTEREST"),
                        new PaymentAllocationOrder().order(8).paymentAllocationRule("DUE_PRINCIPAL"),
                        new PaymentAllocationOrder().order(9).paymentAllocationRule("IN_ADVANCE_PENALTY"),
                        new PaymentAllocationOrder().order(10).paymentAllocationRule("IN_ADVANCE_FEE"),
                        new PaymentAllocationOrder().order(11).paymentAllocationRule("IN_ADVANCE_PRINCIPAL"),
                        new PaymentAllocationOrder().order(12).paymentAllocationRule("IN_ADVANCE_INTEREST")));
    }

    private long resolveExternalAssetOwnerLoanProductAttributeId(long loanProductId, String attributeKey) {
        PageExternalTransferLoanProductAttributesData attributes = externalAssetOwnerLoanProductAttributesApi()
                .retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId, attributeKey);
        List<ExternalTransferLoanProductAttributesData> pageItems = attributes.getPageItems();
        assert pageItems != null;
        Long attributeId = pageItems.stream().filter(attribute -> attribute.getAttributeKey().equals(attributeKey)).findFirst()
                .orElseThrow(() -> new RuntimeException(
                        ErrorMessageHelper.wrongDataInExternalAssetOwnerLoanProductAttribute(attributeKey, loanProductId)))
                .getAttributeId();
        return Objects.requireNonNull(attributeId, "Attribute '" + attributeKey + "' for loan product " + loanProductId + " has no id");
    }

    @When("Admin retrieves the external asset owner loan product attribute template")
    public void retrieveExternalAssetOwnerLoanProductAttributeTemplate() {
        List<ExternalTransferLoanProductAttributesTemplateData> template = ok(
                () -> externalAssetOwnerLoanProductAttributesApi().retrieveTemplateExternalAssetOwnerLoanProductAttributes());
        testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_TEMPLATE, template);
    }

    @Then("The template contains attribute {string} with isMultiValue {string} and attributeValues containing {string} and {string} but not {string}")
    public void checkTemplateAttributeContainsValues(String attributeKey, String isMultiValue, String valuePresent1, String valuePresent2,
            String valueAbsent) {
        ExternalTransferLoanProductAttributesTemplateData attribute = findTemplateAttribute(attributeKey);
        assertThat(attribute.getMultiValue()).isEqualTo(Boolean.valueOf(isMultiValue));
        assertThat(attribute.getAttributeValues()).contains(valuePresent1, valuePresent2);
        assertThat(attribute.getAttributeValues()).doesNotContain(valueAbsent);
    }

    @Then("The template contains attribute {string} with isMultiValue {string} and exact attributeValues {string}")
    public void checkTemplateAttributeExactValues(String attributeKey, String isMultiValue, String exactValuesCsv) {
        ExternalTransferLoanProductAttributesTemplateData attribute = findTemplateAttribute(attributeKey);
        assertThat(attribute.getMultiValue()).isEqualTo(Boolean.valueOf(isMultiValue));
        assertThat(attribute.getAttributeValues()).containsExactlyElementsOf(List.of(exactValuesCsv.split(",")));
    }

    private ExternalTransferLoanProductAttributesTemplateData findTemplateAttribute(String attributeKey) {
        List<ExternalTransferLoanProductAttributesTemplateData> template = testContext()
                .get(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_TEMPLATE);
        return template.stream().filter(entry -> attributeKey.equals(entry.getAttributeKey())).findFirst()
                .orElseThrow(() -> new RuntimeException("No template entry found for attributeKey: " + attributeKey));
    }

    @When("Admin creates external asset owner loan product attribute {string} for loan product {string} with values:")
    public void createExternalAssetOwnerLoanProductAttributeForProductWithValues(String attributeKey, String loanProductName,
            DataTable table) {
        createExternalAssetOwnerLoanProductAttributeForProductId(attributeKey, String.join(", ", table.asList()),
                resolveLoanProductIdByName(loanProductName));
    }

    private void createExternalAssetOwnerLoanProductAttributeForProductId(String attributeKey, String attributeValue, long loanProductId) {
        PageExternalTransferLoanProductAttributesData existingAttributes = externalAssetOwnerLoanProductAttributesApi()
                .retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId, attributeKey);
        if (existingAttributes.getTotalFilteredRecords() > 0) {
            ExternalTransferLoanProductAttributesData existingAttribute = existingAttributes.getPageItems().getFirst();
            PutExternalAssetOwnerLoanProductAttributeRequest request = new PutExternalAssetOwnerLoanProductAttributeRequest()
                    .attributeKey(attributeKey).attributeValue(attributeValue);
            CommandProcessingResult response = ok(() -> externalAssetOwnerLoanProductAttributesApi()
                    .updateExternalAssetOwnerLoanProductAttribute(loanProductId, existingAttribute.getAttributeId(), request));
            testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LAST_RESPONSE, response);
        } else {
            PostExternalAssetOwnerLoanProductAttributeRequest request = new PostExternalAssetOwnerLoanProductAttributeRequest()
                    .attributeKey(attributeKey).attributeValue(attributeValue);
            CommandProcessingResult response = ok(() -> externalAssetOwnerLoanProductAttributesApi()
                    .createExternalAssetOwnerLoanProductAttribute(loanProductId, request));
            testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LAST_RESPONSE, response);
        }
    }

    @Then("Creating external asset owner loan product attribute {string} value {string} for loan product {string} fails with:")
    public void createExternalAssetOwnerLoanProductAttributeForProductFails(String attributeKey, String attributeValue,
            String loanProductName, DataTable table) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        assertCreateExternalAssetOwnerLoanProductAttributeFails(attributeKey, attributeValue, loanProductId, errorMessageFrom(table));
    }

    @Then("Creating external asset owner loan product attribute {string} value {string} for loan product id {long} fails with:")
    public void createExternalAssetOwnerLoanProductAttributeForProductIdFails(String attributeKey, String attributeValue,
            long loanProductId, DataTable table) {
        assertCreateExternalAssetOwnerLoanProductAttributeFails(attributeKey, attributeValue, loanProductId, errorMessageFrom(table));
    }

    @Then("Creating external asset owner loan product attribute {string} with an attribute value exceeding the maximum length for loan product {string} fails with:")
    public void createExternalAssetOwnerLoanProductAttributeExceedingMaxLengthFails(String attributeKey, String loanProductName,
            DataTable table) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        String tooLongValue = "A".repeat(2001);
        assertCreateExternalAssetOwnerLoanProductAttributeFails(attributeKey, tooLongValue, loanProductId, errorMessageFrom(table));
    }

    private String errorMessageFrom(DataTable table) {
        return table.asMaps().get(0).get("errorMessage");
    }

    private void assertCreateExternalAssetOwnerLoanProductAttributeFails(String attributeKey, String attributeValue, long loanProductId,
            String expectedErrorMessagePart) {
        PostExternalAssetOwnerLoanProductAttributeRequest request = new PostExternalAssetOwnerLoanProductAttributeRequest()
                .attributeKey(attributeKey).attributeValue(attributeValue);
        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnerLoanProductAttributesApi().createExternalAssetOwnerLoanProductAttribute(loanProductId, request));
        assertThat(exception.getMessage()).contains(expectedErrorMessagePart);
    }

    @When("Admin updates external asset owner loan product attribute {string} for loan product {string} with attributeKey {string} and value {string}")
    public void updateExternalAssetOwnerLoanProductAttributeForProduct(String storedAttributeKey, String loanProductName,
            String bodyAttributeKey, String bodyAttributeValue) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        long attributeId = resolveExternalAssetOwnerLoanProductAttributeId(loanProductId, storedAttributeKey);
        PutExternalAssetOwnerLoanProductAttributeRequest request = new PutExternalAssetOwnerLoanProductAttributeRequest()
                .attributeKey(bodyAttributeKey).attributeValue(bodyAttributeValue);
        CommandProcessingResult response = ok(() -> externalAssetOwnerLoanProductAttributesApi()
                .updateExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId, request));
        testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LAST_RESPONSE, response);
    }

    @When("Admin updates external asset owner loan product attribute {string} for loan product {string} with attributeKey {string} and values:")
    public void updateExternalAssetOwnerLoanProductAttributeForProductWithValues(String storedAttributeKey, String loanProductName,
            String bodyAttributeKey, DataTable table) {
        updateExternalAssetOwnerLoanProductAttributeForProduct(storedAttributeKey, loanProductName, bodyAttributeKey,
                String.join(", ", table.asList()));
    }

    @Then("Updating external asset owner loan product attribute {string} for loan product {string} with attributeKey {string} and value {string} fails with:")
    public void updateExternalAssetOwnerLoanProductAttributeForProductFails(String storedAttributeKey, String loanProductName,
            String bodyAttributeKey, String bodyAttributeValue, DataTable table) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        long attributeId = resolveExternalAssetOwnerLoanProductAttributeId(loanProductId, storedAttributeKey);
        PutExternalAssetOwnerLoanProductAttributeRequest request = new PutExternalAssetOwnerLoanProductAttributeRequest()
                .attributeKey(bodyAttributeKey).attributeValue(bodyAttributeValue);
        CallFailedRuntimeException exception = fail(() -> externalAssetOwnerLoanProductAttributesApi()
                .updateExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId, request));
        assertThat(exception.getMessage()).contains(errorMessageFrom(table));
    }

    @Then("The request succeeds and returns loan product {string}")
    public void checkLastRequestSucceededForLoanProduct(String loanProductName) {
        CommandProcessingResult response = testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LAST_RESPONSE);
        long expectedLoanProductId = resolveLoanProductIdByName(loanProductName);
        assertThat(response.getResourceId()).isEqualTo(expectedLoanProductId);
    }

    private void checkExternalAssetOwnerLoanProductAttributeValue(String attributeKey, String loanProductName, String expectedValue) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        PageExternalTransferLoanProductAttributesData attributes = externalAssetOwnerLoanProductAttributesApi()
                .retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId, attributeKey);
        assertThat(attributes.getTotalFilteredRecords()).isEqualTo(1);
        assertThat(attributes.getPageItems()).isNotNull();
        ExternalTransferLoanProductAttributesData attribute = attributes.getPageItems().getFirst();
        assertThat(attribute.getAttributeKey()).isEqualTo(attributeKey);
        assertThat(attribute.getLoanProductId()).isEqualTo(loanProductId);
        assertThat(attribute.getAttributeValue()).isEqualTo(expectedValue);
    }

    @Then("External asset owner loan product attribute {string} for loan product {string} has values:")
    public void checkExternalAssetOwnerLoanProductAttributeValues(String attributeKey, String loanProductName, DataTable table) {
        checkExternalAssetOwnerLoanProductAttributeValue(attributeKey, loanProductName, String.join(",", table.asList()));
    }

    @Then("External asset owner loan product attribute {string} for loan product {string} does not exist")
    public void checkExternalAssetOwnerLoanProductAttributeDoesNotExist(String attributeKey, String loanProductName) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        PageExternalTransferLoanProductAttributesData attributes = externalAssetOwnerLoanProductAttributesApi()
                .retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId, attributeKey);
        assertThat(attributes.getTotalFilteredRecords()).isEqualTo(0);
    }

    @When("Admin deletes external asset owner loan product attribute {string} for loan product {string}")
    public void deleteExternalAssetOwnerLoanProductAttributeForProduct(String attributeKey, String loanProductName) {
        long loanProductId = resolveLoanProductIdByName(loanProductName);
        long attributeId = resolveExternalAssetOwnerLoanProductAttributeId(loanProductId, attributeKey);
        ok(() -> externalAssetOwnerLoanProductAttributesApi().deleteExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId));
    }

    @When("Admin makes asset externalization request for type {string} by Loan ID with unique ownerExternalId, force generated transferExternalId and without change test owner with following data:")
    public void createAssetExternalizationRequestByLoanIdUserGeneratedExtIdForceTransferIdNoTestOwner(String type, DataTable table)
            throws IOException {
        // if user created transferExternalId previously, it will use that, otherwise create a new one
        String transferExternalId = Utils.randomStringGenerator("TestTransferExtId_", 10);
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type, transferExternalId);

        createAssetExternalizationRequestByLoanId(table, transferExternalId, false);
    }

    @When("Admin creates a new external asset owner with a unique ownerExternalId")
    public void createExternalAssetOwnerWithUniqueId() {
        String ownerExternalId = Utils.randomStringGenerator(OWNER_EXTERNAL_ID_PREFIX, 20);
        testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_EXTERNAL_ID, ownerExternalId);

        PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(ownerExternalId);
        PostExternalAssetOwnerResponse response = ok(() -> externalAssetOwnersApi().createExternalAssetOwner(request));
        testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_CREATE_RESPONSE, response);

        log.debug("Created external asset owner with externalId: {}, resourceId: {}", ownerExternalId, response.getResourceId());
    }

    @Then("External asset owner creation response has a non-null resourceId")
    public void verifyCreateResponseHasResourceId() {
        PostExternalAssetOwnerResponse response = testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_CREATE_RESPONSE);
        assertThat(response).as("External asset owner create response should not be null").isNotNull();
        assertThat(response.getResourceId()).as("resourceId should not be null").isNotNull();
    }

    @Then("External asset owner list contains the created owner")
    public void verifyOwnerExistsInList() {
        String ownerExternalId = testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_EXTERNAL_ID);
        PostExternalAssetOwnerResponse createResponse = testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_CREATE_RESPONSE);

        List<ExternalTransferOwnerData> owners = ok(() -> externalAssetOwnersApi().retrieveExternalAssetOwners());
        assertThat(owners).as("Owners list should not be empty").isNotEmpty();

        ExternalTransferOwnerData found = owners.stream().filter(o -> ownerExternalId.equals(o.getExternalId())).findFirst().orElse(null);

        assertThat(found).as("Owner with externalId '%s' should exist in the list", ownerExternalId).isNotNull();
        assertThat(found.getId()).as("Owner id from GET should match resourceId from create").isEqualTo(createResponse.getResourceId());
    }

    @When("Admin tries to create an external asset owner with null ownerExternalId then it should fail with {int} status code")
    public void createExternalAssetOwnerWithNullIdFails(int expectedStatusCode) {
        PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(null);

        CallFailedRuntimeException exception = fail(() -> externalAssetOwnersApi().createExternalAssetOwner(request));

        assertThat(exception.getStatus()).as("Expected HTTP %d for null ownerExternalId", expectedStatusCode).isEqualTo(expectedStatusCode);
        assertThat(exception.getDeveloperMessage()).as("Error message should indicate ownerExternalId cannot be blank")
                .containsAnyOf("validation.msg.externalAssetOwner.ownerExternalId.cannot.be.blank", "ownerExternalId");
    }

    @When("Admin tries to create an external asset owner with a duplicate ownerExternalId then it should fail with {int} status code")
    public void createExternalAssetOwnerWithDuplicateIdFails(int expectedStatusCode) {
        String ownerExternalId = testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_EXTERNAL_ID);

        PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest().ownerExternalId(ownerExternalId);

        CallFailedRuntimeException exception = fail(() -> externalAssetOwnersApi().createExternalAssetOwner(request));

        assertThat(exception.getStatus()).as("Expected HTTP %d for duplicate ownerExternalId", expectedStatusCode)
                .isEqualTo(expectedStatusCode);
        assertThat(exception.getDeveloperMessage()).as("Error message should indicate duplicate external id")
                .contains("Provided external id already exists");
    }

    @When("Admin tries to create an external asset owner with empty JSON body then it should fail with {int} status code")
    public void createExternalAssetOwnerWithEmptyBodyFails(int expectedStatusCode) {
        PostExternalAssetOwnerRequest request = new PostExternalAssetOwnerRequest();

        CallFailedRuntimeException exception = fail(() -> externalAssetOwnersApi().createExternalAssetOwner(request));

        assertThat(exception.getStatus()).as("Expected HTTP %d for missing ownerExternalId", expectedStatusCode)
                .isEqualTo(expectedStatusCode);
    }

    @Then("Admin retrieves all external asset owners successfully")
    public void retrieveAllExternalAssetOwners() {
        List<ExternalTransferOwnerData> owners = ok(() -> externalAssetOwnersApi().retrieveExternalAssetOwners());
        assertThat(owners).as("Owners list should not be null").isNotNull();
    }

    @When("Admin creates a new loan product for external asset owner loan product attributes")
    public void createLoanProductForExternalAssetOwnerLoanProductAttributes() {
        final PostLoanProductsResponse response = ok(
                () -> loanProductsApi().createLoanProduct(loanProductsRequestFactory.defaultLoanProductsRequestLP1()));
        testContext().set(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LOAN_PRODUCT_ID, response.getResourceId());
    }

    @Then("External asset owner loan product attributes template contains the following attributes:")
    public void checkExternalAssetOwnerLoanProductAttributesTemplate(final DataTable table) {
        final List<ExternalTransferLoanProductAttributesTemplateData> template = ok(
                () -> externalAssetOwnerLoanProductAttributesApi().retrieveTemplateExternalAssetOwnerLoanProductAttributes());

        table.asMaps().forEach(expectedAttribute -> {
            final String attributeKey = expectedAttribute.get("attributeKey");
            final ExternalTransferLoanProductAttributesTemplateData actualAttribute = template.stream()
                    .filter(attribute -> attributeKey.equals(attribute.getAttributeKey())).findFirst()
                    .orElseThrow(() -> new IllegalStateException(String.format("No attribute %s is found in the template", attributeKey)));

            assertThat(actualAttribute.getAttributeValues()).as("Values of attribute %s in the template", attributeKey)
                    .isEqualTo(List.of(expectedAttribute.get("attributeValues").split(",", -1)));
            assertThat(actualAttribute.getMultiValue()).as("Multi value flag of attribute %s in the template", attributeKey)
                    .isEqualTo(Boolean.parseBoolean(expectedAttribute.get("multiValue")));
        });
    }

    @When("Admin creates external asset owner loan product attribute {string} with value {string} for the new loan product")
    public void createExternalAssetOwnerLoanProductAttribute(final String attributeKey, final String attributeValue) {
        final Long loanProductId = newLoanProductId();

        final CommandProcessingResult result = ok(() -> externalAssetOwnerLoanProductAttributesApi()
                .createExternalAssetOwnerLoanProductAttribute(loanProductId, createAttributeRequest(attributeKey, attributeValue)));

        assertThat(result.getResourceId()).as("Resource id of the created attribute %s", attributeKey).isEqualTo(loanProductId);
    }

    @When("Admin updates external asset owner loan product attribute {string} of the new loan product to value {string}")
    public void updateExternalAssetOwnerLoanProductAttribute(final String attributeKey, final String attributeValue) {
        final Long loanProductId = newLoanProductId();
        final Long attributeId = retrieveAttributeOfNewLoanProduct(attributeKey).getAttributeId();

        ok(() -> externalAssetOwnerLoanProductAttributesApi().updateExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId,
                updateAttributeRequest(attributeKey, attributeValue)));
    }

    @Then("External asset owner loan product attribute {string} of the new loan product has value {string}")
    public void checkExternalAssetOwnerLoanProductAttributeValue(final String attributeKey, final String attributeValue) {
        final Long loanProductId = newLoanProductId();
        final PageExternalTransferLoanProductAttributesData attributes = retrieveAttributesOfNewLoanProduct(attributeKey);

        assertThat(attributes.getTotalFilteredRecords()).as("Number of attributes %s of loan product %s", attributeKey, loanProductId)
                .isOne();
        final ExternalTransferLoanProductAttributesData attribute = attributes.getPageItems().stream().findFirst().orElseThrow();
        assertThat(attribute.getAttributeId()).as("Id of attribute %s", attributeKey).isNotNull();
        assertThat(attribute.getAttributeKey()).as("Key of attribute %s", attributeKey).isEqualTo(attributeKey);
        assertThat(attribute.getAttributeValue()).as("Value of attribute %s", attributeKey).isEqualTo(attributeValue);
        assertThat(attribute.getLoanProductId()).as("Loan product id of attribute %s", attributeKey).isEqualTo(loanProductId);
    }

    @Then("External asset owner loan product attribute {string} of the new loan product does not exist")
    public void checkExternalAssetOwnerLoanProductAttributeDoesNotExist(final String attributeKey) {
        assertThat(retrieveAttributesOfNewLoanProduct(attributeKey).getTotalFilteredRecords())
                .as("Number of attributes %s of loan product %s", attributeKey, newLoanProductId()).isZero();
    }

    @Then("Creating external asset owner loan product attribute {string} with value {string} for the new loan product results a {int} error and {string} error message")
    public void createExternalAssetOwnerLoanProductAttributeFails(final String attributeKey, final String attributeValue,
            final int errorCodeExpected, final String errorMessageType) {
        assertExternalAssetOwnerLoanProductAttributeCreationFails(externalAssetOwnerLoanProductAttributesApi(), newLoanProductId(),
                attributeKey, attributeValue, errorCodeExpected, errorMessageType);
    }

    @Then("Created user creating external asset owner loan product attribute {string} with value {string} for the new loan product results a {int} error and {string} error message")
    public void createExternalAssetOwnerLoanProductAttributeByCreatedUserFails(final String attributeKey, final String attributeValue,
            final int errorCodeExpected, final String errorMessageType) {
        assertExternalAssetOwnerLoanProductAttributeCreationFails(createdUserExternalAssetOwnerLoanProductAttributesApi(),
                newLoanProductId(), attributeKey, attributeValue, errorCodeExpected, errorMessageType);
    }

    @Then("Creating external asset owner loan product attribute {string} with value {string} for non-existing loan product results a {int} error and {string} error message")
    public void createExternalAssetOwnerLoanProductAttributeForNonExistingLoanProductFails(final String attributeKey,
            final String attributeValue, final int errorCodeExpected, final String errorMessageType) {
        assertExternalAssetOwnerLoanProductAttributeCreationFails(externalAssetOwnerLoanProductAttributesApi(),
                NON_EXISTING_LOAN_PRODUCT_ID, attributeKey, attributeValue, errorCodeExpected, errorMessageType);
    }

    @Then("Updating external asset owner loan product attribute {string} of the new loan product to value {string} results a {int} error and {string} error message")
    public void updateExternalAssetOwnerLoanProductAttributeFails(final String attributeKey, final String attributeValue,
            final int errorCodeExpected, final String errorMessageType) {
        assertExternalAssetOwnerLoanProductAttributeUpdateFails(externalAssetOwnerLoanProductAttributesApi(), attributeKey, attributeValue,
                errorCodeExpected, errorMessageType);
    }

    @Then("Created user updating external asset owner loan product attribute {string} of the new loan product to value {string} results a {int} error and {string} error message")
    public void updateExternalAssetOwnerLoanProductAttributeByCreatedUserFails(final String attributeKey, final String attributeValue,
            final int errorCodeExpected, final String errorMessageType) {
        assertExternalAssetOwnerLoanProductAttributeUpdateFails(createdUserExternalAssetOwnerLoanProductAttributesApi(), attributeKey,
                attributeValue, errorCodeExpected, errorMessageType);
    }

    @Then("External asset owner loan product attributes template contains exactly the attribute keys {string}")
    public void checkExternalAssetOwnerLoanProductAttributesTemplateKeys(final String attributeKeys) {
        final List<ExternalTransferLoanProductAttributesTemplateData> template = ok(
                () -> externalAssetOwnerLoanProductAttributesApi().retrieveTemplateExternalAssetOwnerLoanProductAttributes());

        assertThat(template.stream().map(ExternalTransferLoanProductAttributesTemplateData::getAttributeKey).toList())
                .as("Attribute keys in the template").containsExactlyInAnyOrder(attributeKeys.split(",", -1));
    }

    @Then("External asset owner loan product attributes template attribute {string} has multiValue {string}, contains {string} and does not contain {string}")
    public void checkExternalAssetOwnerLoanProductAttributesTemplateAttributeValues(final String attributeKey, final String multiValue,
            final String containedValues, final String notContainedValues) {
        final ExternalTransferLoanProductAttributesTemplateData attribute = ok(
                () -> externalAssetOwnerLoanProductAttributesApi().retrieveTemplateExternalAssetOwnerLoanProductAttributes()).stream()
                .filter(templateAttribute -> attributeKey.equals(templateAttribute.getAttributeKey())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No attribute %s is found in the template", attributeKey)));

        assertThat(attribute.getMultiValue()).as("Multi value flag of attribute %s in the template", attributeKey)
                .isEqualTo(Boolean.parseBoolean(multiValue));
        assertThat(attribute.getAttributeValues()).as("Values of attribute %s in the template", attributeKey)
                .contains(containedValues.split(",", -1)).doesNotContain(notContainedValues.split(",", -1));
    }

    @When("Admin deletes external asset owner loan product attribute {string} of the new loan product")
    public void deleteExternalAssetOwnerLoanProductAttribute(final String attributeKey) {
        final Long loanProductId = newLoanProductId();
        final Long attributeId = retrieveAttributeOfNewLoanProduct(attributeKey).getAttributeId();

        final CommandProcessingResult result = ok(() -> externalAssetOwnerLoanProductAttributesApi()
                .deleteExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId));

        assertThat(result.getResourceId()).as("Resource id of the deleted attribute %s", attributeKey).isEqualTo(loanProductId);
    }

    @When("Created user creates external asset owner loan product attribute {string} with value {string} for the new loan product")
    public void createExternalAssetOwnerLoanProductAttributeByCreatedUser(final String attributeKey, final String attributeValue) {
        final Long loanProductId = newLoanProductId();

        final CommandProcessingResult result = ok(() -> createdUserExternalAssetOwnerLoanProductAttributesApi()
                .createExternalAssetOwnerLoanProductAttribute(loanProductId, createAttributeRequest(attributeKey, attributeValue)));

        assertThat(result.getResourceId()).as("Resource id of the created attribute %s", attributeKey).isEqualTo(loanProductId);
    }

    @When("Created user updates external asset owner loan product attribute {string} of the new loan product to value {string}")
    public void updateExternalAssetOwnerLoanProductAttributeByCreatedUser(final String attributeKey, final String attributeValue) {
        final Long loanProductId = newLoanProductId();
        final Long attributeId = retrieveAttributeOfNewLoanProduct(attributeKey).getAttributeId();

        ok(() -> createdUserExternalAssetOwnerLoanProductAttributesApi().updateExternalAssetOwnerLoanProductAttribute(loanProductId,
                attributeId, updateAttributeRequest(attributeKey, attributeValue)));
    }

    @Then("Updating external asset owner loan product attribute {string} of the new loan product through another loan product to value {string} results a {int} error and {string} error message")
    public void updateExternalAssetOwnerLoanProductAttributeThroughAnotherLoanProductFails(final String attributeKey,
            final String attributeValue, final int errorCodeExpected, final String errorMessageType) {
        final Long attributeId = retrieveAttributeOfNewLoanProduct(attributeKey).getAttributeId();
        final Long anotherLoanProductId = ok(
                () -> loanProductsApi().createLoanProduct(loanProductsRequestFactory.defaultLoanProductsRequestLP1())).getResourceId();

        final CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnerLoanProductAttributesApi().updateExternalAssetOwnerLoanProductAttribute(anotherLoanProductId,
                        attributeId, updateAttributeRequest(attributeKey, attributeValue)));

        assertExternalAssetOwnerLoanProductAttributeError(exception, errorCodeExpected, errorMessageType);
    }

    private void assertExternalAssetOwnerLoanProductAttributeCreationFails(final ExternalAssetOwnerLoanProductAttributesApi attributesApi,
            final Long loanProductId, final String attributeKey, final String attributeValue, final int errorCodeExpected,
            final String errorMessageType) {
        final CallFailedRuntimeException exception = fail(() -> attributesApi.createExternalAssetOwnerLoanProductAttribute(loanProductId,
                createAttributeRequest(attributeKey, attributeValue)));

        assertExternalAssetOwnerLoanProductAttributeError(exception, errorCodeExpected, errorMessageType);
    }

    private void assertExternalAssetOwnerLoanProductAttributeUpdateFails(final ExternalAssetOwnerLoanProductAttributesApi attributesApi,
            final String attributeKey, final String attributeValue, final int errorCodeExpected, final String errorMessageType) {
        final Long loanProductId = newLoanProductId();
        final Long attributeId = retrieveAttributeOfNewLoanProduct(attributeKey).getAttributeId();

        final CallFailedRuntimeException exception = fail(() -> attributesApi.updateExternalAssetOwnerLoanProductAttribute(loanProductId,
                attributeId, updateAttributeRequest(attributeKey, attributeValue)));

        assertExternalAssetOwnerLoanProductAttributeError(exception, errorCodeExpected, errorMessageType);
    }

    private void assertExternalAssetOwnerLoanProductAttributeError(final CallFailedRuntimeException exception, final int errorCodeExpected,
            final String errorMessageType) {
        final String errorMessageExpected = AssetExternalizationErrorMessage.valueOf(errorMessageType).getValue();
        final int errorCodeActual = exception.getStatus();
        final String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .contains(errorMessageExpected);

        logErrorDetails(errorCodeActual, errorMessageActual);
    }

    private Long newLoanProductId() {
        return testContext().get(TestContextKey.EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE_LOAN_PRODUCT_ID);
    }

    private ExternalAssetOwnerLoanProductAttributesApi createdUserExternalAssetOwnerLoanProductAttributesApi() {
        final String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        final String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        return fineractClientConfiguration.fineractFeignClientForUser(username, password).externalAssetOwnerLoanProductAttributes();
    }

    private PageExternalTransferLoanProductAttributesData retrieveAttributesOfNewLoanProduct(final String attributeKey) {
        final Long loanProductId = newLoanProductId();
        return ok(() -> externalAssetOwnerLoanProductAttributesApi().retrieveAllExternalAssetOwnerLoanProductAttributes(loanProductId,
                attributeKey));
    }

    private ExternalTransferLoanProductAttributesData retrieveAttributeOfNewLoanProduct(final String attributeKey) {
        return retrieveAttributesOfNewLoanProduct(attributeKey).getPageItems().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        ErrorMessageHelper.wrongDataInExternalAssetOwnerLoanProductAttribute(attributeKey, newLoanProductId())));
    }

    private PostExternalAssetOwnerLoanProductAttributeRequest createAttributeRequest(final String attributeKey,
            final String attributeValue) {
        return new PostExternalAssetOwnerLoanProductAttributeRequest().attributeKey(attributeKey).attributeValue(attributeValue);
    }

    private PutExternalAssetOwnerLoanProductAttributeRequest updateAttributeRequest(final String attributeKey,
            final String attributeValue) {
        return new PutExternalAssetOwnerLoanProductAttributeRequest().attributeKey(attributeKey).attributeValue(attributeValue);
    }

}
