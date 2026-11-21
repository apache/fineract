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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper.HEAD_OFFICE_ID;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.ISO_DATE_PATTERN;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.LOCALE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.MappingFinancialActivitiesToAccountsApi;
import org.apache.fineract.client.feign.services.SchedulerJobApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateResponse.TypeEnum;
import org.apache.fineract.client.models.CodeValueData;
import org.apache.fineract.client.models.EnumOptionData;
import org.apache.fineract.client.models.FinancialActivityData;
import org.apache.fineract.client.models.GLAccountData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.JobUpdateRequest;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.client.models.PostJournalEntriesTransactionIdRequest;
import org.apache.fineract.client.models.PutGLAccountsRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.ReportedError;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GLAccountDetailIntegrationTest extends FeignIntegrationTest {

    private static final Map<String, Object> WITH_BALANCE = Map.of("fetchRunningBalance", true);
    private static final Map<String, Object> TEMPLATE_WITH_BALANCE = Map.of("template", true, "fetchRunningBalance", true);

    private final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient());
    private final Deque<Executable> cleanup = new ArrayDeque<>();
    private final Set<Long> accountsWithJournalEntries = new HashSet<>();

    @AfterEach
    void cleanUpFixtures() {
        final List<Executable> actions = new ArrayList<>(cleanup);
        cleanup.clear();
        try {
            assertAll(actions);
        } finally {
            accountsWithJournalEntries.clear();
        }
    }

    @Test
    void retrievesTheRequestedAccountWithTagsHierarchyAndFlags() {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final String tagName = prefix + " tag";
        final Long tagId = createAssetTag(tagName);
        final PostGLAccountsRequest rootRequest = accountRequest(prefix + " Root", GLAccountType.ASSET, GLAccountUsage.HEADER)
                .manualEntriesAllowed(false);
        final Long root = createAccount(rootRequest).getResourceId();
        final PostGLAccountsRequest childRequest = accountRequest(prefix + " Child", GLAccountType.ASSET, GLAccountUsage.DETAIL)
                .parentId(root).tagId(tagId).description(prefix + " description");
        final Long child = createAccount(childRequest).getResourceId();
        accountHelper.updateGLAccount(child, new PutGLAccountsRequest().disabled(true));

        final GetGLAccountsResponse expectedRoot = expectedAccount(root, rootRequest, false, null);
        final GetGLAccountsResponse expectedChild = expectedAccount(child, childRequest, true, tagName);
        assertEquals(expectedRoot, account(root, Map.of()));
        assertEquals(expectedChild, account(child, Map.of()));
        assertEquals(expectedChild, account(child, Map.of("fetchRunningBalance", false)));
        assertEquals(expectedChild, account(child, Map.of("fields", "name,glCode")));
    }

    @Test
    void returnsNotFoundForDeletedAccountsWithOrWithoutRunningBalance() {
        final String name = Utils.uniqueRandomStringGenerator("GL", 8);
        final Long id = accountHelper.createGLAccount(accountRequest(name, GLAccountType.INCOME, GLAccountUsage.DETAIL)).getResourceId();
        assertNotNull(id);
        accountHelper.deleteGLAccount(id);

        assertAccountNotFound(id, Map.of());
        assertAccountNotFound(id, Map.of("fetchRunningBalance", false));
        assertAccountNotFound(id, WITH_BALANCE);
        assertAccountNotFound(id, TEMPLATE_WITH_BALANCE);
    }

    @Test
    void returnsTheLatestCalculatedBalanceByDateThenId() {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final PostGLAccountsRequest debitRequest = accountRequest(prefix + " Debit", GLAccountType.ASSET, GLAccountUsage.DETAIL);
        final Long debit = createAccount(debitRequest).getResourceId();
        final Long credit = createAccount(accountRequest(prefix + " Credit", GLAccountType.LIABILITY, GLAccountUsage.DETAIL))
                .getResourceId();
        final Long otherDebit = createAccount(accountRequest(prefix + " Control", GLAccountType.ASSET, GLAccountUsage.DETAIL))
                .getResourceId();
        final Long pendingOnly = createAccount(accountRequest(prefix + " Pending", GLAccountType.ASSET, GLAccountUsage.DETAIL))
                .getResourceId();
        final GetGLAccountsResponse withoutBalance = expectedAccount(debit, debitRequest, false, null);
        assertEquals(withoutBalance, account(debit, Map.of()));
        assertAccountNotFound(debit, WITH_BALANCE);
        assertAccountNotFound(debit, TEMPLATE_WITH_BALANCE);

        suspendRunningBalanceJob();
        final LocalDate date = journalDate();
        final String first = postEntry(date.minusDays(1), debit, credit, new BigDecimal("8.50"));
        final String second = postEntry(date.minusDays(1), debit, credit, new BigDecimal("3.50"));
        final String backdated = postEntry(date.minusDays(2), debit, credit, new BigDecimal("12.75"));
        final String control = postEntry(date.minusDays(1), otherDebit, credit, new BigDecimal("57.25"));
        assertEntry(entryFor(second, debit), date.minusDays(1), false, BigDecimal.ZERO);
        assertAccountNotFound(debit, WITH_BALANCE);

        ok(() -> fineractClient().journalEntries().createGLJournalEntry("updateRunningBalance", new JournalEntryCommand()));

        final JournalEntryTransactionItem firstEntry = entryFor(first, debit);
        final JournalEntryTransactionItem secondEntry = entryFor(second, debit);
        final JournalEntryTransactionItem backdatedEntry = entryFor(backdated, debit);
        assertEntry(firstEntry, date.minusDays(1), true, new BigDecimal("21.25"));
        assertEntry(secondEntry, date.minusDays(1), true, new BigDecimal("24.75"));
        assertEntry(backdatedEntry, date.minusDays(2), true, new BigDecimal("12.75"));
        assertEntry(entryFor(control, otherDebit), date.minusDays(1), true, new BigDecimal("57.25"));
        final Long firstId = firstEntry.getId();
        final Long secondId = secondEntry.getId();
        final Long backdatedId = backdatedEntry.getId();
        assertNotNull(firstId);
        assertNotNull(secondId);
        assertNotNull(backdatedId);
        assertTrue(firstId < secondId);
        assertTrue(secondId < backdatedId);
        final GetGLAccountsResponse withBalance = expectedAccount(debit, debitRequest, false, null).organizationRunningBalance(24L);
        assertEquals(withBalance, account(debit, WITH_BALANCE));
        assertAccountFields(withBalance, account(debit, TEMPLATE_WITH_BALANCE));

        final String sameDate = postEntry(date.minusDays(1), debit, credit, new BigDecimal("2.50"));
        final String newDate = postEntry(date, debit, credit, new BigDecimal("9.25"));
        final String pending = postEntry(date, pendingOnly, credit, new BigDecimal("4.00"));
        assertEntry(entryFor(sameDate, debit), date.minusDays(1), false, BigDecimal.ZERO);
        assertEntry(entryFor(newDate, debit), date, false, BigDecimal.ZERO);
        assertEntry(entryFor(pending, pendingOnly), date, false, BigDecimal.ZERO);
        assertEquals(withBalance, account(debit, WITH_BALANCE));
        assertAccountNotFound(pendingOnly, WITH_BALANCE);
        assertEquals(withoutBalance, account(debit, Map.of()));
        assertEquals(withoutBalance, account(debit, Map.of("fetchRunningBalance", false)));
    }

    @Test
    void preservesAccountFieldsInDetailTemplates() {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final Long parent = createAccount(accountRequest(prefix + " Header", GLAccountType.ASSET, GLAccountUsage.HEADER)).getResourceId();
        assertNotNull(parent);
        final PostGLAccountsRequest request = accountRequest(prefix + " Detail", GLAccountType.ASSET, GLAccountUsage.DETAIL)
                .parentId(parent);
        final Long id = createAccount(request).getResourceId();
        final GetGLAccountsResponse template = account(id, Map.of("template", true));
        assertAccountFields(expectedAccount(id, request, false, null), template);
    }

    @Test
    void retrievesTheOpeningBalanceContraAccountWithoutJournalEntries() throws JsonProcessingException {
        final String prefix = Utils.uniqueRandomStringGenerator("GL", 8);
        final PostGLAccountsRequest request = accountRequest(prefix + " Contra", GLAccountType.EQUITY, GLAccountUsage.DETAIL)
                .description(prefix + " opening balance contra");
        final Long contra = createAccount(request).getResourceId();
        final Long equity = createAccount(accountRequest(prefix + " Equity", GLAccountType.EQUITY, GLAccountUsage.DETAIL)).getResourceId();
        mapOpeningBalanceAccount(contra);

        final String response = ok(() -> fineractClient().journalEntries().retrieveOpeningBalance(HEAD_OFFICE_ID, "USD"));
        final OpeningBalances balances = ObjectMapperFactory.getShared().readValue(response, OpeningBalances.class);
        assertEquals(expectedAccount(contra, request, false, null), balances.contraAccount());
        final List<OpeningBalanceEntry> entries = balances.equityAccountOpeningBalances();
        assertNotNull(entries);
        final Set<Long> fixtureIds = Set.of(contra, equity);
        assertEquals(List.of(equity), entries.stream().map(OpeningBalanceEntry::glAccountId).filter(fixtureIds::contains).toList());
    }

    private static PostGLAccountsRequest accountRequest(final String name, final GLAccountType type, final GLAccountUsage usage) {
        return new PostGLAccountsRequest().name(name).glCode(name.replace(" ", "")).type(type.getValue()).usage(usage.getValue())
                .manualEntriesAllowed(true);
    }

    private PostGLAccountsResponse createAccount(final PostGLAccountsRequest request) {
        final PostGLAccountsResponse response = accountHelper.createGLAccount(request);
        final Long id = response.getResourceId();
        assertNotNull(id);
        cleanup.push(() -> {
            if (accountsWithJournalEntries.contains(id)) {
                accountHelper.updateGLAccount(id, new PutGLAccountsRequest().disabled(true));
            } else {
                accountHelper.deleteGLAccount(id);
            }
        });
        return response;
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

    private GetGLAccountsResponse account(final Long id, final Map<String, Object> parameters) {
        return ok(() -> fineractClient().generalLedgerAccount().retreiveAccountUniversal(id, parameters));
    }

    private static GetGLAccountsResponse expectedAccount(final Long id, final PostGLAccountsRequest request, final boolean disabled,
            final String tagName) {
        final GLAccountType type = GLAccountType.fromInt(request.getType());
        final GLAccountUsage usage = GLAccountUsage.fromInt(request.getUsage());
        assertNotNull(type);
        assertNotNull(usage);
        final Long parent = request.getParentId();
        final Long tagId = request.getTagId();
        final String description = request.getDescription();
        return new GetGLAccountsResponse().id(id).name(request.getName()).glCode(request.getGlCode()).parentId(parent).disabled(disabled)
                .manualEntriesAllowed(request.getManualEntriesAllowed()).description(description == null ? "" : description)
                .type(new EnumOptionData().id(type.getValue().longValue()).code(type.getCode()).value(type.name()))
                .usage(new EnumOptionData().id(usage.getValue().longValue()).code(usage.getCode()).value(usage.name()))
                .nameDecorated(parent == null ? request.getName() : "...." + request.getName())
                .tagId(new CodeValueData().id(tagId == null ? 0L : tagId).name(tagName));
    }

    private static void assertAccountFields(final GetGLAccountsResponse expected, final GetGLAccountsResponse actual) {
        assertNotNull(actual);
        assertAll(() -> assertEquals(expected.getId(), actual.getId()), () -> assertEquals(expected.getName(), actual.getName()),
                () -> assertEquals(expected.getGlCode(), actual.getGlCode()),
                () -> assertEquals(expected.getParentId(), actual.getParentId()),
                () -> assertEquals(expected.getDisabled(), actual.getDisabled()),
                () -> assertEquals(expected.getManualEntriesAllowed(), actual.getManualEntriesAllowed()),
                () -> assertEquals(expected.getDescription(), actual.getDescription()),
                () -> assertEquals(expected.getType(), actual.getType()), () -> assertEquals(expected.getUsage(), actual.getUsage()),
                () -> assertEquals(expected.getNameDecorated(), actual.getNameDecorated()),
                () -> assertEquals(expected.getTagId(), actual.getTagId()),
                () -> assertEquals(expected.getOrganizationRunningBalance(), actual.getOrganizationRunningBalance()));
    }

    private void assertAccountNotFound(final Long id, final Map<String, Object> parameters) {
        final CallFailedRuntimeException exception = FeignCalls
                .fail(() -> fineractClient().generalLedgerAccount().retreiveAccountUniversal(id, parameters));
        assertEquals(404, exception.getStatus());
        assertEquals("error.msg.resource.not.found", exception.getUserMessageGlobalisationCode());
        final ReportedError error = FeignErrors.firstError(exception);
        assertEquals("error.msg.glaccount.id.invalid", error.userMessageGlobalisationCode());
    }

    private void mapOpeningBalanceAccount(final Long accountId) {
        final MappingFinancialActivitiesToAccountsApi activities = fineractClient().mappingFinancialActivitiesToAccounts();
        final int activityId = FinancialActivity.OPENING_BALANCES_TRANSFER_CONTRA.getValue();
        final Optional<GetFinancialActivityAccountsResponse> existing = ok(activities::retrieveAll).stream().filter(mapping -> {
            final FinancialActivityData activity = mapping.getFinancialActivityData();
            return activity != null && Integer.valueOf(activityId).equals(activity.getId());
        }).findFirst();
        final PostFinancialActivityAccountsRequest request = new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) activityId).glAccountId(accountId);
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
        } else {
            final Long id = ok(() -> activities.createGLAccountMappingFinancialActivityAccount(request)).getResourceId();
            assertNotNull(id);
            cleanup.push(() -> ok(() -> activities.deleteGLAccountMappingFinancialActivityAccount(id)));
        }
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
        final String transactionId = new FeignJournalEntryHelper(fineractClient()).createJournalEntry(new JournalEntryCommand()
                .officeId(HEAD_OFFICE_ID).currencyCode("USD").locale(LOCALE).dateFormat(ISO_DATE_PATTERN).transactionDate(date)
                .amount(amount).addDebitsItem(new SingleDebitOrCreditEntryCommand().glAccountId(debit).amount(amount))
                .addCreditsItem(new SingleDebitOrCreditEntryCommand().glAccountId(credit).amount(amount))).getTransactionId();
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

    private JournalEntryTransactionItem entryFor(final String transactionId, final Long accountId) {
        final GetJournalEntriesTransactionIdResponse page = ok(() -> fineractClient().journalEntries()
                .retrieveAllJournalEntriesUniversal(Map.of("transactionId", transactionId, "runningBalance", true)));
        final List<JournalEntryTransactionItem> entries = page.getPageItems();
        assertNotNull(entries);
        final List<JournalEntryTransactionItem> matches = entries.stream().filter(entry -> accountId.equals(entry.getGlAccountId()))
                .toList();
        assertEquals(1, matches.size());
        return matches.getFirst();
    }

    private static void assertEntry(final JournalEntryTransactionItem entry, final LocalDate date, final boolean computed,
            final BigDecimal balance) {
        assertEquals(date, entry.getTransactionDate());
        assertEquals(computed, entry.getRunningBalanceComputed());
        final BigDecimal actual = entry.getOrganizationRunningBalance();
        assertNotNull(actual);
        assertEquals(0, balance.compareTo(actual));
    }

    private record OpeningBalances(GetGLAccountsResponse contraAccount, List<OpeningBalanceEntry> equityAccountOpeningBalances) {
    }

    private record OpeningBalanceEntry(Long glAccountId) {
    }
}
