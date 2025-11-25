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
package org.apache.fineract.client.feign;

import org.apache.fineract.client.feign.services.AccountNumberFormatApi;
import org.apache.fineract.client.feign.services.AccountTransfersApi;
import org.apache.fineract.client.feign.services.AccountingClosureApi;
import org.apache.fineract.client.feign.services.AccountingRulesApi;
import org.apache.fineract.client.feign.services.AdhocQueryApiApi;
import org.apache.fineract.client.feign.services.AuditsApi;
import org.apache.fineract.client.feign.services.AuthenticationHttpBasicApi;
import org.apache.fineract.client.feign.services.BatchApiApi;
import org.apache.fineract.client.feign.services.BulkImportApi;
import org.apache.fineract.client.feign.services.BulkLoansApi;
import org.apache.fineract.client.feign.services.BusinessDateManagementApi;
import org.apache.fineract.client.feign.services.BusinessStepConfigurationApi;
import org.apache.fineract.client.feign.services.CacheApi;
import org.apache.fineract.client.feign.services.CalendarApi;
import org.apache.fineract.client.feign.services.CashierJournalsApi;
import org.apache.fineract.client.feign.services.CashiersApi;
import org.apache.fineract.client.feign.services.CentersApi;
import org.apache.fineract.client.feign.services.ChargesApi;
import org.apache.fineract.client.feign.services.ClientApi;
import org.apache.fineract.client.feign.services.ClientChargesApi;
import org.apache.fineract.client.feign.services.ClientCollateralManagementApi;
import org.apache.fineract.client.feign.services.ClientFamilyMemberApi;
import org.apache.fineract.client.feign.services.ClientIdentifierApi;
import org.apache.fineract.client.feign.services.ClientSearchV2Api;
import org.apache.fineract.client.feign.services.ClientTransactionApi;
import org.apache.fineract.client.feign.services.ClientsAddressApi;
import org.apache.fineract.client.feign.services.CodeValuesApi;
import org.apache.fineract.client.feign.services.CodesApi;
import org.apache.fineract.client.feign.services.CollateralManagementApi;
import org.apache.fineract.client.feign.services.CollectionSheetApi;
import org.apache.fineract.client.feign.services.CreditBureauConfigurationApi;
import org.apache.fineract.client.feign.services.CurrencyApi;
import org.apache.fineract.client.feign.services.DataTablesApi;
import org.apache.fineract.client.feign.services.DefaultApi;
import org.apache.fineract.client.feign.services.DelinquencyRangeAndBucketsManagementApi;
import org.apache.fineract.client.feign.services.DepositAccountOnHoldFundTransactionsApi;
import org.apache.fineract.client.feign.services.DeviceRegistrationApi;
import org.apache.fineract.client.feign.services.DocumentsApi;
import org.apache.fineract.client.feign.services.DocumentsApiFixed;
import org.apache.fineract.client.feign.services.EntityDataTableApi;
import org.apache.fineract.client.feign.services.EntityFieldConfigurationApi;
import org.apache.fineract.client.feign.services.ExternalAssetOwnerLoanProductAttributesApi;
import org.apache.fineract.client.feign.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.feign.services.ExternalEventConfigurationApi;
import org.apache.fineract.client.feign.services.ExternalServicesApi;
import org.apache.fineract.client.feign.services.FetchAuthenticatedUserDetailsApi;
import org.apache.fineract.client.feign.services.FineractEntityApi;
import org.apache.fineract.client.feign.services.FixedDepositAccountApi;
import org.apache.fineract.client.feign.services.FixedDepositAccountTransactionsApi;
import org.apache.fineract.client.feign.services.FixedDepositProductApi;
import org.apache.fineract.client.feign.services.FloatingRatesApi;
import org.apache.fineract.client.feign.services.FundsApi;
import org.apache.fineract.client.feign.services.GeneralLedgerAccountApi;
import org.apache.fineract.client.feign.services.GlobalConfigurationApi;
import org.apache.fineract.client.feign.services.GroupsApi;
import org.apache.fineract.client.feign.services.GroupsLevelApi;
import org.apache.fineract.client.feign.services.GuarantorsApi;
import org.apache.fineract.client.feign.services.HolidaysApi;
import org.apache.fineract.client.feign.services.HooksApi;
import org.apache.fineract.client.feign.services.ImagesApi;
import org.apache.fineract.client.feign.services.InlineJobApi;
import org.apache.fineract.client.feign.services.InstanceModeApi;
import org.apache.fineract.client.feign.services.InterOperationApi;
import org.apache.fineract.client.feign.services.InterestRateChartApi;
import org.apache.fineract.client.feign.services.InterestRateSlabAKAInterestBandsApi;
import org.apache.fineract.client.feign.services.InternalCobApi;
import org.apache.fineract.client.feign.services.JournalEntriesApi;
import org.apache.fineract.client.feign.services.LikelihoodApi;
import org.apache.fineract.client.feign.services.ListReportMailingJobHistoryApi;
import org.apache.fineract.client.feign.services.LoanAccountLockApi;
import org.apache.fineract.client.feign.services.LoanBuyDownFeesApi;
import org.apache.fineract.client.feign.services.LoanCapitalizedIncomeApi;
import org.apache.fineract.client.feign.services.LoanChargesApi;
import org.apache.fineract.client.feign.services.LoanCobCatchUpApi;
import org.apache.fineract.client.feign.services.LoanCollateralApi;
import org.apache.fineract.client.feign.services.LoanCollateralManagementApi;
import org.apache.fineract.client.feign.services.LoanDisbursementDetailsApi;
import org.apache.fineract.client.feign.services.LoanInterestPauseApi;
import org.apache.fineract.client.feign.services.LoanProductsApi;
import org.apache.fineract.client.feign.services.LoanReschedulingApi;
import org.apache.fineract.client.feign.services.LoanTransactionsApi;
import org.apache.fineract.client.feign.services.LoansApi;
import org.apache.fineract.client.feign.services.LoansPointInTimeApi;
import org.apache.fineract.client.feign.services.MakerCheckerOr4EyeFunctionalityApi;
import org.apache.fineract.client.feign.services.MappingFinancialActivitiesToAccountsApi;
import org.apache.fineract.client.feign.services.MeetingsApi;
import org.apache.fineract.client.feign.services.MixMappingApi;
import org.apache.fineract.client.feign.services.MixReportApi;
import org.apache.fineract.client.feign.services.MixTaxonomyApi;
import org.apache.fineract.client.feign.services.NotesApi;
import org.apache.fineract.client.feign.services.NotificationApi;
import org.apache.fineract.client.feign.services.OfficesApi;
import org.apache.fineract.client.feign.services.PasswordPreferencesApi;
import org.apache.fineract.client.feign.services.PaymentTypeApi;
import org.apache.fineract.client.feign.services.PeriodicAccrualAccountingApi;
import org.apache.fineract.client.feign.services.PermissionsApi;
import org.apache.fineract.client.feign.services.PocketApi;
import org.apache.fineract.client.feign.services.PovertyLineApi;
import org.apache.fineract.client.feign.services.ProductMixApi;
import org.apache.fineract.client.feign.services.ProductsApi;
import org.apache.fineract.client.feign.services.ProgressiveLoanApi;
import org.apache.fineract.client.feign.services.ProvisioningCategoryApi;
import org.apache.fineract.client.feign.services.ProvisioningCriteriaApi;
import org.apache.fineract.client.feign.services.ProvisioningEntriesApi;
import org.apache.fineract.client.feign.services.RateApi;
import org.apache.fineract.client.feign.services.RecurringDepositAccountApi;
import org.apache.fineract.client.feign.services.RecurringDepositAccountTransactionsApi;
import org.apache.fineract.client.feign.services.RecurringDepositProductApi;
import org.apache.fineract.client.feign.services.RepaymentWithPostDatedChecksApi;
import org.apache.fineract.client.feign.services.ReportMailingJobsApi;
import org.apache.fineract.client.feign.services.ReportsApi;
import org.apache.fineract.client.feign.services.RescheduleLoansApi;
import org.apache.fineract.client.feign.services.RolesApi;
import org.apache.fineract.client.feign.services.RunReportsApi;
import org.apache.fineract.client.feign.services.SavingsAccountApi;
import org.apache.fineract.client.feign.services.SavingsAccountTransactionsApi;
import org.apache.fineract.client.feign.services.SavingsChargesApi;
import org.apache.fineract.client.feign.services.SavingsProductApi;
import org.apache.fineract.client.feign.services.SchedulerApi;
import org.apache.fineract.client.feign.services.SchedulerJobApi;
import org.apache.fineract.client.feign.services.ScoreCardApi;
import org.apache.fineract.client.feign.services.SearchApiApi;
import org.apache.fineract.client.feign.services.SelfAccountTransferApi;
import org.apache.fineract.client.feign.services.SelfAuthenticationApi;
import org.apache.fineract.client.feign.services.SelfClientApi;
import org.apache.fineract.client.feign.services.SelfDividendApi;
import org.apache.fineract.client.feign.services.SelfLoanProductsApi;
import org.apache.fineract.client.feign.services.SelfLoansApi;
import org.apache.fineract.client.feign.services.SelfRunReportApi;
import org.apache.fineract.client.feign.services.SelfSavingsAccountApi;
import org.apache.fineract.client.feign.services.SelfSavingsProductsApi;
import org.apache.fineract.client.feign.services.SelfScoreCardApi;
import org.apache.fineract.client.feign.services.SelfServiceRegistrationApi;
import org.apache.fineract.client.feign.services.SelfShareAccountsApi;
import org.apache.fineract.client.feign.services.SelfShareProductsApi;
import org.apache.fineract.client.feign.services.SelfSpmApi;
import org.apache.fineract.client.feign.services.SelfThirdPartyTransferApi;
import org.apache.fineract.client.feign.services.SelfUserApi;
import org.apache.fineract.client.feign.services.SelfUserDetailsApi;
import org.apache.fineract.client.feign.services.ShareAccountApi;
import org.apache.fineract.client.feign.services.SmsApi;
import org.apache.fineract.client.feign.services.SpmApiLookUpTableApi;
import org.apache.fineract.client.feign.services.SpmSurveysApi;
import org.apache.fineract.client.feign.services.StaffApi;
import org.apache.fineract.client.feign.services.StandingInstructionsApi;
import org.apache.fineract.client.feign.services.StandingInstructionsHistoryApi;
import org.apache.fineract.client.feign.services.SurveyApi;
import org.apache.fineract.client.feign.services.TaxComponentsApi;
import org.apache.fineract.client.feign.services.TaxGroupApi;
import org.apache.fineract.client.feign.services.TellerCashManagementApi;
import org.apache.fineract.client.feign.services.TwoFactorApi;
import org.apache.fineract.client.feign.services.UserGeneratedDocumentsApi;
import org.apache.fineract.client.feign.services.UsersApi;
import org.apache.fineract.client.feign.services.WorkingDaysApi;

/**
 * Main entry point for creating Feign-based clients for the Fineract API.
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code
 *
 * FineractFeignClient client = FineractFeignClient.builder().baseUrl("https://localhost:8443/fineract-provider/api/v1")
 *         .credentials("username", "password").build();
 *
 * // Access API clients
 * ClientApi clientsApi = client.clients();
 * List<ClientData> clients = clientsApi.retrieveAll();
 * }
 * </pre>
 */
public final class FineractFeignClient {

    private final FineractFeignClientConfig config;

    private FineractFeignClient(Builder builder) {
        this.config = builder.configBuilder.build();
    }

    /**
     * Creates a new builder for configuring a FineractFeignClient.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new client for the specified API interface.
     *
     * @param <T>
     *            The API interface type
     * @param apiType
     *            The API interface class
     * @return A configured Feign client for the specified API
     */
    public <T> T create(Class<T> apiType) {
        return config.createClient(apiType);
    }

    public AccountNumberFormatApi accountNumberFormat() {
        return create(AccountNumberFormatApi.class);
    }

    public AccountTransfersApi accountTransfers() {
        return create(AccountTransfersApi.class);
    }

    public AccountingClosureApi accountingClosure() {
        return create(AccountingClosureApi.class);
    }

    public AccountingRulesApi accountingRules() {
        return create(AccountingRulesApi.class);
    }

    public AdhocQueryApiApi adhocQuery() {
        return create(AdhocQueryApiApi.class);
    }

    public AuditsApi audits() {
        return create(AuditsApi.class);
    }

    public AuthenticationHttpBasicApi authenticationHttpBasic() {
        return create(AuthenticationHttpBasicApi.class);
    }

    public BatchApiApi batch() {
        return create(BatchApiApi.class);
    }

    public BulkImportApi bulkImport() {
        return create(BulkImportApi.class);
    }

    public BulkLoansApi bulkLoans() {
        return create(BulkLoansApi.class);
    }

    public BusinessDateManagementApi businessDateManagement() {
        return create(BusinessDateManagementApi.class);
    }

    public BusinessStepConfigurationApi businessStepConfiguration() {
        return create(BusinessStepConfigurationApi.class);
    }

    public CacheApi cache() {
        return create(CacheApi.class);
    }

    public CalendarApi calendar() {
        return create(CalendarApi.class);
    }

    public CashierJournalsApi cashierJournals() {
        return create(CashierJournalsApi.class);
    }

    public CashiersApi cashiers() {
        return create(CashiersApi.class);
    }

    public CentersApi centers() {
        return create(CentersApi.class);
    }

    public ChargesApi charges() {
        return create(ChargesApi.class);
    }

    public ClientApi clients() {
        return create(ClientApi.class);
    }

    public ClientChargesApi clientCharges() {
        return create(ClientChargesApi.class);
    }

    public ClientCollateralManagementApi clientCollateralManagement() {
        return create(ClientCollateralManagementApi.class);
    }

    public ClientFamilyMemberApi clientFamilyMember() {
        return create(ClientFamilyMemberApi.class);
    }

    public ClientIdentifierApi clientIdentifier() {
        return create(ClientIdentifierApi.class);
    }

    public ClientSearchV2Api clientSearchV2() {
        return create(ClientSearchV2Api.class);
    }

    public ClientTransactionApi clientTransaction() {
        return create(ClientTransactionApi.class);
    }

    public ClientsAddressApi clientsAddress() {
        return create(ClientsAddressApi.class);
    }

    public CodeValuesApi codeValues() {
        return create(CodeValuesApi.class);
    }

    public CodesApi codes() {
        return create(CodesApi.class);
    }

    public CollateralManagementApi collateralManagement() {
        return create(CollateralManagementApi.class);
    }

    public CollectionSheetApi collectionSheet() {
        return create(CollectionSheetApi.class);
    }

    public CreditBureauConfigurationApi creditBureauConfiguration() {
        return create(CreditBureauConfigurationApi.class);
    }

    public CurrencyApi currency() {
        return create(CurrencyApi.class);
    }

    public DataTablesApi dataTables() {
        return create(DataTablesApi.class);
    }

    public DefaultApi defaultApi() {
        return create(DefaultApi.class);
    }

    public DelinquencyRangeAndBucketsManagementApi delinquencyRangeAndBucketsManagement() {
        return create(DelinquencyRangeAndBucketsManagementApi.class);
    }

    public DepositAccountOnHoldFundTransactionsApi depositAccountOnHoldFundTransactions() {
        return create(DepositAccountOnHoldFundTransactionsApi.class);
    }

    public DeviceRegistrationApi deviceRegistration() {
        return create(DeviceRegistrationApi.class);
    }

    public DocumentsApi documents() {
        return create(DocumentsApi.class);
    }

    public DocumentsApiFixed documentsFixed() {
        return create(DocumentsApiFixed.class);
    }

    public EntityDataTableApi entityDataTable() {
        return create(EntityDataTableApi.class);
    }

    public EntityFieldConfigurationApi entityFieldConfiguration() {
        return create(EntityFieldConfigurationApi.class);
    }

    public ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributes() {
        return create(ExternalAssetOwnerLoanProductAttributesApi.class);
    }

    public ExternalAssetOwnersApi externalAssetOwners() {
        return create(ExternalAssetOwnersApi.class);
    }

    public ExternalEventConfigurationApi externalEventConfiguration() {
        return create(ExternalEventConfigurationApi.class);
    }

    public ExternalServicesApi externalServices() {
        return create(ExternalServicesApi.class);
    }

    public FetchAuthenticatedUserDetailsApi fetchAuthenticatedUserDetails() {
        return create(FetchAuthenticatedUserDetailsApi.class);
    }

    public FineractEntityApi fineractEntity() {
        return create(FineractEntityApi.class);
    }

    public FixedDepositAccountApi fixedDepositAccount() {
        return create(FixedDepositAccountApi.class);
    }

    public FixedDepositAccountTransactionsApi fixedDepositAccountTransactions() {
        return create(FixedDepositAccountTransactionsApi.class);
    }

    public FixedDepositProductApi fixedDepositProduct() {
        return create(FixedDepositProductApi.class);
    }

    public FloatingRatesApi floatingRates() {
        return create(FloatingRatesApi.class);
    }

    public FundsApi funds() {
        return create(FundsApi.class);
    }

    public GeneralLedgerAccountApi generalLedgerAccount() {
        return create(GeneralLedgerAccountApi.class);
    }

    public GlobalConfigurationApi globalConfiguration() {
        return create(GlobalConfigurationApi.class);
    }

    public GroupsApi groups() {
        return create(GroupsApi.class);
    }

    public GroupsLevelApi groupsLevel() {
        return create(GroupsLevelApi.class);
    }

    public GuarantorsApi guarantors() {
        return create(GuarantorsApi.class);
    }

    public HolidaysApi holidays() {
        return create(HolidaysApi.class);
    }

    public HooksApi hooks() {
        return create(HooksApi.class);
    }

    public ImagesApi images() {
        return create(ImagesApi.class);
    }

    public InlineJobApi inlineJob() {
        return create(InlineJobApi.class);
    }

    public InstanceModeApi instanceMode() {
        return create(InstanceModeApi.class);
    }

    public InterOperationApi interOperation() {
        return create(InterOperationApi.class);
    }

    public InterestRateChartApi interestRateChart() {
        return create(InterestRateChartApi.class);
    }

    public InterestRateSlabAKAInterestBandsApi interestRateSlabAKAInterestBands() {
        return create(InterestRateSlabAKAInterestBandsApi.class);
    }

    public InternalCobApi internalCob() {
        return create(InternalCobApi.class);
    }

    public JournalEntriesApi journalEntries() {
        return create(JournalEntriesApi.class);
    }

    public LikelihoodApi likelihood() {
        return create(LikelihoodApi.class);
    }

    public ListReportMailingJobHistoryApi listReportMailingJobHistory() {
        return create(ListReportMailingJobHistoryApi.class);
    }

    public LoanAccountLockApi loanAccountLock() {
        return create(LoanAccountLockApi.class);
    }

    public LoanBuyDownFeesApi loanBuyDownFees() {
        return create(LoanBuyDownFeesApi.class);
    }

    public LoanCapitalizedIncomeApi loanCapitalizedIncome() {
        return create(LoanCapitalizedIncomeApi.class);
    }

    public LoanChargesApi loanCharges() {
        return create(LoanChargesApi.class);
    }

    public LoanCobCatchUpApi loanCobCatchUp() {
        return create(LoanCobCatchUpApi.class);
    }

    public LoanCollateralApi loanCollateral() {
        return create(LoanCollateralApi.class);
    }

    public LoanCollateralManagementApi loanCollateralManagement() {
        return create(LoanCollateralManagementApi.class);
    }

    public LoanDisbursementDetailsApi loanDisbursementDetails() {
        return create(LoanDisbursementDetailsApi.class);
    }

    public LoanInterestPauseApi loanInterestPause() {
        return create(LoanInterestPauseApi.class);
    }

    public LoanProductsApi loanProducts() {
        return create(LoanProductsApi.class);
    }

    public LoanReschedulingApi loanRescheduling() {
        return create(LoanReschedulingApi.class);
    }

    public LoanTransactionsApi loanTransactions() {
        return create(LoanTransactionsApi.class);
    }

    public LoansApi loans() {
        return create(LoansApi.class);
    }

    public LoansPointInTimeApi loansPointInTime() {
        return create(LoansPointInTimeApi.class);
    }

    public MakerCheckerOr4EyeFunctionalityApi makerCheckerOr4EyeFunctionality() {
        return create(MakerCheckerOr4EyeFunctionalityApi.class);
    }

    public MappingFinancialActivitiesToAccountsApi mappingFinancialActivitiesToAccounts() {
        return create(MappingFinancialActivitiesToAccountsApi.class);
    }

    public MeetingsApi meetings() {
        return create(MeetingsApi.class);
    }

    public MixMappingApi mixMapping() {
        return create(MixMappingApi.class);
    }

    public MixReportApi mixReport() {
        return create(MixReportApi.class);
    }

    public MixTaxonomyApi mixTaxonomy() {
        return create(MixTaxonomyApi.class);
    }

    public NotesApi notes() {
        return create(NotesApi.class);
    }

    public NotificationApi notification() {
        return create(NotificationApi.class);
    }

    public OfficesApi offices() {
        return create(OfficesApi.class);
    }

    public PasswordPreferencesApi passwordPreferences() {
        return create(PasswordPreferencesApi.class);
    }

    public PaymentTypeApi paymentType() {
        return create(PaymentTypeApi.class);
    }

    public PeriodicAccrualAccountingApi periodicAccrualAccounting() {
        return create(PeriodicAccrualAccountingApi.class);
    }

    public PermissionsApi permissions() {
        return create(PermissionsApi.class);
    }

    public PocketApi pocket() {
        return create(PocketApi.class);
    }

    public PovertyLineApi povertyLine() {
        return create(PovertyLineApi.class);
    }

    public ProductMixApi productMix() {
        return create(ProductMixApi.class);
    }

    public ProductsApi products() {
        return create(ProductsApi.class);
    }

    public ProgressiveLoanApi progressiveLoan() {
        return create(ProgressiveLoanApi.class);
    }

    public ProvisioningCategoryApi provisioningCategory() {
        return create(ProvisioningCategoryApi.class);
    }

    public ProvisioningCriteriaApi provisioningCriteria() {
        return create(ProvisioningCriteriaApi.class);
    }

    public ProvisioningEntriesApi provisioningEntries() {
        return create(ProvisioningEntriesApi.class);
    }

    public RateApi rate() {
        return create(RateApi.class);
    }

    public RecurringDepositAccountApi recurringDepositAccount() {
        return create(RecurringDepositAccountApi.class);
    }

    public RecurringDepositAccountTransactionsApi recurringDepositAccountTransactions() {
        return create(RecurringDepositAccountTransactionsApi.class);
    }

    public RecurringDepositProductApi recurringDepositProduct() {
        return create(RecurringDepositProductApi.class);
    }

    public RepaymentWithPostDatedChecksApi repaymentWithPostDatedChecks() {
        return create(RepaymentWithPostDatedChecksApi.class);
    }

    public ReportMailingJobsApi reportMailingJobs() {
        return create(ReportMailingJobsApi.class);
    }

    public ReportsApi reports() {
        return create(ReportsApi.class);
    }

    public RescheduleLoansApi rescheduleLoans() {
        return create(RescheduleLoansApi.class);
    }

    public RolesApi roles() {
        return create(RolesApi.class);
    }

    public RunReportsApi runReports() {
        return create(RunReportsApi.class);
    }

    public SavingsAccountApi savingsAccount() {
        return create(SavingsAccountApi.class);
    }

    public SavingsAccountTransactionsApi savingsAccountTransactions() {
        return create(SavingsAccountTransactionsApi.class);
    }

    public SavingsChargesApi savingsCharges() {
        return create(SavingsChargesApi.class);
    }

    public SavingsProductApi savingsProduct() {
        return create(SavingsProductApi.class);
    }

    public SchedulerApi scheduler() {
        return create(SchedulerApi.class);
    }

    public SchedulerJobApi schedulerJob() {
        return create(SchedulerJobApi.class);
    }

    public ScoreCardApi scoreCard() {
        return create(ScoreCardApi.class);
    }

    public SearchApiApi search() {
        return create(SearchApiApi.class);
    }

    public SelfAccountTransferApi selfAccountTransfer() {
        return create(SelfAccountTransferApi.class);
    }

    public SelfAuthenticationApi selfAuthentication() {
        return create(SelfAuthenticationApi.class);
    }

    public SelfClientApi selfClient() {
        return create(SelfClientApi.class);
    }

    public SelfDividendApi selfDividend() {
        return create(SelfDividendApi.class);
    }

    public SelfLoanProductsApi selfLoanProducts() {
        return create(SelfLoanProductsApi.class);
    }

    public SelfLoansApi selfLoans() {
        return create(SelfLoansApi.class);
    }

    public SelfRunReportApi selfRunReport() {
        return create(SelfRunReportApi.class);
    }

    public SelfSavingsAccountApi selfSavingsAccount() {
        return create(SelfSavingsAccountApi.class);
    }

    public SelfSavingsProductsApi selfSavingsProducts() {
        return create(SelfSavingsProductsApi.class);
    }

    public SelfScoreCardApi selfScoreCard() {
        return create(SelfScoreCardApi.class);
    }

    public SelfServiceRegistrationApi selfServiceRegistration() {
        return create(SelfServiceRegistrationApi.class);
    }

    public SelfShareAccountsApi selfShareAccounts() {
        return create(SelfShareAccountsApi.class);
    }

    public SelfShareProductsApi selfShareProducts() {
        return create(SelfShareProductsApi.class);
    }

    public SelfSpmApi selfSpm() {
        return create(SelfSpmApi.class);
    }

    public SelfThirdPartyTransferApi selfThirdPartyTransfer() {
        return create(SelfThirdPartyTransferApi.class);
    }

    public SelfUserApi selfUser() {
        return create(SelfUserApi.class);
    }

    public SelfUserDetailsApi selfUserDetails() {
        return create(SelfUserDetailsApi.class);
    }

    public ShareAccountApi shareAccount() {
        return create(ShareAccountApi.class);
    }

    public SmsApi sms() {
        return create(SmsApi.class);
    }

    public SpmApiLookUpTableApi spmApiLookUpTable() {
        return create(SpmApiLookUpTableApi.class);
    }

    public SpmSurveysApi spmSurveys() {
        return create(SpmSurveysApi.class);
    }

    public StaffApi staff() {
        return create(StaffApi.class);
    }

    public StandingInstructionsApi standingInstructions() {
        return create(StandingInstructionsApi.class);
    }

    public StandingInstructionsHistoryApi standingInstructionsHistory() {
        return create(StandingInstructionsHistoryApi.class);
    }

    public SurveyApi survey() {
        return create(SurveyApi.class);
    }

    public TaxComponentsApi taxComponents() {
        return create(TaxComponentsApi.class);
    }

    public TaxGroupApi taxGroup() {
        return create(TaxGroupApi.class);
    }

    public TellerCashManagementApi tellerCashManagement() {
        return create(TellerCashManagementApi.class);
    }

    public TwoFactorApi twoFactor() {
        return create(TwoFactorApi.class);
    }

    public UserGeneratedDocumentsApi userGeneratedDocuments() {
        return create(UserGeneratedDocumentsApi.class);
    }

    public UsersApi users() {
        return create(UsersApi.class);
    }

    public WorkingDaysApi workingDays() {
        return create(WorkingDaysApi.class);
    }

    /**
     * Builder for creating and configuring a FineractFeignClient.
     */
    public static class Builder {

        private final FineractFeignClientConfig.Builder configBuilder = FineractFeignClientConfig.builder();

        /**
         * Sets the base URL for the Fineract API.
         *
         * @param baseUrl
         *            The base URL (e.g., "https://localhost:8443/fineract-provider/api/v1")
         * @return This builder instance
         */
        public Builder baseUrl(String baseUrl) {
            configBuilder.baseUrl(baseUrl);
            return this;
        }

        /**
         * Sets the credentials for Basic Authentication.
         *
         * @param username
         *            The username
         * @param password
         *            The password
         * @return This builder instance
         */
        public Builder credentials(String username, String password) {
            configBuilder.credentials(username, password);
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param timeout
         *            The timeout value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder connectTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
            configBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the read timeout.
         *
         * @param timeout
         *            The timeout value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder readTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
            configBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
         * Enables or disables debug logging.
         *
         * @param enabled
         *            true to enable debug logging, false to disable
         * @return This builder instance
         */
        public Builder debug(boolean enabled) {
            configBuilder.debugEnabled(enabled);
            return this;
        }

        /**
         * Disables SSL certificate verification. Use only for testing with self-signed certificates.
         *
         * @param disable
         *            true to disable SSL verification, false to enable
         * @return This builder instance
         */
        public Builder disableSslVerification(boolean disable) {
            configBuilder.disableSslVerification(disable);
            return this;
        }

        public Builder tenantId(String tenantId) {
            configBuilder.tenantId(tenantId);
            return this;
        }

        /**
         * Sets the connection time-to-live (TTL) for connection pool recycling.
         *
         * @param ttl
         *            The time-to-live value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder connectionTimeToLive(long ttl, java.util.concurrent.TimeUnit unit) {
            configBuilder.connectionTimeToLive(ttl, unit);
            return this;
        }

        /**
         * Sets the idle connection eviction time.
         *
         * @param time
         *            The eviction time value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder idleConnectionEvictionTime(long time, java.util.concurrent.TimeUnit unit) {
            configBuilder.idleConnectionEvictionTime(time, unit);
            return this;
        }

        /**
         * Sets the maximum total connections in the pool.
         *
         * @param max
         *            Maximum total connections
         * @return This builder instance
         */
        public Builder maxConnections(int max) {
            configBuilder.maxConnTotal(max);
            return this;
        }

        /**
         * Sets the maximum connections per route.
         *
         * @param max
         *            Maximum connections per route
         * @return This builder instance
         */
        public Builder maxConnectionsPerRoute(int max) {
            configBuilder.maxConnPerRoute(max);
            return this;
        }

        /**
         * Sets the HTTP client type.
         *
         * @param clientType
         *            The HTTP client type (APACHE or OKHTTP)
         * @return This builder instance
         */
        public Builder httpClientType(FineractFeignClientConfig.HttpClientType clientType) {
            configBuilder.httpClientType(clientType);
            return this;
        }

        /**
         * Builds a new FineractFeignClient with the current configuration.
         *
         * @return A new FineractFeignClient instance
         */
        public FineractFeignClient build() {
            return new FineractFeignClient(this);
        }
    }
}
