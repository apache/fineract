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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SkipRepaymentOnMonthFirstTest extends FeignLoanTestBase {

    private static final String MEETING_START_DATE = "15 September 2011";
    private static final String MONTHLY_FREQUENCY = "3";
    private static final String EVERY_ONE_MONTH = "1";

    private final FeignGroupHelper groupHelper = new FeignGroupHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @AfterEach
    public void resetSkipRepaymentConfiguration() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void checkRepaymentSkipOnFirstDayOfMonth() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH,
                new PutGlobalConfigurationsRequest().enabled(true));

        final Long clientId = createClient();
        final Long groupId = groupHelper.createActiveGroup().getGroupId();
        groupHelper.associateClient(groupId, clientId);

        final Long calendarId = CalendarHelper.createMeetingForGroup(groupId, MEETING_START_DATE, MONTHLY_FREQUENCY, EVERY_ONE_MONTH, null)
                .getResourceId();

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .buildRequest(null));

        final Long loanId = applyForLoanApplication(clientId, groupId, loanProductId, calendarId);

        verifyLoanRepaymentSchedule(getLoanDetails(loanId).getRepaymentSchedule().getPeriods());
    }

    private Long applyForLoanApplication(final Long clientId, final Long groupId, final Long loanProductId, final Long calendarId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 October 2011", 12000.0, 4)//
                .groupId(groupId)//
                .loanType("jlg")//
                .calendarId(calendarId)//
                .syncDisbursementWithMeeting(false)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }

    private void verifyLoanRepaymentSchedule(final List<GetLoansLoanIdRepaymentPeriod> periods) {
        assertEquals(LocalDate.of(2011, 10, 15), periods.get(1).getDueDate(), "Checking for Repayment Date for 1st Month");
        assertEquals(LocalDate.of(2011, 11, 15), periods.get(2).getDueDate(), "Checking for Repayment Date for 2nd Month");
        assertEquals(LocalDate.of(2011, 12, 15), periods.get(3).getDueDate(), "Checking for Repayment Date for 3rd Month");
        assertEquals(LocalDate.of(2012, 1, 15), periods.get(4).getDueDate(), "Checking for Repayment Date for 4th Month");
    }
}
