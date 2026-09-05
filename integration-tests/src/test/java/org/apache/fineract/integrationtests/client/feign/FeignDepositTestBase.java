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
package org.apache.fineract.integrationtests.client.feign;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignInterestRateChartHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRecurringDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRecurringDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base for the fixed and recurring deposit tests.
 * <p>
 * These deliberately do not extend {@link FeignSavingsTestBase}. A deposit account is a {@code SavingsAccount} subclass
 * sharing its table, and the internal listing the savings lifecycle extension cleans up with is
 * {@code SELECT sa.id FROM SavingsAccount sa WHERE sa.status = :status} -- so that extension would close the very
 * deposit account a test is working on, between one test and the next. A deposit test cleans up its own accounts
 * through the lifecycle it is exercising anyway.
 */
public abstract class FeignDepositTestBase extends FeignIntegrationTest {

    protected static FeignFixedDepositHelper fixedDepositHelper;
    protected static FeignFixedDepositProductHelper fixedDepositProductHelper;
    protected static FeignRecurringDepositHelper recurringDepositHelper;
    protected static FeignRecurringDepositProductHelper recurringDepositProductHelper;
    protected static FeignInterestRateChartHelper interestRateChartHelper;
    protected static FeignFinancialActivityAccountHelper financialActivityAccountHelper;
    protected static FeignSavingsHelper savingsHelper;
    protected static FeignSavingsProductHelper savingsProductHelper;
    protected static FeignClientHelper clientHelper;
    protected static FeignAccountHelper accountHelper;
    protected static FeignJournalEntryHelper journalEntryHelper;
    protected static FeignSchedulerHelper schedulerHelper;
    protected static FeignBusinessDateHelper businessDateHelper;
    protected static FeignGlobalConfigurationHelper globalConfigurationHelper;
    protected static FeignTaxComponentHelper taxComponentHelper;
    protected static FeignTaxGroupHelper taxGroupHelper;

    @BeforeAll
    public static void setupDepositHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        fixedDepositHelper = new FeignFixedDepositHelper(client);
        fixedDepositProductHelper = new FeignFixedDepositProductHelper(client);
        recurringDepositHelper = new FeignRecurringDepositHelper(client);
        recurringDepositProductHelper = new FeignRecurringDepositProductHelper(client);
        interestRateChartHelper = new FeignInterestRateChartHelper(client);
        financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(client);
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        clientHelper = new FeignClientHelper(client);
        accountHelper = new FeignAccountHelper(client);
        journalEntryHelper = new FeignJournalEntryHelper(client);
        schedulerHelper = new FeignSchedulerHelper(client);
        businessDateHelper = new FeignBusinessDateHelper(client);
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(client);
        taxComponentHelper = new FeignTaxComponentHelper(client);
        taxGroupHelper = new FeignTaxGroupHelper(client);
    }
}
