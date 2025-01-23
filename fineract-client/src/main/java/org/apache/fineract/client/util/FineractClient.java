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
import java.time.Duration;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
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
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.services.BusinessStepConfigurationApi;
import org.apache.fineract.client.services.CacheApi;
import org.apache.fineract.client.services.CashierJournalsApi;
import org.apache.fineract.client.services.CashiersApi;
import org.apache.fineract.client.services.CentersApi;
import org.apache.fineract.client.services.ChargesApi;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.ClientChargesApi;
import org.apache.fineract.client.services.ClientIdentifierApi;
import org.apache.fineract.client.services.ClientSearchV2Api;
import org.apache.fineract.client.services.ClientTransactionApi;
import org.apache.fineract.client.services.ClientsAddressApi;
import org.apache.fineract.client.services.CodeValuesApi;
import org.apache.fineract.client.services.CodesApi;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.client.services.DataTablesApi;
import org.apache.fineract.client.services.DefaultApi;
import org.apache.fineract.client.services.DelinquencyRangeAndBucketsManagementApi;
import org.apache.fineract.client.services.DocumentsApiFixed;
import org.apache.fineract.client.services.EntityDataTableApi;
import org.apache.fineract.client.services.EntityFieldConfigurationApi;
import org.apache.fineract.client.services.ExternalAssetOwnerLoanProductAttributesApi;
import org.apache.fineract.client.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.services.ExternalEventConfigurationApi;
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
import org.apache.fineract.client.services.ImagesApi;
import org.apache.fineract.client.services.InterestRateChartApi;
import org.apache.fineract.client.services.InterestRateSlabAKAInterestBandsApi;
import org.apache.fineract.client.services.JournalEntriesApi;
import org.apache.fineract.client.services.ListReportMailingJobHistoryApi;
import org.apache.fineract.client.services.LoanAccountLockApi;
import org.apache.fineract.client.services.LoanChargesApi;
import org.apache.fineract.client.services.LoanCobCatchUpApi;
import org.apache.fineract.client.services.LoanCollateralApi;
import org.apache.fineract.client.services.LoanDisbursementDetailsApi;
import org.apache.fineract.client.services.LoanInterestPauseApi;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.client.services.LoanReschedulingApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.MakerCheckerOr4EyeFunctionalityApi;
import org.apache.fineract.client.services.MappingFinancialActivitiesToAccountsApi;
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
import org.apache.fineract.client.services.RescheduleLoansApi;
import org.apache.fineract.client.services.RolesApi;
import org.apache.fineract.client.services.RunReportsApi;
import org.apache.fineract.client.services.SavingsAccountApi;
import org.apache.fineract.client.services.SavingsAccountTransactionsApi;
import org.apache.fineract.client.services.SavingsChargesApi;
import org.apache.fineract.client.services.SavingsProductApi;
import org.apache.fineract.client.services.SchedulerApi;
import org.apache.fineract.client.services.SchedulerJobApi;
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
import org.apache.fineract.client.util.JSON.GsonCustomConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Fineract Client Java SDK API entry point.
 *
 * @author Michael Vorburger.ch
 */
public final class FineractClient {

    /**
     * Constant to be used in requests where Fineract's API requires a dateFormat to be given. This matches the format
     * in which LocalDate instances are serialized. (BTW: In a Java client API, it seems weird to have strong LocalDate
     * (not String) instances, and then have to specify its format, see
     * https://issues.apache.org/jira/browse/FINERACT-1233.)
     */
    // Matching org.apache.fineract.client.util.JSON.LocalDateTypeAdapter.formatter
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;

    public final AccountingClosureApi glClosures;
    public final AccountingRulesApi accountingRules;
    public final AccountNumberFormatApi accountNumberFormats;
    public final AccountTransfersApi accountTransfers;
    public final AdhocQueryApiApi adhocQuery;
    public final AuditsApi audits;
    public final AuthenticationHttpBasicApi authentication;
    public final BatchApiApi batches;
    public final BusinessDateManagementApi businessDateManagement;
    public final BusinessStepConfigurationApi businessStepConfiguration;
    public final CacheApi caches;
    public final CashierJournalsApi cashiersJournal;
    public final CashiersApi cashiers;
    public final CentersApi centers;
    public final ChargesApi charges;
    public final ClientApi clients;

    public final ClientSearchV2Api clientSearchV2;
    public final ClientChargesApi clientCharges;
    public final ClientIdentifierApi clientIdentifiers;
    public final ClientsAddressApi clientAddresses;
    public final ClientTransactionApi clientTransactions;
    public final CodesApi codes;
    public final CodeValuesApi codeValues;
    public final CurrencyApi currencies;
    public final DataTablesApi dataTables;
    public final @Deprecated DefaultApi legacy; // TODO FINERACT-1222
    public final DocumentsApiFixed documents;
    public final DelinquencyRangeAndBucketsManagementApi delinquencyRangeAndBucketsManagement;
    public final EntityDataTableApi entityDatatableChecks;
    public final EntityFieldConfigurationApi entityFieldConfigurations;
    public final ExternalEventConfigurationApi externalEventConfigurationApi;
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
    public final ImagesApi images;
    public final InterestRateChartApi interestRateCharts;
    public final InterestRateSlabAKAInterestBandsApi interestRateChartLabs;
    public final JournalEntriesApi journalEntries;
    public final ListReportMailingJobHistoryApi reportMailings;
    public final LoanChargesApi loanCharges;
    public final LoanCobCatchUpApi loanCobCatchUpApi;
    public final LoanCollateralApi loanCollaterals;
    public final LoanProductsApi loanProducts;
    public final LoanReschedulingApi loanSchedules;
    public final LoansApi loans;
    public final LoanDisbursementDetailsApi loanDisbursementDetails;
    public final LoanTransactionsApi loanTransactions;
    public final MakerCheckerOr4EyeFunctionalityApi makerCheckers;
    public final MappingFinancialActivitiesToAccountsApi financialActivyAccountMappings;
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
    public final RescheduleLoansApi rescheduleLoans;
    public final RolesApi roles;
    public final RunReportsApi reportsRun;
    public final SavingsAccountApi savingsAccounts;
    public final SavingsChargesApi savingsAccountCharges;
    public final SavingsProductApi savingsProducts;
    public final SavingsAccountTransactionsApi savingsTransactions;
    public final SchedulerApi jobsScheduler;
    public final SchedulerJobApi jobs;
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
    public final LoanInterestPauseApi loanInterestPauseApi;

    public final ExternalAssetOwnersApi externalAssetOwners;
    public final ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributes;
    public final LoanAccountLockApi loanAccountLockApi;

    private FineractClient(OkHttpClient okHttpClient, Retrofit retrofit) {
        this.okHttpClient = okHttpClient;
        this.retrofit = retrofit;

        loanAccountLockApi = retrofit.create(LoanAccountLockApi.class);
        externalAssetOwners = retrofit.create(ExternalAssetOwnersApi.class);
        externalAssetOwnerLoanProductAttributes = retrofit.create(ExternalAssetOwnerLoanProductAttributesApi.class);
        glClosures = retrofit.create(AccountingClosureApi.class);
        accountingRules = retrofit.create(AccountingRulesApi.class);
        accountNumberFormats = retrofit.create(AccountNumberFormatApi.class);
        accountTransfers = retrofit.create(AccountTransfersApi.class);
        adhocQuery = retrofit.create(AdhocQueryApiApi.class);
        audits = retrofit.create(AuditsApi.class);
        authentication = retrofit.create(AuthenticationHttpBasicApi.class);
        batches = retrofit.create(BatchApiApi.class);
        businessDateManagement = retrofit.create(BusinessDateManagementApi.class);
        businessStepConfiguration = retrofit.create(BusinessStepConfigurationApi.class);
        externalEventConfigurationApi = retrofit.create(ExternalEventConfigurationApi.class);
        caches = retrofit.create(CacheApi.class);
        cashiersJournal = retrofit.create(CashierJournalsApi.class);
        cashiers = retrofit.create(CashiersApi.class);
        centers = retrofit.create(CentersApi.class);
        charges = retrofit.create(ChargesApi.class);
        clients = retrofit.create(ClientApi.class);
        clientSearchV2 = retrofit.create(ClientSearchV2Api.class);
        clientCharges = retrofit.create(ClientChargesApi.class);
        clientIdentifiers = retrofit.create(ClientIdentifierApi.class);
        clientAddresses = retrofit.create(ClientsAddressApi.class);
        clientTransactions = retrofit.create(ClientTransactionApi.class);
        codes = retrofit.create(CodesApi.class);
        codeValues = retrofit.create(CodeValuesApi.class);
        currencies = retrofit.create(CurrencyApi.class);
        dataTables = retrofit.create(DataTablesApi.class);
        delinquencyRangeAndBucketsManagement = retrofit.create(DelinquencyRangeAndBucketsManagementApi.class);
        legacy = retrofit.create(DefaultApi.class);
        documents = retrofit.create(DocumentsApiFixed.class);
        entityDatatableChecks = retrofit.create(EntityDataTableApi.class);
        entityFieldConfigurations = retrofit.create(EntityFieldConfigurationApi.class);
        externalServices = retrofit.create(ExternalServicesApi.class);
        userDetails = retrofit.create(FetchAuthenticatedUserDetailsApi.class);
        fixedDepositAccounts = retrofit.create(FixedDepositAccountApi.class);
        fixedDepositProducts = retrofit.create(FixedDepositProductApi.class);
        floatingRates = retrofit.create(FloatingRatesApi.class);
        glAccounts = retrofit.create(GeneralLedgerAccountApi.class);
        globalConfigurations = retrofit.create(GlobalConfigurationApi.class);
        groups = retrofit.create(GroupsApi.class);
        holidays = retrofit.create(HolidaysApi.class);
        hooks = retrofit.create(HooksApi.class);
        images = retrofit.create(ImagesApi.class);
        interestRateCharts = retrofit.create(InterestRateChartApi.class);
        interestRateChartLabs = retrofit.create(InterestRateSlabAKAInterestBandsApi.class);
        journalEntries = retrofit.create(JournalEntriesApi.class);
        reportMailings = retrofit.create(ListReportMailingJobHistoryApi.class);
        loanCharges = retrofit.create(LoanChargesApi.class);
        loanCobCatchUpApi = retrofit.create(LoanCobCatchUpApi.class);
        loanCollaterals = retrofit.create(LoanCollateralApi.class);
        loanProducts = retrofit.create(LoanProductsApi.class);
        loanSchedules = retrofit.create(LoanReschedulingApi.class);
        loans = retrofit.create(LoansApi.class);
        loanDisbursementDetails = retrofit.create(LoanDisbursementDetailsApi.class);
        loanTransactions = retrofit.create(LoanTransactionsApi.class);
        makerCheckers = retrofit.create(MakerCheckerOr4EyeFunctionalityApi.class);
        financialActivyAccountMappings = retrofit.create(MappingFinancialActivitiesToAccountsApi.class);
        jobs = retrofit.create(SchedulerJobApi.class);
        mixMappings = retrofit.create(MixMappingApi.class);
        mixReports = retrofit.create(MixReportApi.class);
        mixTaxonomies = retrofit.create(MixTaxonomyApi.class);
        notes = retrofit.create(NotesApi.class);
        notifications = retrofit.create(NotificationApi.class);
        offices = retrofit.create(OfficesApi.class);
        passwordPreferences = retrofit.create(PasswordPreferencesApi.class);
        paymentTypes = retrofit.create(PaymentTypeApi.class);
        periodicAccrualAccounting = retrofit.create(PeriodicAccrualAccountingApi.class);
        permissions = retrofit.create(PermissionsApi.class);
        selfPockets = retrofit.create(PocketApi.class);
        provisioningCategories = retrofit.create(ProvisioningCategoryApi.class);
        provisioningCriterias = retrofit.create(ProvisioningCriteriaApi.class);
        provisioningEntries = retrofit.create(ProvisioningEntriesApi.class);
        recurringDepositAccounts = retrofit.create(RecurringDepositAccountApi.class);
        recurringDepositAccountTransactions = retrofit.create(RecurringDepositAccountTransactionsApi.class);
        recurringDepositProducts = retrofit.create(RecurringDepositProductApi.class);
        reportMailingJobs = retrofit.create(ReportMailingJobsApi.class);
        reports = retrofit.create(ReportsApi.class);
        rescheduleLoans = retrofit.create(RescheduleLoansApi.class);
        roles = retrofit.create(RolesApi.class);
        reportsRun = retrofit.create(RunReportsApi.class);
        savingsAccounts = retrofit.create(SavingsAccountApi.class);
        savingsAccountCharges = retrofit.create(SavingsChargesApi.class);
        savingsProducts = retrofit.create(SavingsProductApi.class);
        savingsTransactions = retrofit.create(SavingsAccountTransactionsApi.class);
        jobsScheduler = retrofit.create(SchedulerApi.class);
        surveyScorecards = retrofit.create(ScoreCardApi.class);
        search = retrofit.create(SearchApiApi.class);
        selfAccountTransfers = retrofit.create(SelfAccountTransferApi.class);
        selfAuthentication = retrofit.create(SelfAuthenticationApi.class);
        selfClients = retrofit.create(SelfClientApi.class);
        selfShareProducts = retrofit.create(SelfDividendApi.class);
        selfLoanProducts = retrofit.create(SelfLoanProductsApi.class);
        selfLoans = retrofit.create(SelfLoansApi.class);
        selfReportsRun = retrofit.create(SelfRunReportApi.class);
        selfSavingsAccounts = retrofit.create(SelfSavingsAccountApi.class);
        selfSurveyScorecards = retrofit.create(SelfScoreCardApi.class);
        selfRegistration = retrofit.create(SelfServiceRegistrationApi.class);
        selfShareAccounts = retrofit.create(SelfShareAccountsApi.class);
        selfSurveys = retrofit.create(SelfSpmApi.class);
        selfThirdPartyBeneficiaries = retrofit.create(SelfThirdPartyTransferApi.class);
        selfUser = retrofit.create(SelfUserApi.class);
        selfUserDetails = retrofit.create(SelfUserDetailsApi.class);
        shareAccounts = retrofit.create(ShareAccountApi.class);
        surveyLookupTables = retrofit.create(SpmApiLookUpTableApi.class);
        surveys = retrofit.create(SpmSurveysApi.class);
        staff = retrofit.create(StaffApi.class);
        standingInstructions = retrofit.create(StandingInstructionsApi.class);
        standingInstructionsHistory = retrofit.create(StandingInstructionsHistoryApi.class);
        taxComponents = retrofit.create(TaxComponentsApi.class);
        taxGroups = retrofit.create(TaxGroupApi.class);
        tellers = retrofit.create(TellerCashManagementApi.class);
        templates = retrofit.create(UserGeneratedDocumentsApi.class);
        users = retrofit.create(UsersApi.class);
        workingDays = retrofit.create(WorkingDaysApi.class);
        loanInterestPauseApi = retrofit.create(LoanInterestPauseApi.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    public OkHttpClient okHttpClient() {
        return this.okHttpClient;
    }

    public HttpUrl baseURL() {
        return this.retrofit.baseUrl();
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface, using
     * {@link Retrofit#create(Class)}. This method is typically not required to be invoked for standard API usage, but
     * can be a handy back door for non-trivial advanced customizations of the API client if you have extended Fineract
     * with your own REST APIs.
     */
    public <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static final class Builder {

        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        private final JSON json = new JSON();
        private final OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        private final Retrofit.Builder retrofitBuilder = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonCustomConverterFactory.create(json.getGson()));

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
            okBuilder.addInterceptor(logging);
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            okBuilder.readTimeout(timeout);
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
                HostnameVerifier insecureHostnameVerifier = (hostname, session) -> true;// NOSONAR
                okBuilder.hostnameVerifier(insecureHostnameVerifier);

                try {
                    X509TrustManager insecureX509TrustManager = new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }
                    };

                    // TODO "SSL" or "TLS" as in hooks.processor.ProcessorHelper?
                    SSLContext sslContext = SSLContext.getInstance("SSL");// NOSONAR
                    sslContext.init(null, new TrustManager[] { insecureX509TrustManager }, new SecureRandom());
                    SSLSocketFactory insecureSslSocketFactory = sslContext.getSocketFactory();

                    okBuilder.sslSocketFactory(insecureSslSocketFactory, insecureX509TrustManager);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new IllegalStateException("insecure() SSL configuration failed", e);
                }
            }
            return this;
        }

        public FineractClient build() {
            // URL
            retrofitBuilder.baseUrl(has("baseURL", baseURL));

            // Tenant
            if (tenant != null) {
                ApiKeyAuth tenantAuth = new ApiKeyAuth("header", "fineract-platform-tenantid");
                tenantAuth.setApiKey(has("tenant", tenant));
                okBuilder.addInterceptor(tenantAuth);
            } else {
                log.warn("Tenant hasn't been configured for the client");
            }

            // BASIC Auth
            if (username != null && password != null) {
                HttpBasicAuth basicAuth = new HttpBasicAuth();
                basicAuth.setCredentials(has("username", username), has("password", password));
                okBuilder.addInterceptor(basicAuth);
            } else {
                log.warn("Username and password haven't been configured for the client");
            }

            OkHttpClient okHttpClient = okBuilder.build();
            retrofitBuilder.client(okHttpClient);

            return new FineractClient(okHttpClient, retrofitBuilder.build());
        }

        /**
         * Obtain the internal Retrofit Builder. This method is typically not required to be invoked for simple API
         * usages, but can be a handy back door for non-trivial advanced customizations of the API client.
         *
         */
        public retrofit2.Retrofit.Builder getRetrofitBuilder() {
            return retrofitBuilder;
        }

        /**
         * Obtain the internal OkHttp Builder. This method is typically not required to be invoked for simple API
         * usages, but can be a handy back door for non-trivial advanced customizations of the API client.
         *
         */
        public okhttp3.OkHttpClient.Builder getOkBuilder() {
            return okBuilder;
        }

        private <T> T has(String propertyName, T value) throws IllegalStateException {
            if (value == null) {
                throw new IllegalStateException("Must call " + propertyName + "(...) to create valid Builder");
            }
            return value;
        }
    }
}
