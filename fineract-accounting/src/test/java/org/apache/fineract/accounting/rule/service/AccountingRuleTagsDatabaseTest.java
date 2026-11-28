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
package org.apache.fineract.accounting.rule.service;

import static org.apache.fineract.query.QAccAccountingRule.accAccountingRule;
import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QAccRuleTags.accRuleTags;
import static org.apache.fineract.query.QMCode.mCode;
import static org.apache.fineract.query.QMCodeValue.mCodeValue;
import static org.apache.fineract.query.QMOffice.mOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformServiceImpl;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.accounting.rule.exception.AccountingRuleNotFoundException;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@TestInstance(Lifecycle.PER_CLASS)
@Import({ AccountingRuleReadPlatformServiceImpl.class, GLAccountReadPlatformServiceImpl.class })
class AccountingRuleTagsDatabaseTest extends QuerydslDatabaseTest {

    private static final long OFFICE_ID = 9100L;
    private static final long OTHER_OFFICE_ID = 9101L;
    private static final long TAGGED_RULE_ID = 9200L;
    private static final long MIXED_RULE_ID = 9201L;
    private static final long DIRECT_RULE_ID = 9202L;
    private static final long OTHER_OFFICE_RULE_ID = 9203L;
    private static final long SYSTEM_RULE_ID = 9204L;
    private static final int CODE_ID = 9300;
    private static final int SHARED_TAG_ID = 9301;
    private static final int CREDIT_TAG_ID = 9302;
    private static final int UNNAMED_TAG_ID = 9303;
    private static final int OTHER_TAG_ID = 9304;
    private static final long SHARED_ACCOUNT_ID = 9400L;
    private static final long CREDIT_ACCOUNT_ID = 9401L;
    private static final long FIXED_CREDIT_ACCOUNT_ID = 9402L;
    private static final long FIXED_DEBIT_ACCOUNT_ID = 9403L;
    private static final String OFFICE_HIERARCHY = ".9100.";
    private static final String OTHER_OFFICE_HIERARCHY = ".9101.";
    private static final String SHARED_TAG = "Shared tag";
    private static final String CREDIT_TAG = "Credit tag";
    private static final String OTHER_TAG = "Other tag";
    private static final EnumOptionData CREDIT = new EnumOptionData(1L, "journalEntryType.credit", "CREDIT");
    private static final EnumOptionData DEBIT = new EnumOptionData(2L, "journalEntrytType.debit", "DEBIT");

    private final AccountingRuleReadPlatformService service;
    private final SQLQueryFactory queryFactory;
    private final PlatformTransactionManager transactionManager;

    @BeforeAll
    void insertFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            insertOffice(OFFICE_ID, "Rule tags office", OFFICE_HIERARCHY);
            insertOffice(OTHER_OFFICE_ID, "Other rule tags office", OTHER_OFFICE_HIERARCHY);
            queryFactory.insert(mCode).set(mCode.id, CODE_ID).set(mCode.codeName, "Accounting rule tag test").execute();
            insertCodeValue(SHARED_TAG_ID, SHARED_TAG);
            insertCodeValue(CREDIT_TAG_ID, CREDIT_TAG);
            insertCodeValue(UNNAMED_TAG_ID, null);
            insertCodeValue(OTHER_TAG_ID, OTHER_TAG);

            insertAccount(SHARED_ACCOUNT_ID, "Shared account", "RULE-SHARED", SHARED_TAG_ID);
            insertAccount(CREDIT_ACCOUNT_ID, "Tagged credit account", "RULE-TAGGED", CREDIT_TAG_ID);
            insertAccount(FIXED_CREDIT_ACCOUNT_ID, "Fixed credit account", "RULE-CREDIT", null);
            insertAccount(FIXED_DEBIT_ACCOUNT_ID, "Fixed debit account", "RULE-DEBIT", OTHER_TAG_ID);

            insertRule(TAGGED_RULE_ID, OFFICE_ID, "Rule with tags on both sides", null, null, false);
            insertRule(MIXED_RULE_ID, OFFICE_ID, "Rule with a direct account and a tag", FIXED_CREDIT_ACCOUNT_ID, null, false);
            insertRule(DIRECT_RULE_ID, OFFICE_ID, "Rule with direct accounts", FIXED_CREDIT_ACCOUNT_ID, FIXED_DEBIT_ACCOUNT_ID, false);
            insertRule(OTHER_OFFICE_RULE_ID, OTHER_OFFICE_ID, "Rule with only credit tags", null, null, false);
            insertRule(SYSTEM_RULE_ID, OFFICE_ID, "System accounting rule", null, null, true);

            insertRuleTag(9500L, TAGGED_RULE_ID, SHARED_TAG_ID, JournalEntryType.CREDIT);
            insertRuleTag(9501L, TAGGED_RULE_ID, CREDIT_TAG_ID, JournalEntryType.CREDIT);
            insertRuleTag(9502L, TAGGED_RULE_ID, SHARED_TAG_ID, JournalEntryType.DEBIT);
            insertRuleTag(9503L, TAGGED_RULE_ID, UNNAMED_TAG_ID, JournalEntryType.DEBIT);
            insertRuleTag(9504L, MIXED_RULE_ID, OTHER_TAG_ID, JournalEntryType.DEBIT);
            insertRuleTag(9505L, OTHER_OFFICE_RULE_ID, OTHER_TAG_ID, JournalEntryType.CREDIT);
        });
    }

    @AfterAll
    void deleteFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            final List<Long> ruleIds = List.of(TAGGED_RULE_ID, MIXED_RULE_ID, DIRECT_RULE_ID, OTHER_OFFICE_RULE_ID, SYSTEM_RULE_ID);
            queryFactory.delete(accRuleTags).where(accRuleTags.accRuleId.in(ruleIds)).execute();
            queryFactory.delete(accAccountingRule).where(accAccountingRule.id.in(ruleIds)).execute();
            queryFactory.delete(accGlAccount)
                    .where(accGlAccount.id.in(SHARED_ACCOUNT_ID, CREDIT_ACCOUNT_ID, FIXED_CREDIT_ACCOUNT_ID, FIXED_DEBIT_ACCOUNT_ID))
                    .execute();
            queryFactory.delete(mCodeValue).where(mCodeValue.id.in(SHARED_TAG_ID, CREDIT_TAG_ID, UNNAMED_TAG_ID, OTHER_TAG_ID)).execute();
            queryFactory.delete(mCode).where(mCode.id.eq(CODE_ID)).execute();
            queryFactory.delete(mOffice).where(mOffice.id.in(OFFICE_ID, OTHER_OFFICE_ID)).execute();
        });
    }

    @Test
    void listsOnlyNonSystemRulesInTheOfficeHierarchyAndMapsTheirTags() {
        final List<AccountingRuleData> rules = service.retrieveAllAccountingRules(OFFICE_HIERARCHY + "%", false);

        assertThat(rules).extracting(AccountingRuleData::getId).containsExactlyInAnyOrder(TAGGED_RULE_ID, MIXED_RULE_ID, DIRECT_RULE_ID);
        assertThat(rules).filteredOn(rule -> rule.getId().equals(TAGGED_RULE_ID)).singleElement().satisfies(this::assertTagsOnBothSides);
        assertThat(rules).filteredOn(rule -> rule.getId().equals(MIXED_RULE_ID)).singleElement().satisfies(this::assertMixedRule);
        assertThat(rules).filteredOn(rule -> rule.getId().equals(DIRECT_RULE_ID)).singleElement()
                .satisfies(this::assertAccountsOnBothSides);
    }

    @Test
    void retrievesTagsAndDirectAccountsForTheRequestedRule() {
        assertTagsOnBothSides(service.retrieveAccountingRuleById(TAGGED_RULE_ID));
        assertMixedRule(service.retrieveAccountingRuleById(MIXED_RULE_ID));
        assertAccountsOnBothSides(service.retrieveAccountingRuleById(DIRECT_RULE_ID));
    }

    @Test
    void returnsAnEmptyTagListWhenOnlyTheOtherTransactionTypeMatches() {
        final AccountingRuleData details = service.retrieveAccountingRuleById(OTHER_OFFICE_RULE_ID);
        final List<AccountingRuleData> rules = service.retrieveAllAccountingRules(OTHER_OFFICE_HIERARCHY + "%", false);

        assertThat(details.getCreditTags()).containsExactly(tag(9505L, OTHER_TAG_ID, OTHER_TAG, CREDIT));
        assertThat(details.getDebitTags()).isEmpty();
        assertThat(rules).singleElement().satisfies(rule -> {
            assertThat(rule.getId()).isEqualTo(OTHER_OFFICE_RULE_ID);
            assertThat(rule.getCreditTags()).containsExactly(tag(9505L, OTHER_TAG_ID, OTHER_TAG, CREDIT));
            assertThat(rule.getDebitTags()).isEmpty();
        });
    }

    @Test
    void listsAssociatedAccountsWhenRequested() {
        final List<AccountingRuleData> rules = service.retrieveAllAccountingRules(OFFICE_HIERARCHY + "%", true);

        assertThat(rules).filteredOn(rule -> rule.getId().equals(TAGGED_RULE_ID)).singleElement().satisfies(rule -> {
            final GLAccountDataForLookup shared = account(SHARED_ACCOUNT_ID, "Shared account", "RULE-SHARED");
            assertThat(rule.getCreditAccounts()).containsExactlyInAnyOrder(shared,
                    account(CREDIT_ACCOUNT_ID, "Tagged credit account", "RULE-TAGGED"));
            assertThat(rule.getDebitAccounts()).containsExactly(shared);
            assertThat(rule.getCreditTags()).isNull();
            assertThat(rule.getDebitTags()).isNull();
        });
        assertThat(rules).filteredOn(rule -> rule.getId().equals(MIXED_RULE_ID)).singleElement().satisfies(this::assertAccountsOnBothSides);
    }

    @Test
    void throwsTheExistingExceptionForAMissingRule() {
        final AccountingRuleNotFoundException exception = assertThrows(AccountingRuleNotFoundException.class,
                () -> service.retrieveAccountingRuleById(Long.MAX_VALUE));

        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.accounting.rule.id.invalid");
    }

    private void assertTagsOnBothSides(final AccountingRuleData rule) {
        assertThat(rule.getId()).isEqualTo(TAGGED_RULE_ID);
        assertThat(rule.getCreditTags()).containsExactlyInAnyOrder(tag(9500L, SHARED_TAG_ID, SHARED_TAG, CREDIT),
                tag(9501L, CREDIT_TAG_ID, CREDIT_TAG, CREDIT));
        assertThat(rule.getDebitTags()).containsExactlyInAnyOrder(tag(9502L, SHARED_TAG_ID, SHARED_TAG, DEBIT),
                tag(9503L, UNNAMED_TAG_ID, null, DEBIT));
        assertThat(rule.getCreditAccounts()).isNull();
        assertThat(rule.getDebitAccounts()).isNull();
    }

    private void assertMixedRule(final AccountingRuleData rule) {
        assertThat(rule.getId()).isEqualTo(MIXED_RULE_ID);
        assertThat(rule.getCreditAccounts()).containsExactly(account(FIXED_CREDIT_ACCOUNT_ID, "Fixed credit account", "RULE-CREDIT"));
        assertThat(rule.getCreditTags()).isNull();
        assertThat(rule.getDebitTags()).containsExactly(tag(9504L, OTHER_TAG_ID, OTHER_TAG, DEBIT));
        assertThat(rule.getDebitAccounts()).isNull();
    }

    private void assertAccountsOnBothSides(final AccountingRuleData rule) {
        assertThat(rule.getCreditAccounts()).containsExactly(account(FIXED_CREDIT_ACCOUNT_ID, "Fixed credit account", "RULE-CREDIT"));
        assertThat(rule.getDebitAccounts()).containsExactly(account(FIXED_DEBIT_ACCOUNT_ID, "Fixed debit account", "RULE-DEBIT"));
        assertThat(rule.getCreditTags()).isNull();
        assertThat(rule.getDebitTags()).isNull();
    }

    private static AccountingTagRuleData tag(final long id, final long codeValueId, final String name, final EnumOptionData type) {
        return new AccountingTagRuleData().setId(id).setTag(new CodeValueData().setId(codeValueId).setName(name)).setTransactionType(type);
    }

    private static GLAccountDataForLookup account(final long id, final String name, final String code) {
        return new GLAccountDataForLookup().setId(id).setName(name).setGlCode(code);
    }

    private void insertOffice(final long id, final String name, final String hierarchy) {
        queryFactory.insert(mOffice).set(mOffice.id, id).set(mOffice.name, name).set(mOffice.hierarchy, hierarchy)
                .set(mOffice.openingDate, Date.valueOf(LocalDate.of(2025, 1, 1))).execute();
    }

    private void insertCodeValue(final int id, final String name) {
        queryFactory.insert(mCodeValue).set(mCodeValue.id, id).set(mCodeValue.codeId, CODE_ID).set(mCodeValue.codeValue, name).execute();
    }

    private void insertAccount(final long id, final String name, final String code, final Integer tagId) {
        queryFactory.insert(accGlAccount).set(accGlAccount.id, id).set(accGlAccount.name, name).set(accGlAccount.glCode, code)
                .set(accGlAccount.classificationEnum, 1).set(accGlAccount.tagId, tagId).execute();
    }

    private void insertRule(final long id, final long officeId, final String name, final Long creditAccountId, final Long debitAccountId,
            final boolean systemDefined) {
        queryFactory.insert(accAccountingRule).set(accAccountingRule.id, id).set(accAccountingRule.officeId, officeId)
                .set(accAccountingRule.name, name).set(accAccountingRule.creditAccountId, creditAccountId)
                .set(accAccountingRule.debitAccountId, debitAccountId).set(accAccountingRule.systemDefined, systemDefined).execute();
    }

    private void insertRuleTag(final long id, final long ruleId, final int tagId, final JournalEntryType transactionType) {
        queryFactory.insert(accRuleTags).set(accRuleTags.id, id).set(accRuleTags.accRuleId, ruleId).set(accRuleTags.tagId, tagId)
                .set(accRuleTags.accTypeEnum, transactionType.getValue()).execute();
    }
}
