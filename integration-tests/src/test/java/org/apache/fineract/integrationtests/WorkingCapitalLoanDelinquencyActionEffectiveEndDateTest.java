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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanDelinquencyActionEffectiveEndDateTest {

    private static final int PERIOD_FREQUENCY_DAYS = 30;

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    @Test
    public void testPauseCreatesDelinquencyActionWithNoEffectiveEndDate() {
        BusinessDateHelper.runAt("01 July 2026", () -> {
            final Long loanId = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", LocalDate.of(2026, Month.JULY, 1),
                    LocalDate.of(2026, Month.JULY, 10));

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanId);
            assertEquals(1, actions.size());

            final WorkingCapitalLoanDelinquencyActionData pause = actions.getFirst();
            assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, pause.getAction());
            assertEquals(LocalDate.of(2026, Month.JULY, 1), pause.getStartDate());
            assertEquals(LocalDate.of(2026, Month.JULY, 10), pause.getEndDate());
            assertNull(pause.getEffectiveEndDate(), "effectiveEndDate must be null when the pause has not been resumed");
        });
    }

    @Test
    public void testResumeCreatesResumeActionAndSetsEffectiveEndDateOnPause() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", LocalDate.of(2026, Month.JULY, 1),
                    LocalDate.of(2026, Month.JULY, 10));
        });

        BusinessDateHelper.runAt("05 July 2026", () -> {
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "resume", LocalDate.of(2026, Month.JULY, 5),
                    null);

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(2, actions.size());

            final WorkingCapitalLoanDelinquencyActionData pause = actions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()).findFirst().orElseThrow();
            final WorkingCapitalLoanDelinquencyActionData resume = actions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.RESUME == a.getAction()).findFirst().orElseThrow();

            assertEquals(LocalDate.of(2026, Month.JULY, 5), resume.getStartDate());
            assertNull(resume.getEffectiveEndDate(), "RESUME rows should never have an effectiveEndDate");

            assertNotNull(pause.getEffectiveEndDate(), "effectiveEndDate must be set on the pause after a resume");
            assertEquals(LocalDate.of(2026, Month.JULY, 5), pause.getEffectiveEndDate());

            assertEquals(LocalDate.of(2026, Month.JULY, 10), pause.getEndDate(), "original endDate must be unchanged");
        });
    }

    @Test
    public void testEffectiveEndDateMatchesEarliestResumeWithinPauseWindow() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", LocalDate.of(2026, Month.JULY, 1),
                    LocalDate.of(2026, Month.JULY, 20));
        });

        BusinessDateHelper.runAt("05 July 2026", () -> {
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "resume", LocalDate.of(2026, Month.JULY, 5),
                    null);

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            final WorkingCapitalLoanDelinquencyActionData pause = actions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()).findFirst().orElseThrow();

            assertEquals(LocalDate.of(2026, Month.JULY, 5), pause.getEffectiveEndDate());
            assertEquals(LocalDate.of(2026, Month.JULY, 20), pause.getEndDate(), "original endDate must be unchanged");
        });
    }

    @Test
    public void testMultiplePausesEachGetCorrectEffectiveEndDate() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", LocalDate.of(2026, Month.JULY, 1),
                    LocalDate.of(2026, Month.JULY, 10));
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", LocalDate.of(2026, Month.JULY, 15),
                    LocalDate.of(2026, Month.JULY, 25));
        });

        BusinessDateHelper.runAt("05 July 2026", () -> {
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "resume", LocalDate.of(2026, Month.JULY, 5),
                    null);

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);

            final WorkingCapitalLoanDelinquencyActionData firstPause = actions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()
                            && LocalDate.of(2026, Month.JULY, 1).equals(a.getStartDate()))
                    .findFirst().orElseThrow();
            final WorkingCapitalLoanDelinquencyActionData secondPause = actions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()
                            && LocalDate.of(2026, Month.JULY, 15).equals(a.getStartDate()))
                    .findFirst().orElseThrow();

            assertEquals(LocalDate.of(2026, Month.JULY, 5), firstPause.getEffectiveEndDate(),
                    "first pause effectiveEndDate should match the resume date");
            assertNull(secondPause.getEffectiveEndDate(), "second pause was not resumed and must have no effectiveEndDate");
        });
    }

    // --- helpers (mirrors WorkingCapitalLoanDelinquencyActionIntegrationTest setup) ---

    private Long createActiveLoan(final LocalDate disbursementDate) {
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = submitAndApproveLoan(clientId, productId);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);
        return loanId;
    }

    private Long createWorkingCapitalLoanDelinquencyBucket(final int frequencyDays) {
        final PostDelinquencyRangeResponse range1 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        assertNotNull(range1);

        final List<Integer> rangeIds = new ArrayList<>();
        rangeIds.add(Math.toIntExact(range1.getResourceId()));

        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds.stream().map(Long::valueOf).toList(), frequencyDays, 0,
                        new BigDecimal("3"), 1);
        assertNotNull(bucket);
        return bucket.getResourceId();
    }

    private Long createProduct(final Long delinquencyBucketId) {
        final String uniqueName = "WCL Product " + Utils.randomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(delinquencyBucketId).build()).getResourceId();
    }

    private Long submitAndApproveLoan(final Long clientId, final Long productId) {
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(10000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000));
        final Long loanId = applicationHelper.submit(builder.buildSubmitRequest());

        final LocalDate submittedOnDate = FeignCalls
                .ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId))
                .getTimeline().getSubmittedOnDate();
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate));
        return loanId;
    }
}
