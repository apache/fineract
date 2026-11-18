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
package org.apache.fineract.integrationtests.client.feign.tests;

import static java.util.Comparator.naturalOrder;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT;
import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_BUSINESS_DATE;
import static org.apache.fineract.infrastructure.jobs.service.JobName.GENERATE_LOANLOSS_PROVISIONING;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.services.SchedulerJobApi.RetrieveHistoryQueryParams;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.EnumOptionType;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.JobDetailHistoryDataSwagger;
import org.apache.fineract.client.models.JobExecuteRequest;
import org.apache.fineract.client.models.JobExecuteResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanProductData;
import org.apache.fineract.client.models.LoanProductProvisioningEntryData;
import org.apache.fineract.client.models.PageLoanProductProvisioningEntryData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningEntriesResponse;
import org.apache.fineract.client.models.ProvisionEntryRequest;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.client.models.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.client.models.ProvisioningEntryData;
import org.apache.fineract.client.models.PutGLAccountsRequest;
import org.apache.fineract.client.models.PutProvisioningEntriesRequest;
import org.apache.fineract.client.models.PutProvisioningEntriesResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class FeignLoanProductProvisioningDataTest extends FeignLoanTestBase {

    private final List<Long> accountIds = new ArrayList<>();
    private final List<Fixture> fixtures = new ArrayList<>();
    private final Set<Long> criteriaAccountIds = new HashSet<>();
    private boolean schedulerActive;
    private boolean businessDateEnabled;
    private boolean automaticCobDate;
    private LocalDate previousBusinessDate;
    private LocalDate provisioningDate;

    @BeforeEach
    public void prepareProvisioningEnvironment() {
        schedulerActive = schedulerHelper.getSchedulerStatus();
        businessDateEnabled = Boolean.TRUE.equals(globalConfigurationHelper.retrieveConfigurationByName(ENABLE_BUSINESS_DATE).getEnabled());
        automaticCobDate = Boolean.TRUE
                .equals(globalConfigurationHelper.retrieveConfigurationByName(ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT).getEnabled());
        final LocalDate businessDate = businessDateHelper.getBusinessDate("BUSINESS_DATE").getDate();
        assertNotNull(businessDate);
        previousBusinessDate = businessDate;
        schedulerHelper.stopScheduler();
        Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1))
                .until(() -> ok(() -> fineractClient().schedulerJob().retrieveAllSchedulerJobs()).stream()
                        .noneMatch(job -> Boolean.TRUE.equals(job.getCurrentlyRunning())));
        globalConfigurationHelper.updateConfigurationByName(ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT, false);
        provisioningDate = nextProvisioningDate();
    }

    @AfterEach
    public void cleanupProvisioningEnvironment() {
        try {
            assertAll("Provisioning fixture cleanup and tenant state restoration", this::cleanupFixtures, this::restoreTenantState);
        } finally {
            accountIds.clear();
            fixtures.clear();
            criteriaAccountIds.clear();
            previousBusinessDate = null;
            provisioningDate = null;
        }
    }

    @Test
    public void createsProvisioningWithInclusiveAgesLoanStatusAndBorrowerOffice() {
        final LocalDate date = provisioningDate;
        runAt(date.toString(), () -> {
            final Fixture fixture = createFixture(date);
            for (final int age : List.of(-1, 0, 30, 31, 60, 61, 90, 91)) {
                createActiveLoan(fixture, date.minusDays(age), age == 0 ? "246.80" : "123.40", 1);
            }
            final Long pendingLoanId = applicationLoan(application(fixture, date.minusDays(1), "123.40", 1));
            assertEquals(Boolean.TRUE, retrieveLoan(pendingLoanId).getStatus().getPendingApproval());
            final Long approvedLoanId = applicationLoan(application(fixture, date.minusDays(1), "123.40", 1));
            approveLoan(approvedLoanId, LoanRequestBuilders.approveLoan(123.40, format(date.minusDays(31))));
            assertEquals(Boolean.TRUE, retrieveLoan(approvedLoanId).getStatus().getWaitingForDisbursal());
            final Long closedLoanId = createActiveLoan(fixture, date.minusDays(1), "123.40", 1);
            makeLoanRepayment(closedLoanId, "repayment", format(date), 123.40);
            assertEquals(Boolean.TRUE, retrieveLoan(closedLoanId).getStatus().getClosedObligationsMet());

            final Long unmappedProductId = createLoanProduct(onePeriod30DaysNoInterest());
            assertNotNull(unmappedProductId);
            final PostLoansRequest unmappedApplication = application(fixture, date.minusDays(1), "123.40", 1).productId(unmappedProductId);
            disburse(applicationLoan(unmappedApplication), date.minusDays(31), "123.40");

            final Long groupOfficeId = new FeignOfficeHelper(fineractClient()).createOffice(date.minusYears(1)).getResourceId();
            assertNotNull(groupOfficeId);
            final GetLoansLoanIdResponse groupLoan = createActiveGroupLoan(fixture, groupOfficeId, date);
            assertEquals(Boolean.TRUE, groupLoan.getStatus().getActive());

            final Long entryId = createEntry(date, false);
            assertMetadata(entryId, date, false);
            assertEntries(entryId, fixture, Set.of(fixture.productId, unmappedProductId),
                    List.of(new ExpectedEntry(fixture.officeId, 0, 0, "18.51"), new ExpectedEntry(fixture.officeId, 0, 30, "6.17"),
                            new ExpectedEntry(fixture.officeId, 1, 31, "12.34"), new ExpectedEntry(fixture.officeId, 1, 60, "12.34"),
                            new ExpectedEntry(fixture.officeId, 2, 61, "18.51"), new ExpectedEntry(fixture.officeId, 2, 90, "18.51"),
                            new ExpectedEntry(groupOfficeId, 0, 1, "6.17")));
        });
    }

    @Test
    public void recreatesProvisioningUsingOriginalDateAndNextUnpaidInstallment() {
        final LocalDate date = provisioningDate;
        runAt(date.toString(), () -> {
            final Fixture fixture = createFixture(date);
            final Long loanId = createActiveLoan(fixture, date.minusDays(31), "200.00", 2);
            final Long entryId = createEntry(date, false);
            assertEntries(entryId, fixture, Set.of(fixture.productId), List.of(new ExpectedEntry(fixture.officeId, 1, 31, "20.00")));
            makeLoanRepayment(loanId, "repayment", format(date), 100.00);
            final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanHelper.getLoanDetails(loanId, "repaymentSchedule"));
            assertTrue(periods.stream()
                    .anyMatch(period -> date.minusDays(31).equals(period.getDueDate()) && Boolean.TRUE.equals(period.getComplete())));
            assertTrue(periods.stream()
                    .anyMatch(period -> date.minusDays(1).equals(period.getDueDate()) && Boolean.FALSE.equals(period.getComplete())));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", date.plusDays(1).toString());
            recreateEntry(entryId);
            assertMetadata(entryId, date, false);
            assertEntries(entryId, fixture, Set.of(fixture.productId), List.of(new ExpectedEntry(fixture.officeId, 0, 1, "5.00")));

            makeLoanRepayment(loanId, "repayment", format(date.plusDays(1)), 100.00);
            assertEquals(Boolean.TRUE, retrieveLoan(loanId).getStatus().getClosedObligationsMet());
            recreateEntry(entryId);
            assertMetadata(entryId, date, false);
            final PageLoanProductProvisioningEntryData page = retrieveEntries(entryId, fixture.productId);
            assertTrue(page.getPageItems().isEmpty());
            assertEquals(0, page.getTotalFilteredRecords());
        });
    }

    @Test
    public void createsProvisioningJournalsAndGeneratesNextBusinessDateThroughJob() {
        final LocalDate date = provisioningDate;
        runAt(date.toString(), () -> {
            final Fixture fixture = createFixture(date);
            createActiveLoan(fixture, date.minusDays(2), "123.40", 1);
            final Long createdEntryId = createEntry(date, true);
            assertMetadata(createdEntryId, date, true);
            assertEntries(createdEntryId, fixture, Set.of(fixture.productId), List.of(new ExpectedEntry(fixture.officeId, 0, 2, "6.17")));
            assertJournals(createdEntryId, fixture, date, "6.17");

            final LocalDate nextDate = date.plusDays(1);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", nextDate.toString());
            executeProvisioningJob();
            final ProvisioningEntryData generated = histories().stream().filter(entry -> nextDate.equals(entry.getCreatedDate()))
                    .findFirst().orElseThrow();
            final Long generatedEntryId = generated.getId();
            assertNotNull(generatedEntryId);
            assertMetadata(generatedEntryId, nextDate, true);
            assertEntries(generatedEntryId, fixture, Set.of(fixture.productId), List.of(new ExpectedEntry(fixture.officeId, 0, 3, "6.17")));
            assertJournals(generatedEntryId, fixture, nextDate, "6.17");
        });
    }

    private Fixture createFixture(final LocalDate date) {
        final Long officeId = new FeignOfficeHelper(fineractClient()).createOffice(date.minusYears(1)).getResourceId();
        assertNotNull(officeId);
        final Long clientId = clientHelper
                .createClient(ClientRequestBuilders.createActivePersonClientWithExternalId(format(date.minusYears(1))).officeId(officeId))
                .getClientId();
        assertNotNull(clientId);
        final Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        assertNotNull(productId);
        final Account liability = accountHelper.createLiabilityAccount("ProvisionLiability" + UUID.randomUUID());
        final Long liabilityId = trackAccount(liability);
        final Account expense = accountHelper.createExpenseAccount("ProvisionExpense" + UUID.randomUUID());
        final Long expenseId = trackAccount(expense);
        final List<ProvisioningCategoryData> categories = ok(() -> fineractClient().provisioningCategory().retrieveAll8());
        assertTrue(categories.size() >= 3);
        final List<ProvisioningCriteriaDefinitionData> definitions = List.of(
                definition(categories.get(0), 0, 30, "5.00", liabilityId, expenseId),
                definition(categories.get(1), 31, 60, "10.00", liabilityId, expenseId),
                definition(categories.get(2), 61, 90, "15.00", liabilityId, expenseId));
        final PostProvisioningCriteriaRequest request = new PostProvisioningCriteriaRequest().criteriaName("Provision" + UUID.randomUUID())
                .loanProducts(List.of(new LoanProductData().id(productId))).definitions(definitions);
        final Long criteriaId = ok(() -> fineractClient().provisioningCriteria().createProvisioningCriteria(request)).getResourceId();
        assertNotNull(criteriaId);
        final Fixture fixture = new Fixture(officeId, clientId, productId, criteriaId, liabilityId, expenseId, definitions);
        fixtures.add(fixture);
        criteriaAccountIds.addAll(List.of(liabilityId, expenseId));
        return fixture;
    }

    private static ProvisioningCriteriaDefinitionData definition(final ProvisioningCategoryData category, final long minimum,
            final long maximum, final String percentage, final Long liabilityId, final Long expenseId) {
        final Long categoryId = category.getId();
        assertNotNull(categoryId);
        return new ProvisioningCriteriaDefinitionData().categoryId(categoryId).categoryName(category.getCategoryName()).minAge(minimum)
                .maxAge(maximum).provisioningPercentage(new BigDecimal(percentage)).liabilityAccount(liabilityId).expenseAccount(expenseId);
    }

    private PostLoansRequest application(final Fixture fixture, final LocalDate firstDueDate, final String principal,
            final int repayments) {
        final String disbursementDate = format(firstDueDate.minusDays(30));
        return LoanRequestBuilders.legacyDaysBasedApplication(fixture.clientId, fixture.productId, principal, 30 * repayments, repayments,
                30, disbursementDate, disbursementDate).repaymentsStartingFromDate(format(firstDueDate));
    }

    private Long applicationLoan(final PostLoansRequest request) {
        final Long loanId = applyForLoan(request);
        assertNotNull(loanId);
        return loanId;
    }

    private Long createActiveLoan(final Fixture fixture, final LocalDate firstDueDate, final String principal, final int repayments) {
        final Long loanId = applicationLoan(application(fixture, firstDueDate, principal, repayments));
        disburse(loanId, firstDueDate.minusDays(30), principal);
        final GetLoansLoanIdResponse loan = loanHelper.getLoanDetails(loanId, "repaymentSchedule");
        assertEquals(Boolean.TRUE, loan.getStatus().getActive());
        assertTrue(repaymentPeriods(loan).stream().anyMatch(period -> firstDueDate.equals(period.getDueDate())));
        return loanId;
    }

    private static List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods(final GetLoansLoanIdResponse loan) {
        final GetLoansLoanIdRepaymentSchedule schedule = loan.getRepaymentSchedule();
        assertNotNull(schedule);
        final List<GetLoansLoanIdRepaymentPeriod> periods = schedule.getPeriods();
        assertNotNull(periods);
        return periods;
    }

    private GetLoansLoanIdResponse createActiveGroupLoan(final Fixture fixture, final Long officeId, final LocalDate date) {
        final FeignGroupHelper groupHelper = new FeignGroupHelper(fineractClient());
        final Long groupId = groupHelper.createActiveGroup(officeId, format(date.minusYears(1))).getResourceId();
        assertNotNull(groupId);
        final PostLoansRequest request = application(fixture, date.minusDays(1), "123.40", 1).clientId(null).groupId(groupId)
                .loanType("group");
        final Long loanId = applicationLoan(request);
        disburse(loanId, date.minusDays(31), "123.40");
        return retrieveLoan(loanId);
    }

    private GetLoansLoanIdResponse retrieveLoan(final Long loanId) {
        return ok(() -> fineractClient().loans().retrieveOneLoan(loanId, Map.of()));
    }

    private void disburse(final Long loanId, final LocalDate date, final String principal) {
        final double amount = new BigDecimal(principal).doubleValue();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, format(date)));
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(amount, format(date)));
    }

    private Long createEntry(final LocalDate date, final boolean journals) {
        final ProvisionEntryRequest request = new ProvisionEntryRequest().date(format(date)).dateFormat(DATETIME_PATTERN).locale("en")
                .createjournalentries(journals);
        final ApiResponse<PostProvisioningEntriesResponse> response = ok(
                () -> fineractClient().provisioningEntries().createProvisioningEntriesWithHttpInfo(request, Map.of()));
        assertEquals(200, response.getStatusCode());
        final PostProvisioningEntriesResponse created = response.getData();
        assertNotNull(created);
        final Long entryId = created.getResourceId();
        assertNotNull(entryId);
        return entryId;
    }

    private void recreateEntry(final Long entryId) {
        final ApiResponse<PutProvisioningEntriesResponse> response = ok(() -> fineractClient().provisioningEntries()
                .modifyProvisioningEntryWithHttpInfo(entryId, "recreateprovisioningentry", new PutProvisioningEntriesRequest(), Map.of()));
        assertEquals(200, response.getStatusCode());
        final PutProvisioningEntriesResponse recreated = response.getData();
        assertNotNull(recreated);
        assertEquals(entryId, recreated.getResourceId());
    }

    private void assertMetadata(final Long entryId, final LocalDate date, final boolean journals) {
        final ApiResponse<ProvisioningEntryData> response = ok(
                () -> fineractClient().provisioningEntries().retrieveOneProvisioningEntryWithHttpInfo(entryId, Map.of()));
        assertEquals(200, response.getStatusCode());
        final ProvisioningEntryData entry = response.getData();
        assertNotNull(entry);
        assertEquals(entryId, entry.getId());
        assertEquals(date, entry.getCreatedDate());
        assertEquals(Boolean.valueOf(journals), entry.getJournalEntry());
    }

    private PageLoanProductProvisioningEntryData retrieveEntries(final Long entryId, final Long productId) {
        final ApiResponse<PageLoanProductProvisioningEntryData> response = ok(() -> fineractClient().provisioningEntries()
                .retrieveProvisioningEntriesLoanProductsWithHttpInfo(entryId, null, null, null, productId, null, Map.of()));
        assertEquals(200, response.getStatusCode());
        final PageLoanProductProvisioningEntryData page = response.getData();
        assertNotNull(page);
        assertNotNull(page.getPageItems());
        return page;
    }

    private void assertEntries(final Long entryId, final Fixture fixture, final Set<Long> productIds,
            final List<ExpectedEntry> expectedEntries) {
        final List<LoanProductProvisioningEntryData> remaining = new ArrayList<>(
                retrieveEntries(entryId, null).getPageItems().stream().filter(entry -> productIds.contains(entry.getProductId())).toList());
        for (final ExpectedEntry expected : expectedEntries) {
            final LoanProductProvisioningEntryData actual = remaining.stream()
                    .filter(entry -> expected.officeId.equals(entry.getOfficeId()) && entry.getOverdueInDays() != null
                            && expected.overdueDays == entry.getOverdueInDays())
                    .findFirst().orElseThrow(() -> new AssertionError("Missing provisioning entry " + expected));
            assertEquals(entryId, actual.getHistoryId());
            assertEquals(fixture.productId, actual.getProductId());
            assertEquals(fixture.criteriaId, actual.getCriteriaId());
            assertEquals(fixture.definitions.get(expected.categoryIndex).getCategoryId(), actual.getCategoryId());
            assertEquals("USD", actual.getCurrencyCode());
            assertEquals(fixture.liabilityId, actual.getLiablityAccount());
            assertEquals(fixture.expenseId, actual.getExpenseAccount());
            final BigDecimal amountReserved = actual.getAmountreserved();
            assertNotNull(amountReserved);
            assertEquals(0, new BigDecimal(expected.amount).compareTo(amountReserved));
            assertNull(actual.getBalance());
            assertNull(actual.getPercentage());
            remaining.remove(actual);
        }
        assertTrue(remaining.isEmpty(), "Unexpected provisioning entries: " + remaining);
    }

    private void assertJournals(final Long entryId, final Fixture fixture, final LocalDate date, final String amount) {
        final List<JournalEntryTransactionItem> pageItems = ok(
                () -> fineractClient().journalEntries().retrieveAllJournalEntriesUniversal(Map.of("transactionId", "P" + entryId)))
                .getPageItems();
        assertNotNull(pageItems);
        final Set<Long> accountIds = Set.of(fixture.liabilityId, fixture.expenseId);
        final List<JournalEntryTransactionItem> entries = pageItems.stream().filter(entry -> accountIds.contains(entry.getGlAccountId()))
                .toList();
        assertEquals(2, entries.size());
        for (final Long accountId : accountIds) {
            final List<JournalEntryTransactionItem> matches = entries.stream().filter(entry -> accountId.equals(entry.getGlAccountId()))
                    .toList();
            assertEquals(1, matches.size());
            final JournalEntryTransactionItem entry = matches.getFirst();
            assertEquals(fixture.officeId, entry.getOfficeId());
            assertEquals(entryId, entry.getEntityId());
            assertEquals("P" + entryId, entry.getTransactionId());
            assertEquals(date, entry.getTransactionDate());
            assertEquals(Boolean.FALSE, entry.getReversed());
            final EnumOptionType entryType = entry.getEntryType();
            assertNotNull(entryType);
            assertEquals(accountId.equals(fixture.liabilityId) ? "CREDIT" : "DEBIT", entryType.getValue());
            final Double actualAmount = entry.getAmount();
            assertNotNull(actualAmount);
            assertEquals(0, new BigDecimal(amount).compareTo(BigDecimal.valueOf(actualAmount)));
        }
    }

    private void executeProvisioningJob() {
        final GetJobsResponse job = ok(() -> fineractClient().schedulerJob().retrieveAllSchedulerJobs()).stream()
                .filter(candidate -> GENERATE_LOANLOSS_PROVISIONING.toString().equals(candidate.getDisplayName())).findFirst()
                .orElseThrow();
        final Long jobId = job.getJobId();
        assertNotNull(jobId);
        final Optional<Long> previousRunId = latestRun(jobId).map(JobDetailHistoryDataSwagger::getId);
        final ApiResponse<JobExecuteResponse> accepted = ok(
                () -> fineractClient().schedulerJob().executeJobWithHttpInfo(jobId, "executeJob", new JobExecuteRequest(), Map.of()));
        assertEquals(202, accepted.getStatusCode());
        final JobDetailHistoryDataSwagger completed = Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1))
                .until(() -> latestRun(jobId).orElse(null), run -> run != null && run.getId() != null && run.getJobRunEndTime() != null
                        && previousRunId.map(id -> run.getId() > id).orElse(true));
        assertEquals("application", completed.getTriggerType());
        assertEquals("success", completed.getStatus());
    }

    private Optional<JobDetailHistoryDataSwagger> latestRun(final Long jobId) {
        final List<JobDetailHistoryDataSwagger> pageItems = ok(() -> fineractClient().schedulerJob().retrieveHistory(jobId,
                new RetrieveHistoryQueryParams().offset(0).limit(1).orderBy("id").sortOrder("DESC"))).getPageItems();
        assertNotNull(pageItems);
        return pageItems.stream().findFirst();
    }

    private List<ProvisioningEntryData> histories() {
        final List<ProvisioningEntryData> entries = ok(
                () -> fineractClient().provisioningEntries().retrieveAllProvisioningEntries((Integer) null, (Integer) null)).getPageItems();
        assertNotNull(entries);
        return entries;
    }

    private static String format(final LocalDate date) {
        return date.format(dateTimeFormatter);
    }

    private record Fixture(Long officeId, Long clientId, Long productId, Long criteriaId, Long liabilityId, Long expenseId,
            List<ProvisioningCriteriaDefinitionData> definitions) {
    }

    private record ExpectedEntry(Long officeId, int categoryIndex, long overdueDays, String amount) {
    }

    private Long trackAccount(final Account account) {
        final Integer accountId = account.getAccountID();
        assertNotNull(accountId);
        final Long id = accountId.longValue();
        accountIds.add(id);
        return id;
    }

    private LocalDate nextProvisioningDate() {
        final LocalDate latestHistoryDate = histories().stream().map(ProvisioningEntryData::getCreatedDate).filter(Objects::nonNull)
                .max(naturalOrder()).orElse(previousBusinessDate);
        return Stream.of(Utils.getLocalDateOfTenant(), previousBusinessDate, latestHistoryDate).max(naturalOrder()).orElseThrow()
                .plusDays(1);
    }

    private void cleanupFixtures() {
        final List<Executable> cleanup = new ArrayList<>();
        for (final Fixture fixture : fixtures) {
            cleanup.add(() -> deleteUnreferencedCriteria(fixture));
        }
        for (final Long accountId : accountIds) {
            cleanup.add(() -> cleanupAccount(accountId));
        }
        assertAll("Provisioning fixture cleanup", cleanup);
    }

    private void deleteUnreferencedCriteria(final Fixture fixture) {
        final List<LoanProductProvisioningEntryData> entries = retrieveEntries(null, fixture.productId).getPageItems();
        if (entries.stream().noneMatch(entry -> fixture.criteriaId.equals(entry.getCriteriaId()))) {
            ok(() -> fineractClient().provisioningCriteria().deleteProvisioningCriteria(fixture.criteriaId));
            criteriaAccountIds.removeAll(List.of(fixture.liabilityId, fixture.expenseId));
        }
    }

    private void cleanupAccount(final Long accountId) {
        if (!criteriaAccountIds.contains(accountId)) {
            final List<JournalEntryTransactionItem> journalEntries = ok(() -> fineractClient().journalEntries()
                    .retrieveAllJournalEntriesUniversal(Map.of("glAccountId", accountId, "limit", 1))).getPageItems();
            assertNotNull(journalEntries);
            if (journalEntries.isEmpty()) {
                accountHelper.deleteGLAccount(accountId);
                return;
            }
        }
        accountHelper.updateGLAccount(accountId, new PutGLAccountsRequest().disabled(true));
        assertEquals(Boolean.TRUE, accountHelper.getGLAccount(accountId).getDisabled());
    }

    private void restoreTenantState() {
        if (previousBusinessDate == null) {
            return;
        }
        try {
            globalConfigurationHelper.updateConfigurationByName(ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT, false);
            globalConfigurationHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, true);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", previousBusinessDate.toString());
        } finally {
            assertAll("Provisioning configuration restoration",
                    () -> globalConfigurationHelper.updateConfigurationByName(ENABLE_AUTOMATIC_COB_DATE_ADJUSTMENT, automaticCobDate),
                    () -> globalConfigurationHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, businessDateEnabled),
                    () -> schedulerHelper.updateSchedulerStatus(schedulerActive));
        }
    }
}
