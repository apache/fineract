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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignDepositTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ClientTestData;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-3303: a rejected recurring deposit transaction reports the real reason in {@code defaultUserMessage}, with
 * {@code transactionDate} as the parameter name and the rejected date as the value.
 */
public class RecurringDepositTransactionErrorMessageIntegrationTest extends FeignDepositTestBase {

    private static final int DEPOSIT_PERIOD_MONTHS = 14;

    @Test
    public void depositOnAnAccountThatIsNotActiveReportsTheAccountIsNotActive() {
        LocalDate startDate = Utils.getLocalDateOfTenant().minusMonths(1);
        String start = Utils.dateFormatter.format(startDate);
        Long accountId = submitAccount(start);
        recurringDepositHelper.approve(accountId, start);

        CallFailedRuntimeException failure = FeignCalls.fail(() -> FineractFeignClientHelper.getFineractFeignClient()
                .recurringDepositAccountTransactions().transactionRecurringDepositAccountTransaction(accountId,
                        DepositRequestBuilders.depositTransaction(start, DepositTestData.RECURRING_DEPOSIT_AMOUNT), "deposit"));

        assertTransactionDateError(failure, "Transaction is not allowed. Account is not active.", start);
    }

    @Test
    public void modifyingADepositToBeforeTheDepositStartDateNamesTheStartDate() {
        LocalDate startDate = Utils.getLocalDateOfTenant().minusMonths(1);
        String start = Utils.dateFormatter.format(startDate);
        Long accountId = activeAccount(start);
        Long transactionId = recurringDepositHelper.deposit(accountId, start, DepositTestData.RECURRING_DEPOSIT_AMOUNT).getResourceId();
        String beforeStart = Utils.dateFormatter.format(startDate.minusDays(1));

        CallFailedRuntimeException failure = modify(accountId, transactionId, beforeStart);

        assertTransactionDateError(failure,
                "Transaction is not allowed. Transaction date cannot be before the account deposit start date " + start + ".", beforeStart);
    }

    @Test
    public void modifyingADepositToTheMaturityDateReportsTheMaturityDate() {
        LocalDate startDate = Utils.getLocalDateOfTenant().minusMonths(1);
        String start = Utils.dateFormatter.format(startDate);
        Long accountId = activeAccount(start);
        Long transactionId = recurringDepositHelper.deposit(accountId, start, DepositTestData.RECURRING_DEPOSIT_AMOUNT).getResourceId();
        String maturity = Utils.dateFormatter.format(startDate.plusMonths(DEPOSIT_PERIOD_MONTHS));

        CallFailedRuntimeException failure = modify(accountId, transactionId, maturity);

        assertTransactionDateError(failure, "Transaction is not allowed. Transaction date is on or after account maturity date.", maturity);
    }

    private Long activeAccount(String start) {
        Long accountId = submitAccount(start);
        recurringDepositHelper.approve(accountId, start);
        recurringDepositHelper.activate(accountId, start);
        return accountId;
    }

    private Long submitAccount(String start) {
        LocalDate today = Utils.getLocalDateOfTenant();
        Long clientId = clientHelper
                .createClient(new PostClientsRequest().officeId(1L).legalFormId(1L).firstname(Utils.randomFirstNameGenerator())
                        .lastname(Utils.randomLastNameGenerator()).active(true).activationDate(ClientTestData.DEFAULT_ACTIVATION_DATE)
                        .dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE))
                .getClientId();
        Long productId = recurringDepositProductHelper.createProduct(DepositRequestBuilders.withChart(
                DepositRequestBuilders.recurringDepositProduct().accountingRule(SavingsTestData.AccountingRule.NONE),
                Utils.dateFormatter.format(today.minusMonths(3)), Utils.dateFormatter.format(today.plusYears(10)),
                DepositTestData.recurringChartSlabsFor("period"))).getResourceId();
        return recurringDepositHelper.submitApplication(DepositRequestBuilders.recurringDepositAccount(clientId, productId, start, start,
                DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)).getSavingsId();
    }

    private CallFailedRuntimeException modify(Long accountId, Long transactionId, String transactionDate) {
        return FeignCalls.fail(() -> FineractFeignClientHelper.getFineractFeignClient().recurringDepositAccountTransactions()
                .handleCommandsRecurringDepositAccountTransaction(accountId, transactionId,
                        DepositRequestBuilders.depositTransaction(transactionDate, DepositTestData.RECURRING_DEPOSIT_AMOUNT), "modify"));
    }

    private static void assertTransactionDateError(CallFailedRuntimeException failure, String expectedMessage, String rejectedDate) {
        assertEquals(400, failure.getStatus(), failure.getResponseBody());
        JsonObject error = JsonParser.parseString(failure.getResponseBody()).getAsJsonObject().getAsJsonArray("errors").get(0)
                .getAsJsonObject();
        assertEquals(expectedMessage, error.get("defaultUserMessage").getAsString());
        assertEquals("transactionDate", error.get("parameterName").getAsString());
        assertEquals(rejectedDate, error.get("value").getAsString());
    }
}
