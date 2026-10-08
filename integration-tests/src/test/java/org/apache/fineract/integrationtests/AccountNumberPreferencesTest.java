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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetAccountNumberFormatsIdResponse;
import org.apache.fineract.client.models.GetCentersCenterIdResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetGroupsGroupIdResponse;
import org.apache.fineract.client.models.PostAccountNumberFormatsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PutAccountNumberFormatsRequest;
import org.apache.fineract.client.models.PutAccountNumberFormatsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountNumberPreferencesTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(AccountNumberPreferencesTest.class);
    private static final String DEFAULT_CLIENT_ACTIVATION_DATE = "04 March 2011";
    private static final String SAVINGS_SUBMITTED_DATE = "08 January 2013";
    private static final long CLIENT_ACCOUNT_TYPE = 1L;
    private static final long LOAN_ACCOUNT_TYPE = 2L;
    private static final long SAVINGS_ACCOUNT_TYPE = 3L;
    private static final long CENTER_ACCOUNT_TYPE = 4L;
    private static final long GROUPS_ACCOUNT_TYPE = 5L;
    private static final long CLIENT_TYPE_PREFIX = 101L;
    private static final long OFFICE_NAME_PREFIX = 1L;
    private Long clientId;
    private Long loanProductId;
    private Long loanId;
    private Long savingsProductId;
    private Long savingsId;
    private final String loanPrincipalAmount = "100000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";
    private final String dateString = "04 September 2014";
    private FeignGroupHelper groupHelper;
    private FeignCenterHelper centerHelper;
    private FeignCollateralHelper collateralHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private Long clientAccountNumberPreferenceId;
    private Long loanAccountNumberPreferenceId;
    private Long savingsAccountNumberPreferenceId;
    private Long groupsAccountNumberPreferenceId;
    private Long centerAccountNumberPreferenceId;
    private static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private Boolean isAccountPreferenceSetUp = false;
    private String clientCodeValueName;
    private Long clientCodeValueId;
    private final String clientTypeName = "CLIENT_TYPE";
    private final String officeName = "OFFICE_NAME";
    private final String loanShortName = "LOAN_PRODUCT_SHORT_NAME";
    private final String savingsShortName = "SAVINGS_PRODUCT_SHORT_NAME";
    private Long groupID;
    private Long centerId;
    private String groupAccountNo;

    @BeforeAll
    public void setup() {
        this.groupHelper = new FeignGroupHelper(fineractClient());
        this.centerHelper = new FeignCenterHelper(fineractClient());
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
    }

    @Test
    public void testAccountNumberPreferences() {

        /* Create Loan and Savings Product */
        this.createLoanAndSavingsProduct();

        /* Ensure no account number preferences are present in the system */
        this.deleteAllAccountNumberPreferences();

        /*
         * Validate the default account number generation rules for clients, loans and savings accounts.
         */
        this.validateDefaultAccountNumberGeneration();

        /* Create and Validate account number preferences */
        this.createAccountNumberPreference();

        /*
         * Validate account number preference rules apply to Clients,Loans and Saving Accounts
         */
        this.validateAccountNumberGenerationWithPreferences();

        /* Validate account number preferences Updation */
        this.updateAccountNumberPreference();

        /*
         * Validate account number preference rules apply to Clients,Loans and Saving Accounts after Updation
         */
        this.validateAccountNumberGenerationWithPreferences();

        /* Delete all account number preferences */
        this.deleteAllAccountNumberPreferences();

    }

    private void createLoanAndSavingsProduct() {
        this.createLoanProduct();
        this.createSavingsProduct();
    }

    private void deleteAllAccountNumberPreferences() {
        List<GetAccountNumberFormatsIdResponse> preferenceIds = ok(
                () -> fineractClient().accountNumberFormat().retrieveAllAccountNumberFormats());
        /* Deletion of valid account preference ID */
        for (GetAccountNumberFormatsIdResponse preferenceId : preferenceIds) {
            Long id = preferenceId.getId();
            Long deletedId = ok(() -> fineractClient().accountNumberFormat().deleteAccountNumberFormat(id)).getResourceId();
            LOG.info("Successfully deleted account number preference (ID: {} )", deletedId);
        }
        /* Deletion of invalid account preference ID should fail */
        LOG.info(
                "---------------------------------DELETING ACCOUNT NUMBER PREFERENCE WITH INVALID ID------------------------------------------");

        CallFailedRuntimeException deletionError = fail(() -> fineractClient().accountNumberFormat().deleteAccountNumberFormat(10L));
        Assertions.assertEquals(404, deletionError.getStatus());
        Assertions.assertEquals("error.msg.resource.not.found", deletionError.getUserMessageGlobalisationCode());
    }

    private void validateDefaultAccountNumberGeneration() {
        this.createAndValidateClientEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateLoanEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateSavingsEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateGroup(this.isAccountPreferenceSetUp);
        this.createAndValidateCenter(this.isAccountPreferenceSetUp);
    }

    private void validateAccountNumberGenerationWithPreferences() {
        this.isAccountPreferenceSetUp = true;
        this.createAndValidateClientEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateLoanEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateSavingsEntity(this.isAccountPreferenceSetUp);
        this.createAndValidateGroup(this.isAccountPreferenceSetUp);
        this.createAndValidateCenter(this.isAccountPreferenceSetUp);
    }

    private void createAccountNumberPreference() {
        this.clientAccountNumberPreferenceId = createAccountNumberFormat(CLIENT_ACCOUNT_TYPE, CLIENT_TYPE_PREFIX);
        LOG.info("Successfully created account number preferences for Client (ID: {})", this.clientAccountNumberPreferenceId);

        this.loanAccountNumberPreferenceId = createAccountNumberFormat(LOAN_ACCOUNT_TYPE, OFFICE_NAME_PREFIX);
        LOG.info("Successfully created account number preferences for Loan (ID: {} )", this.loanAccountNumberPreferenceId);

        this.savingsAccountNumberPreferenceId = createAccountNumberFormat(SAVINGS_ACCOUNT_TYPE, OFFICE_NAME_PREFIX);
        LOG.info("Successfully created account number preferences for Savings (ID: {})", this.savingsAccountNumberPreferenceId);

        this.groupsAccountNumberPreferenceId = createAccountNumberFormat(GROUPS_ACCOUNT_TYPE, OFFICE_NAME_PREFIX);
        LOG.info("Successfully created account number preferences for Groups (ID: {})", this.groupsAccountNumberPreferenceId);

        this.centerAccountNumberPreferenceId = createAccountNumberFormat(CENTER_ACCOUNT_TYPE, OFFICE_NAME_PREFIX);
        LOG.info("Successfully created account number preferences for Center (ID: {})", this.centerAccountNumberPreferenceId);

        for (Long preferenceId : List.of(this.clientAccountNumberPreferenceId, this.loanAccountNumberPreferenceId,
                this.savingsAccountNumberPreferenceId, this.groupsAccountNumberPreferenceId, this.centerAccountNumberPreferenceId)) {
            Assertions.assertEquals(preferenceId, retrieveAccountNumberFormat(preferenceId).getId());
        }

        this.createAccountNumberPreferenceInvalidData(1000L, 1001L);
        this.createAccountNumberPreferenceDuplicateData(1L, 101L);

    }

    private void createAccountNumberPreferenceDuplicateData(final Long accountType, final Long prefixType) {
        /* Creating account Preference with duplicate data should fail */
        LOG.info(
                "---------------------------------CREATING ACCOUNT NUMBER PREFERENCE WITH DUPLICATE DATA------------------------------------------");

        CallFailedRuntimeException creationError = fail(() -> fineractClient().accountNumberFormat()
                .createAccountNumberFormat(new PostAccountNumberFormatsRequest().accountType(accountType).prefixType(prefixType)));

        Assertions.assertEquals(403, creationError.getStatus());
        Assertions.assertEquals("error.msg.account.number.format.duplicate.account.type", creationError.getUserMessageGlobalisationCode());

    }

    private void createAccountNumberPreferenceInvalidData(final Long accountType, final Long prefixType) {

        /* Creating account Preference with invalid data should fail */
        LOG.info(
                "---------------------------------CREATING ACCOUNT NUMBER PREFERENCE WITH INVALID DATA------------------------------------------");

        CallFailedRuntimeException creationError = fail(() -> fineractClient().accountNumberFormat()
                .createAccountNumberFormat(new PostAccountNumberFormatsRequest().accountType(accountType).prefixType(prefixType)));

        Assertions.assertEquals(400, creationError.getStatus());
        String errorCode = FeignErrors.firstError(creationError).userMessageGlobalisationCode();
        Assertions.assertTrue(
                List.of("validation.msg.accountNumberFormat.accountType.is.not.within.expected.range",
                        "validation.msg.accountNumberFormat.prefixType.is.not.one.of.expected.enumerations").contains(errorCode),
                errorCode);
    }

    private void updateAccountNumberPreference() {
        PutAccountNumberFormatsResponse accountNumberPreferences = ok(() -> fineractClient().accountNumberFormat()
                .updateAccountNumberFormat(this.clientAccountNumberPreferenceId, new PutAccountNumberFormatsRequest().prefixType(101L)));

        LOG.info("--------------------------UPDATION SUCCESSFUL FOR ACCOUNT NUMBER PREFERENCE ID {}",
                accountNumberPreferences.getResourceId());

        Assertions.assertEquals(accountNumberPreferences.getResourceId(),
                retrieveAccountNumberFormat(accountNumberPreferences.getResourceId()).getId());

        /* Update invalid account preference id should fail */
        LOG.info(
                "---------------------------------UPDATING ACCOUNT NUMBER PREFERENCE WITH INVALID DATA------------------------------------------");

        /* Invalid Account Type */
        CallFailedRuntimeException updationError = fail(() -> fineractClient().accountNumberFormat().updateAccountNumberFormat(9999L,
                new PutAccountNumberFormatsRequest().prefixType(101L)));
        Assertions.assertEquals(404, updationError.getStatus());
        Assertions.assertEquals("error.msg.resource.not.found", updationError.getUserMessageGlobalisationCode());
        /* Invalid Prefix Type */
        CallFailedRuntimeException updationError1 = fail(() -> fineractClient().accountNumberFormat()
                .updateAccountNumberFormat(this.clientAccountNumberPreferenceId, new PutAccountNumberFormatsRequest().prefixType(103L)));

        Assertions.assertEquals(400, updationError1.getStatus());
        Assertions.assertEquals("validation.msg.validation.errors.exist", updationError1.getUserMessageGlobalisationCode());

    }

    private void createAndValidateClientEntity(Boolean isAccountPreferenceSetUp) {
        if (isAccountPreferenceSetUp) {
            this.createAndValidateClientBasedOnAccountPreference();
        } else {
            this.createAndValidateClientWithoutAccountPreference();
        }
    }

    private void createAndValidateGroup(Boolean isAccountPreferenceSetUp) {
        this.groupID = groupHelper.createGroup().getGroupId();
        Assertions.assertEquals(this.groupID, groupHelper.retrieveGroup(this.groupID).getId());

        groupHelper.activateGroup(this.groupID);
        GetGroupsGroupIdResponse group = groupHelper.retrieveGroup(this.groupID);
        Assertions.assertTrue(group.getActive());

        this.groupAccountNo = group.getAccountNo();

        if (isAccountPreferenceSetUp) {
            String groupsPrefixName = retrieveAccountNumberFormat(this.groupsAccountNumberPreferenceId).getPrefixType().getValue();

            if (groupsPrefixName.equals(this.officeName)) {
                this.validateAccountNumberLengthAndStartsWithPrefix(this.groupAccountNo, group.getOfficeName());
            }
        } else {
            validateAccountNumberLengthAndStartsWithPrefix(this.groupAccountNo, null);
        }
    }

    private void createAndValidateCenter(Boolean isAccountPreferenceSetUp) {
        Long officeId = new OfficeHelper().createOffice(LocalDate.of(2007, 7, 1)).getResourceId();

        String name = "CenterCreation" + new Timestamp(new java.util.Date().getTime());
        this.centerId = centerHelper.createCenter(name, officeId).getResourceId();

        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(this.centerId);

        Assertions.assertNotNull(center);
        Assertions.assertTrue(center.getName().equals(name));

        if (isAccountPreferenceSetUp) {
            String centerPrefixName = retrieveAccountNumberFormat(this.centerAccountNumberPreferenceId).getPrefixType().getValue();

            if (centerPrefixName.equals(this.officeName)) {
                this.validateAccountNumberLengthAndStartsWithPrefix(center.getAccountNo(), center.getOfficeName());
            }
        } else {
            validateAccountNumberLengthAndStartsWithPrefix(center.getAccountNo(), null);
        }
    }

    private void createAndValidateClientWithoutAccountPreference() {
        this.clientId = createClient();
        Assertions.assertNotNull(this.clientId);
        String clientAccountNo = clientHelper.getClient(this.clientId).getAccountNo();
        validateAccountNumberLengthAndStartsWithPrefix(clientAccountNo, null);
    }

    private void createAndValidateClientBasedOnAccountPreference() {
        final String codeName = "ClientType";
        String clientAccountNo = null;
        String clientPrefixName = retrieveAccountNumberFormat(this.clientAccountNumberPreferenceId).getPrefixType().getValue();
        if (clientPrefixName.equals(this.clientTypeName)) {

            /* Retrieve/Create Code Values for the Code "ClientType" */
            Long clientTypeCodeId = codeHelper.retrieveCodeByName(codeName).getId();
            codeHelper.retrieveOrCreateCodeValueId(codeName);
            GetCodeValuesDataResponse codeValue = codeHelper.retrieveAllCodeValues(clientTypeCodeId).get(0);

            this.clientCodeValueName = codeValue.getName();
            this.clientCodeValueId = codeValue.getId();

            /* Create Client with Client Type */
            this.clientId = clientHelper.createClient(
                    ClientRequestBuilders.createActivePersonClient(DEFAULT_CLIENT_ACTIVATION_DATE).clientTypeId(this.clientCodeValueId))
                    .getClientId();

            GetClientsClientIdResponse client = clientHelper.getClient(this.clientId);
            Assertions.assertEquals(this.clientId, client.getId());

            // Assertions.assertNotNull(clientId);

            clientAccountNo = client.getAccountNo();
            this.validateAccountNumberLengthAndStartsWithPrefix(clientAccountNo, this.clientCodeValueName);

        } else if (clientPrefixName.equals(this.officeName)) {
            this.clientId = createClient();
            GetClientsClientIdResponse client = clientHelper.getClient(this.clientId);
            Assertions.assertEquals(this.clientId, client.getId());
            // Assertions.assertNotNull(clientId);
            clientAccountNo = client.getAccountNo();
            this.validateAccountNumberLengthAndStartsWithPrefix(clientAccountNo, client.getOfficeName());
        }
    }

    private void validateAccountNumberLengthAndStartsWithPrefix(final String accountNumber, String prefix) {
        if (prefix != null) {
            prefix = prefix.substring(0, Math.min(prefix.length(), 10));
            Assertions.assertEquals(accountNumber.length(), prefix.length() + 9);
            Assertions.assertTrue(accountNumber.startsWith(prefix));
        } else {
            Assertions.assertEquals(9, accountNumber.length());
        }
    }

    private void createLoanProduct() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");
        this.loanProductId = createLoanProduct(
                new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount).withNumberOfRepayments(numberOfRepayments)
                        .withinterestRatePerPeriod(interestRatePerPeriod).withInterestRateFrequencyTypeAsYear().buildRequest());
        LOG.info("Successfully created loan product  (ID: {} )", this.loanProductId);
    }

    private void createAndValidateLoanEntity(Boolean isAccountPreferenceSetUp) {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(this.clientId, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);

        final PostLoansRequest loanApplication = new PostLoansRequest().clientId(this.clientId).productId(this.loanProductId)
                .loanType("individual").principal(new BigDecimal(loanPrincipalAmount))
                .loanTermFrequency(Integer.valueOf(numberOfRepayments)).loanTermFrequencyType(2)
                .numberOfRepayments(Integer.valueOf(numberOfRepayments)).repaymentEvery(1).repaymentFrequencyType(2).amortizationType(1)
                .interestCalculationPeriodType(0).interestType(1).interestRatePerPeriod(new BigDecimal(interestRatePerPeriod))
                .submittedOnDate(dateString).expectedDisbursementDate(dateString).graceOnPrincipalPayment(2).graceOnInterestPayment(2)
                .transactionProcessingStrategyCode(LoanApplicationTestBuilder.DEFAULT_STRATEGY)
                .maxOutstandingLoanBalance(new BigDecimal("36000"))
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)))
                .dateFormat("dd MMMM yyyy").locale("en_GB");
        this.loanId = loanHelper.applyForLoan(loanApplication).getLoanId();

        String loanAccountNo = loanHelper.getLoanDetails(this.loanId).getAccountNo();

        if (isAccountPreferenceSetUp) {
            String loanPrefixName = retrieveAccountNumberFormat(this.loanAccountNumberPreferenceId).getPrefixType().getValue();

            if (loanPrefixName.equals(this.officeName)) {
                String loanOfficeName = clientHelper.getClient(this.clientId).getOfficeName();
                this.validateAccountNumberLengthAndStartsWithPrefix(loanAccountNo, loanOfficeName);
            } else if (loanPrefixName.equals(this.loanShortName)) {
                String loanShortName = loanHelper.retrieveLoanProduct(this.loanProductId).getShortName();
                this.validateAccountNumberLengthAndStartsWithPrefix(loanAccountNo, loanShortName);
            }
            LOG.info("SUCCESSFULLY CREATED LOAN APPLICATION BASED ON ACCOUNT PREFERENCES (ID: {} )", this.loanId);
        } else {
            this.validateAccountNumberLengthAndStartsWithPrefix(loanAccountNo, null);
            LOG.info("SUCCESSFULLY CREATED LOAN APPLICATION (ID: {} )", loanId);
        }
    }

    private void createSavingsProduct() {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        this.savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(new BigDecimal(MINIMUM_OPENING_BALANCE))).getResourceId();
        LOG.info("Sucessfully created savings product (ID: {} )", this.savingsProductId);
    }

    private void createAndValidateSavingsEntity(Boolean isAccountPreferenceSetUp) {
        this.savingsId = savingsHelper.submitApplication(this.clientId, this.savingsProductId, SAVINGS_SUBMITTED_DATE).getSavingsId();

        String savingsAccountNo = savingsHelper.getSavingsDetails(this.savingsId).getAccountNo();

        if (isAccountPreferenceSetUp) {
            String savingsPrefixName = retrieveAccountNumberFormat(this.savingsAccountNumberPreferenceId).getPrefixType().getValue();

            if (savingsPrefixName.equals(this.officeName)) {
                String savingsOfficeName = clientHelper.getClient(this.clientId).getOfficeName();
                this.validateAccountNumberLengthAndStartsWithPrefix(savingsAccountNo, savingsOfficeName);
            } else if (savingsPrefixName.equals(this.savingsShortName)) {
                String savingsShortName = savingsProductHelper.getSavingsProduct(this.savingsProductId).getShortName();
                this.validateAccountNumberLengthAndStartsWithPrefix(savingsAccountNo, savingsShortName);
            }
            LOG.info("SUCCESSFULLY CREATED SAVINGS APPLICATION BASED ON ACCOUNT PREFERENCES (ID:  {} )", this.savingsId);
        } else {
            this.validateAccountNumberLengthAndStartsWithPrefix(savingsAccountNo, null);
            LOG.info("SUCCESSFULLY CREATED SAVINGS APPLICATION (ID:{} )", this.savingsId);
        }
    }

    private Long createAccountNumberFormat(long accountType, long prefixType) {
        return ok(() -> fineractClient().accountNumberFormat()
                .createAccountNumberFormat(new PostAccountNumberFormatsRequest().accountType(accountType).prefixType(prefixType)))
                .getResourceId();
    }

    private GetAccountNumberFormatsIdResponse retrieveAccountNumberFormat(Long accountNumberFormatId) {
        return ok(() -> fineractClient().accountNumberFormat().retrieveOneAccountNumberFormat(accountNumberFormatId));
    }
}
