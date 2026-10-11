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
package org.apache.fineract.integrationtests.savings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.client.models.StandingInstructionCreationRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.StandingInstructionsHelper;
import org.junit.jupiter.api.Test;

/**
 * A monthly standing instruction on the 31st that becomes valid in a 30-day month used to fail the Execute Standing
 * Instruction job, because its first due date was built with withDayOfMonth(31) in April. It now runs on the last day
 * of the shorter month. This goes through the real read mapper and job, not a mocked StandingInstructionData.
 */
class StandingInstructionMonthEndRecurrenceTest extends FeignSavingsTestBase {

    private static final String JOB_NAME = "Execute Standing Instruction";
    private static final String SAVINGS_ACCOUNT_TYPE = "2";
    private static final String RECURRENCE_FREQUENCY_MONTHS = "2";

    @Test
    void monthlyInstructionOnThe31stRunsOnTheLastDayOfAShorterMonth() {
        final Long[] accounts = new Long[2];
        runAt("10 April 2026", () -> {
            final Long clientId = createClient("01 April 2026");
            final Long productId = createDefaultSavingsProduct().getResourceId();
            accounts[0] = createApproveActivateSavings(clientId, productId, "01 April 2026");
            accounts[1] = createApproveActivateSavings(clientId, productId, "01 April 2026");
            deposit(accounts[0], "1000", "01 April 2026");

            final StandingInstructionCreationRequest request = new StandingInstructionsHelper()
                    .build(clientId.toString(), accounts[0].toString(), accounts[1].toString(), SAVINGS_ACCOUNT_TYPE, SAVINGS_ACCOUNT_TYPE,
                            "10 April 2026", "31 December 2026", "31 January")
                    .recurrenceFrequency(RECURRENCE_FREQUENCY_MONTHS).amount("100");
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions().createStandingInstruction(request));
        });

        runAt("29 April 2026", () -> schedulerHelper.executeAndAwaitJob(JOB_NAME));
        assertBalance(accounts[0], "1000");
        assertBalance(accounts[1], "0");

        runAt("30 April 2026", () -> schedulerHelper.executeAndAwaitJob(JOB_NAME));
        assertBalance(accounts[0], "900");
        assertBalance(accounts[1], "100");
    }

    private void assertBalance(final Long savingsId, final String expected) {
        final BigDecimal balance = Optional.ofNullable(savingsHelper.getSavingsSummary(savingsId).getAccountBalance())
                .orElse(BigDecimal.ZERO);
        assertEquals(0, new BigDecimal(expected).compareTo(balance), () -> "balance of savings account " + savingsId + " was " + balance);
    }
}
