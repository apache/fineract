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
package org.apache.fineract.accounting.provisioning.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.accounting.provisioning.domain.ProvisioningEntry;
import org.apache.fineract.accounting.provisioning.domain.ProvisioningEntryRepository;
import org.apache.fineract.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for the provisioning entry approval lifecycle: a provisioning entry must start unreviewed, be approvable or
 * rejectable exactly once, optionally have its approval undone before journal entries exist, and only have journal
 * entries created (or its rows regenerated) while in the appropriate state.
 *
 * <p>
 * Fixtures are created by calling {@link ProvisioningEntriesWritePlatformService#createProvisioningEntries}, the real
 * API entry point for the "create" command, rather than by hand-constructing an entity directly. This keeps the tests
 * independent of exactly how "draft" is established internally (field default, constructor, or service-level
 * assignment) and independent of any internal helper method signatures. The created entity is captured via an
 * {@link ArgumentCaptor} on the repository save call, purely to obtain the id a real database would assign, since the
 * repository here is a mock and does not persist anything.
 *
 * <p>
 * {@code isJournalEntryCreated} is a pre-existing field, unrelated to the review status introduced here, so it is set
 * directly on the created entry where a test needs to simulate "journal entries already exist for this entry" without
 * going through the full posting flow.
 */
class ProvisioningEntryApprovalLifecycleTest {

    private ProvisioningEntryRepository provisioningEntryRepository;
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    private ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;
    private ProvisioningEntriesWritePlatformService writePlatformService;

    @BeforeEach
    void setUp() {
        // Fineract's shared write-service infrastructure (e.g. audit-field updates via
        // DateUtils.getBusinessLocalDate()) reads the current business date from thread-local context. A real
        // implementation is free to update the entity's existing lastModifiedBy/lastModifiedDate audit fields
        // when transitioning its review status - a normal, pre-existing Fineract pattern - so this context must
        // be initialized here the same way other tests in this codebase do (see e.g. EnricherTest), regardless
        // of whether this particular implementation happens to touch those fields.
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        final Map<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(businessDates));

        this.provisioningEntriesReadPlatformService = mock(ProvisioningEntriesReadPlatformService.class);
        final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService = mock(
                ProvisioningCriteriaReadPlatformService.class);
        final LoanProductRepository loanProductRepository = mock(LoanProductRepository.class);
        final GLAccountRepository glAccountRepository = mock(GLAccountRepository.class);
        final OfficeRepository officeRepository = mock(OfficeRepository.class);
        final ProvisioningCategoryRepository provisioningCategoryRepository = mock(ProvisioningCategoryRepository.class);
        final PlatformSecurityContext platformSecurityContext = mock(PlatformSecurityContext.class);
        this.provisioningEntryRepository = mock(ProvisioningEntryRepository.class);
        this.journalEntryWritePlatformService = mock(JournalEntryWritePlatformService.class);
        final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer = mock(
                ProvisioningEntriesDefinitionJsonDeserializer.class);
        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();

        this.writePlatformService = new ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl(provisioningEntriesReadPlatformService,
                provisioningCriteriaReadPlatformService, loanProductRepository, glAccountRepository, officeRepository,
                provisioningCategoryRepository, platformSecurityContext, provisioningEntryRepository, journalEntryWritePlatformService,
                fromApiJsonDeserializer, fromApiJsonHelper);

        // Shared, unrelated-to-the-lifecycle stubs needed by the real createProvisioningEntries(JsonCommand)/
        // reCreateProvisioningEntries code paths: no pre-existing entry for the date, at least one provisioning
        // criteria configured (required for creation to proceed at all, per the existing, unrelated
        // NoProvisioningCriteriaDefinitionFound check), and no loan-product provisioning rows to process (kept
        // empty so the real creation/regeneration logic can run without needing full loan-product/office/
        // category/GL-account fixture data, which is unrelated to what this feature's tests are checking).
        when(this.provisioningEntryRepository.findByProvisioningEntryDate(any())).thenReturn(null);
        when(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias())
                .thenReturn(Collections.singletonList(ProvisioningCriteriaData.toLookup(1L, "Test Criteria", "test_user")));
        when(this.provisioningEntriesReadPlatformService.retrieveLoanProductsProvisioningData(any())).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private JsonCommand createCommand(final LocalDate date, final Boolean createJournalEntries) {
        final StringBuilder json = new StringBuilder("{\"date\": \"").append(date)
                .append("\", \"locale\": \"en\", \"dateFormat\": \"yyyy-MM-dd\"");
        if (createJournalEntries != null) {
            json.append(", \"createjournalentries\": ").append(createJournalEntries);
        }
        json.append("}");
        final JsonElement parsed = JsonParser.parseString(json.toString());
        return JsonCommand.from(json.toString(), parsed, new FromJsonHelper(), null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    /**
     * Creates a provisioning entry through the real, externally-contracted create command, then wires up findById so
     * subsequent write-service calls can look it up. The created entity is captured off the repository's save call
     * purely to simulate the id a real database would assign, since the repository here is a mock and does not actually
     * persist anything.
     */
    private ProvisioningEntry createRealDraftEntry(final Long simulatedId, final LocalDate date) {
        this.writePlatformService.createProvisioningEntries(createCommand(date, null));

        final ArgumentCaptor<ProvisioningEntry> captor = ArgumentCaptor.forClass(ProvisioningEntry.class);
        verify(this.provisioningEntryRepository, org.mockito.Mockito.atLeastOnce()).saveAndFlush(captor.capture());
        final List<ProvisioningEntry> savedEntries = captor.getAllValues();
        final ProvisioningEntry entry = savedEntries.get(savedEntries.size() - 1);

        setPrivateField(entry, "id", simulatedId);
        when(this.provisioningEntryRepository.findById(simulatedId)).thenReturn(Optional.of(entry));
        return entry;
    }

    private JsonCommand emptyCommand() {
        final String json = "{}";
        final JsonElement parsed = JsonParser.parseString(json);
        return JsonCommand.from(json, parsed, new FromJsonHelper(), null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }

    /**
     * Replicates a genuinely empty request body, as opposed to {@link #emptyCommand()}'s parsed-but-empty JSON object.
     * The pre-existing recreateprovisioningentry/createjournalentry commands are called this way in practice (see
     * ProvisioningIntegrationTest), so approve/reject/undo must tolerate it too - a parsed empty object and a null
     * parsed element are not the same thing to code that reads individual parameters off the command.
     */
    private JsonCommand emptyBodyCommand() {
        return JsonCommand.from("", null, new FromJsonHelper(), null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }

    private void stubNoPriorJournaledEntry() {
        when(this.provisioningEntriesReadPlatformService.retrieveExistingProvisioningIdDateWithJournals()).thenReturn(null);
    }

    @Test
    void newlyCreatedEntry_cannotHaveJournalEntriesCreatedUntilApproved() {
        final Long id = 1L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void approvingADraftEntry_thenAllowsJournalEntriesToBeCreated() {
        final Long id = 2L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 2));
        stubNoPriorJournaledEntry();

        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatCode(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand())).doesNotThrowAnyException();
    }

    @Test
    void approvingWithAnEmptyRequestBody_doesNotThrow() {
        final Long id = 16L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 17));

        assertThatCode(() -> this.writePlatformService.approveProvisioningEntry(id, emptyBodyCommand())).doesNotThrowAnyException();
    }

    @Test
    void rejectingWithAnEmptyRequestBody_doesNotThrow() {
        final Long id = 17L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 18));

        assertThatCode(() -> this.writePlatformService.rejectProvisioningEntry(id, emptyBodyCommand())).doesNotThrowAnyException();
    }

    @Test
    void approvingAnAlreadyApprovedEntry_fails() {
        final Long id = 3L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 3));

        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.approveProvisioningEntry(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectingADraftEntry_thenJournalEntriesStillCannotBeCreated() {
        final Long id = 4L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 4));

        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectingAnAlreadyRejectedEntry_fails() {
        final Long id = 5L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 5));

        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.rejectProvisioningEntry(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void undoingApproval_beforeJournalEntriesExist_returnsEntryToUnapprovedState() {
        final Long id = 6L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 6));

        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());
        this.writePlatformService.undoProvisioningEntryApproval(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void undoingApproval_afterJournalEntriesAlreadyExist_fails() {
        final Long id = 7L;
        final ProvisioningEntry entry = createRealDraftEntry(id, LocalDate.of(2026, 1, 7));
        // isJournalEntryCreated is a pre-existing field, unrelated to the new review status this feature adds
        // (see class-level note) - set directly here to simulate "this entry's journal entries already exist"
        // without depending on the heavier real-posting code path.
        setPrivateField(entry, "isJournalEntryCreated", true);
        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.undoProvisioningEntryApproval(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void approvedEntry_cannotBeRegenerated() {
        final Long id = 8L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 8));
        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.reCreateProvisioningEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectedEntry_cannotBeRegenerated() {
        final Long id = 9L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 9));
        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.reCreateProvisioningEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectedEntry_cannotHaveJournalEntriesCreated() {
        final Long id = 10L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 10));
        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void approvedEntry_cannotBeRejected() {
        final Long id = 11L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 11));
        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.rejectProvisioningEntry(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectedEntry_cannotBeApproved() {
        final Long id = 12L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 12));
        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.approveProvisioningEntry(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void reApprovingAfterUndo_reEnablesJournalPosting() {
        final Long id = 13L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 13));
        stubNoPriorJournaledEntry();

        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());
        this.writePlatformService.undoProvisioningEntryApproval(id, emptyCommand());
        this.writePlatformService.approveProvisioningEntry(id, emptyCommand());

        assertThatCode(() -> this.writePlatformService.createProvisioningJournalEntries(id, emptyCommand())).doesNotThrowAnyException();
    }

    @Test
    void draftEntry_canBeRegenerated() {
        final Long id = 14L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 14));

        assertThatCode(() -> this.writePlatformService.reCreateProvisioningEntries(id, emptyCommand())).doesNotThrowAnyException();
    }

    @Test
    void undoApproval_onRejectedEntry_fails() {
        final Long id = 15L;
        createRealDraftEntry(id, LocalDate.of(2026, 1, 15));
        this.writePlatformService.rejectProvisioningEntry(id, emptyCommand());

        assertThatThrownBy(() -> this.writePlatformService.undoProvisioningEntryApproval(id, emptyCommand()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void creatingEntryWithImmediateJournalPosting_stillRequiresApprovalFirst() {
        // Exercises the create-with-immediate-posting entry point (the pre-existing "createjournalentries" flag
        // on the create command) directly. A freshly created entry is always draft, so this must fail the same
        // way posting for any other draft entry does - immediate posting at creation time previously bypassed
        // review entirely, which is the gap this feature closes.
        assertThatThrownBy(() -> this.writePlatformService.createProvisioningEntries(createCommand(LocalDate.of(2026, 1, 16), true)))
                .isInstanceOf(RuntimeException.class);
    }

    private static void setPrivateField(final Object target, final String fieldName, final Object value) {
        Class<?> currentClass = target.getClass();
        while (currentClass != null) {
            try {
                final Field field = currentClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to set field " + fieldName, e);
            }
        }
        throw new IllegalStateException("Field " + fieldName + " not found on " + target.getClass());
    }
}
