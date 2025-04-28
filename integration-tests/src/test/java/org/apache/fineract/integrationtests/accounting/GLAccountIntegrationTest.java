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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutGLAccountsRequest;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GLAccountIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void createUpdateDeleteGLAccountTest() {
        // CREATE
        String uniqueString = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
        final PostGLAccountsResponse newAccount = createGLAccount(uniqueString);
        GetGLAccountsResponse accountDetails = AccountHelper.getGLAccount(newAccount.getResourceId());
        Assertions.assertEquals(uniqueString, accountDetails.getGlCode());
        Assertions.assertEquals(uniqueString, accountDetails.getName());
        Assertions.assertEquals(true, accountDetails.getManualEntriesAllowed());
        Assertions.assertEquals((long) GLAccountType.INCOME.getValue(), accountDetails.getType().getId());
        Assertions.assertEquals(1L, accountDetails.getUsage().getId());
        Assertions.assertEquals(uniqueString, accountDetails.getDescription());
        // UPDATE
        AccountHelper.updateGLAccount(newAccount.getResourceId(), new PutGLAccountsRequest().description("newDescription").name("newName")
                .glCode("newGLCode").type(GLAccountType.ASSET.getValue()).manualEntriesAllowed(false).usage(2));
        accountDetails = AccountHelper.getGLAccount(newAccount.getResourceId());
        Assertions.assertEquals("newDescription", accountDetails.getDescription());
        Assertions.assertEquals("newName", accountDetails.getName());
        Assertions.assertEquals("newGLCode", accountDetails.getGlCode());
        Assertions.assertEquals((long) GLAccountType.ASSET.getValue(), accountDetails.getType().getId());
        Assertions.assertEquals(2L, accountDetails.getUsage().getId());
        // DELETE
        AccountHelper.deleteGLAccount(newAccount.getResourceId());
    }

    @Test
    public void testDeleteGLAccountWhileThereAreChildren() {
        String uniqueNameForParent = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
        final PostGLAccountsResponse newParentAccount = AccountHelper
                .createGLAccount(new PostGLAccountsRequest().type(GLAccountType.INCOME.getValue()).glCode(uniqueNameForParent)
                        .manualEntriesAllowed(true).usage(2).description(uniqueNameForParent).name(uniqueNameForParent));
        String uniqueNameForChild = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
        final PostGLAccountsResponse newChildAccount = AccountHelper.createGLAccount(
                new PostGLAccountsRequest().type(GLAccountType.INCOME.getValue()).glCode(uniqueNameForChild).manualEntriesAllowed(true)
                        .usage(1).parentId(newParentAccount.getResourceId()).description(uniqueNameForChild).name(uniqueNameForChild));
        // DELETE
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> AccountHelper.deleteGLAccount(newParentAccount.getResourceId()));
        assertEquals(403, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.glaccount.glcode.invalid.delete.has.children"));
        AccountHelper.deleteGLAccount(newChildAccount.getResourceId());
        AccountHelper.deleteGLAccount(newParentAccount.getResourceId());
    }

    @Test
    public void testDeleteGLAccountWhileMappedToProduct() {
        String uniqueString = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
        final PostGLAccountsResponse newAccount = createGLAccount(uniqueString);
        loanProductHelper.createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                .incomeFromCapitalizationAccountId(newAccount.getResourceId())
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        // DELETE
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> AccountHelper.deleteGLAccount(newAccount.getResourceId()));
        assertEquals(403, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.glaccount.glcode.invalid.delete.product.mapping"));
    }

    @Test
    public void testDeleteGLAccountWhileThereIsJournalEntry() {
        runAt("01 January 2024", () -> {
            String uniqueString = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
            final PostGLAccountsResponse newAccount = createGLAccount(uniqueString);
            uniqueString = Utils.uniqueRandomStringGenerator("UNIQUE_FEE_INCOME" + Calendar.getInstance().getTimeInMillis(), 5);
            final PostGLAccountsResponse newAccount2 = createGLAccount(uniqueString);
            JournalEntryHelper.createJournalEntry("", new JournalEntryCommand().amount(BigDecimal.TEN).officeId(1L).currencyCode("USD")
                    .locale("en").dateFormat("uuuu-MM-dd").transactionDate(LocalDate.of(2024, 1, 1))
                    .addCreditsItem(new SingleDebitOrCreditEntryCommand().glAccountId(newAccount.getResourceId()).amount(BigDecimal.TEN))
                    .addDebitsItem(new SingleDebitOrCreditEntryCommand().glAccountId(newAccount2.getResourceId()).amount(BigDecimal.TEN)));
            // DELETE
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> AccountHelper.deleteGLAccount(newAccount.getResourceId()));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.glaccount.glcode.invalid.delete.transactions.logged"));
        });
    }

    private PostGLAccountsResponse createGLAccount(String uniqueString) {
        return AccountHelper.createGLAccount(new PostGLAccountsRequest().type(GLAccountType.INCOME.getValue()).glCode(uniqueString)
                .manualEntriesAllowed(true).usage(1).description(uniqueString).name(uniqueString));
    }

}
