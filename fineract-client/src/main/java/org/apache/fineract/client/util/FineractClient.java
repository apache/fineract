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
package org.apache.fineract.client.util;

import org.apache.fineract.client.ApiClient;
import org.apache.fineract.client.auth.ApiKeyAuth;
import org.apache.fineract.client.services.*;

/**
 * Fineract Client Java SDK API entry point. This is recommended to be used instead of {@link ApiClient}.
 *
 * @author Michael Vorburger.ch
 */
public class FineractClient {

    private final ApiClient api;

    public final AccountingClosureApi glClosures;
    public final AccountingRulesApi accountingRules;
    public final AccountNumberFormatApi accountNumberFormats;
    public final AccountTransfersApi accountTransfers;
    public final AdhocQueryApiApi adhocQuery;
    public final AuditsApi audits;
    public final AuthenticationHttpBasicApi authentication;
    public final BatchApiApi batches;
    public final CacheApi caches;
    public final CashierJournalsApi cashiersJournal;
    public final CashiersApi cashiers;
    public final CentersApi centers;
    public final ChargesApi charges;
    public final ClientApi clients;
    public final ClientChargesApi clientCharges;
    public final ClientIdentifierApi clientIdentifiers;
    public final ClientsAddressApi clientAddresses;
    public final ClientTransactionApi clientTransactions;
    public final CodesApi codes;
    public final CodeValuesApi codeValues;
    public final CurrencyApi currencies;
    public final DataTablesApi dataTables;
    public final @Deprecated DefaultApi legacy; // TODO FINERACT-1222
    public final DocumentsApi documents;
    public final EntityDataTableApi entityDatatableChecks;
    public final EntityFieldConfigurationApi entityFieldConfigurations;
    public final ExternalServicesApi externalServices;
    public final FetchAuthenticatedUserDetailsApi userDetails;
    public final FixedDepositAccountApi fixedDepositAccounts;
    public final FixedDepositProductApi fixedDepositProducts;
    public final FloatingRatesApi floatingRates;
    public final GeneralLedgerAccountApi glAccounts;
    public final GlobalConfigurationApi globalConfigurations;
    public final GroupsApi groups;
    public final HolidaysApi holidays;
    public final HooksApi hooks;
    public final InterestRateChartApi interestRateCharts;
    public final InterestRateSlabAKAInterestBandsApi interestRateChartLabs;
    public final JournalEntriesApi journalEntries;
    public final ListReportMailingJobHistoryApi reportMailings;
    public final LoanChargesApi loanCharges;
    public final LoanCollateralApi loanCollaterals;
    public final LoanProductsApi loanProducts;
    public final LoanReschedulingApi loanSchedules;
    public final LoansApi loans;
    public final LoanTransactionsApi loanTransactions;
    public final MakerCheckerOr4EyeFunctionalityApi makerCheckers;
    public final MappingFinancialActivitiesToAccountsApi financialActivyAccountMappings;
    public final MifosxBatchJobsApi jobs;
    public final MixMappingApi mixMappings;
    public final MixReportApi mixReports;
    public final MixTaxonomyApi mixTaxonomies;
    public final NotesApi notes;
    public final NotificationApi notifications;
    public final OfficesApi offices;
    public final PasswordPreferencesApi passwordPreferences;
    public final PaymentTypeApi paymentTypes;
    public final PeriodicAccrualAccountingApi periodicAccrualAccounting;
    public final PermissionsApi permissions;
    public final PocketApi selfPockets;
    public final ProvisioningCategoryApi provisioningCategories;
    public final ProvisioningCriteriaApi provisioningCriterias;
    public final ProvisioningEntriesApi provisioningEntries;
    public final RecurringDepositAccountApi recurringDepositAccounts;
    public final RecurringDepositAccountTransactionsApi recurringDepositAccountTransactions;
    public final RecurringDepositProductApi recurringDepositProducts;
    public final ReportMailingJobsApi reportMailingJobs;
    public final ReportsApi reports;
    public final RolesApi roles;
    public final RunReportsApi reportsRun;
    public final SavingsAccountApi savingsAccounts;
    public final SavingsChargesApi savingsAccountCharges;
    public final SavingsProductApi savingsProducts;
    public final SchedulerApi jobsScheduler;
    public final ScoreCardApi surveyScorecards;
    public final SearchApiApi search;
    public final SelfAccountTransferApi selfAccountTransfers;
    public final SelfAuthenticationApi selfAuthentication;
    public final SelfClientApi selfClients;
    public final SelfDividendApi selfShareProducts;
    public final SelfLoanProductsApi selfLoanProducts;
    public final SelfLoansApi selfLoans;
    public final SelfRunReportApi selfReportsRun;
    public final SelfSavingsAccountApi selfSavingsAccounts;
    public final SelfScoreCardApi selfSurveyScorecards;
    public final SelfServiceRegistrationApi selfRegistration;
    public final SelfShareAccountsApi selfShareAccounts;
    public final SelfSpmApi selfSurveys;
    public final SelfThirdPartyTransferApi selfThirdPartyBeneficiaries;
    public final SelfUserApi selfUser;
    public final SelfUserDetailsApi selfUserDetails;
    public final ShareAccountApi shareAccounts;
    public final SpmApiLookUpTableApi surveyLookupTables;
    public final SpmSurveysApi surveys;
    public final StaffApi staff;
    public final StandingInstructionsApi standingInstructions;
    public final StandingInstructionsHistoryApi standingInstructionsHistory;
    public final TaxComponentsApi taxComponents;
    public final TaxGroupApi taxGroups;
    public final TellerCashManagementApi tellers;
    public final UserGeneratedDocumentsApi templates;
    public final UsersApi users;
    public final WorkingDaysApi workingDays;

    private FineractClient(ApiClient apiClient) {
        api = apiClient;

        glClosures = apiClient.createService(AccountingClosureApi.class);
        accountingRules = apiClient.createService(AccountingRulesApi.class);
        accountNumberFormats = apiClient.createService(AccountNumberFormatApi.class);
        accountTransfers = apiClient.createService(AccountTransfersApi.class);
        adhocQuery = apiClient.createService(AdhocQueryApiApi.class);
        audits = apiClient.createService(AuditsApi.class);
        authentication = apiClient.createService(AuthenticationHttpBasicApi.class);
        batches = apiClient.createService(BatchApiApi.class);
        caches = apiClient.createService(CacheApi.class);
        cashiersJournal = apiClient.createService(CashierJournalsApi.class);
        cashiers = apiClient.createService(CashiersApi.class);
        centers = apiClient.createService(CentersApi.class);
        charges = apiClient.createService(ChargesApi.class);
        clients = apiClient.createService(ClientApi.class);
        clientCharges = apiClient.createService(ClientChargesApi.class);
        clientIdentifiers = apiClient.createService(ClientIdentifierApi.class);
        clientAddresses = apiClient.createService(ClientsAddressApi.class);
        clientTransactions = apiClient.createService(ClientTransactionApi.class);
        codes = apiClient.createService(CodesApi.class);
        codeValues = apiClient.createService(CodeValuesApi.class);
        currencies = apiClient.createService(CurrencyApi.class);
        dataTables = apiClient.createService(DataTablesApi.class);
        legacy = apiClient.createService(DefaultApi.class);
        documents = apiClient.createService(DocumentsApi.class);
        entityDatatableChecks = apiClient.createService(EntityDataTableApi.class);
        entityFieldConfigurations = apiClient.createService(EntityFieldConfigurationApi.class);
        externalServices = apiClient.createService(ExternalServicesApi.class);
        userDetails = apiClient.createService(FetchAuthenticatedUserDetailsApi.class);
        fixedDepositAccounts = apiClient.createService(FixedDepositAccountApi.class);
        fixedDepositProducts = apiClient.createService(FixedDepositProductApi.class);
        floatingRates = apiClient.createService(FloatingRatesApi.class);
        glAccounts = apiClient.createService(GeneralLedgerAccountApi.class);
        globalConfigurations = apiClient.createService(GlobalConfigurationApi.class);
        groups = apiClient.createService(GroupsApi.class);
        holidays = apiClient.createService(HolidaysApi.class);
        hooks = apiClient.createService(HooksApi.class);
        interestRateCharts = apiClient.createService(InterestRateChartApi.class);
        interestRateChartLabs = apiClient.createService(InterestRateSlabAKAInterestBandsApi.class);
        journalEntries = apiClient.createService(JournalEntriesApi.class);
        reportMailings = apiClient.createService(ListReportMailingJobHistoryApi.class);
        loanCharges = apiClient.createService(LoanChargesApi.class);
        loanCollaterals = apiClient.createService(LoanCollateralApi.class);
        loanProducts = apiClient.createService(LoanProductsApi.class);
        loanSchedules = apiClient.createService(LoanReschedulingApi.class);
        loans = apiClient.createService(LoansApi.class);
        loanTransactions = apiClient.createService(LoanTransactionsApi.class);
        makerCheckers = apiClient.createService(MakerCheckerOr4EyeFunctionalityApi.class);
        financialActivyAccountMappings = apiClient.createService(MappingFinancialActivitiesToAccountsApi.class);
        jobs = apiClient.createService(MifosxBatchJobsApi.class);
        mixMappings = apiClient.createService(MixMappingApi.class);
        mixReports = apiClient.createService(MixReportApi.class);
        mixTaxonomies = apiClient.createService(MixTaxonomyApi.class);
        notes = apiClient.createService(NotesApi.class);
        notifications = apiClient.createService(NotificationApi.class);
        offices = apiClient.createService(OfficesApi.class);
        passwordPreferences = apiClient.createService(PasswordPreferencesApi.class);
        paymentTypes = apiClient.createService(PaymentTypeApi.class);
        periodicAccrualAccounting = apiClient.createService(PeriodicAccrualAccountingApi.class);
        permissions = apiClient.createService(PermissionsApi.class);
        selfPockets = apiClient.createService(PocketApi.class);
        provisioningCategories = apiClient.createService(ProvisioningCategoryApi.class);
        provisioningCriterias = apiClient.createService(ProvisioningCriteriaApi.class);
        provisioningEntries = apiClient.createService(ProvisioningEntriesApi.class);
        recurringDepositAccounts = apiClient.createService(RecurringDepositAccountApi.class);
        recurringDepositAccountTransactions = apiClient.createService(RecurringDepositAccountTransactionsApi.class);
        recurringDepositProducts = apiClient.createService(RecurringDepositProductApi.class);
        reportMailingJobs = apiClient.createService(ReportMailingJobsApi.class);
        reports = apiClient.createService(ReportsApi.class);
        roles = apiClient.createService(RolesApi.class);
        reportsRun = apiClient.createService(RunReportsApi.class);
        savingsAccounts = apiClient.createService(SavingsAccountApi.class);
        savingsAccountCharges = apiClient.createService(SavingsChargesApi.class);
        savingsProducts = apiClient.createService(SavingsProductApi.class);
        jobsScheduler = apiClient.createService(SchedulerApi.class);
        surveyScorecards = apiClient.createService(ScoreCardApi.class);
        search = apiClient.createService(SearchApiApi.class);
        selfAccountTransfers = apiClient.createService(SelfAccountTransferApi.class);
        selfAuthentication = apiClient.createService(SelfAuthenticationApi.class);
        selfClients = apiClient.createService(SelfClientApi.class);
        selfShareProducts = apiClient.createService(SelfDividendApi.class);
        selfLoanProducts = apiClient.createService(SelfLoanProductsApi.class);
        selfLoans = apiClient.createService(SelfLoansApi.class);
        selfReportsRun = apiClient.createService(SelfRunReportApi.class);
        selfSavingsAccounts = apiClient.createService(SelfSavingsAccountApi.class);
        selfSurveyScorecards = apiClient.createService(SelfScoreCardApi.class);
        selfRegistration = apiClient.createService(SelfServiceRegistrationApi.class);
        selfShareAccounts = apiClient.createService(SelfShareAccountsApi.class);
        selfSurveys = apiClient.createService(SelfSpmApi.class);
        selfThirdPartyBeneficiaries = apiClient.createService(SelfThirdPartyTransferApi.class);
        selfUser = apiClient.createService(SelfUserApi.class);
        selfUserDetails = apiClient.createService(SelfUserDetailsApi.class);
        shareAccounts = apiClient.createService(ShareAccountApi.class);
        surveyLookupTables = apiClient.createService(SpmApiLookUpTableApi.class);
        surveys = apiClient.createService(SpmSurveysApi.class);
        staff = apiClient.createService(StaffApi.class);
        standingInstructions = apiClient.createService(StandingInstructionsApi.class);
        standingInstructionsHistory = apiClient.createService(StandingInstructionsHistoryApi.class);
        taxComponents = apiClient.createService(TaxComponentsApi.class);
        taxGroups = apiClient.createService(TaxGroupApi.class);
        tellers = apiClient.createService(TellerCashManagementApi.class);
        templates = apiClient.createService(UserGeneratedDocumentsApi.class);
        users = apiClient.createService(UsersApi.class);
        workingDays = apiClient.createService(WorkingDaysApi.class);
    }

    public static FineractClientBuilder builder() {
        return new FineractClientBuilder();
    }

    protected <S> S createService(Class<S> serviceClass) {
        return api.createService(serviceClass);
    }

    public static class FineractClientBuilder {

        private String baseURL;
        private String tenant;
        private String username;
        private String password;

        private FineractClientBuilder() {}

        public FineractClientBuilder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public FineractClientBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public FineractClientBuilder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public FineractClient build() {
            ApiClient apiClient = new ApiClient("basicAuth", has("username", username), has("password", password));
            apiClient.getAdapterBuilder().baseUrl(has("baseURL", baseURL));
            ApiKeyAuth authorization = new ApiKeyAuth("header", "fineract-platform-tenantid");
            authorization.setApiKey(has("tenant", tenant));
            apiClient.addAuthorization("tenantid", authorization);
            return new FineractClient(apiClient);
        }

        private <T> T has(String propertyName, T value) throws IllegalStateException {
            if (value == null) {
                throw new IllegalStateException("Must call " + propertyName + "(...) to create valid Builder");
            }
            return value;
        }
    }
}
