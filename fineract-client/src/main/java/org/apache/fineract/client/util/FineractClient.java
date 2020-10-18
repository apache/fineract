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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.fineract.client.ApiClient;
import org.apache.fineract.client.auth.ApiKeyAuth;
import org.apache.fineract.client.auth.HttpBasicAuth;
import org.apache.fineract.client.services.AccountNumberFormatApi;
import org.apache.fineract.client.services.AccountTransfersApi;
import org.apache.fineract.client.services.AccountingClosureApi;
import org.apache.fineract.client.services.AccountingRulesApi;
import org.apache.fineract.client.services.AdhocQueryApiApi;
import org.apache.fineract.client.services.AuditsApi;
import org.apache.fineract.client.services.AuthenticationHttpBasicApi;
import org.apache.fineract.client.services.BatchApiApi;
import org.apache.fineract.client.services.CacheApi;
import org.apache.fineract.client.services.CashierJournalsApi;
import org.apache.fineract.client.services.CashiersApi;
import org.apache.fineract.client.services.CentersApi;
import org.apache.fineract.client.services.ChargesApi;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.ClientChargesApi;
import org.apache.fineract.client.services.ClientIdentifierApi;
import org.apache.fineract.client.services.ClientTransactionApi;
import org.apache.fineract.client.services.ClientsAddressApi;
import org.apache.fineract.client.services.CodeValuesApi;
import org.apache.fineract.client.services.CodesApi;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.client.services.DataTablesApi;
import org.apache.fineract.client.services.DefaultApi;
import org.apache.fineract.client.services.DocumentsApi;
import org.apache.fineract.client.services.EntityDataTableApi;
import org.apache.fineract.client.services.EntityFieldConfigurationApi;
import org.apache.fineract.client.services.ExternalServicesApi;
import org.apache.fineract.client.services.FetchAuthenticatedUserDetailsApi;
import org.apache.fineract.client.services.FixedDepositAccountApi;
import org.apache.fineract.client.services.FixedDepositProductApi;
import org.apache.fineract.client.services.FloatingRatesApi;
import org.apache.fineract.client.services.GeneralLedgerAccountApi;
import org.apache.fineract.client.services.GlobalConfigurationApi;
import org.apache.fineract.client.services.GroupsApi;
import org.apache.fineract.client.services.HolidaysApi;
import org.apache.fineract.client.services.HooksApi;
import org.apache.fineract.client.services.InterestRateChartApi;
import org.apache.fineract.client.services.InterestRateSlabAKAInterestBandsApi;
import org.apache.fineract.client.services.JournalEntriesApi;
import org.apache.fineract.client.services.ListReportMailingJobHistoryApi;
import org.apache.fineract.client.services.LoanChargesApi;
import org.apache.fineract.client.services.LoanCollateralApi;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.client.services.LoanReschedulingApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.MakerCheckerOr4EyeFunctionalityApi;
import org.apache.fineract.client.services.MappingFinancialActivitiesToAccountsApi;
import org.apache.fineract.client.services.MifosxBatchJobsApi;
import org.apache.fineract.client.services.MixMappingApi;
import org.apache.fineract.client.services.MixReportApi;
import org.apache.fineract.client.services.MixTaxonomyApi;
import org.apache.fineract.client.services.NotesApi;
import org.apache.fineract.client.services.NotificationApi;
import org.apache.fineract.client.services.OfficesApi;
import org.apache.fineract.client.services.PasswordPreferencesApi;
import org.apache.fineract.client.services.PaymentTypeApi;
import org.apache.fineract.client.services.PeriodicAccrualAccountingApi;
import org.apache.fineract.client.services.PermissionsApi;
import org.apache.fineract.client.services.PocketApi;
import org.apache.fineract.client.services.ProvisioningCategoryApi;
import org.apache.fineract.client.services.ProvisioningCriteriaApi;
import org.apache.fineract.client.services.ProvisioningEntriesApi;
import org.apache.fineract.client.services.RecurringDepositAccountApi;
import org.apache.fineract.client.services.RecurringDepositAccountTransactionsApi;
import org.apache.fineract.client.services.RecurringDepositProductApi;
import org.apache.fineract.client.services.ReportMailingJobsApi;
import org.apache.fineract.client.services.ReportsApi;
import org.apache.fineract.client.services.RolesApi;
import org.apache.fineract.client.services.RunReportsApi;
import org.apache.fineract.client.services.SavingsAccountApi;
import org.apache.fineract.client.services.SavingsChargesApi;
import org.apache.fineract.client.services.SavingsProductApi;
import org.apache.fineract.client.services.SchedulerApi;
import org.apache.fineract.client.services.ScoreCardApi;
import org.apache.fineract.client.services.SearchApiApi;
import org.apache.fineract.client.services.SelfAccountTransferApi;
import org.apache.fineract.client.services.SelfAuthenticationApi;
import org.apache.fineract.client.services.SelfClientApi;
import org.apache.fineract.client.services.SelfDividendApi;
import org.apache.fineract.client.services.SelfLoanProductsApi;
import org.apache.fineract.client.services.SelfLoansApi;
import org.apache.fineract.client.services.SelfRunReportApi;
import org.apache.fineract.client.services.SelfSavingsAccountApi;
import org.apache.fineract.client.services.SelfScoreCardApi;
import org.apache.fineract.client.services.SelfServiceRegistrationApi;
import org.apache.fineract.client.services.SelfShareAccountsApi;
import org.apache.fineract.client.services.SelfSpmApi;
import org.apache.fineract.client.services.SelfThirdPartyTransferApi;
import org.apache.fineract.client.services.SelfUserApi;
import org.apache.fineract.client.services.SelfUserDetailsApi;
import org.apache.fineract.client.services.ShareAccountApi;
import org.apache.fineract.client.services.SpmApiLookUpTableApi;
import org.apache.fineract.client.services.SpmSurveysApi;
import org.apache.fineract.client.services.StaffApi;
import org.apache.fineract.client.services.StandingInstructionsApi;
import org.apache.fineract.client.services.StandingInstructionsHistoryApi;
import org.apache.fineract.client.services.TaxComponentsApi;
import org.apache.fineract.client.services.TaxGroupApi;
import org.apache.fineract.client.services.TellerCashManagementApi;
import org.apache.fineract.client.services.UserGeneratedDocumentsApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.client.services.WorkingDaysApi;

/**
 * Fineract Client Java SDK API entry point. This is recommended to be used instead of {@link ApiClient}.
 *
 * @author Michael Vorburger.ch
 */
public final class FineractClient {

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

    public static Builder builder() {
        return new Builder();
    }

    protected <S> S createService(Class<S> serviceClass) {
        return api.createService(serviceClass);
    }

    public static final class Builder {

        private final ApiClient apiClient = new ApiClient();
        private String baseURL;
        private String tenant;
        private String username;
        private String password;

        private Builder() {}

        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder logging(Level level) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(level);
            getApiClient().getOkBuilder().addInterceptor(logging);
            return this;
        }

        /**
         * Skip Fineract API host SSL certificate verification. DO NOT USE THIS when invoking a production server's API!
         * This is intended for https://localhost:8443/ testing of development servers with self-signed certificates,
         * only. If you do not understand what this is, do not use it. You WILL cause a security issue in your
         * application due to the possibility of a "man in the middle" attack when this is enabled.
         */
        @SuppressWarnings("unused")
        public Builder insecure(boolean insecure) {
            // Nota bene: Similar code to this is also in Fineract Provider's
            // org.apache.fineract.infrastructure.hooks.processor.ProcessorHelper
            if (insecure) {
                HostnameVerifier insecureHostnameVerifier = (hostname, session) -> true;
                apiClient.getOkBuilder().hostnameVerifier(insecureHostnameVerifier);

                try {
                    X509TrustManager insecureX509TrustManager = new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }
                    };

                    var sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, new TrustManager[] { insecureX509TrustManager }, new SecureRandom());
                    SSLSocketFactory insecureSslSocketFactory = sslContext.getSocketFactory();

                    apiClient.getOkBuilder().sslSocketFactory(insecureSslSocketFactory, insecureX509TrustManager);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new IllegalStateException("insecure() SSL configuration failed", e);
                }
            }
            return this;
        }

        public FineractClient build() {
            // URL
            apiClient.getAdapterBuilder().baseUrl(has("baseURL", baseURL));

            // Tenant
            ApiKeyAuth tenantAuth = new ApiKeyAuth("header", "fineract-platform-tenantid");
            tenantAuth.setApiKey(has("tenant", tenant));
            apiClient.addAuthorization("tenantid", tenantAuth);

            // BASIC Auth
            HttpBasicAuth basicAuth = new HttpBasicAuth();
            basicAuth.setCredentials(has("username", username), has("password", password));
            apiClient.addAuthorization("basicAuth", basicAuth);

            return new FineractClient(apiClient);
        }

        /**
         * Obtain the internal Retrofit ApiClient. This method is typically not required to be invoked for simple API
         * usages, but can be a handy back door for non-trivial advanced customizations of the API client.
         *
         * @return the {@link ApiClient} which {@link #build()} will use.
         */
        public ApiClient getApiClient() {
            return apiClient;
        }

        private <T> T has(String propertyName, T value) throws IllegalStateException {
            if (value == null) {
                throw new IllegalStateException("Must call " + propertyName + "(...) to create valid Builder");
            }
            return value;
        }
    }
}
