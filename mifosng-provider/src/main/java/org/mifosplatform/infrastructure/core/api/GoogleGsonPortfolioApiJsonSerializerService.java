package org.mifosplatform.infrastructure.core.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.security.data.AuthenticatedUserData;
import org.mifosplatform.organisation.staff.data.BulkTransferLoanOfficerData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.NoteData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioApiJsonSerializerService} that uses
 * google-gson to serialize Java object representation into JSON.
 */
@Service
public class GoogleGsonPortfolioApiJsonSerializerService implements PortfolioApiJsonSerializerService {

    private static final Set<String> STAFF_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "firstname", "lastname",
            "displayName", "officeId", "officeName", "loanOfficerFlag", "allowedOffices"));

    private static final Set<String> SAVINGS_PRODUCT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("currencyOptions", "id",
            "createdOn", "lastModifedOn", "locale", "name", "description", "currencyCode", "digitsAfterDecimal", "interstRate",
            "minInterestRate", "maxInterestRate", "savingsDepositAmount", "savingProductType", "tenureType", "tenure", "frequency",
            "interestType", "interestCalculationMethod", "minimumBalanceForWithdrawal", "isPartialDepositAllowed", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType", "currencyOptions", "savingsProductTypeOptions", "tenureTypeOptions",
            "savingFrequencyOptions", "savingsInterestTypeOptions", "lockinPeriodTypeOptions", "interestCalculationOptions"));

    private static final Set<String> SAVINGS_DEPOSIT_PRODUCT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("currencyOptions",
            "interestCompoundedEveryPeriodTypeOptions", "id", "externalId", "name", "description", "createdOn", "lastModifedOn",
            "currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths", "maturityDefaultInterestRate",
            "maturityMinInterestRate", "maturityMaxInterestRate", "interestCompoundedEvery", "interestCompoundedEveryPeriodType",
            "renewalAllowed", "preClosureAllowed", "preClosureInterestRate", "interestCompoundingAllowed", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType"));

    private static final Set<String> SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("productOptions",
            "interestCompoundedEveryPeriodTypeOptions", "id", "externalId", "clientId", "clientName", "productId", "productName", "status",
            "currency", "deposit", "maturityInterestRate", "tenureInMonths", "interestCompoundedEvery",
            "interestCompoundedEveryPeriodType", "renewalAllowed", "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate",
            "rejectedonDate", "closedonDate", "transactions", "permissions", "isInterestWithdrawable", "interestPaid",
            "interestCompoundingAllowed", "availableInterestForWithdrawal", "availableWithdrawalAmount", "todaysDate",
            "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType"));

    private static final Set<String> DOCUMENT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "parentEntityType",
            "parentEntityId", "name", "fileName", "type", "size", "description"));

    private static final Set<String> CLIENT_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("pendingApprovalLoans",
            "awaitingDisbursalLoans", "openLoans", "closedLoans", "anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount",
            "activeLoanCount", "closedLoanCount", "pendingApprovalDespositAccountsCount", "pendingApprovalDespositAccounts",
            "approvedDespositAccountsCount", "approvedDespositAccounts", "withdrawnByClientDespositAccountsCount",
            "withdrawnByClientDespositAccounts", "closedDepositAccountsCount", "closedDepositAccounts", "rejectedDepositAccountsCount",
            "rejectedDepositAccounts", "preclosedDepositAccountsCount", "preclosedDepositAccounts"));

    private static final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("pendingApprovalLoans",
            "awaitingDisbursalLoans", "openLoans", "closedLoans", "anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount",
            "activeLoanCount", "closedLoanCount"));

    private static final Set<String> GROUP_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "name", "externalId",
            "clientMembers", "allowedClients", "allowedOffices"));

    private static final Set<String> NOTE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientId", "loanId",
            "loanTransactionId", "noteType", "note", "createdById", "createdByUsername", "createdOn", "updatedById", "updatedByUsername",
            "updatedOn"));

    private static final Set<String> LOAN_SCHEDULE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("periods",
            "cumulativePrincipalDisbursed"));

    private static final Set<String> LOAN_TRANSACTION_NEW_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "type", "date",
            "currency", "amount"));

    private static final Set<String> LOAN_REASSIGNMENT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("officeId", "fromLoanOfficerId",
            "assignmentDate", "officeOptions", "loanOfficerOptions", "accountSummaryCollection"));

    private static final Set<String> LOAN_CHARGES_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("chargeOptions"));

    private static final Set<String> SAVINGS_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "status", "externalId",
            "clientId", "clientName", "productId", "productName", "productType", "currencyData", "savingsDepostiAmountPerPeriod",
            "savingsFrequencyType", "totalDepositAmount", "reccuringInterestRate", "savingInterestRate", "interestType",
            "interestCalculationMethod", "tenure", "tenureType", "projectedCommencementDate", "actualCommencementDate", "maturesOnDate",
            "projectedInterestAccuredOnMaturity", "actualInterestAccured", "projectedMaturityAmount", "actualMaturityAmount",
            "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate", "rejectedonDate", "closedonDate", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType"));

    // guarantors
    private static final Set<String> GUARANTOR_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("externalGuarantor", "existingClientId",
            "firstname", "lastname", "addressLine1", "addressLine2", "city", "state", "zip", "country", "mobileNumber", "housePhoneNumber",
            "comment", "dob"));

    private static final Set<String> SAVING_SCHEDULE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("periods", "cumulativeDepositDue"));

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public GoogleGsonPortfolioApiJsonSerializerService(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeAuthenticatedUserDataToJson(final boolean prettyPrint, final AuthenticatedUserData authenticatedUserData) {
        final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
        return helper.serializedJsonFrom(gsonDeserializer, authenticatedUserData);
    }

    @Override
    public String serializeGenericResultsetDataToJson(final boolean prettyPrint, final GenericResultsetData resultsetData) {
        final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
        return helper.serializedJsonFrom(gsonDeserializer, resultsetData);
    }

    @Override
    public String serializeDatatableDataToJson(final boolean prettyPrint, final Collection<DatatableData> datatables) {
        final Gson gsonDeserializer = helper.createGsonBuilder(prettyPrint);
        return helper.serializedJsonFrom(gsonDeserializer, datatables.toArray(new DatatableData[datatables.size()]));
    }

    @Override
    public String serializeSavingProductDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final Collection<SavingProductData> products) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(SAVINGS_PRODUCT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, products.toArray(new SavingProductData[products.size()]));
    }

    @Override
    public String serializeSavingProductDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final SavingProductData savingProduct) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(SAVINGS_PRODUCT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, savingProduct);
    }

    @Override
    public String serializeDepositProductDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final Collection<DepositProductData> products) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                SAVINGS_DEPOSIT_PRODUCT_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, products.toArray(new DepositProductData[products.size()]));
    }

    @Override
    public String serializeDepositProductDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final DepositProductData depositProduct) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                SAVINGS_DEPOSIT_PRODUCT_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, depositProduct);
    }

    @Override
    public String serializeDepositAccountDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final Collection<DepositAccountData> accounts) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, accounts.toArray(new DepositAccountData[accounts.size()]));
    }

    @Override
    public String serializeDepositAccountDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final DepositAccountData account) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, account);
    }

    @Override
    public String serializeClientAccountSummaryCollectionDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final ClientAccountSummaryCollectionData clientAccount) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(CLIENT_ACCOUNTS_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, clientAccount);
    }

    @Override
    public String serializeGroupAccountSummaryCollectionDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final GroupAccountSummaryCollectionData groupAccount) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GROUP_ACCOUNTS_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, groupAccount);
    }

    @Override
    public String serializeGroupDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final Collection<GroupData> groups) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GROUP_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, groups.toArray(new GroupData[groups.size()]));
    }

    @Override
    public String serializeGroupDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final GroupData group) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GROUP_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, group);
    }

    @Override
    public String serializeNoteDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<NoteData> notes) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(NOTE_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, notes.toArray(new NoteData[notes.size()]));
    }

    @Override
    public String serializeNoteDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final NoteData note) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(NOTE_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, note);
    }

    @Override
    public String serializeLoanScheduleDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final LoanScheduleData loanSchedule) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(LOAN_SCHEDULE_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, loanSchedule);
    }

    @Override
    public String serializeLoanTransactionDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final LoanTransactionData transaction) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                LOAN_TRANSACTION_NEW_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, transaction);
    }

    @Override
    public String serializeLoanReassignmentDataToJson(boolean prettyPrint, Set<String> responseParameters,
            BulkTransferLoanOfficerData loanReassignmentData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                LOAN_REASSIGNMENT_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, loanReassignmentData);
    }

    @Override
    public String serializeLoanChargeDataToJson(boolean prettyPrint, Set<String> responseParameters, LoanChargeData charge) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(LOAN_CHARGES_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, charge);
    }

    @Override
    public String serializeStaffDataToJson(boolean prettyPrint, Set<String> responseParameters, StaffData staff) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(STAFF_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, staff);
    }

    @Override
    public String serializeStaffDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<StaffData> staff) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(STAFF_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, staff.toArray(new StaffData[staff.size()]));
    }

    @Override
    public String serializeDocumentDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DocumentData> documentDatas) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(DOCUMENT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, documentDatas.toArray(new DocumentData[documentDatas.size()]));
    }

    @Override
    public String serializeDocumentDataToJson(boolean prettyPrint, Set<String> responseParameters, DocumentData documentData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(DOCUMENT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, documentData);
    }

    @Override
    public String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingAccountData account) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(SAVINGS_ACCOUNTS_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, account);
    }

    @Override
    public String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters,
            Collection<SavingAccountData> accounts) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(SAVINGS_ACCOUNTS_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, accounts.toArray(new SavingAccountData[accounts.size()]));
    }

    @Override
    public String serializeGuarantorDataToJson(boolean prettyPrint, Set<String> responseParameters, GuarantorData guarantorData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GUARANTOR_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, guarantorData);
    }

    @Override
    public String serializeSavingScheduleDataToJson(boolean prettyPrint, Set<String> responseParameters,
            SavingScheduleData savingScheduleData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(SAVING_SCHEDULE_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, savingScheduleData);
    }
}