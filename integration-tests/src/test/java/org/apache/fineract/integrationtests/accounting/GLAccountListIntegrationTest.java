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
package org.apache.fineract.integrationtests.accounting;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.AccountingRulesApi;
import org.apache.fineract.client.feign.services.ChargesApi;
import org.apache.fineract.client.feign.services.FixedDepositProductApi;
import org.apache.fineract.client.feign.services.GeneralLedgerAccountApi;
import org.apache.fineract.client.feign.services.GeneralLedgerAccountApi.RetrieveAllAccountsQueryParams;
import org.apache.fineract.client.feign.services.LoanProductsApi;
import org.apache.fineract.client.feign.services.MappingFinancialActivitiesToAccountsApi;
import org.apache.fineract.client.feign.services.ProductsApi;
import org.apache.fineract.client.feign.services.ProvisioningCategoryApi;
import org.apache.fineract.client.feign.services.ProvisioningCriteriaApi;
import org.apache.fineract.client.feign.services.RecurringDepositProductApi;
import org.apache.fineract.client.feign.services.SavingsProductApi;
import org.apache.fineract.client.feign.services.SchedulerJobApi;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanProductsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.AccountRuleRequest;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateResponse.TypeEnum;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.CodeValueData;
import org.apache.fineract.client.models.EnumOptionData;
import org.apache.fineract.client.models.FinancialActivityData;
import org.apache.fineract.client.models.GLAccountData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GetGLAccountsTemplateResponse;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanProductsAccountingMappingOptions;
import org.apache.fineract.client.models.GetLoanProductsAssetAccountOptions;
import org.apache.fineract.client.models.GetLoanProductsExpenseAccountOptions;
import org.apache.fineract.client.models.GetLoanProductsIncomeAccountOptions;
import org.apache.fineract.client.models.GetLoanProductsLiabilityAccountOptions;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetProductsAccountingMappingOptions;
import org.apache.fineract.client.models.GetProductsAssetAccountOptions;
import org.apache.fineract.client.models.GetProductsEquityAccountOptions;
import org.apache.fineract.client.models.GetProductsIncomeAccountOptions;
import org.apache.fineract.client.models.GetProductsLiabilityAccountOptions;
import org.apache.fineract.client.models.GetSavingsProductsAccountingMappingOptions;
import org.apache.fineract.client.models.GetSavingsProductsAssetAccountOptions;
import org.apache.fineract.client.models.GetSavingsProductsExpenseAccountOptions;
import org.apache.fineract.client.models.GetSavingsProductsIncomeAccountOptions;
import org.apache.fineract.client.models.GetSavingsProductsLiabilityAccountOptions;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.JobUpdateRequest;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanProductData;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostJournalEntriesTransactionIdRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostRecurringDepositProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.client.models.PutGLAccountsRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAccountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.ReportedError;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GLAccountListIntegrationTest extends FeignIntegrationTest {

    private static final int ASSET = GLAccountType.ASSET.getValue();
    private static final int INCOME = GLAccountType.INCOME.getValue();
    private static final int DETAIL = GLAccountUsage.DETAIL.getValue();
    private static final int HEADER = GLAccountUsage.HEADER.getValue();
    private static final Map<String, Object> TEMPLATE = Map.of("template", true);

    private final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient());
    private final Deque<Executable> cleanup = new ArrayDeque<>();
    private final Set<Long> accountsWithJournalEntries = new HashSet<>();

    @AfterEach
    void cleanUpAccountsAndTags() {
        try {
            assertAll(cleanup);
        } finally {
            cleanup.clear();
            accountsWithJournalEntries.clear();
        }
    }

    @Test
    void listsAccountsInCodeOrderAndCombinesSearchWithAllFilters() {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final String match = prefix + "Find";
        final Long tagId = createAssetTag(prefix);
        final Long parent = createAccount("Parent " + match, prefix + "30", ASSET, HEADER, false, null, null);
        final String childName = "Child " + match + " name";
        final Long nameMatch = createAccount(childName, prefix + "10", ASSET, DETAIL, false, parent, tagId);
        final Long codeMatch = createAccount(prefix + " code", prefix + "Z" + match + "120", ASSET, DETAIL, false, null, null);
        final Long disabled = createAccount(match + " disabled", prefix + "40", ASSET, DETAIL, false, null, null);
        accountHelper.updateGLAccount(disabled, new PutGLAccountsRequest().disabled(true));
        final Long otherType = createAccount(match + " income", prefix + "50", INCOME, DETAIL, false, null, null);
        final Long unmatched = createAccount(prefix + " Unmatched", prefix + "60", ASSET, DETAIL, false, null, null);
        final Long bothMatch = createAccount(match + " both", match + "10", ASSET, DETAIL, true, null, null);
        final Set<Long> fixtureIds = Set.of(parent, nameMatch, codeMatch, disabled, otherType, unmatched, bothMatch);
        final List<GetGLAccountsResponse> unfiltered = accounts(new RetrieveAllAccountsQueryParams(), fixtureIds);

        assertEquals(List.of(nameMatch, parent, disabled, otherType, unmatched, bothMatch, codeMatch), ids(unfiltered));
        assertEquals(unfiltered, accounts(new RetrieveAllAccountsQueryParams().searchParam("   "), fixtureIds));
        assertEquals(List.of(nameMatch, parent, disabled, unmatched, bothMatch, codeMatch),
                ids(accounts(new RetrieveAllAccountsQueryParams().type(ASSET), fixtureIds)));
        assertEquals(List.of(parent), ids(accounts(new RetrieveAllAccountsQueryParams().usage(HEADER), fixtureIds)));
        final List<GetGLAccountsResponse> manualAccounts = accounts(new RetrieveAllAccountsQueryParams().manualEntriesAllowed(true),
                fixtureIds);
        assertEquals(List.of(bothMatch), ids(manualAccounts));
        assertEquals(Boolean.TRUE, manualAccounts.getFirst().getManualEntriesAllowed());
        final List<GetGLAccountsResponse> disabledAccounts = accounts(new RetrieveAllAccountsQueryParams().disabled(true), fixtureIds);
        assertEquals(List.of(disabled), ids(disabledAccounts));
        assertEquals(Boolean.TRUE, disabledAccounts.getFirst().getDisabled());
        assertEquals(List.of(nameMatch, parent, disabled, otherType, bothMatch, codeMatch),
                ids(accounts(new RetrieveAllAccountsQueryParams().searchParam(match), fixtureIds)));
        assertEquals(List.of(nameMatch), ids(accounts(new RetrieveAllAccountsQueryParams().searchParam(match + " name"), fixtureIds)));
        assertEquals(List.of(nameMatch, codeMatch), ids(accounts(new RetrieveAllAccountsQueryParams().type(ASSET).usage(DETAIL)
                .manualEntriesAllowed(false).disabled(false).searchParam(match), fixtureIds)));
        assertEquals(List.of(bothMatch), ids(accounts(new RetrieveAllAccountsQueryParams().searchParam(match + "_0"), fixtureIds)));
        assertEquals(List.of(bothMatch, codeMatch),
                ids(accounts(new RetrieveAllAccountsQueryParams().searchParam(match + "%0"), fixtureIds)));
        assertTrue(ok(() -> fineractClient().generalLedgerAccount()
                .retrieveAllAccounts(new RetrieveAllAccountsQueryParams().searchParam(prefix + "Absent"))).isEmpty());

        final GetGLAccountsResponse child = unfiltered.getFirst();
        assertEquals(accountHelper.getGLAccount(nameMatch), child);
        assertEquals(parent, child.getParentId());
        assertEquals(childName, child.getName());
        assertEquals("...." + childName, child.getNameDecorated());
        assertEquals(childName + " description", child.getDescription());
        assertEquals(Boolean.FALSE, child.getManualEntriesAllowed());
        assertEquals(Boolean.FALSE, child.getDisabled());
        final EnumOptionData type = child.getType();
        final EnumOptionData usage = child.getUsage();
        assertNotNull(type);
        assertNotNull(usage);
        assertEquals(ASSET, type.getId());
        assertEquals(DETAIL, usage.getId());
        final CodeValueData childTag = child.getTagId();
        assertNotNull(childTag);
        assertEquals(tagId, childTag.getId());
        assertEquals(prefix, childTag.getName());

        final GetGLAccountsResponse root = unfiltered.get(1);
        assertNull(root.getParentId());
        assertEquals(root.getName(), root.getNameDecorated());
        final EnumOptionData rootUsage = root.getUsage();
        assertNotNull(rootUsage);
        assertEquals(HEADER, rootUsage.getId());
        final CodeValueData rootTag = root.getTagId();
        assertNotNull(rootTag);
        assertEquals(0L, rootTag.getId());
        assertNull(rootTag.getName());
    }

    @Test
    void rejectsInvalidAccountTypeOrUsageFilters() {
        assertInvalidFilter(new RetrieveAllAccountsQueryParams().type(99), "99");
        assertInvalidFilter(new RetrieveAllAccountsQueryParams().type(99).usage(98), "99");
        assertInvalidFilter(new RetrieveAllAccountsQueryParams().type(ASSET).usage(98), Integer.toString(ASSET));
        assertInvalidFilter(new RetrieveAllAccountsQueryParams().usage(98), "null");
    }

    @Test
    void showsOnlyEnabledHeadersInTheNewAndExistingAccountTemplates() {
        final GeneralLedgerAccountApi glAccounts = fineractClient().generalLedgerAccount();
        final int expense = GLAccountType.EXPENSE.getValue();
        final RetrieveAllAccountsQueryParams expenseHeaderFilter = new RetrieveAllAccountsQueryParams().type(expense).usage(HEADER)
                .disabled(false);
        if (ok(() -> glAccounts.retrieveAllAccounts(expenseHeaderFilter)).isEmpty()) {
            assertNull(ok(() -> glAccounts.retrieveNewAccountDetails(expense)).getExpenseHeaderAccountOptions());
        }

        final GLAccountFixtures fixtures = createAccountFixtures(HEADER);
        final Long assetId = fixtures.expected(GLAccountType.ASSET).getFirst();
        final GetGLAccountsTemplateResponse newAccount = ok(() -> glAccounts.retrieveNewAccountDetails(ASSET));
        fixtures.assertIds(GLAccountType.ASSET, newAccount.getAssetHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.LIABILITY, newAccount.getLiabilityHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.EQUITY, newAccount.getEquityHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.INCOME, newAccount.getIncomeHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.EXPENSE, newAccount.getExpenseHeaderAccountOptions());

        final GetGLAccountsResponse existingAccount = ok(() -> glAccounts.retreiveAccountUniversal(assetId, TEMPLATE));
        fixtures.assertIds(GLAccountType.ASSET, existingAccount.getAssetHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.LIABILITY, existingAccount.getLiabilityHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.EQUITY, existingAccount.getEquityHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.INCOME, existingAccount.getIncomeHeaderAccountOptions());
        fixtures.assertIds(GLAccountType.EXPENSE, existingAccount.getExpenseHeaderAccountOptions());
        assertEquals(assetId, existingAccount.getId());
    }

    @Test
    void showsEnabledDetailAccountsInAccountingRuleAndProvisioningTemplates() {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final FineractFeignClient client = fineractClient();
        final AccountingRulesApi rules = client.accountingRules();
        final ProvisioningCriteriaApi provisioning = client.provisioningCriteria();
        final ProvisioningCategoryApi categories = client.provisioningCategory();
        final String name = Utils.uniqueRandomStringGenerator("GL options ", 8);
        final Long asset = fixtures.expected(GLAccountType.ASSET).getFirst();
        final Long income = fixtures.expected(GLAccountType.INCOME).getFirst();
        final Long ruleId = ok(() -> rules
                .createAccountingRule(new AccountRuleRequest().name(name).officeId(1L).accountToDebit(asset).accountToCredit(income)))
                .getResourceId();
        assertNotNull(ruleId);
        cleanup.push(() -> ok(() -> rules.deleteAccountingRule(ruleId)));
        fixtures.assertAllIds(ok(() -> rules.retrieveTemplateAccountingRuleUniversal(Map.of())).getAllowedAccounts());
        fixtures.assertAllIds(ok(() -> rules.retrieveOneAccountingRuleUniversal(ruleId, TEMPLATE)).getAllowedAccounts());

        final Long categoryId = ok(() -> categories.createProvisioningCategory("{\"categoryname\":\"" + name + "\"}")).getResourceId();
        assertNotNull(categoryId);
        cleanup.push(() -> ok(() -> categories.deleteProvisioningCategory(categoryId)));
        final ProvisioningCriteriaDefinitionData definition = new ProvisioningCriteriaDefinitionData().categoryId(categoryId)
                .categoryName(name).minAge(1L).maxAge(90L).provisioningPercentage(BigDecimal.TEN)
                .liabilityAccount(fixtures.expected(GLAccountType.LIABILITY).getFirst())
                .expenseAccount(fixtures.expected(GLAccountType.EXPENSE).getFirst());
        final Long loanId = createLoanProduct(Utils.uniqueRandomStringGenerator("GLPROV", 8));
        final Long criteriaId = ok(() -> provisioning.createProvisioningCriteria(new PostProvisioningCriteriaRequest().criteriaName(name)
                .loanProducts(List.of(new LoanProductData().id(loanId))).definitions(List.of(definition)))).getResourceId();
        assertNotNull(criteriaId);
        cleanup.push(() -> ok(() -> provisioning.deleteProvisioningCriteria(criteriaId)));
        fixtures.assertAllIds(ok(() -> provisioning.retrieveTemplate1Universal(Map.of())).getGlAccounts());
        fixtures.assertAllIds(ok(() -> provisioning.retrieveOneProvisioningCriteriaUniversal(criteriaId, TEMPLATE)).getGlAccounts());
    }

    @Test
    void showsEnabledDetailAccountsInFinancialActivityTaxAndChargeOptions() {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final FineractFeignClient client = fineractClient();
        final MappingFinancialActivitiesToAccountsApi activities = client.mappingFinancialActivitiesToAccounts();
        final Long mappingId = openingBalanceMapping(fixtures.expected(GLAccountType.EQUITY).getFirst());
        fixtures.assertDropdown(ok(() -> activities.retrieveTemplateUniversal(Map.of())).getGlAccountOptions());
        fixtures.assertDropdown(ok(() -> activities.retreiveUniversal(mappingId, TEMPLATE)).getGlAccountOptions());
        fixtures.assertDropdown(ok(() -> client.taxComponents().retrieveTemplateTaxComponentUniversal(Map.of())).getGlAccountOptions());

        final ChargesApi charges = client.charges();
        final Long chargeId = ok(() -> charges.createCharge(ChargeRequestBuilders.loanDisbursementFee(10.0))).getResourceId();
        assertNotNull(chargeId);
        cleanup.push(() -> ok(() -> charges.deleteCharge(chargeId)));
        final ChargeData chargeTemplate = ok(() -> charges.retrieveTemplateChargeUniversal(Map.of()));
        fixtures.assertChargeOptions(chargeTemplate.getAssetAccountOptions(), chargeTemplate.getExpenseAccountOptions(),
                chargeTemplate.getIncomeOrLiabilityAccountOptions());
        final ChargeData existingCharge = ok(() -> charges.retrieveOneChargeUniversal(chargeId, TEMPLATE));
        fixtures.assertChargeOptions(existingCharge.getAssetAccountOptions(), existingCharge.getExpenseAccountOptions(),
                existingCharge.getIncomeOrLiabilityAccountOptions());
    }

    @Test
    void excludesTheContraAccountAndNonPostingAccountsFromOpeningBalances() throws JsonProcessingException {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final Long equity = fixtures.expected(GLAccountType.EQUITY).getFirst();
        openingBalanceMapping(equity);
        final String response = ok(() -> fineractClient().journalEntries().retrieveOpeningBalance(1L, "USD"));
        final OpeningBalances openingBalances = ObjectMapperFactory.getShared().readValue(response, OpeningBalances.class);
        final GLAccountData contraAccount = openingBalances.contraAccount();
        assertNotNull(contraAccount);
        assertEquals(equity, contraAccount.getId());
        for (final GLAccountType type : GLAccountType.values()) {
            final List<OpeningBalanceEntry> balances = switch (type) {
                case ASSET -> openingBalances.assetAccountOpeningBalances();
                case LIABILITY -> openingBalances.liabilityAccountOpeningBalances();
                case EQUITY -> openingBalances.equityAccountOpeningBalances();
                case INCOME -> openingBalances.incomeAccountOpeningBalances();
                case EXPENSE -> openingBalances.expenseAccountOpeningBalances();
            };
            assertNotNull(balances, type.name());
            final List<Long> expected = fixtures.expected(type).stream().filter(id -> !equity.equals(id)).toList();
            final List<Long> actual = balances.stream().map(OpeningBalanceEntry::glAccountId).filter(fixtures.fixtureIds()::contains)
                    .toList();
            assertEquals(expected, actual, type.name());
        }
    }

    @Test
    void showsEnabledDetailAccountsInLoanProductTemplatesByIdAndExternalId() {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final LoanProductsApi loans = fineractClient().loanProducts();
        final String loanExternalId = Utils.uniqueRandomStringGenerator("GLLOAN", 8);
        final Long loanId = createLoanProduct(loanExternalId);
        final GetLoanProductsAccountingMappingOptions loanTemplateOptions = ok(() -> loans.retrieveTemplateLoanProduct(false))
                .getAccountingMappingOptions();
        assertNotNull(loanTemplateOptions);
        fixtures.assertUnorderedIds(GLAccountType.ASSET, loanTemplateOptions.getAssetAccountOptions(),
                GetLoanProductsAssetAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.LIABILITY, loanTemplateOptions.getLiabilityAccountOptions(),
                GetLoanProductsLiabilityAccountOptions::getId);
        fixtures.assertIds(GLAccountType.EQUITY, loanTemplateOptions.getEquityAccountOptions());
        fixtures.assertUnorderedIds(GLAccountType.INCOME, loanTemplateOptions.getIncomeAccountOptions(),
                GetLoanProductsIncomeAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.EXPENSE, loanTemplateOptions.getExpenseAccountOptions(),
                GetLoanProductsExpenseAccountOptions::getId);
        final GetLoanProductsProductIdResponse loanById = ok(() -> loans.retrieveOneLoanProductUniversal(loanId, TEMPLATE));
        final Map<String, List<GLAccountData>> loanAccountOptions = loanById.getAccountingMappingOptions();
        fixtures.assertDropdown(loanAccountOptions);
        final GetLoanProductsProductIdResponse loanByExternalId = ok(
                () -> loans.retrieveLoanProductDetailsByExternalIdUniversal(loanExternalId, TEMPLATE));
        assertEquals(loanId, loanByExternalId.getId());
        assertEquals(loanAccountOptions, loanByExternalId.getAccountingMappingOptions());

        final WorkingCapitalLoanProductsApi workingCapital = fineractClient().workingCapitalLoanProducts();
        final String externalId = Utils.uniqueRandomStringGenerator("GLWC", 8);
        final PostWorkingCapitalLoanProductsRequest product = new WorkingCapitalLoanProductTestBuilder().withExternalId(externalId)
                .withName(externalId).withShortName(Utils.uniqueRandomStringGenerator("", 4)).build();
        final Long productId = ok(() -> workingCapital.createWorkingCapitalLoanProduct(product)).getResourceId();
        assertNotNull(productId);
        cleanup.push(() -> ok(() -> workingCapital.deleteWorkingCapitalLoanProduct(productId)));
        fixtures.assertDropdown(ok(workingCapital::retrieveTemplateWorkingCapitalLoanProduct).getAccountingMappingOptions());
        final GetWorkingCapitalLoanProductsProductIdResponse workingCapitalById = ok(
                () -> workingCapital.retrieveOneWorkingCapitalLoanProductUniversal(productId, TEMPLATE));
        final Map<String, List<GLAccountData>> workingCapitalAccountOptions = workingCapitalById.getAccountingMappingOptions();
        fixtures.assertDropdown(workingCapitalAccountOptions);
        final GetWorkingCapitalLoanProductsProductIdResponse workingCapitalByExternalId = ok(
                () -> workingCapital.retrieveOneWorkingCapitalLoanProductByExternalIdUniversal(externalId, TEMPLATE));
        assertEquals(productId, workingCapitalByExternalId.getId());
        assertEquals(workingCapitalAccountOptions, workingCapitalByExternalId.getAccountingMappingOptions());
    }

    @Test
    void showsEnabledDetailAccountsInSavingsAndDepositProductTemplates() throws JsonProcessingException {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final FineractFeignClient client = fineractClient();
        final SavingsProductApi savings = client.savingsProduct();
        final FixedDepositProductApi fixed = client.fixedDepositProduct();
        final RecurringDepositProductApi recurring = client.recurringDepositProduct();
        final Long savingsId = ok(() -> savings.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct())).getResourceId();
        assertNotNull(savingsId);
        cleanup.push(() -> ok(() -> savings.deleteSavingsProduct(savingsId)));

        final String chartValidFrom = "01 January 2013";
        final String chartValidTo = "01 January 2030";
        final PostFixedDepositProductsRequest fixedRequest = DepositRequestBuilders.withChart(DepositRequestBuilders.fixedDepositProduct(),
                chartValidFrom, chartValidTo, DepositTestData.periodRangeChartSlabs());
        final Long fixedId = ok(() -> fixed.createFixedDepositProduct(fixedRequest)).getResourceId();
        assertNotNull(fixedId);
        cleanup.push(() -> ok(() -> fixed.deleteFixedDepositProduct(fixedId)));

        final PostRecurringDepositProductsRequest recurringRequest = DepositRequestBuilders.withChart(
                DepositRequestBuilders.recurringDepositProduct(), chartValidFrom, chartValidTo,
                DepositTestData.recurringChartSlabsFor("period"));
        final Long recurringId = ok(() -> recurring.createRecurringDepositProduct(recurringRequest)).getResourceId();
        assertNotNull(recurringId);
        cleanup.push(() -> ok(() -> recurring.deleteRecurringDepositProduct(recurringId)));

        final GetSavingsProductsAccountingMappingOptions savingsTemplateOptions = ok(savings::retrieveTemplateSavingsProduct)
                .getAccountingMappingOptions();
        assertNotNull(savingsTemplateOptions);
        fixtures.assertUnorderedIds(GLAccountType.ASSET, savingsTemplateOptions.getAssetAccountOptions(),
                GetSavingsProductsAssetAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.LIABILITY, savingsTemplateOptions.getLiabilityAccountOptions(),
                GetSavingsProductsLiabilityAccountOptions::getId);
        fixtures.assertIds(GLAccountType.EQUITY, savingsTemplateOptions.getEquityAccountOptions());
        fixtures.assertUnorderedIds(GLAccountType.INCOME, savingsTemplateOptions.getIncomeAccountOptions(),
                GetSavingsProductsIncomeAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.EXPENSE, savingsTemplateOptions.getExpenseAccountOptions(),
                GetSavingsProductsExpenseAccountOptions::getId);
        fixtures.assertDropdown(ok(() -> savings.retrieveOneSavingsProductUniversal(savingsId, TEMPLATE)).getAccountingMappingOptions());
        fixtures.assertDropdown(readAccountingMappingOptions(ok(fixed::retrieveTemplateFixedDepositProduct)));
        fixtures.assertDropdown(ok(() -> fixed.retrieveOneFixedDepositProductUniversal(fixedId, TEMPLATE)).getAccountingMappingOptions());
        fixtures.assertDropdown(readAccountingMappingOptions(ok(recurring::retrieveTemplateRecurringDepositProduct)));
        fixtures.assertDropdown(
                ok(() -> recurring.retrieveOneRecurringDepositProductUniversal(recurringId, TEMPLATE)).getAccountingMappingOptions());
    }

    @Test
    void showsEnabledDetailAccountsInShareProductTemplates() throws JsonProcessingException {
        final GLAccountFixtures fixtures = createAccountFixtures(DETAIL);
        final ProductsApi products = fineractClient().products();
        final PostProductsTypeRequest request = FeignShareAccountHelper.defaultShareProduct();
        final Long productId = new FeignShareAccountHelper(fineractClient()).createShareProduct(request);
        assertNotNull(productId);
        final Map<String, List<GLAccountData>> shareTemplateOptions = readAccountingMappingOptions(
                ok(() -> products.retrieveTemplateShareProduct("share")));
        fixtures.assertDropdown(shareTemplateOptions, GLAccountType.ASSET, GLAccountType.LIABILITY, GLAccountType.EQUITY,
                GLAccountType.INCOME);
        final GetProductsAccountingMappingOptions existingShareOptions = ok(
                () -> products.retrieveOneShareProductUniversal(productId, "share", TEMPLATE)).getAccountingMappingOptions();
        assertNotNull(existingShareOptions);
        fixtures.assertUnorderedIds(GLAccountType.ASSET, existingShareOptions.getAssetAccountOptions(),
                GetProductsAssetAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.LIABILITY, existingShareOptions.getLiabilityAccountOptions(),
                GetProductsLiabilityAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.EQUITY, existingShareOptions.getEquityAccountOptions(),
                GetProductsEquityAccountOptions::getId);
        fixtures.assertUnorderedIds(GLAccountType.INCOME, existingShareOptions.getIncomeAccountOptions(),
                GetProductsIncomeAccountOptions::getId);
    }

    @Test
    void downloadsOrderedAccountsAndTagsInBothWorkbooks() throws IOException {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final Long tag = createAssetTag(prefix);
        final Long header = createAccount(prefix + " Header", prefix + "01", ASSET, HEADER, false, null, null);
        final Long detail = createAccount(prefix + " (Detail)", prefix + "02", ASSET, DETAIL, true, header, tag);
        final Long disabled = createAccount(prefix + " Disabled", prefix + "03", ASSET, DETAIL, true, null, null);
        accountHelper.updateGLAccount(disabled, new PutGLAccountsRequest().disabled(true));
        final Set<String> fixtureIds = Set.of(header.toString(), detail.toString(), disabled.toString());
        final FeignBulkImportHelper bulk = new FeignBulkImportHelper(fineractClient());
        final DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = bulk.downloadTemplate("glaccounts", Map.of())) {
            final Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
            assertNotNull(sheet);
            final List<List<String>> rows = new ArrayList<>();
            for (final Row row : sheet) {
                final String id = formatter.formatCellValue(row.getCell(ChartOfAccountsConstants.LOOKUP_ACCOUNT_ID_COL));
                if (fixtureIds.contains(id)) {
                    rows.add(List.of(id, formatter.formatCellValue(row.getCell(ChartOfAccountsConstants.LOOKUP_ACCOUNT_NAME_COL)),
                            formatter.formatCellValue(row.getCell(ChartOfAccountsConstants.LOOKUP_TAG_COL)),
                            formatter.formatCellValue(row.getCell(ChartOfAccountsConstants.LOOKUP_TAG_ID_COL))));
                }
            }
            assertEquals(List.of(List.of(header.toString(), prefix + " Header", "", ""),
                    List.of(detail.toString(), prefix + " (Detail)", prefix, tag.toString()),
                    List.of(disabled.toString(), prefix + " Disabled", "", "")), rows);
        }
        try (Workbook workbook = bulk.downloadTemplate("journalentries", Map.of())) {
            final Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME);
            assertNotNull(sheet);
            final List<List<String>> rows = new ArrayList<>();
            for (final Row row : sheet) {
                final String id = formatter.formatCellValue(row.getCell(0));
                if (fixtureIds.contains(id)) {
                    rows.add(List.of(id, formatter.formatCellValue(row.getCell(1))));
                }
            }
            assertEquals(List.of(List.of(header.toString(), prefix + "_Header"), List.of(detail.toString(), prefix + "__Detail_"),
                    List.of(disabled.toString(), prefix + "_Disabled")), rows);
        }
    }

    @Test
    void selectsTheHighestEntryIdOnDatesWithACalculatedBalance() {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final Long account = createAccount(prefix + " Debit", prefix + "01", ASSET, DETAIL, true, null, null);
        final Long credit = createAccount(prefix + " Credit", prefix + "02", INCOME, DETAIL, true, null, null);
        final Long noEntries = createAccount(prefix + " Empty", prefix + "03", ASSET, DETAIL, true, null, null);
        final Long otherDebit = createAccount(prefix + " Other debit", prefix + "04", GLAccountType.EXPENSE.getValue(), DETAIL, true, null,
                null);
        final List<Long> expectedIds = List.of(account, noEntries);
        final Set<Long> fixtureIds = Set.of(account, noEntries);
        final List<GetGLAccountsResponse> withoutBalances = accounts(new RetrieveAllAccountsQueryParams(), fixtureIds);
        assertEquals(expectedIds, ids(withoutBalances));
        withoutBalances.forEach(row -> assertNull(row.getOrganizationRunningBalance()));

        suspendRunningBalanceJob();
        final LocalDate date = journalDate();
        final String later = postEntry(date.minusDays(1), account, credit, new BigDecimal("8.50"));
        final String earlier = postEntry(date.minusDays(2), account, credit, new BigDecimal("12.75"));
        final String otherAccountTransaction = postEntry(date, otherDebit, credit, new BigDecimal("4.00"));
        final RetrieveAllAccountsQueryParams balanceFilter = new RetrieveAllAccountsQueryParams().fetchRunningBalance(true);
        assertBalances(balanceFilter, expectedIds, 0L);
        recalculateBalances();

        final JournalEntryTransactionItem earlierEntry = entryFor(earlier, account);
        final JournalEntryTransactionItem laterEntry = entryFor(later, account);
        assertEntry(earlierEntry, date.minusDays(2), true, new BigDecimal("12.75"));
        assertEntry(laterEntry, date.minusDays(1), true, new BigDecimal("21.25"));
        assertEntry(entryFor(otherAccountTransaction, otherDebit), date, true, new BigDecimal("4.00"));
        final Long earlierId = earlierEntry.getId();
        final Long laterId = laterEntry.getId();
        assertNotNull(earlierId);
        assertNotNull(laterId);
        assertTrue(earlierId > laterId);
        final List<GetGLAccountsResponse> calculatedBalances = assertBalances(balanceFilter, expectedIds, 12L);
        assertEquals(withoutBalances, accounts(new RetrieveAllAccountsQueryParams(), fixtureIds));
        assertEquals(withoutBalances, accounts(new RetrieveAllAccountsQueryParams().fetchRunningBalance(false), fixtureIds));

        final RetrieveAllAccountsQueryParams combinedFilters = new RetrieveAllAccountsQueryParams().type(ASSET).usage(DETAIL)
                .disabled(false).searchParam(prefix).fetchRunningBalance(true);
        final List<GetGLAccountsResponse> combined = ok(() -> fineractClient().generalLedgerAccount().retrieveAllAccounts(combinedFilters));
        assertEquals(calculatedBalances, combined);

        final String newDate = postEntry(date, account, credit, new BigDecimal("3.50"));
        assertEntry(entryFor(newDate, account), date, false, BigDecimal.ZERO);
        assertBalances(balanceFilter, expectedIds, 12L);

        final String backdated = postEntry(date.minusDays(2), account, credit, new BigDecimal("2.50"));
        final JournalEntryTransactionItem backdatedEntry = entryFor(backdated, account);
        assertEntry(backdatedEntry, date.minusDays(2), false, BigDecimal.ZERO);
        final Long backdatedId = backdatedEntry.getId();
        assertNotNull(backdatedId);
        assertTrue(backdatedId > earlierId);
        assertBalances(balanceFilter, expectedIds, 0L);
    }

    private Long createAccount(final String name, final String code, final int type, final int usage, final boolean manualEntriesAllowed,
            final Long parentId, final Long tagId) {
        final Long id = accountHelper
                .createGLAccount(new PostGLAccountsRequest().name(name).glCode(code).description(name + " description")
                        .manualEntriesAllowed(manualEntriesAllowed).type(type).usage(usage).parentId(parentId).tagId(tagId))
                .getResourceId();
        assertNotNull(id);
        cleanup.push(() -> {
            if (accountsWithJournalEntries.contains(id)) {
                accountHelper.updateGLAccount(id, new PutGLAccountsRequest().disabled(true));
            } else {
                accountHelper.deleteGLAccount(id);
            }
        });
        return id;
    }

    private Long createAssetTag(final String name) {
        final FeignCodeHelper codes = new FeignCodeHelper(fineractClient());
        final Long codeId = codes.retrieveCodeByName("AssetAccountTags").getId();
        assertNotNull(codeId);
        final Long tagId = codes.createCodeValue(codeId, name, 0);
        assertNotNull(tagId);
        cleanup.push(() -> ok(() -> fineractClient().codeValues().deleteCodeValue(codeId, tagId)));
        return tagId;
    }

    private GLAccountFixtures createAccountFixtures(final int usage) {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final Map<GLAccountType, List<Long>> byType = new EnumMap<>(GLAccountType.class);
        final Set<Long> fixtureIds = new HashSet<>();
        for (final GLAccountType type : GLAccountType.values()) {
            final String code = prefix + type.getValue();
            final Long second = type == GLAccountType.ASSET || type == GLAccountType.EQUITY
                    ? createAccount(code + " Second", code + "02", type.getValue(), usage, false, null, null)
                    : null;
            final Long first = createAccount(code + " First", code + "01", type.getValue(), usage, false, null, null);
            final List<Long> expected = second == null ? List.of(first) : List.of(first, second);
            byType.put(type, expected);
            fixtureIds.addAll(expected);
        }
        final Long disabled = createAccount(prefix + " Disabled", prefix + ASSET + "03", ASSET, usage, false, null, null);
        accountHelper.updateGLAccount(disabled, new PutGLAccountsRequest().disabled(true));
        final int otherUsage = usage == DETAIL ? HEADER : DETAIL;
        final Long other = createAccount(prefix + " Other usage", prefix + ASSET + "04", ASSET, otherUsage, false, null, null);
        fixtureIds.addAll(List.of(disabled, other));
        return new GLAccountFixtures(byType, fixtureIds);
    }

    private Long createLoanProduct(final String externalId) {
        final Long id = ok(() -> fineractClient().loanProducts()
                .createLoanProduct(new LoanProductTestBuilder().withExternalId(externalId).buildRequest())).getResourceId();
        assertNotNull(id);
        return id;
    }

    private Long openingBalanceMapping(final Long equity) {
        final MappingFinancialActivitiesToAccountsApi activities = fineractClient().mappingFinancialActivitiesToAccounts();
        final int activityId = FinancialActivity.OPENING_BALANCES_TRANSFER_CONTRA.getValue();
        final Optional<GetFinancialActivityAccountsResponse> existing = ok(activities::retrieveAll).stream().filter(mapping -> {
            final FinancialActivityData activity = mapping.getFinancialActivityData();
            return activity != null && Integer.valueOf(activityId).equals(activity.getId());
        }).findFirst();
        final PostFinancialActivityAccountsRequest request = new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) activityId).glAccountId(equity);
        if (existing.isPresent()) {
            final GetFinancialActivityAccountsResponse mapping = existing.get();
            final Long id = mapping.getId();
            final GLAccountData originalAccount = mapping.getGlAccountData();
            assertNotNull(id);
            assertNotNull(originalAccount);
            final Long originalId = originalAccount.getId();
            assertNotNull(originalId);
            cleanup.push(() -> ok(() -> activities.updateGLAccountMappingFinancialActivityAccount(id,
                    new PostFinancialActivityAccountsRequest().financialActivityId((long) activityId).glAccountId(originalId))));
            ok(() -> activities.updateGLAccountMappingFinancialActivityAccount(id, request));
            return id;
        }
        final Long id = ok(() -> activities.createGLAccountMappingFinancialActivityAccount(request)).getResourceId();
        assertNotNull(id);
        cleanup.push(() -> ok(() -> activities.deleteGLAccountMappingFinancialActivityAccount(id)));
        return id;
    }

    private List<GetGLAccountsResponse> accounts(final RetrieveAllAccountsQueryParams filters, final Set<Long> fixtureIds) {
        return ok(() -> fineractClient().generalLedgerAccount().retrieveAllAccounts(filters)).stream()
                .filter(account -> fixtureIds.contains(account.getId())).toList();
    }

    private static List<Long> ids(final List<GetGLAccountsResponse> accounts) {
        return accounts.stream().map(GetGLAccountsResponse::getId).toList();
    }

    private static Map<String, List<GLAccountData>> readAccountingMappingOptions(final String response) throws JsonProcessingException {
        return ObjectMapperFactory.getShared().readValue(response, ProductTemplate.class).accountingMappingOptions();
    }

    private void assertInvalidFilter(final RetrieveAllAccountsQueryParams filters, final String reportedValue) {
        final CallFailedRuntimeException exception = FeignCalls
                .fail(() -> fineractClient().generalLedgerAccount().retrieveAllAccounts(filters));
        assertEquals(403, exception.getStatus());
        assertEquals("validation.msg.domain.rule.violation", exception.getUserMessageGlobalisationCode());
        final ReportedError error = FeignErrors.firstError(exception);
        assertEquals("error.msg.glaccount.usage.invalid", error.userMessageGlobalisationCode());
        assertEquals("The following COA usage is invalid: " + reportedValue, error.developerMessage());
    }

    private LocalDate journalDate() {
        final GlobalConfigurationPropertyData config = new FeignGlobalConfigurationHelper(fineractClient())
                .getGlobalConfigurationByName("enable-business-date");
        assertNotNull(config);
        if (Boolean.TRUE.equals(config.getEnabled())) {
            return ok(() -> fineractClient().businessDateManagement().getBusinessDates()).stream()
                    .filter(date -> TypeEnum.BUSINESS_DATE.equals(date.getType())).findFirst().map(BusinessDateResponse::getDate)
                    .orElseGet(Utils::getLocalDateOfTenant);
        }
        return Utils.getLocalDateOfTenant();
    }

    private String postEntry(final LocalDate date, final Long debit, final Long credit, final BigDecimal amount) {
        final String transactionId = new FeignJournalEntryHelper(fineractClient()).createJournalEntry(
                new JournalEntryCommand().officeId(1L).currencyCode("USD").locale("en").dateFormat("uuuu-MM-dd").transactionDate(date)
                        .amount(amount).addDebitsItem(new SingleDebitOrCreditEntryCommand().glAccountId(debit).amount(amount))
                        .addCreditsItem(new SingleDebitOrCreditEntryCommand().glAccountId(credit).amount(amount)))
                .getTransactionId();
        accountsWithJournalEntries.add(debit);
        accountsWithJournalEntries.add(credit);
        assertNotNull(transactionId);
        cleanup.push(() -> ok(() -> fineractClient().journalEntries().createReversalJournalEntry(transactionId, "reverse",
                new PostJournalEntriesTransactionIdRequest())));
        return transactionId;
    }

    private void suspendRunningBalanceJob() {
        final SchedulerJobApi scheduler = fineractClient().schedulerJob();
        final GetJobsResponse job = ok(scheduler::retrieveAllSchedulerJobs).stream().filter(item -> "ACC_RBAL".equals(item.getShortName()))
                .findFirst().orElseThrow();
        final Long jobId = job.getJobId();
        assertNotNull(jobId);
        if (Boolean.TRUE.equals(job.getActive())) {
            cleanup.addLast(() -> ok(() -> scheduler.updateJobDetail(jobId, new JobUpdateRequest().active(true))));
            ok(() -> scheduler.updateJobDetail(jobId, new JobUpdateRequest().active(false)));
        }
        await().atMost(Duration.ofSeconds(60)).until(() -> {
            final GetJobsResponse current = ok(() -> scheduler.retrieveOneSchedulerJob(jobId));
            return Boolean.FALSE.equals(current.getActive()) && Boolean.FALSE.equals(current.getCurrentlyRunning());
        });
    }

    private void recalculateBalances() {
        ok(() -> fineractClient().journalEntries().createGLJournalEntry("updateRunningBalance", new JournalEntryCommand()));
    }

    private JournalEntryTransactionItem entryFor(final String transactionId, final Long account) {
        final GetJournalEntriesTransactionIdResponse page = ok(() -> fineractClient().journalEntries()
                .retrieveAllJournalEntriesUniversal(Map.of("transactionId", transactionId, "runningBalance", true)));
        final List<JournalEntryTransactionItem> entries = page.getPageItems();
        assertNotNull(entries);
        return entries.stream().filter(entry -> account.equals(entry.getGlAccountId())).findFirst().orElseThrow();
    }

    private static void assertEntry(final JournalEntryTransactionItem entry, final LocalDate date, final boolean computed,
            final BigDecimal balance) {
        assertEquals(date, entry.getTransactionDate());
        assertEquals(computed, entry.getRunningBalanceComputed());
        final BigDecimal actual = entry.getOrganizationRunningBalance();
        assertNotNull(actual);
        assertEquals(0, balance.compareTo(actual));
    }

    private List<GetGLAccountsResponse> assertBalances(final RetrieveAllAccountsQueryParams filters, final List<Long> expectedIds,
            final long balance) {
        final List<GetGLAccountsResponse> withBalances = accounts(filters, Set.copyOf(expectedIds));
        assertEquals(expectedIds, ids(withBalances));
        assertEquals(List.of(balance, 0L), withBalances.stream().map(GetGLAccountsResponse::getOrganizationRunningBalance).toList());
        return withBalances;
    }

    private record OpeningBalances(GLAccountData contraAccount, List<OpeningBalanceEntry> assetAccountOpeningBalances,
            @JsonProperty("liabityAccountOpeningBalances") List<OpeningBalanceEntry> liabilityAccountOpeningBalances,
            List<OpeningBalanceEntry> equityAccountOpeningBalances, List<OpeningBalanceEntry> incomeAccountOpeningBalances,
            List<OpeningBalanceEntry> expenseAccountOpeningBalances) {
    }

    private record OpeningBalanceEntry(Long glAccountId) {
    }

    private record ProductTemplate(Map<String, List<GLAccountData>> accountingMappingOptions) {
    }
}
