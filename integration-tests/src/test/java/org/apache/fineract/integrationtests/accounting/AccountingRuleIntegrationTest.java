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

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.errorGlobalisationCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountRuleRequest;
import org.apache.fineract.client.models.AccountingRuleData;
import org.apache.fineract.client.models.EnumOptionType;
import org.apache.fineract.client.models.GLAccountDataForLookup;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostAccountingRulesResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountRuleHelper;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountingRuleIntegrationTest extends FeignIntegrationTest {

    private static final String INVALID_DEBITS = "error.msg.glJournalEntry.invalid.debits";
    private static final String INVALID_CREDITS = "error.msg.glJournalEntry.invalid.credits";

    private FeignAccountHelper accountHelper;

    @BeforeAll
    public void setup() {
        accountHelper = new FeignAccountHelper(fineractClient());
    }

    @Test
    public void testAccountingRuleCreation() {
        // given
        final Account accountToCredit = accountHelper.createIncomeAccount();
        final Account accountToDebit = accountHelper.createExpenseAccount();
        final long officeId = headOfficeId();

        // when
        final PostAccountingRulesResponse accountingRule = AccountRuleHelper.createAccountRule(officeId, accountToCredit, accountToDebit);
        final List<AccountingRuleData> accountingRules = AccountRuleHelper.getAccountingRules();
        // then
        assertNotNull(accountingRule);
        assertNotNull(accountingRule.getResourceId());
        assertNotNull(accountingRules);
        assertFalse(accountingRules.isEmpty());
    }

    @Test
    void taggedAccountsAreUsedForAssociationsAndJournalEntryValidation() {
        final long officeId = headOfficeId();
        final Long debitTag = createAccountTag();
        final Long secondDebitTag = createAccountTag();
        final Long creditTag = createAccountTag();
        final Long otherDebitTag = createAccountTag();
        final Long tagWithoutAccounts = createAccountTag();
        final GetGLAccountsResponse debit = createAccount(debitTag);
        final GetGLAccountsResponse secondDebit = createAccount(debitTag);
        final GetGLAccountsResponse debitFromSecondTag = createAccount(secondDebitTag);
        final GetGLAccountsResponse credit = createAccount(creditTag);
        final GetGLAccountsResponse otherDebit = createAccount(otherDebitTag);
        final GetGLAccountsResponse untagged = createAccount(null);
        final Long ruleId = requireNonNull(createTaggedRule(officeId, List.of(debitTag, secondDebitTag), creditTag).getResourceId(),
                "Missing tagged accounting rule ID");
        final Long otherRuleId = requireNonNull(
                createTaggedRule(officeId, List.of(otherDebitTag, creditTag), tagWithoutAccounts).getResourceId(),
                "Missing rule ID for the tag without credit accounts");
        final Long emptyDebitRuleId = requireNonNull(createTaggedRule(officeId, List.of(tagWithoutAccounts), creditTag).getResourceId(),
                "Missing rule ID for the tag without debit accounts");

        final List<AccountingRuleData> rules = retrieveRulesWithAccounts();
        final AccountingRuleData rule = findRule(rules, ruleId);
        final AccountingRuleData otherRule = findRule(rules, otherRuleId);
        final AccountingRuleData emptyDebitRule = findRule(rules, emptyDebitRuleId);
        assertAccountsInAnyOrder(requireNonNull(rule.getDebitAccounts(), "Missing debit accounts for the tagged rule"),
                List.of(debit, secondDebit, debitFromSecondTag));
        assertEquals(List.of(lookup(credit)), rule.getCreditAccounts());
        assertAccountsInAnyOrder(requireNonNull(otherRule.getDebitAccounts(), "Missing debit accounts for the second tagged rule"),
                List.of(otherDebit, credit));
        assertEquals(List.of(), otherRule.getCreditAccounts());
        assertEquals(List.of(), emptyDebitRule.getDebitAccounts());
        assertEquals(List.of(lookup(credit)), emptyDebitRule.getCreditAccounts());

        final LocalDate date = Utils.getLocalDateOfTenant();
        new FeignBusinessDateHelper(fineractClient()).runAt(date.toString(), () -> {
            final FeignJournalEntryHelper journalEntries = new FeignJournalEntryHelper(fineractClient());
            final JournalEntryCommand entry = journalEntry(officeId, ruleId, date,
                    List.of(journalLine(debit), journalLine(secondDebit), journalLine(debitFromSecondTag)),
                    List.of(journalLine(credit).amount(BigDecimal.valueOf(30))));
            final String transactionId = requireNonNull(journalEntries.createJournalEntry(entry).getTransactionId(),
                    "Missing journal entry transaction ID");
            final List<JournalEntryTransactionItem> postedEntries = requireNonNull(
                    journalEntries.getJournalEntriesByTransactionId(transactionId).getPageItems(), "Missing posted journal entries");
            final Map<Long, String> postedAccounts = postedEntries.stream()
                    .collect(Collectors.toMap(item -> requireNonNull(item.getGlAccountId(), "Missing posted GL account ID"), item -> {
                        final EnumOptionType entryType = requireNonNull(item.getEntryType(), "Missing journal entry type");
                        return requireNonNull(entryType.getValue(), "Missing journal entry type value");
                    }));
            assertEquals(Map.of(accountId(debit), JournalEntryType.DEBIT.name(), accountId(secondDebit), JournalEntryType.DEBIT.name(),
                    accountId(debitFromSecondTag), JournalEntryType.DEBIT.name(), accountId(credit), JournalEntryType.CREDIT.name()),
                    postedAccounts);

            assertJournalEntryRejected(journalEntry(officeId, ruleId, date, List.of(journalLine(untagged)), List.of(journalLine(credit))),
                    INVALID_DEBITS);

            assertJournalEntryRejected(journalEntry(officeId, ruleId, date, List.of(journalLine(debit)), List.of(journalLine(otherDebit))),
                    INVALID_CREDITS);

            assertJournalEntryRejected(
                    journalEntry(officeId, otherRuleId, date, List.of(journalLine(otherDebit)), List.of(journalLine(credit))),
                    INVALID_CREDITS);

            assertJournalEntryRejected(
                    journalEntry(officeId, emptyDebitRuleId, date, List.of(journalLine(debit)), List.of(journalLine(credit))),
                    INVALID_DEBITS);
        });
    }

    @Test
    void mixedRulesReturnFixedAndTaggedAccountsAndRejectJournalEntries() {
        final long officeId = headOfficeId();
        final long tagId = createAccountTag();
        final GetGLAccountsResponse taggedAccount = createAccount(tagId);
        final GetGLAccountsResponse fixedAccount = createAccount(null);
        final Long fixedDebitRuleId = requireNonNull(
                createRule(new AccountRuleRequest().officeId(officeId).accountToDebit(accountId(fixedAccount)).creditTags(List.of(tagId)))
                        .getResourceId(),
                "Missing accounting rule ID for the fixed debit account");
        final Long fixedCreditRuleId = requireNonNull(
                createRule(new AccountRuleRequest().officeId(officeId).debitTags(List.of(tagId)).accountToCredit(accountId(fixedAccount)))
                        .getResourceId(),
                "Missing accounting rule ID for the fixed credit account");

        final List<AccountingRuleData> rules = retrieveRulesWithAccounts();
        final AccountingRuleData fixedDebitRule = findRule(rules, fixedDebitRuleId);
        assertEquals(List.of(lookup(fixedAccount)), fixedDebitRule.getDebitAccounts());
        assertEquals(List.of(lookup(taggedAccount)), fixedDebitRule.getCreditAccounts());
        final AccountingRuleData fixedCreditRule = findRule(rules, fixedCreditRuleId);
        assertEquals(List.of(lookup(taggedAccount)), fixedCreditRule.getDebitAccounts());
        assertEquals(List.of(lookup(fixedAccount)), fixedCreditRule.getCreditAccounts());

        final LocalDate date = Utils.getLocalDateOfTenant();
        new FeignBusinessDateHelper(fineractClient()).runAt(date.toString(), () -> {
            final JournalEntryCommand fixedDebitEntry = journalEntry(officeId, fixedDebitRuleId, date, List.of(journalLine(fixedAccount)),
                    List.of(journalLine(taggedAccount)));
            assertJournalEntryRejected(fixedDebitEntry, INVALID_DEBITS);

            final JournalEntryCommand fixedCreditEntry = journalEntry(officeId, fixedCreditRuleId, date,
                    List.of(journalLine(taggedAccount)), List.of(journalLine(fixedAccount)));
            assertJournalEntryRejected(fixedCreditEntry, INVALID_CREDITS);

            final GetJournalEntriesTransactionIdResponse journalEntries = ok(
                    () -> fineractClient().journalEntries().retrieveAllJournalEntries(Map.of("glAccountId", accountId(fixedAccount))));
            assertEquals(List.of(), journalEntries.getPageItems());
        });
    }

    private List<AccountingRuleData> retrieveRulesWithAccounts() {
        return ok(() -> fineractClient().accountingRules().retrieveAllAccountingRulesUniversal(Map.of("associations", "all")));
    }

    private static AccountingRuleData findRule(final List<AccountingRuleData> rules, final Long ruleId) {
        return rules.stream().filter(rule -> ruleId.equals(rule.getId())).findFirst().orElseThrow();
    }

    private void assertJournalEntryRejected(final JournalEntryCommand entry, final String expectedErrorCode) {
        final CallFailedRuntimeException failure = fail(() -> fineractClient().journalEntries().createGLJournalEntry("", entry));
        assertEquals(403, failure.getStatus());
        assertEquals(expectedErrorCode, errorGlobalisationCode(failure));
    }

    private static void assertAccountsInAnyOrder(final List<GLAccountDataForLookup> actual, final List<GetGLAccountsResponse> expected) {
        final List<GLAccountDataForLookup> remaining = new ArrayList<>(actual);
        for (final GetGLAccountsResponse account : expected) {
            assertTrue(remaining.remove(lookup(account)), () -> "Missing account " + account.getGlCode());
        }
        assertTrue(remaining.isEmpty(), () -> "Unexpected accounts " + remaining);
    }

    private long createAccountTag() {
        final PostCodeValuesDataRequest request = new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("RULE_TAG_", 8))
                .position(1).isActive(true);
        final PostCodeValueDataResponse response = ok(
                () -> fineractClient().codeValues().createCodeValueByCodeName(AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME, request));
        return requireNonNull(response.getSubResourceId(), "Missing account tag ID");
    }

    private GetGLAccountsResponse createAccount(final @Nullable Long tagId) {
        final PostGLAccountsRequest request = new PostGLAccountsRequest().name(Utils.uniqueRandomStringGenerator("RULE_GL_", 8))
                .glCode(Utils.uniqueRandomStringGenerator("GL_", 8)).type(GLAccountType.ASSET.getValue())
                .usage(GLAccountUsage.DETAIL.getValue()).manualEntriesAllowed(true);
        if (tagId != null) {
            request.tagId(tagId);
        }
        final Long id = requireNonNull(accountHelper.createGLAccount(request).getResourceId(), "Missing created GL account ID");
        return accountHelper.getGLAccount(id);
    }

    private PostAccountingRulesResponse createTaggedRule(final long officeId, final List<Long> debitTags, final Long creditTag) {
        return createRule(new AccountRuleRequest().officeId(officeId).debitTags(debitTags).creditTags(List.of(creditTag))
                .allowMultipleDebitEntries(true).allowMultipleCreditEntries(false));
    }

    private PostAccountingRulesResponse createRule(final AccountRuleRequest request) {
        final String name = Utils.uniqueRandomStringGenerator("TAGGED_RULE_", 8);
        request.name(name).description(name);
        return ok(() -> fineractClient().accountingRules().createAccountingRule(request));
    }

    private static GLAccountDataForLookup lookup(final GetGLAccountsResponse account) {
        return new GLAccountDataForLookup().id(accountId(account)).name(requireNonNull(account.getName(), "Missing GL account name"))
                .glCode(requireNonNull(account.getGlCode(), "Missing GL code"));
    }

    private static JournalEntryCommand journalEntry(final long officeId, final long ruleId, final LocalDate date,
            final List<SingleDebitOrCreditEntryCommand> debits, final List<SingleDebitOrCreditEntryCommand> credits) {
        return new JournalEntryCommand().officeId(officeId).accountingRule(ruleId).transactionDate(date)
                .dateFormat(LoanTestData.ISO_DATE_PATTERN).locale(LoanTestData.LOCALE).currencyCode("USD").debits(debits).credits(credits);
    }

    private static SingleDebitOrCreditEntryCommand journalLine(final GetGLAccountsResponse account) {
        return new SingleDebitOrCreditEntryCommand().glAccountId(accountId(account)).amount(BigDecimal.TEN);
    }

    private static long accountId(final GetGLAccountsResponse account) {
        return requireNonNull(account.getId(), "Missing GL account ID");
    }

    private static long headOfficeId() {
        return requireNonNull(OfficeHelper.getHeadOffice().getId(), "Missing head office ID");
    }
}
