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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-3216: journal entry search and lookup only return entries posted in the user's office or one of its child
 * offices.
 */
public class JournalEntryOfficeHierarchyAccessIntegrationTest extends FeignIntegrationTest {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2020, 1, 1);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2024, 1, 15);
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(fineractClient());
    private final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient());
    private final FeignJournalEntryHelper journalHelper = new FeignJournalEntryHelper(fineractClient());

    @Test
    public void testJournalEntriesAreLimitedToUserOfficeHierarchy() {
        Long parentOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long childOfficeId = createChildOffice(parentOfficeId);
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Account debitAccount = accountHelper.createAssetAccount();
        Account creditAccount = accountHelper.createLiabilityAccount();

        String parentTransactionId = postEntry(parentOfficeId, debitAccount, creditAccount);
        String childTransactionId = postEntry(childOfficeId, debitAccount, creditAccount);
        String siblingTransactionId = postEntry(siblingOfficeId, debitAccount, creditAccount);

        FineractFeignClient parentOfficeUser = createUserInOffice(parentOfficeId);

        assertEquals(Set.of(parentTransactionId, childTransactionId), transactionIdsOn(parentOfficeUser, debitAccount),
                "A user must see entries of its office and its child offices only");
        assertEquals(Set.of(), transactionIdsOn(parentOfficeUser, debitAccount, Map.of("officeId", siblingOfficeId)),
                "Filtering on an office outside the user's hierarchy must return nothing");
        assertEquals(Set.of(parentTransactionId, childTransactionId, siblingTransactionId),
                transactionIdsOn(fineractClient(), debitAccount), "A head office user must still see every office");

        Long childEntryId = entryIdOf(childTransactionId);
        Long siblingEntryId = entryIdOf(siblingTransactionId);

        assertEquals(childEntryId,
                FeignCalls.ok(() -> parentOfficeUser.journalEntries().retrieveJournalEntryById(childEntryId, false, false)).getId(),
                "A user must be able to read an entry of a child office");
        CallFailedRuntimeException failure = FeignCalls
                .fail(() -> parentOfficeUser.journalEntries().retrieveJournalEntryById(siblingEntryId, false, false));
        assertEquals(404, failure.getStatus(), "An entry outside the user's hierarchy must answer like a missing one");
        assertEquals(siblingEntryId,
                FeignCalls.ok(() -> fineractClient().journalEntries().retrieveJournalEntryById(siblingEntryId, false, false)).getId(),
                "A head office user must still read any entry");
    }

    private Long createChildOffice(Long parentOfficeId) {
        PostOfficesRequest request = new PostOfficesRequest().parentId(parentOfficeId).name(Utils.uniqueRandomStringGenerator("O_", 9))
                .openingDate(OFFICE_OPENING_DATE).dateFormat(DATE_FORMAT).locale("en");
        return FeignCalls.ok(() -> fineractClient().offices().createOffice(request)).getOfficeId();
    }

    private String postEntry(Long officeId, Account debitAccount, Account creditAccount) {
        JournalEntryCommand command = new JournalEntryCommand().officeId(officeId).currencyCode("USD").locale("en").dateFormat(DATE_FORMAT)
                .transactionDate(TRANSACTION_DATE)
                .addCreditsItem(
                        new SingleDebitOrCreditEntryCommand().glAccountId(creditAccount.getAccountID().longValue()).amount(BigDecimal.TEN))
                .addDebitsItem(
                        new SingleDebitOrCreditEntryCommand().glAccountId(debitAccount.getAccountID().longValue()).amount(BigDecimal.TEN));
        return journalHelper.createJournalEntry(command).getTransactionId();
    }

    private static Set<String> transactionIdsOn(FineractFeignClient client, Account account) {
        return transactionIdsOn(client, account, Map.of());
    }

    private static Set<String> transactionIdsOn(FineractFeignClient client, Account account, Map<String, Object> extraParams) {
        Map<String, Object> params = new HashMap<>(extraParams);
        params.put("glAccountId", account.getAccountID().longValue());
        params.put("limit", 100);
        List<JournalEntryTransactionItem> entries = FeignCalls.ok(() -> client.journalEntries().retrieveAllJournalEntries(params))
                .getPageItems();
        return entries.stream().map(JournalEntryTransactionItem::getTransactionId).collect(Collectors.toSet());
    }

    private Long entryIdOf(String transactionId) {
        return journalHelper.getJournalEntriesByTransactionId(transactionId).getPageItems().get(0).getId();
    }

    private static FineractFeignClient createUserInOffice(Long officeId) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of("READ_JOURNALENTRY", true));
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
