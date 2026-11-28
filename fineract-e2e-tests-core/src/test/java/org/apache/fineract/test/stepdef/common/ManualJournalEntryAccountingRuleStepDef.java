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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountRuleRequest;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.test.data.GLAType;
import org.apache.fineract.test.data.GLAUsage;
import org.apache.fineract.test.helper.BusinessDateHelper;

@RequiredArgsConstructor
public class ManualJournalEntryAccountingRuleStepDef {

    private static final long HEAD_OFFICE_ID = 1L;

    private final FineractFeignClient fineractFeignClient;
    private final BusinessDateHelper businessDateHelper;

    private Map<String, Long> ruleAccounts;
    private Long accountingRuleId;

    @Given("an accounting rule with a fixed {string} account and tags on the other side")
    public void createMixedAccountingRule(final String fixedSide) {
        final Long allowedTagId = createAssetAccountTag();
        final Long otherTagId = createAssetAccountTag();
        ruleAccounts = Map.of("fixed", createAccount(null), "tagged", createAccount(allowedTagId), "other", createAccount(otherTagId));

        final AccountRuleRequest rule = new AccountRuleRequest().officeId(HEAD_OFFICE_ID).name(UUID.randomUUID().toString());
        switch (fixedSide) {
            case "debit" -> rule.accountToDebit(ruleAccounts.get("fixed")).creditTags(List.of(allowedTagId));
            case "credit" -> rule.debitTags(List.of(allowedTagId)).accountToCredit(ruleAccounts.get("fixed"));
            default -> throw new IllegalArgumentException("Unknown fixed side: " + fixedSide);
        }
        accountingRuleId = ok(() -> fineractFeignClient.accountingRules().createAccountingRule(rule)).getResourceId();
    }

    @Then("a manual journal entry debiting the {string} account and crediting the {string} account is saved")
    public void verifyEntryIsSaved(final String debit, final String credit) {
        final JournalEntryCommand request = journalEntry(debit, credit);
        final String transactionId = ok(() -> fineractFeignClient.journalEntries().createGLJournalEntry("", request)).getTransactionId();
        assertThat(transactionId).isNotBlank();
        assertThat(journalLines(Map.of("transactionId", transactionId)))
                .extracting(JournalEntryTransactionItem::getGlAccountId, line -> line.getEntryType().getValue(),
                        JournalEntryTransactionItem::getAmount)
                .containsExactlyInAnyOrder(tuple(ruleAccounts.get(debit), "DEBIT", 10.0), tuple(ruleAccounts.get(credit), "CREDIT", 10.0));
    }

    @Then("a manual journal entry with debit {string} and credit {string} is rejected with {string} and leaves no lines")
    public void verifyEntryIsRejected(final String debit, final String credit, final String errorCode) throws JsonProcessingException {
        final JournalEntryCommand request = journalEntry(debit, credit);
        final CallFailedRuntimeException failure = fail(() -> fineractFeignClient.journalEntries().createGLJournalEntry("", request));
        assertThat(failure.getStatus()).isEqualTo(403);
        assertThat(ObjectMapperFactory.getShared().readTree(failure.getResponseBody()).path("errors").path(0)
                .path("userMessageGlobalisationCode").asText()).isEqualTo(errorCode);
        assertThat(journalLines(Map.of("glAccountId", ruleAccounts.get(debit)))).isEmpty();
        assertThat(journalLines(Map.of("glAccountId", ruleAccounts.get(credit)))).isEmpty();
    }

    private Long createAssetAccountTag() {
        final PostCodeValuesDataRequest tag = new PostCodeValuesDataRequest().name(UUID.randomUUID().toString()).position(1).isActive(true);
        return ok(() -> fineractFeignClient.codeValues().createCodeValueByCodeName("AssetAccountTags", tag)).getSubResourceId();
    }

    private Long createAccount(final Long tagId) {
        final String accountCode = UUID.randomUUID().toString();
        final PostGLAccountsRequest request = new PostGLAccountsRequest().name(accountCode).glCode(accountCode).type(GLAType.ASSET.value)
                .usage(GLAUsage.DETAIL.value).manualEntriesAllowed(true).tagId(tagId);
        return ok(() -> fineractFeignClient.generalLedgerAccount().createGLAccount(request, Map.of())).getResourceId();
    }

    private JournalEntryCommand journalEntry(final String debit, final String credit) {
        return new JournalEntryCommand().accountingRule(accountingRuleId).officeId(HEAD_OFFICE_ID).currencyCode("USD")
                .transactionDate(businessDateHelper.getBusinessLocalDate()).dateFormat("yyyy-MM-dd").locale("en")
                .addDebitsItem(new SingleDebitOrCreditEntryCommand().glAccountId(ruleAccounts.get(debit)).amount(BigDecimal.TEN))
                .addCreditsItem(new SingleDebitOrCreditEntryCommand().glAccountId(ruleAccounts.get(credit)).amount(BigDecimal.TEN));
    }

    private List<JournalEntryTransactionItem> journalLines(final Map<String, Object> filters) {
        return ok(() -> fineractFeignClient.journalEntries().retrieveAllJournalEntries(filters)).getPageItems();
    }
}
