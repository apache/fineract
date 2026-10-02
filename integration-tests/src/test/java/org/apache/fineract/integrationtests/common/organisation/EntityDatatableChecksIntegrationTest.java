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
package org.apache.fineract.integrationtests.common.organisation;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetEntityDatatableChecksResponse;
import org.apache.fineract.client.models.PostClientsDatatable;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostEntityDatatableChecksTemplateResponse;
import org.apache.fineract.client.models.PostGroupsDatatable;
import org.apache.fineract.client.models.PostGroupsRequest;
import org.apache.fineract.client.models.PostLoansDataTable;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostSavingsAccountsDatatable;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity Datatable Checks Integration Test for checking Creation, Deletion and Retrieval of Entity-Datatable Check
 */
public class EntityDatatableChecksIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EntityDatatableChecksIntegrationTest.class);
    private FeignDatatableHelper datatableHelper;
    private FeignGroupHelper groupHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignCollateralHelper collateralHelper;

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final String GROUP_APP_TABLE_NAME = "m_group";
    private static final String SAVINGS_APP_TABLE_NAME = "m_savings_account";
    private static final String LOAN_APP_TABLE_NAME = "m_loan";

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";

    @BeforeAll
    public void setup() {
        this.datatableHelper = new FeignDatatableHelper(fineractClient());
        this.groupHelper = new FeignGroupHelper(fineractClient());
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
    }

    @Test
    public void validateCreateDeleteDatatableCheck() {
        // creating datatable
        String datatableName = createAndVerifyDatatable(CLIENT_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateDeleteEntityDatatableCheck() {
        // creating datatable
        String datatableName = createAndVerifyDatatable(CLIENT_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateRetriveEntityDatatableChecksList() {
        // retrieving entity datatable check
        List<GetEntityDatatableChecksResponse> entityDatatableChecksList = EntityDatatableChecksHelper.retrieveEntityDatatableCheck();
        assertNotNull(entityDatatableChecksList, "ERROR IN RETRIEVING THE ENTITY DATATABLE CHECKS");
    }

    @Test
    public void validateCreateClientWithEntityDatatableCheck() {

        // creating datatable
        String registeredTableName = createAndVerifyDatatable(CLIENT_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating client with datatables
        final Long clientID = clientHelper.createClientPending(ClientRequestBuilders.createPendingClient("04 March 2014").datatables(List
                .of(new PostClientsDatatable().registeredTableName(registeredTableName).data(FeignDatatableHelper.testDatatableEntry()))))
                .getClientId();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting datatable entries
        Long appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, clientID).getResourceId();
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateClientWithEntityDatatableCheckWithFailure() {
        // creating datatable
        String registeredTableName = createAndVerifyDatatable(CLIENT_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating client with datatables with error
        CallFailedRuntimeException clientError = fail(
                () -> fineractClient().clients().createClient(ClientRequestBuilders.createPendingClient("04 March 2014")));
        assertEquals(403, clientError.getStatus());
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                FeignErrors.firstError(clientError).userMessageGlobalisationCode());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateGroupWithEntityDatatableCheck() {

        // creating datatable
        String registeredTableName = createAndVerifyDatatable(GROUP_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(GROUP_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating group with datatables
        final Long groupId = groupHelper.createGroup(pendingGroupRequest().datatables(List
                .of(new PostGroupsDatatable().registeredTableName(registeredTableName).data(FeignDatatableHelper.testDatatableEntry()))))
                .getGroupId();
        assertEquals(groupId, groupHelper.retrieveGroup(groupId).getId());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting datatable entries
        Long appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, groupId).getResourceId();
        assertEquals(groupId, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateGroupWithEntityDatatableCheckWithFailure() {
        // creating datatable
        String registeredTableName = createAndVerifyDatatable(GROUP_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(GROUP_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating group with datatables with error
        CallFailedRuntimeException groupError = fail(() -> fineractClient().groups().createGroup(pendingGroupRequest()));
        assertEquals(403, groupError.getStatus());
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                FeignErrors.firstError(groupError).userMessageGlobalisationCode());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateSavingsWithEntityDatatableCheck() {

        // creating datatable
        String registeredTableName = createAndVerifyDatatable(SAVINGS_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(SAVINGS_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        final Long clientID = createClient();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());

        final Long savingsProductID = createSavingsProduct();
        Assertions.assertNotNull(savingsProductID);

        // creating savings with datatables
        final Long savingsId = savingsHelper
                .submitApplication(SavingsRequestBuilders.submitSavingsApplication(clientID, savingsProductID, "01 December 2016")
                        .datatables(List.of(new PostSavingsAccountsDatatable().registeredTableName(registeredTableName)
                                .data(FeignDatatableHelper.testDatatableEntry()))))
                .getSavingsId();
        Assertions.assertNotNull(savingsId);

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting datatable entries
        Long appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, savingsId).getResourceId();
        assertEquals(savingsId, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateSavingsWithEntityDatatableCheckWithFailure() {
        // creating datatable
        String registeredTableName = createAndVerifyDatatable(SAVINGS_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(SAVINGS_APP_TABLE_NAME, registeredTableName, 100L, null).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        final Long clientID = createClient();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());

        final Long savingsProductID = createSavingsProduct();
        Assertions.assertNotNull(savingsProductID);

        // creating savings with datatables with error
        PostSavingsAccountsRequest savingsRequest = SavingsRequestBuilders.submitSavingsApplication(clientID, savingsProductID,
                "01 December 2016");
        CallFailedRuntimeException savingsError = fail(() -> fineractClient().savingsAccount().submitSavingsApplication(savingsRequest));
        assertEquals(403, savingsError.getStatus());
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                FeignErrors.firstError(savingsError).userMessageGlobalisationCode());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateLoanWithEntityDatatableCheck() {
        // creating client
        final Long clientID = createClient();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());

        // creating loan product
        final Long loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        Assertions.assertNotNull(loanProductID);

        // creating datatable
        String registeredTableName = createAndVerifyDatatable(LOAN_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(LOAN_APP_TABLE_NAME, registeredTableName, 100L, loanProductID).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating new loan application
        final Long loanID = loanHelper
                .applyForLoan(loanApplication(clientID, loanProductID, 5).datatables(List.of(
                        new PostLoansDataTable().registeredTableName(registeredTableName).data(FeignDatatableHelper.testDatatableEntry()))))
                .getLoanId();
        Assertions.assertNotNull(loanID);

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting datatable entries
        Long appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, loanID).getResourceId();
        assertEquals(loanID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateCreateLoanWithEntityDatatableCheckWithFailure() {
        // creating client
        final Long clientID = createClient();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());

        // creating loan product
        final Long loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        Assertions.assertNotNull(loanProductID);

        // creating datatable
        String registeredTableName = createAndVerifyDatatable(LOAN_APP_TABLE_NAME);

        // creating new entity datatable check
        Long entityDatatableCheckId = EntityDatatableChecksHelper
                .createEntityDatatableCheck(LOAN_APP_TABLE_NAME, registeredTableName, 100L, loanProductID).getResourceId();
        assertNotNull(entityDatatableCheckId, "ERROR IN CREATING THE ENTITY DATATABLE CHECK");

        // creating new loan application with error
        PostLoansRequest loanRequest = loanApplication(clientID, loanProductID, 5);
        CallFailedRuntimeException loanError = fail(
                () -> fineractClient().loans().calculateLoanScheduleOrSubmitLoanApplication(loanRequest, (String) null));
        assertEquals(403, loanError.getStatus());
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                FeignErrors.firstError(loanError).userMessageGlobalisationCode());

        // deleting entity datatable check
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull(entityDatatableCheckId, "ERROR IN DELETING THE ENTITY DATATABLE CHECK");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName).getResourceIdentifier();
        assertEquals(registeredTableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void createClientWithDatatableUsingEntitySubtype() {
        // creating datatable for client entity person subentity
        final String datatableNamePerson = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_person_", 5).toLowerCase()
                .toLowerCase();
        final String datatableNameEntity = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_entity_", 5).toLowerCase()
                .toLowerCase();

        String itsAString = "itsastring";
        final List<PostColumnHeaderData> datatableColumnsList = List
                .of(new PostColumnHeaderData().name(itsAString).type("String").mandatory(true).length(10L));

        // Person Subtype
        PostDataTablesRequest datatableRequest = new PostDataTablesRequest().datatableName(datatableNamePerson)
                .apptableName(CLIENT_APP_TABLE_NAME).entitySubType("PERSON").multiRow(false).columns(datatableColumnsList);
        LOG.info("request : {}", datatableRequest);

        datatableHelper.createDatatable(datatableRequest);

        PostEntityDatatableChecksTemplateResponse entityDatatableChecksResponse = EntityDatatableChecksHelper
                .createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableNamePerson, 100L, null);
        assertNotNull(entityDatatableChecksResponse);
        final Long personDatatableCheck = entityDatatableChecksResponse.getResourceId();
        LOG.info("entityDatatableChecksResponse Person: {}", entityDatatableChecksResponse.getResourceId());

        // Entity Subtype
        datatableRequest = new PostDataTablesRequest().datatableName(datatableNameEntity).apptableName(CLIENT_APP_TABLE_NAME)
                .entitySubType("ENTITY").multiRow(false).columns(datatableColumnsList);
        LOG.info("request : {}", datatableRequest);

        datatableHelper.createDatatable(datatableRequest);

        entityDatatableChecksResponse = EntityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableNameEntity,
                100L, null);
        assertNotNull(entityDatatableChecksResponse);
        final Long entityDatatableCheck = entityDatatableChecksResponse.getResourceId();
        LOG.info("entityDatatableChecksResponse Entity: {}", entityDatatableChecksResponse.getResourceId());

        final PostClientsDatatable datatables = new PostClientsDatatable().registeredTableName(datatableNamePerson)
                .data(Map.of(itsAString, Utils.randomStringGenerator("", 8), "locale", "en"));
        LOG.info("datatables : {}", datatables);

        PostClientsResponse postClientsResponse = clientHelper
                .createClient(ClientRequestBuilders.createActivePersonClient("04 March 2011").datatables(List.of(datatables)));
        assertNotNull(postClientsResponse);
        assertNotNull(postClientsResponse.getResourceId());

        // Remove the Entity Datatable checks for others tests
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(personDatatableCheck);
        EntityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheck);
    }

    private String createAndVerifyDatatable(String apptableName) {
        String datatableName = this.datatableHelper.createDatatable(FeignDatatableHelper.testDatatableRequest(apptableName))
                .getResourceIdentifier();
        assertEquals(datatableName, this.datatableHelper.getDatatable(datatableName).getRegisteredTableName(),
                "ERROR IN CREATING THE DATATABLE");
        return datatableName;
    }

    private static PostGroupsRequest pendingGroupRequest() {
        return new PostGroupsRequest().officeId(1L).name(Utils.uniqueRandomStringGenerator("Group_Name_", 5))
                .externalId(UUID.randomUUID().toString()).dateFormat("dd MMMM yyyy").locale("en").active(false)
                .submittedOnDate("04 March 2011");
    }

    private Long createSavingsProduct() {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        return savingsProductHelper.createSavingsProduct(SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(new BigDecimal(MINIMUM_OPENING_BALANCE))).getResourceId();
    }

    private Long createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(repaymentStrategy) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails(digitsAfterDecimal, inMultiplesOf).buildRequest());
    }

    private PostLoansRequest loanApplication(final Long clientID, final Long loanProductID, final int graceOnPrincipalPayment) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        return new PostLoansRequest().clientId(clientID).productId(loanProductID).loanType("individual")
                .principal(new BigDecimal("10000000.00")).loanTermFrequency(24).loanTermFrequencyType(2).numberOfRepayments(24)
                .repaymentEvery(1).repaymentFrequencyType(2).interestRatePerPeriod(new BigDecimal("2")).amortizationType(0).interestType(0)
                .interestCalculationPeriodType(1).graceOnPrincipalPayment(graceOnPrincipalPayment)
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY).expectedDisbursementDate("02 June 2014")
                .submittedOnDate("02 June 2014").maxOutstandingLoanBalance(new BigDecimal("36000"))
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)))
                .dateFormat("dd MMMM yyyy").locale("en_GB");
    }
}
