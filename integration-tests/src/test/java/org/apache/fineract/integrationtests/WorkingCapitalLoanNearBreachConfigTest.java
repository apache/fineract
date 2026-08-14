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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.NearBreachFrequencyTypeEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachRequest;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalLoanNearBreachActionsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalLoanNearBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanNearBreachConfigTest {

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalLoanNearBreachHelper nearBreachHelper = new WorkingCapitalLoanNearBreachHelper();
    private final WorkingCapitalLoanBreachHelper breachHelper = new WorkingCapitalLoanBreachHelper();
    private final WorkingCapitalLoanNearBreachActionsHelper nearBreachActionsHelper = new WorkingCapitalLoanNearBreachActionsHelper();

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdNearBreachIds = new ArrayList<>();
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
        for (final Long nearBreachId : createdNearBreachIds) {
            if (nearBreachId == null) {
                continue;
            }
            try {
                nearBreachHelper.delete(nearBreachId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdNearBreachIds.clear();
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
    public void testCreateNearBreachActionByIdSucceeds() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);
        final BigDecimal newThreshold = BigDecimal.valueOf(40);
        final Integer newFrequency = 15;
        final String newFrequencyType = "DAYS";

        nearBreachActionsHelper.createNearBreachActionById(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(newThreshold, newFrequency, newFrequencyType));

        final List<WorkingCapitalLoanNearBreachActionData> history = nearBreachActionsHelper.getNearBreachChangeActionsById(loanId);
        assertNotNull(history);
        assertFalse(history.isEmpty());
        final WorkingCapitalLoanNearBreachActionData latest = history.getFirst();
        assertNotNull(latest.getId());
        assertEquals(0, newThreshold.compareTo(latest.getThreshold()));
        assertEquals(newFrequency, latest.getFrequency());
        assertEquals(newFrequencyType, latest.getFrequencyType());
    }

    @Test
    public void testCreateNearBreachActionByExternalIdSucceeds() {
        final String externalId = "wcl-nb-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId, externalId);
        final BigDecimal newThreshold = BigDecimal.valueOf(55);
        final Integer newFrequency = 7;
        final String newFrequencyType = "DAYS";

        nearBreachActionsHelper.createNearBreachActionByExternalId(externalId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(newThreshold, newFrequency, newFrequencyType));

        final List<WorkingCapitalLoanNearBreachActionData> history = nearBreachActionsHelper
                .getNearBreachChangeActionsByExternalId(externalId);
        assertNotNull(history);
        assertFalse(history.isEmpty());
        final WorkingCapitalLoanNearBreachActionData latest = history.getFirst();
        assertEquals(0, newThreshold.compareTo(latest.getThreshold()));
        assertEquals(newFrequency, latest.getFrequency());
        assertEquals(newFrequencyType, latest.getFrequencyType());
    }

    @Test
    public void testMultipleActionsAppendToHistory() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);

        nearBreachActionsHelper.createNearBreachActionById(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(30), 7, "DAYS"));
        nearBreachActionsHelper.createNearBreachActionById(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(45), 14, "DAYS"));
        nearBreachActionsHelper.createNearBreachActionById(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(60), 21, "DAYS"));

        final List<WorkingCapitalLoanNearBreachActionData> history = nearBreachActionsHelper.getNearBreachChangeActionsById(loanId);
        assertThat(history.size()).isGreaterThanOrEqualTo(3);
        assertEquals(21, history.get(0).getFrequency());
        assertEquals(14, history.get(1).getFrequency());
        assertEquals(7, history.get(2).getFrequency());
    }

    @Test
    public void testGetHistoryReturnsEmptyListWhenNoChangesMade() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);

        final List<WorkingCapitalLoanNearBreachActionData> history = nearBreachActionsHelper.getNearBreachChangeActionsById(loanId);
        assertNotNull(history);
        assertThat(history).isEmpty();
    }

    @Test
    public void testCobEvaluatesNearBreachWithUpdatedConfig() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(50), 7, "DAYS");
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdHolder[0] = createActiveLoanWithNearBreach(nearBreachId, null, LocalDate.of(2026, 1, 1));
            nearBreachActionsHelper.createNearBreachActionById(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(1), 14, "DAYS"));
        });
        final Long loanId = loanIdHolder[0];

        BusinessDateHelper.runAt("09 January 2026", () -> {
            FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final List<WorkingCapitalLoanBreachScheduleData> schedule = nearBreachActionsHelper.getBreachSchedule(loanId);
            assertFalse(schedule.isEmpty());
            final WorkingCapitalLoanBreachScheduleData period = schedule.getFirst();
            assertTrue(period.getNearBreach() == null || !Boolean.TRUE.equals(period.getNearBreach()),
                    "Near breach should not be flagged on day 7 after config changed to 14-day frequency");
        });

        BusinessDateHelper.runAt("16 January 2026", () -> {
            FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final List<WorkingCapitalLoanBreachScheduleData> schedule = nearBreachActionsHelper.getBreachSchedule(loanId);
            assertFalse(schedule.isEmpty());
            final WorkingCapitalLoanBreachScheduleData period = schedule.getFirst();
            assertEquals(Boolean.TRUE, period.getNearBreach(),
                    "Near breach should be flagged on the 14-day evaluation point with no payment");
        });
    }

    @Test
    public void testCreateNearBreachActionWithMissingThresholdFails() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);
        final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request = new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .action(PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.ActionEnum.RESCHEDULE).nearBreachFrequency(7)
                .nearBreachFrequencyType(NearBreachFrequencyTypeEnum.DAYS).locale("en");

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId, request);
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testCreateNearBreachActionWithThresholdOver100Fails() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(101), 7, "DAYS"));
        assertEquals(400, ex.getStatus());
        assertThat(ex.getDeveloperMessage()).contains("must.not.exceed.100.percent");
    }

    @Test
    public void testCreateNearBreachActionWithMissingFrequencyFails() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);
        final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request = new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .action(PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.ActionEnum.RESCHEDULE)
                .nearBreachThreshold(BigDecimal.valueOf(40)).nearBreachFrequencyType(NearBreachFrequencyTypeEnum.DAYS).locale("en");

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId, request);
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testCreateNearBreachActionWithMissingActionFails() {
        final Long nearBreachId = createNearBreachTemplate(BigDecimal.valueOf(20), 7, "DAYS");
        final Long loanId = createActiveLoanWithNearBreach(nearBreachId);
        final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request = new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .nearBreachThreshold(BigDecimal.valueOf(40)).nearBreachFrequency(7)
                .nearBreachFrequencyType(NearBreachFrequencyTypeEnum.DAYS).locale("en");

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId, request);
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testCreateNearBreachActionOnLoanWithoutNearBreachConfigFails() {
        final Long loanId = createActiveLoan();

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(40), 7, "DAYS"));
        assertEquals(400, ex.getStatus());
        assertThat(ex.getDeveloperMessage()).contains("near.breach.action.not.allowed.loan.has.no.near.breach.configuration");
    }

    @Test
    public void testCreateNearBreachActionOnPendingLoanFails() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).buildSubmitRequest());

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(40), 7, "DAYS"));
        assertEquals(400, ex.getStatus());
        assertThat(ex.getDeveloperMessage()).contains("near.breach.action.not.allowed.for.non.active.loan");
    }

    @Test
    public void testCreateNearBreachActionOnApprovedLoanFails() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).buildSubmitRequest());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(Utils.getLocalDateOfTenant(), BigDecimal.valueOf(5000), null));

        final CallFailedRuntimeException ex = nearBreachActionsHelper.createNearBreachActionByIdExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.createNearBreachRescheduleAction(BigDecimal.valueOf(40), 7, "DAYS"));
        assertEquals(400, ex.getStatus());
        assertThat(ex.getDeveloperMessage()).contains("near.breach.action.not.allowed.for.non.active.loan");
    }

    private Long createActiveLoan() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).buildSubmitRequest());
        final LocalDate today = Utils.getLocalDateOfTenant();
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));
        return loanId;
    }

    private Long createActiveLoanWithNearBreach(final Long nearBreachId) {
        return createActiveLoanWithNearBreach(nearBreachId, null);
    }

    private Long createActiveLoanWithNearBreach(final Long nearBreachId, final String externalId) {
        final LocalDate today = Utils.getLocalDateOfTenant();
        return createActiveLoanWithNearBreach(nearBreachId, externalId, today);
    }

    private Long createActiveLoanWithNearBreach(final Long nearBreachId, final String externalId,
            final LocalDate approvalAndDisbursementDate) {
        final Long productId = createProductWithNearBreach(nearBreachId);
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder()
                .withClientId(createdClientId).withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000));
        if (externalId != null) {
            builder.withExternalId(externalId);
        }
        final Long loanId = submitAndTrack(builder.buildSubmitRequest());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000)));
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL NB Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithNearBreach(final Long nearBreachId) {
        final String uniqueName = "WCL NB Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long breachId = breachHelper
                .create(new WorkingCapitalLoanBreachRequest().name(Utils.randomStringGenerator("Breach", 12)).breachFrequency(60)
                        .breachFrequencyType("DAYS").breachAmountCalculationType("PERCENTAGE").breachAmount(BigDecimal.valueOf(10)));
        createdBreachIds.add(breachId);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withBreachId(breachId).withNearBreachId(nearBreachId).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createNearBreachTemplate(final BigDecimal threshold, final Integer frequency, final String frequencyType) {
        final String name = Utils.randomStringGenerator("NearBreach", 12);
        final Long id = nearBreachHelper.create(new WorkingCapitalLoanNearBreachRequest().nearBreachName(name)
                .nearBreachThreshold(threshold).nearBreachFrequency(frequency).nearBreachFrequencyType(frequencyType)).getResourceId();
        createdNearBreachIds.add(id);
        return id;
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest request) {
        final Long loanId = loanHelper.submit(request);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
