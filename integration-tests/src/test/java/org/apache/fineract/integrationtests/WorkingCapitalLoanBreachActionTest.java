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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanBreachActionTest {

    private static final String WC_BREACH_PAUSE_EVENT = "WorkingCapitalLoanBreachPauseBusinessEvent";

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
    private final WorkingCapitalLoanBreachActionHelper breachActionHelper = new WorkingCapitalLoanBreachActionHelper();
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdBreachIds = new ArrayList<>();
    private final Long createdClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

    @AfterEach
    void cleanup() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdProductIds.clear();
        for (final Long breachId : createdBreachIds) {
            if (breachId == null) {
                continue;
            }
            try {
                breachHelper.delete(breachId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdBreachIds.clear();
    }

    @Test
    public void testPauseCreatesBreachActionWithNoEffectiveEndDate() {
        BusinessDateHelper.runAt("01 July 2026", () -> {
            final Long loanId = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            breachActionHelper.pause(loanId, "2026-07-01", "2026-07-10");

            final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanId);
            assertEquals(1, actions.size());

            final WorkingCapitalLoanBreachActionData pause = actions.getFirst();
            assertNotNull(pause.getAction());
            assertEquals(WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE, pause.getAction());
            assertEquals(LocalDate.of(2026, Month.JULY, 1), pause.getStartDate());
            assertEquals(LocalDate.of(2026, Month.JULY, 10), pause.getEndDate());
            assertNull(pause.getEffectiveEndDate(), "effectiveEndDate must be null when the pause has not been resumed");
        });
    }

    @Test
    public void testPausePublishesExternalBusinessEvent() {
        externalEventHelper.enableBusinessEvent(WC_BREACH_PAUSE_EVENT);
        try {
            BusinessDateHelper.runAt("01 July 2026", () -> {
                final Long loanId = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

                externalEventHelper.deleteAllExternalEvents();
                breachActionHelper.pause(loanId, "2026-07-01", "2026-07-10");

                final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_BREACH_PAUSE_EVENT);
                final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(e.getAggregateRootId())).findFirst()
                        .orElse(null);
                assertNotNull(event, "Expected breach pause external event for loan");
                assertEquals(WC_BREACH_PAUSE_EVENT, event.getType());
                assertEquals(loanId, event.getAggregateRootId());
            });
        } finally {
            externalEventHelper.disableBusinessEvent(WC_BREACH_PAUSE_EVENT);
        }
    }

    @Test
    public void testResumeCreatesResumeActionAndSetsEffectiveEndDateOnPause() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            breachActionHelper.pause(loanIdHolder[0], "2026-07-01", "2026-07-10");
        });

        BusinessDateHelper.runAt("05 July 2026", () -> {
            breachActionHelper.resume(loanIdHolder[0], "2026-07-05");

            final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanIdHolder[0]);
            assertEquals(2, actions.size());

            final WorkingCapitalLoanBreachActionData pause = actions.stream()
                    .filter(a -> WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE == a.getAction()).findFirst().orElseThrow();
            final WorkingCapitalLoanBreachActionData resume = actions.stream()
                    .filter(a -> WorkingCapitalLoanBreachActionData.ActionEnum.RESUME == a.getAction()).findFirst().orElseThrow();

            assertEquals(LocalDate.of(2026, Month.JULY, 5), resume.getStartDate());
            assertNull(resume.getEffectiveEndDate(), "RESUME rows should never have an effectiveEndDate");

            assertNotNull(pause.getEffectiveEndDate(), "effectiveEndDate must be set on the pause after a resume");
            assertEquals(LocalDate.of(2026, Month.JULY, 5), pause.getEffectiveEndDate());

            assertEquals(LocalDate.of(2026, Month.JULY, 10), pause.getEndDate());
        });
    }

    @Test
    public void testPauseWithoutResumeHasNullEffectiveEndDate() {
        BusinessDateHelper.runAt("01 July 2026", () -> {
            final Long loanId = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            breachActionHelper.pause(loanId, "2026-07-01", "2026-07-15");

            final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanId);
            assertEquals(1, actions.size());
            assertNull(actions.getFirst().getEffectiveEndDate());
        });
    }

    @Test
    public void testResumeOutsidePauseWindowDoesNotSetEffectiveEndDate() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            breachActionHelper.pause(loanIdHolder[0], "2026-07-01", "2026-07-10");
        });

        BusinessDateHelper.runAt("20 July 2026", () -> {
            CallFailedRuntimeException ex = breachActionHelper.resumeExpectingFailure(loanIdHolder[0], "2026-07-20");
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("Resume breach action can only be created during an active pause"));

            final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanIdHolder[0]);
            final WorkingCapitalLoanBreachActionData pause = actions.stream()
                    .filter(a -> WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE == a.getAction()).findFirst().orElseThrow();

            assertNull(pause.getEffectiveEndDate(),
                    "effectiveEndDate must remain null when the resume date falls outside the pause window");
        });
    }

    @Test
    public void testMultiplePausesEachGetCorrectEffectiveEndDate() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 July 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, Month.JULY, 1));

            breachActionHelper.pause(loanIdHolder[0], "2026-07-01", "2026-07-10");
            breachActionHelper.pause(loanIdHolder[0], "2026-07-15", "2026-07-25");
        });

        BusinessDateHelper.runAt("05 July 2026", () -> {
            breachActionHelper.resume(loanIdHolder[0], "2026-07-05");

            final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanIdHolder[0]);

            final WorkingCapitalLoanBreachActionData firstPause = actions.stream()
                    .filter(a -> WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE == a.getAction()
                            && LocalDate.of(2026, Month.JULY, 1).equals(a.getStartDate()))
                    .findFirst().orElseThrow();
            final WorkingCapitalLoanBreachActionData secondPause = actions.stream()
                    .filter(a -> WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE == a.getAction()
                            && LocalDate.of(2026, Month.JULY, 15).equals(a.getStartDate()))
                    .findFirst().orElseThrow();

            assertEquals(LocalDate.of(2026, Month.JULY, 5), firstPause.getEffectiveEndDate(),
                    "first pause effectiveEndDate should match the resume date");
            assertNull(secondPause.getEffectiveEndDate(), "second pause was not resumed and must have no effectiveEndDate");
        });
    }

    // --- helpers ---

    private Long createActiveLoan(final LocalDate approvalAndDisbursementDate) {
        final Long productId = createProductWithBreach();
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder()
                .withClientId(createdClientId).withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000));

        final Long loanId = submitAndTrack(builder.buildSubmitRequest());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000)));
        return loanId;
    }

    private Long createProductWithBreach() {
        final String uniqueName = "WCL Breach Test " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long breachId = breachHelper
                .create(new WorkingCapitalBreachRequest().name(Utils.randomStringGenerator("Breach", 12)).breachFrequency(60)
                        .breachFrequencyType("DAYS").breachAmountCalculationType("PERCENTAGE").breachAmount(BigDecimal.valueOf(10)));
        createdBreachIds.add(breachId);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withBreachId(breachId).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long submitAndTrack(final org.apache.fineract.client.models.PostWorkingCapitalLoansRequest request) {
        final Long loanId = loanHelper.submit(request);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
