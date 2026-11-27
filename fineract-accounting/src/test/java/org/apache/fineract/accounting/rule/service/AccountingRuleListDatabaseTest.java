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
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformServiceImpl;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.accounting.rule.exception.AccountingRuleNotFoundException;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@TestInstance(Lifecycle.PER_CLASS)
@Import({ AccountingRuleReadPlatformServiceImpl.class, GLAccountReadPlatformServiceImpl.class })
class AccountingRuleListDatabaseTest extends QuerydslDatabaseTest {

    private static final long PARENT_OFFICE = 9801L;
    private static final long CURRENT_OFFICE = 9802L;
    private static final long CHILD_OFFICE = 9803L;
    private static final long SIBLING_OFFICE = 9804L;
    private static final String CURRENT_HIERARCHY = ".9801.9802.";
    private static final long DIRECT_RULE = 9807L;
    private static final long TAGGED_RULE = 9808L;
    private static final long MIXED_RULE = 9824L;
    private static final long SIBLING_RULE = 9818L;
    private static final long PARENT_RULE = 9819L;
    private static final long SYSTEM_RULE = 9820L;
    private static final long NO_OFFICE_RULE = 9821L;
    private static final List<Long> RULE_IDS = List.of(DIRECT_RULE, TAGGED_RULE, MIXED_RULE, SIBLING_RULE, PARENT_RULE, SYSTEM_RULE,
            NO_OFFICE_RULE);
    private static final long DIRECT_DEBIT = 9831L;
    private static final long DIRECT_CREDIT = 9832L;
    private static final long TAGGED_DEBIT = 9833L;
    private static final long TAGGED_CREDIT = 9834L;
    private static final int DEBIT_TAG = 9841;
    private static final int CREDIT_TAG = 9842;

    private final AccountingRuleReadPlatformService service;
    private final SQLQueryFactory queryFactory;
    private final PlatformTransactionManager transactionManager;

    @BeforeAll
    void insertFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            insertOffice(PARENT_OFFICE, null, ".9801.", "Parent Office");
            insertOffice(CURRENT_OFFICE, PARENT_OFFICE, CURRENT_HIERARCHY, "Current Office");
            insertOffice(CHILD_OFFICE, CURRENT_OFFICE, ".9801.9802.9803.", "Child Office");
            insertOffice(SIBLING_OFFICE, PARENT_OFFICE, ".9801.9804.", "Sibling Office");

            final Integer accountTagCode = queryFactory.select(mCode.id).from(mCode).where(mCode.codeName.eq("AssetAccountTags"))
                    .fetchOne();
            assertThat(accountTagCode).isNotNull();
            insertTag(DEBIT_TAG, accountTagCode, "Debit Tag");
            insertTag(CREDIT_TAG, accountTagCode, "Credit Tag");
            insertAccount(DIRECT_DEBIT, "Direct Debit", "SQL018-DD", null);
            insertAccount(DIRECT_CREDIT, "Direct Credit", "SQL018-DC", null);
            insertAccount(TAGGED_DEBIT, "Tagged Debit", "SQL018-TD", DEBIT_TAG);
            insertAccount(TAGGED_CREDIT, "Tagged Credit", "SQL018-TC", CREDIT_TAG);

            insertRule(MIXED_RULE, "Mixed Rule", CHILD_OFFICE, null, DIRECT_CREDIT);
            insertRule(TAGGED_RULE, "Tagged Rule", CHILD_OFFICE, null, null);
            insertRule(DIRECT_RULE, "Direct Rule", CURRENT_OFFICE, DIRECT_DEBIT, DIRECT_CREDIT);
            insertRule(SIBLING_RULE, "Sibling Rule", SIBLING_OFFICE, null, null);
            insertRule(PARENT_RULE, "Parent Rule", PARENT_OFFICE, null, null);
            insertRule(SYSTEM_RULE, "System Rule", CURRENT_OFFICE, null, null);
            insertRule(NO_OFFICE_RULE, "Rule Without Office", null, null, null);
            queryFactory.update(accAccountingRule).where(accAccountingRule.id.eq(SYSTEM_RULE)).set(accAccountingRule.systemDefined, true)
                    .execute();
            queryFactory.update(accAccountingRule).where(accAccountingRule.id.eq(DIRECT_RULE))
                    .set(accAccountingRule.description, "Fixed posting").set(accAccountingRule.allowMultipleCredits, true).execute();
            queryFactory.update(accAccountingRule).where(accAccountingRule.id.eq(TAGGED_RULE))
                    .set(accAccountingRule.allowMultipleDebits, true).execute();

            insertRuleTag(TAGGED_RULE, DEBIT_TAG, JournalEntryType.DEBIT);
            insertRuleTag(TAGGED_RULE, CREDIT_TAG, JournalEntryType.CREDIT);
            insertRuleTag(MIXED_RULE, DEBIT_TAG, JournalEntryType.DEBIT);
            insertRuleTag(MIXED_RULE, CREDIT_TAG, JournalEntryType.CREDIT);
        });
    }

    @AfterAll
    void deleteFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            queryFactory.delete(accRuleTags).where(accRuleTags.accRuleId.in(RULE_IDS)).execute();
            queryFactory.delete(accAccountingRule).where(accAccountingRule.id.in(RULE_IDS)).execute();
            queryFactory.delete(accGlAccount).where(accGlAccount.id.in(DIRECT_DEBIT, DIRECT_CREDIT, TAGGED_DEBIT, TAGGED_CREDIT)).execute();
            queryFactory.delete(mCodeValue).where(mCodeValue.id.in(DEBIT_TAG, CREDIT_TAG)).execute();
            queryFactory.delete(mOffice).where(mOffice.id.in(CHILD_OFFICE, SIBLING_OFFICE)).execute();
            queryFactory.delete(mOffice).where(mOffice.id.eq(CURRENT_OFFICE)).execute();
            queryFactory.delete(mOffice).where(mOffice.id.eq(PARENT_OFFICE)).execute();
        });
    }

    @Test
    void filtersByOfficeHierarchyAndPreservesListOrder() {
        final List<AccountingRuleData> rules = service.retrieveAllAccountingRules(CURRENT_HIERARCHY + "%", false);

        assertThat(rules).extracting(AccountingRuleData::getId).containsExactly(TAGGED_RULE, MIXED_RULE, DIRECT_RULE);

        final AccountingRuleData taggedRule = rules.getFirst();
        assertThat(taggedRule.getOfficeId()).isEqualTo(CHILD_OFFICE);
        assertThat(taggedRule.getOfficeName()).isEqualTo("Child Office");
        assertThat(taggedRule.getName()).isEqualTo("Tagged Rule");
        assertThat(taggedRule.getDescription()).isNull();
        assertThat(taggedRule.isAllowMultipleDebitEntries()).isTrue();
        assertThat(taggedRule.isAllowMultipleCreditEntries()).isFalse();

        final AccountingRuleData directRule = rules.getLast();
        assertThat(directRule.getOfficeId()).isEqualTo(CURRENT_OFFICE);
        assertThat(directRule.getOfficeName()).isEqualTo("Current Office");
        assertThat(directRule.getName()).isEqualTo("Direct Rule");
        assertThat(directRule.getDescription()).isEqualTo("Fixed posting");
        assertThat(directRule.isSystemDefined()).isFalse();
        assertThat(directRule.isAllowMultipleDebitEntries()).isFalse();
        assertThat(directRule.isAllowMultipleCreditEntries()).isTrue();

        assertThat(service.retrieveAllAccountingRules(CURRENT_HIERARCHY, false)).extracting(AccountingRuleData::getId)
                .containsExactly(DIRECT_RULE);
        assertThat(service.retrieveAllAccountingRules(".unmatched.%", false)).isEmpty();
    }

    @Test
    void preservesEscapedCharactersInHierarchyPattern() {
        assertThat(service.retrieveAllAccountingRules("\\" + CURRENT_HIERARCHY + "%", false)).extracting(AccountingRuleData::getId)
                .containsExactly(TAGGED_RULE, MIXED_RULE, DIRECT_RULE);
    }

    @Test
    void includesEveryNonSystemRuleWithAnOfficeWhenHierarchyIsOmitted() {
        assertThat(service.retrieveAllAccountingRules(null, false)).extracting(AccountingRuleData::getId)
                .containsExactlyInAnyOrder(DIRECT_RULE, TAGGED_RULE, MIXED_RULE, SIBLING_RULE, PARENT_RULE);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void loadsAssociationsIndependentlyForEachSide(final boolean includeAccounts) {
        final List<AccountingRuleData> rules = service.retrieveAllAccountingRules(null, includeAccounts);
        final AccountingRuleData direct = findRule(rules, DIRECT_RULE);
        assertThat(direct.getDebitAccounts()).containsExactly(account(DIRECT_DEBIT, "Direct Debit", "SQL018-DD"));
        assertThat(direct.getCreditAccounts()).containsExactly(account(DIRECT_CREDIT, "Direct Credit", "SQL018-DC"));
        assertThat(direct.getDebitTags()).isNull();
        assertThat(direct.getCreditTags()).isNull();

        final AccountingRuleData tagged = findRule(rules, TAGGED_RULE);
        final AccountingRuleData mixed = findRule(rules, MIXED_RULE);
        final AccountingRuleData untagged = findRule(rules, SIBLING_RULE);
        assertThat(mixed.getCreditAccounts()).containsExactly(account(DIRECT_CREDIT, "Direct Credit", "SQL018-DC"));
        assertThat(mixed.getCreditTags()).isNull();

        if (includeAccounts) {
            assertThat(tagged.getDebitAccounts()).containsExactly(account(TAGGED_DEBIT, "Tagged Debit", "SQL018-TD"));
            assertThat(tagged.getCreditAccounts()).containsExactly(account(TAGGED_CREDIT, "Tagged Credit", "SQL018-TC"));
            assertThat(tagged.getDebitTags()).isNull();
            assertThat(tagged.getCreditTags()).isNull();
            assertThat(mixed.getDebitAccounts()).containsExactly(account(TAGGED_DEBIT, "Tagged Debit", "SQL018-TD"));
            assertThat(mixed.getDebitTags()).isNull();
            assertThat(untagged.getDebitAccounts()).isEmpty();
            assertThat(untagged.getCreditAccounts()).isEmpty();
            assertThat(untagged.getDebitTags()).isNull();
            assertThat(untagged.getCreditTags()).isNull();
        } else {
            assertTag(tagged.getDebitTags(), DEBIT_TAG, "Debit Tag", JournalEntryType.DEBIT);
            assertTag(tagged.getCreditTags(), CREDIT_TAG, "Credit Tag", JournalEntryType.CREDIT);
            assertThat(tagged.getDebitAccounts()).isNull();
            assertThat(tagged.getCreditAccounts()).isNull();
            assertTag(mixed.getDebitTags(), DEBIT_TAG, "Debit Tag", JournalEntryType.DEBIT);
            assertThat(mixed.getDebitAccounts()).isNull();
            assertThat(untagged.getDebitTags()).isEmpty();
            assertThat(untagged.getCreditTags()).isEmpty();
            assertThat(untagged.getDebitAccounts()).isNull();
            assertThat(untagged.getCreditAccounts()).isNull();
        }
    }

    @Test
    void returnsRuleByIdAndReportsMissingRule() {
        final AccountingRuleData rule = service.retrieveAccountingRuleById(MIXED_RULE);

        assertThat(rule.getId()).isEqualTo(MIXED_RULE);
        assertThat(rule.getCreditAccounts()).containsExactly(account(DIRECT_CREDIT, "Direct Credit", "SQL018-DC"));
        assertThat(rule.getCreditTags()).isNull();
        assertTag(rule.getDebitTags(), DEBIT_TAG, "Debit Tag", JournalEntryType.DEBIT);
        assertThat(rule.getDebitAccounts()).isNull();
        assertThrows(AccountingRuleNotFoundException.class, () -> service.retrieveAccountingRuleById(Long.MAX_VALUE));
    }

    private void insertOffice(final long id, final Long parentId, final String hierarchy, final String name) {
        queryFactory.insert(mOffice).set(mOffice.id, id).set(mOffice.parentId, parentId).set(mOffice.hierarchy, hierarchy)
                .set(mOffice.name, name).set(mOffice.openingDate, Date.valueOf(LocalDate.of(2025, 1, 1))).execute();
    }

    private void insertTag(final int id, final Integer codeId, final String name) {
        queryFactory.insert(mCodeValue).set(mCodeValue.id, id).set(mCodeValue.codeId, codeId).set(mCodeValue.codeValue, name).execute();
    }

    private void insertAccount(final long id, final String name, final String glCode, final Integer tagId) {
        queryFactory.insert(accGlAccount).set(accGlAccount.id, id).set(accGlAccount.name, name).set(accGlAccount.glCode, glCode)
                .set(accGlAccount.classificationEnum, GLAccountType.ASSET.getValue()).set(accGlAccount.tagId, tagId).execute();
    }

    private void insertRule(final long id, final String name, final Long officeId, final Long debitId, final Long creditId) {
        queryFactory.insert(accAccountingRule).set(accAccountingRule.id, id).set(accAccountingRule.name, name)
                .set(accAccountingRule.officeId, officeId).set(accAccountingRule.debitAccountId, debitId)
                .set(accAccountingRule.creditAccountId, creditId).execute();
    }

    private void insertRuleTag(final long ruleId, final int tagId, final JournalEntryType type) {
        queryFactory.insert(accRuleTags).set(accRuleTags.accRuleId, ruleId).set(accRuleTags.tagId, tagId)
                .set(accRuleTags.accTypeEnum, type.getValue()).execute();
    }

    private static AccountingRuleData findRule(final List<AccountingRuleData> rules, final long id) {
        return rules.stream().filter(rule -> rule.getId() == id).findFirst().orElseThrow();
    }

    private static GLAccountDataForLookup account(final long id, final String name, final String glCode) {
        return new GLAccountDataForLookup().setId(id).setName(name).setGlCode(glCode);
    }

    private static void assertTag(final List<AccountingTagRuleData> tags, final int id, final String name, final JournalEntryType type) {
        assertThat(tags).singleElement().satisfies(tag -> {
            assertThat(tag.getId()).isNotNull();
            assertThat(tag.getTag()).isEqualTo(CodeValueData.instance((long) id, name));
            assertThat(tag.getTransactionType().getId()).isEqualTo(type.getValue().longValue());
        });
    }
}
