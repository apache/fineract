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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.ExternalAssetOwnerLoanProductAttributesApi;
import org.apache.fineract.client.feign.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.feign.services.LoanProductsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.JournalEntryData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.PostExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutExternalAssetOwnerLoanProductAttributeRequest;
import org.apache.fineract.test.data.AssetExternalizationErrorMessage;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanOwnershipTransferEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AssetExternalizationStepDef extends AbstractStepDef {

    public static final String OWNER_EXTERNAL_ID_PREFIX = "TestOwner-";
    public static final String DATE_FORMAT_ASSET_EXT = "yyyy-MM-dd";
    public static final String DEFAULT_LOCALE = "en";
    public static final String TRANSACTION_TYPE_SALE = "sale";
    public static final String TRANSACTION_TYPE_BUYBACK = "buyback";
    public static final String TRANSACTION_TYPE_INTERMEDIARY_SALE = "intermediarySale";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_ASSET_EXT);

    @Autowired
    private FineractFeignClient fineractFeignClient;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @Autowired
    private EventAssertion eventAssertion;

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

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanId(loanId, request,
                    Map.of("command", transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());

        } else if ((transferData.get(0).equals(TRANSACTION_TYPE_SALE) || transferData.get(0).equals(TRANSACTION_TYPE_INTERMEDIARY_SALE))) {
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

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanId(loanId, request,
                    Map.of("command", transferData.get(0)));
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
                Map.of("command", TRANSACTION_TYPE_BUYBACK));
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
        testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                response.getResourceExternalId());
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

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        ExternalAssetOwnerRequest request = new ExternalAssetOwnerRequest();
        if (transferData.get(0).equals(TRANSACTION_TYPE_BUYBACK)) {
            request.settlementDate(transferData.get(1))//
                    .transferExternalId(transferExternalId)//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanExternalId(loanExternalId, request,
                    Map.of("command", transferData.get(0)));
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_RESPONSE, response);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE,
                    response.getResourceExternalId());
        } else if (transferData.get(0).equals(TRANSACTION_TYPE_SALE)) {
            String ownerExternalId = Utils.randomNameGenerator(OWNER_EXTERNAL_ID_PREFIX, 3);

            request.settlementDate(transferData.get(1))//
                    .ownerExternalId(ownerExternalId)//
                    .transferExternalId(transferExternalId)//
                    .purchasePriceRatio(transferData.get(2))//
                    .dateFormat(DATE_FORMAT_ASSET_EXT)//
                    .locale(DEFAULT_LOCALE);//

            PostInitiateTransferResponse response = externalAssetOwnersApi().transferRequestWithLoanExternalId(loanExternalId, request,
                    Map.of("command", transferData.get(0)));
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

        log.debug("loanId: {}", loanId);
        log.debug("ownerExternalIdStored: {}", ownerExternalIdStored);
        log.debug("transferExternalId generated by user: {}", transferExternalIdExpected);
        log.debug("transferExternalIdActual: {}", transferExternalIdActual);

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

    @Then("Fetching Asset externalization details by loan id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanId(int numberOfElements, DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanId", loanId));

        checkExternalAssetDetails(loanId, null, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by loan external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByLoanExternalId(int numberOfElements, DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        PageExternalTransferData response = externalAssetOwnersApi().getTransfers(Map.of("loanExternalId", loanExternalId));
        checkExternalAssetDetails(null, loanExternalId, response, numberOfElements, table);
    }

    @Then("Fetching Asset externalization details by transfer external id gives numberOfElements: {int} with correct ownerExternalId and the following data:")
    public void checkAssetExternalizationDetailsByTransferExternalId(int numberOfElements, DataTable table) throws IOException {
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
            } else { // in case transfer has previous intermediarySale transfer
                if (transactionType.equalsIgnoreCase(TRANSACTION_TYPE_SALE)
                        && (status.equals(ExternalTransferData.StatusEnum.ACTIVE.getValue())
                                || status.equals(ExternalTransferData.StatusEnum.PENDING.getValue()))) {
                    ownerExternalId = ownerExternalIdStored;
                    previousAssetOwner = intermediarySaleAssetOwner;
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
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of("command", transferData.get(0))));

        int errorCodeActual = exception.getStatus();
        String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
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

        AssetExternalizationErrorMessage errorMsgType = AssetExternalizationErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = errorMsgType.getValue();

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of("command", transferData.get(0))));

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

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
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
                () -> externalAssetOwnersApi().transferRequestWithLoanId(loanId, request, Map.of("command", TRANSACTION_TYPE_SALE)));

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

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
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
        ExternalTransferData result = content.stream().filter(bizEvent -> bizEvent.getTransferExternalId().equals(transferExternalId))
                .toList().stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("transfersResponse.getContent() is empty"));

        return result;
    }

    @Then("The asset external owner has the following OWNER Journal entries:")
    public void checkJournalEntriesOwner(DataTable table) throws IOException {
        String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);

        ExternalOwnerJournalEntryData journalEntriesOfOwner = externalAssetOwnersApi().getJournalEntriesOfOwner(ownerExternalId, Map.of());
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

        int linesActual = journalEntriesOfOwner.getJournalEntryData().getNumberOfElements();
        assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInAssetExternalizationJournalEntry(linesActual, linesExpected))
                .isEqualTo(linesExpected);

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

        log.debug("loanId: {}", loanId);
        log.debug("ownerExternalIdStored: {}", ownerExternalIdStored);
        log.debug("transferExternalId generated by user: {}", transferExternalIdExpected);
        log.debug("transferExternalIdActual: {}", transferExternalIdActual);

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
            transferExternalId = Utils.randomNameGenerator("TestTransferExtId_", 3);
            testContext().set(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type, transferExternalId);
        }

        createAssetExternalizationRequestByLoanId(table, transferExternalId);
    }

    @When("Admin send {string} command to the transaction type {string}")
    public void adminTransactionCommandTheWithType(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);

        externalAssetOwnersApi().transferRequestWithId1(transferExternalId, Map.of("command", command));
    }

    @When("Admin send {string} command to the transaction type {string} will throw error")
    public void adminTransactionCommandTheWithTypeThrowError(String command, String type) throws IOException {
        String transferExternalId = testContext()
                .get(TestContextKey.ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED + "_" + type);

        CallFailedRuntimeException exception = fail(
                () -> externalAssetOwnersApi().transferRequestWithId1(transferExternalId, Map.of("command", command)));

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
                () -> externalAssetOwnersApi().transferRequestWithId1(transferExternalId, Map.of("command", command)));

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

        externalAssetOwnersApi().transferRequestWithId1(transferExternalId, Map.of("command", command));
    }

    @When("Admin set external asset owner loan product attribute {string} value {string} for loan product {string}")
    public void setAExternalAssetOwnerLoanProductAttribute(String externalAssetOwnerLoanProductAttributeKey,
            String externalAssetOwnerLoanProductAttributeValue, String loanProductName) throws IOException {
        List<GetLoanProductsResponse> loanProducts = loanProductsApi().retrieveAllLoanProducts(Map.of());
        long loanProductId = loanProducts.stream().filter(loanProduct -> loanProduct.getName().equals(loanProductName)).findFirst()
                .orElseThrow(() -> new RuntimeException("No loan product is found!")).getId();

        PageExternalTransferLoanProductAttributesData getExternalAssetOwnerLoanProductAttribute = externalAssetOwnerLoanProductAttributesApi()
                .getExternalAssetOwnerLoanProductAttributes(loanProductId,
                        Map.of("attributeKey", externalAssetOwnerLoanProductAttributeKey));

        if (getExternalAssetOwnerLoanProductAttribute.getTotalFilteredRecords() == 0) {
            PostExternalAssetOwnerLoanProductAttributeRequest setLoanProductAttributeRequest = new PostExternalAssetOwnerLoanProductAttributeRequest()
                    .attributeKey(externalAssetOwnerLoanProductAttributeKey).attributeValue(externalAssetOwnerLoanProductAttributeValue);
            externalAssetOwnerLoanProductAttributesApi().postExternalAssetOwnerLoanProductAttribute(loanProductId,
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
            externalAssetOwnerLoanProductAttributesApi().updateLoanProductAttribute(loanProductId, attributeId,
                    setLoanProductAttributeRequest);
        }
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
