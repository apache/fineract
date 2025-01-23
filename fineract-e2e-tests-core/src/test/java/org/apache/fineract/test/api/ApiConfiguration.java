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
package org.apache.fineract.test.api;

import org.apache.fineract.client.services.BatchApiApi;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.services.BusinessStepConfigurationApi;
import org.apache.fineract.client.services.ChargesApi;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.CodeValuesApi;
import org.apache.fineract.client.services.CodesApi;
import org.apache.fineract.client.services.CurrencyApi;
import org.apache.fineract.client.services.DataTablesApi;
import org.apache.fineract.client.services.DefaultApi;
import org.apache.fineract.client.services.DelinquencyRangeAndBucketsManagementApi;
import org.apache.fineract.client.services.ExternalAssetOwnerLoanProductAttributesApi;
import org.apache.fineract.client.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.services.ExternalEventConfigurationApi;
import org.apache.fineract.client.services.FundsApi;
import org.apache.fineract.client.services.GeneralLedgerAccountApi;
import org.apache.fineract.client.services.GlobalConfigurationApi;
import org.apache.fineract.client.services.InlineJobApi;
import org.apache.fineract.client.services.JournalEntriesApi;
import org.apache.fineract.client.services.LoanAccountLockApi;
import org.apache.fineract.client.services.LoanChargesApi;
import org.apache.fineract.client.services.LoanCobCatchUpApi;
import org.apache.fineract.client.services.LoanInterestPauseApi;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.MappingFinancialActivitiesToAccountsApi;
import org.apache.fineract.client.services.PaymentTypeApi;
import org.apache.fineract.client.services.RescheduleLoansApi;
import org.apache.fineract.client.services.RolesApi;
import org.apache.fineract.client.services.SavingsAccountApi;
import org.apache.fineract.client.services.SavingsAccountTransactionsApi;
import org.apache.fineract.client.services.SavingsProductApi;
import org.apache.fineract.client.services.SchedulerApi;
import org.apache.fineract.client.services.SchedulerJobApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.client.util.FineractClient;
import org.apache.fineract.test.stepdef.loan.LoanProductsCustomApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfiguration {

    @Autowired
    private FineractClient fineractClient;

    @Bean
    public SchedulerApi schedulerApi() {
        return fineractClient.createService(SchedulerApi.class);
    }

    @Bean
    public SchedulerJobApi schedulerJobApi() {
        return fineractClient.createService(SchedulerJobApi.class);
    }

    @Bean
    public CurrencyApi currencyApi() {
        return fineractClient.createService(CurrencyApi.class);
    }

    @Bean
    public DataTablesApi dataTablesApi() {
        return fineractClient.createService(DataTablesApi.class);
    }

    @Bean
    public ChargesApi chargesApi() {
        return fineractClient.createService(ChargesApi.class);
    }

    @Bean
    public GeneralLedgerAccountApi generalLedgerAccountApi() {
        return fineractClient.createService(GeneralLedgerAccountApi.class);
    }

    @Bean
    public LoanProductsApi loanProductsApi() {
        return fineractClient.createService(LoanProductsApi.class);
    }

    @Bean
    public LoanProductsCustomApi loanProductsCustomApi() {
        return fineractClient.createService(LoanProductsCustomApi.class);
    }

    @Bean
    public SavingsProductApi savingsProductApi() {
        return fineractClient.createService(SavingsProductApi.class);
    }

    @Bean
    public SavingsAccountTransactionsApi savingsAccountTransactionsApi() {
        return fineractClient.createService(SavingsAccountTransactionsApi.class);
    }

    @Bean
    public SavingsAccountApi savingsAccountApi() {
        return fineractClient.createService(SavingsAccountApi.class);
    }

    @Bean
    public CodesApi codesApi() {
        return fineractClient.createService(CodesApi.class);
    }

    @Bean
    public CodeValuesApi codeValuesApi() {
        return fineractClient.createService(CodeValuesApi.class);
    }

    @Bean
    public DelinquencyRangeAndBucketsManagementApi delinquencyRangeAndBucketsManagementApi() {
        return fineractClient.createService(DelinquencyRangeAndBucketsManagementApi.class);
    }

    @Bean
    public FundsApi fundsApi() {
        return fineractClient.createService(FundsApi.class);
    }

    @Bean
    public GlobalConfigurationApi globalConfigurationApi() {
        return fineractClient.createService(GlobalConfigurationApi.class);
    }

    @Bean
    public PaymentTypeApi paymentTypeApi() {
        return fineractClient.createService(PaymentTypeApi.class);
    }

    @Bean
    public BusinessDateManagementApi businessDateManagementApi() {
        return fineractClient.createService(BusinessDateManagementApi.class);
    }

    @Bean
    public ClientApi clientApi() {
        return fineractClient.createService(ClientApi.class);
    }

    @Bean
    public BatchApiApi batchApiApi() {
        return fineractClient.createService(BatchApiApi.class);
    }

    @Bean
    public LoansApi loansApi() {
        return fineractClient.createService(LoansApi.class);
    }

    @Bean
    public JournalEntriesApi journalEntriesApi() {
        return fineractClient.createService(JournalEntriesApi.class);
    }

    @Bean
    public InlineJobApi inlineJobApi() {
        return fineractClient.createService(InlineJobApi.class);
    }

    @Bean
    public LoanTransactionsApi loanTransactionsApi() {
        return fineractClient.createService(LoanTransactionsApi.class);
    }

    @Bean
    public LoanChargesApi loanChargesApi() {
        return fineractClient.createService(LoanChargesApi.class);
    }

    @Bean
    public ExternalEventConfigurationApi externalEventConfigurationApi() {
        return fineractClient.createService(ExternalEventConfigurationApi.class);
    }

    @Bean
    public LoanCobCatchUpApi loanCobCatchUpApi() {
        return fineractClient.createService(LoanCobCatchUpApi.class);
    }

    @Bean
    public RolesApi rolesApi() {
        return fineractClient.createService(RolesApi.class);
    }

    @Bean
    public UsersApi usersApi() {
        return fineractClient.createService(UsersApi.class);
    }

    @Bean
    public ExternalAssetOwnersApi externalAssetOwnersApi() {
        return fineractClient.createService(ExternalAssetOwnersApi.class);
    }

    @Bean
    public ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributesApi() {
        return fineractClient.createService(ExternalAssetOwnerLoanProductAttributesApi.class);
    }

    @Bean
    public BusinessStepConfigurationApi businessStepConfigurationApi() {
        return fineractClient.createService(BusinessStepConfigurationApi.class);
    }

    @Bean
    public MappingFinancialActivitiesToAccountsApi mappingFinancialActivitiesToAccountsApi() {
        return fineractClient.createService(MappingFinancialActivitiesToAccountsApi.class);
    }

    @Bean
    public LoanAccountLockApi loanAccountLockApi() {
        return fineractClient.createService(LoanAccountLockApi.class);
    }

    @Bean
    public DefaultApi defaultApi() {
        return fineractClient.createService(DefaultApi.class);
    }

    @Bean
    public RescheduleLoansApi rescheduleLoansApi() {
        return fineractClient.createService(RescheduleLoansApi.class);
    }

    @Bean
    public LoanInterestPauseApi loanInterestPauseApi() {
        return fineractClient.createService(LoanInterestPauseApi.class);
    }
}
