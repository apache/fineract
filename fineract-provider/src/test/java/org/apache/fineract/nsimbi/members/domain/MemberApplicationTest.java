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
package org.apache.fineract.nsimbi.members.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.nsimbi.members.exception.InvalidMemberApplicationStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberApplicationTest {

    private static final Long MAKER = 10L;
    private static final Long CHECKER = 20L;
    private static final OffsetDateTime CREATED = OffsetDateTime.parse("2026-09-16T09:00:00+03:00");
    private static final OffsetDateTime SUBMITTED = CREATED.plusHours(1);
    private static final OffsetDateTime REVIEWED = CREATED.plusHours(2);
    private static final OffsetDateTime DECIDED = CREATED.plusHours(3);

    @Test
    void directApplicationStartsInDraft() {
        MemberApplication application = draft();
        assertEquals(1L, application.getClientId());
        assertEquals(MemberApplicationChannel.DIRECT, application.getChannel());
        assertEquals(MAKER, application.getMakerAppUserId());
        assertEquals(MemberApplicationWorkflowStatus.DRAFT, application.getWorkflowStatus());
        assertEquals(CREATED, application.getCreatedOn());
        assertNull(application.getAgencyReference());
        assertNull(application.getCheckerAppUserId());
        assertNull(application.getSubmittedOn());
        assertNull(application.getReviewStartedOn());
        assertNull(application.getDecidedOn());
        assertNull(application.getDecisionReason());
        assertNull(application.getLastModifiedOn());
    }

    @Test
    void agencyApplicationRecordsReference() {
        MemberApplication application = new MemberApplication(1L, MemberApplicationChannel.AGENCY, MAKER, "AGENCY-001", CREATED);
        assertEquals(MemberApplicationChannel.AGENCY, application.getChannel());
        assertEquals("AGENCY-001", application.getAgencyReference());
        assertEquals(MemberApplicationWorkflowStatus.DRAFT, application.getWorkflowStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t\n" })
    void agencyRequiresNonblankReference(String reference) {
        assertThrows(IllegalArgumentException.class,
                () -> new MemberApplication(1L, MemberApplicationChannel.AGENCY, MAKER, reference, CREATED));
    }

    @ParameterizedTest
    @ValueSource(strings = { "AGENCY-001", "", " ", "\t" })
    void directRejectsAnySuppliedReference(String reference) {
        assertThrows(IllegalArgumentException.class,
                () -> new MemberApplication(1L, MemberApplicationChannel.DIRECT, MAKER, reference, CREATED));
    }

    @Test
    void constructorRequiresClientChannelMakerAndCreatedOn() {
        assertThrows(NullPointerException.class, () -> new MemberApplication(null, MemberApplicationChannel.DIRECT, MAKER, null, CREATED));
        assertThrows(NullPointerException.class, () -> new MemberApplication(1L, null, MAKER, null, CREATED));
        assertThrows(NullPointerException.class, () -> new MemberApplication(1L, MemberApplicationChannel.DIRECT, null, null, CREATED));
        assertThrows(NullPointerException.class, () -> new MemberApplication(1L, MemberApplicationChannel.DIRECT, MAKER, null, null));
    }

    @Test
    void draftCanBeSubmitted() {
        MemberApplication application = draft();
        application.submit(SUBMITTED);
        assertEquals(MemberApplicationWorkflowStatus.SUBMITTED, application.getWorkflowStatus());
        assertEquals(SUBMITTED, application.getSubmittedOn());
        assertEquals(SUBMITTED, application.getLastModifiedOn());
        assertEquals(CREATED, application.getCreatedOn());
    }

    @Test
    void submittedApplicationCanEnterReview() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.SUBMITTED);
        application.startReview(CHECKER, REVIEWED);
        assertEquals(MemberApplicationWorkflowStatus.UNDER_REVIEW, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertEquals(REVIEWED, application.getReviewStartedOn());
        assertEquals(REVIEWED, application.getLastModifiedOn());
        assertEquals(SUBMITTED, application.getSubmittedOn());
    }

    @Test
    void reviewedApplicationCanBeApproved() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        application.approve(CHECKER, DECIDED);
        assertEquals(MemberApplicationWorkflowStatus.APPROVED, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertEquals(DECIDED, application.getDecidedOn());
        assertEquals(DECIDED, application.getLastModifiedOn());
        assertEquals(REVIEWED, application.getReviewStartedOn());
        assertNull(application.getDecisionReason());
    }

    @Test
    void reviewedApplicationCanBeRejected() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        application.reject(CHECKER, "Not eligible", DECIDED);
        assertEquals(MemberApplicationWorkflowStatus.REJECTED, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertEquals("Not eligible", application.getDecisionReason());
        assertEquals(DECIDED, application.getDecidedOn());
        assertEquals(DECIDED, application.getLastModifiedOn());
    }

    @Test
    void reviewedApplicationCanBeReturnedForCorrection() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        application.returnForCorrection(CHECKER, "Correct applicant details", DECIDED);
        assertEquals(MemberApplicationWorkflowStatus.RETURNED_FOR_CORRECTION, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertEquals("Correct applicant details", application.getDecisionReason());
        assertEquals(DECIDED, application.getLastModifiedOn());
        assertNull(application.getDecidedOn());
    }

    @Test
    void resubmissionClearsPreviousReviewAndCorrectionFields() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.RETURNED_FOR_CORRECTION);
        OffsetDateTime resubmitted = DECIDED.plusHours(1);
        application.submit(resubmitted);
        assertEquals(MemberApplicationWorkflowStatus.SUBMITTED, application.getWorkflowStatus());
        assertEquals(resubmitted, application.getSubmittedOn());
        assertEquals(resubmitted, application.getLastModifiedOn());
        assertNull(application.getCheckerAppUserId());
        assertNull(application.getReviewStartedOn());
        assertNull(application.getDecisionReason());
        assertNull(application.getDecidedOn());
        application.startReview(CHECKER, resubmitted.plusHours(1));
        application.approve(CHECKER, resubmitted.plusHours(2));
        assertEquals(MemberApplicationWorkflowStatus.APPROVED, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertNull(application.getDecisionReason());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t\n" })
    void rejectionRequiresReasonWithoutMutatingApplication(String reason) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, "reject.reason.required", () -> application.reject(CHECKER, reason, DECIDED));
        assertReviewUnchanged(application);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t\n" })
    void correctionRequiresReasonWithoutMutatingApplication(String reason) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, "return.for.correction.reason.required",
                () -> application.returnForCorrection(CHECKER, reason, DECIDED));
        assertReviewUnchanged(application);
    }

    @Test
    void makerCannotReviewOwnApplication() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.SUBMITTED);
        assertCodeUnchanged(application, "start.review.maker.cannot.be.checker", () -> application.startReview(MAKER, REVIEWED));
        assertEquals(MemberApplicationWorkflowStatus.SUBMITTED, application.getWorkflowStatus());
        assertNull(application.getCheckerAppUserId());
        assertNull(application.getReviewStartedOn());
        assertEquals(SUBMITTED, application.getLastModifiedOn());
    }

    @Test
    void makerCannotApproveRejectOrReturnApplication() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, "approve.maker.cannot.be.checker", () -> application.approve(MAKER, DECIDED));
        assertCodeUnchanged(application, "reject.maker.cannot.be.checker", () -> application.reject(MAKER, "Reason", DECIDED));
        assertCodeUnchanged(application, "return.for.correction.maker.cannot.be.checker",
                () -> application.returnForCorrection(MAKER, "Reason", DECIDED));
        assertReviewUnchanged(application);
    }

    @Test
    void checkerIsRequiredForAllReviewActions() {
        MemberApplication submitted = atState(MemberApplicationWorkflowStatus.SUBMITTED);
        assertCodeUnchanged(submitted, "start.review.checker.required", () -> submitted.startReview(null, REVIEWED));
        MemberApplication reviewed = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(reviewed, "approve.checker.required", () -> reviewed.approve(null, DECIDED));
        assertCodeUnchanged(reviewed, "reject.checker.required", () -> reviewed.reject(null, "Reason", DECIDED));
        assertCodeUnchanged(reviewed, "return.for.correction.checker.required",
                () -> reviewed.returnForCorrection(null, "Reason", DECIDED));
        assertReviewUnchanged(reviewed);
    }

    @Test
    void allTransitionsRequireTimestampsWithoutMutatingApplication() {
        MemberApplication draft = draft();
        assertCodeUnchanged(draft, "submit.timestamp.required", () -> draft.submit(null));
        assertCodeUnchanged(draft, "withdraw.timestamp.required", () -> draft.withdraw(null));
        assertEquals(MemberApplicationWorkflowStatus.DRAFT, draft.getWorkflowStatus());
        assertNull(draft.getSubmittedOn());
        assertNull(draft.getDecidedOn());
        assertNull(draft.getLastModifiedOn());
        MemberApplication submitted = atState(MemberApplicationWorkflowStatus.SUBMITTED);
        assertCodeUnchanged(submitted, "start.review.timestamp.required", () -> submitted.startReview(CHECKER, null));
        assertNull(submitted.getCheckerAppUserId());
        assertNull(submitted.getReviewStartedOn());
        MemberApplication reviewed = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(reviewed, "approve.timestamp.required", () -> reviewed.approve(CHECKER, null));
        assertCodeUnchanged(reviewed, "reject.timestamp.required", () -> reviewed.reject(CHECKER, "Reason", null));
        assertCodeUnchanged(reviewed, "return.for.correction.timestamp.required",
                () -> reviewed.returnForCorrection(CHECKER, "Reason", null));
        assertReviewUnchanged(reviewed);
    }

    @ParameterizedTest
    @EnumSource(value = MemberApplicationWorkflowStatus.class, names = { "DRAFT", "SUBMITTED", "UNDER_REVIEW", "RETURNED_FOR_CORRECTION" })
    void anyNonterminalApplicationCanBeWithdrawn(MemberApplicationWorkflowStatus status) {
        MemberApplication application = atState(status);
        assertFalse(status.isTerminal());
        application.withdraw(DECIDED.plusHours(1));
        assertEquals(MemberApplicationWorkflowStatus.WITHDRAWN, application.getWorkflowStatus());
        assertEquals(DECIDED.plusHours(1), application.getDecidedOn());
        assertEquals(DECIDED.plusHours(1), application.getLastModifiedOn());
    }

    @ParameterizedTest
    @EnumSource(value = MemberApplicationWorkflowStatus.class, names = { "APPROVED", "REJECTED", "WITHDRAWN" })
    void terminalApplicationsRejectEveryTransition(MemberApplicationWorkflowStatus status) {
        MemberApplication application = atState(status);
        assertTrue(status.isTerminal());
        assertCodeUnchanged(application, "submit.invalid.state", () -> application.submit(DECIDED));
        assertCodeUnchanged(application, "start.review.invalid.state", () -> application.startReview(CHECKER, DECIDED));
        assertCodeUnchanged(application, "approve.invalid.state", () -> application.approve(CHECKER, DECIDED));
        assertCodeUnchanged(application, "reject.invalid.state", () -> application.reject(CHECKER, "Reason", DECIDED));
        assertCodeUnchanged(application, "return.for.correction.invalid.state",
                () -> application.returnForCorrection(CHECKER, "Reason", DECIDED));
        assertCodeUnchanged(application, "withdraw.invalid.state", () -> application.withdraw(DECIDED));
        assertEquals(status, application.getWorkflowStatus());
        assertEquals(DECIDED, application.getDecidedOn());
        assertEquals(DECIDED, application.getLastModifiedOn());
    }

    @ParameterizedTest
    @EnumSource(value = MemberApplicationWorkflowStatus.class, names = { "DRAFT", "SUBMITTED", "UNDER_REVIEW", "RETURNED_FOR_CORRECTION" })
    void invalidNonterminalTransitionsUseDomainException(MemberApplicationWorkflowStatus status) {
        MemberApplication application = atState(status);
        if (status != MemberApplicationWorkflowStatus.UNDER_REVIEW) {
            assertCodeUnchanged(application, "approve.invalid.state", () -> application.approve(CHECKER, DECIDED));
            assertCodeUnchanged(application, "reject.invalid.state", () -> application.reject(CHECKER, "Reason", DECIDED));
            assertCodeUnchanged(application, "return.for.correction.invalid.state",
                    () -> application.returnForCorrection(CHECKER, "Reason", DECIDED));
        }
        if (status != MemberApplicationWorkflowStatus.SUBMITTED) {
            assertCodeUnchanged(application, "start.review.invalid.state", () -> application.startReview(CHECKER, REVIEWED));
        }
        if (status == MemberApplicationWorkflowStatus.SUBMITTED || status == MemberApplicationWorkflowStatus.UNDER_REVIEW) {
            assertCodeUnchanged(application, "submit.invalid.state", () -> application.submit(SUBMITTED));
        }
        assertEquals(status, application.getWorkflowStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = { "approve", "reject", "return.for.correction" })
    void differentCheckerCannotDecideApplication(String action) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, action + ".checker.not.assigned", () -> decide(application, action, 30L, "Reason", DECIDED));
    }

    @Test
    void submissionCannotPrecedeCreation() {
        MemberApplication application = draft();
        assertCodeUnchanged(application, "submit.timestamp.before.previous.transition", () -> application.submit(CREATED.minusNanos(1)));
    }

    @Test
    void reviewCannotPrecedeSubmission() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.SUBMITTED);
        assertCodeUnchanged(application, "start.review.timestamp.before.previous.transition",
                () -> application.startReview(CHECKER, SUBMITTED.minusNanos(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "approve", "reject", "return.for.correction" })
    void decisionCannotPrecedeReview(String action) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, action + ".timestamp.before.previous.transition",
                () -> decide(application, action, CHECKER, "Reason", REVIEWED.minusNanos(1)));
    }

    @Test
    void resubmissionCannotPrecedeCorrectionDecision() {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.RETURNED_FOR_CORRECTION);
        assertCodeUnchanged(application, "submit.timestamp.before.previous.transition", () -> application.submit(DECIDED.minusNanos(1)));
    }

    @ParameterizedTest
    @EnumSource(value = MemberApplicationWorkflowStatus.class, names = { "DRAFT", "SUBMITTED", "UNDER_REVIEW", "RETURNED_FOR_CORRECTION" })
    void withdrawalCannotPrecedeLatestWorkflowTimestamp(MemberApplicationWorkflowStatus status) {
        MemberApplication application = atState(status);
        OffsetDateTime latest = switch (status) {
            case DRAFT -> CREATED;
            case SUBMITTED -> SUBMITTED;
            case UNDER_REVIEW -> REVIEWED;
            case RETURNED_FOR_CORRECTION -> DECIDED;
            default -> throw new AssertionError("Unexpected state: " + status);
        };
        assertCodeUnchanged(application, "withdraw.timestamp.before.previous.transition", () -> application.withdraw(latest.minusNanos(1)));
    }

    @ParameterizedTest
    @EnumSource(value = MemberApplicationWorkflowStatus.class, names = { "DRAFT", "SUBMITTED", "UNDER_REVIEW", "RETURNED_FOR_CORRECTION" })
    void withdrawalAcceptsLatestWorkflowTimestamp(MemberApplicationWorkflowStatus status) {
        MemberApplication application = atState(status);
        OffsetDateTime latest = application.getLastModifiedOn() == null ? CREATED : application.getLastModifiedOn();
        application.withdraw(latest);
        assertEquals(MemberApplicationWorkflowStatus.WITHDRAWN, application.getWorkflowStatus());
        assertEquals(latest, application.getDecidedOn());
        assertEquals(latest, application.getLastModifiedOn());
    }

    @ParameterizedTest
    @ValueSource(strings = { "approve", "reject", "return.for.correction" })
    void transitionsAcceptEqualInstantsWithDifferentOffsets(String action) {
        MemberApplication application = draft();
        OffsetDateTime sameInstant = CREATED.withOffsetSameInstant(ZoneOffset.UTC);
        application.submit(sameInstant);
        application.startReview(CHECKER, CREATED);
        decide(application, action, CHECKER, "Reason", sameInstant);
        assertEquals(sameInstant, application.getLastModifiedOn());
        if ("return.for.correction".equals(action)) {
            application.submit(CREATED);
            assertEquals(MemberApplicationWorkflowStatus.SUBMITTED, application.getWorkflowStatus());
            assertEquals(CREATED, application.getSubmittedOn());
        } else {
            assertEquals(sameInstant, application.getDecidedOn());
        }
    }

    @Test
    void chronologyComparesInstantsRatherThanLocalTimes() {
        MemberApplication application = draft();
        OffsetDateTime earlierInstant = CREATED.minusMinutes(1).withOffsetSameInstant(ZoneOffset.ofHours(4));
        assertTrue(earlierInstant.toLocalDateTime().isAfter(CREATED.toLocalDateTime()));
        assertCodeUnchanged(application, "submit.timestamp.before.previous.transition", () -> application.submit(earlierInstant));
    }

    @Test
    void agencyReferenceIsTrimmedBeforeStorage() {
        MemberApplication application = new MemberApplication(1L, MemberApplicationChannel.AGENCY, MAKER, " \tAGENCY-001\n ", CREATED);
        assertEquals("AGENCY-001", application.getAgencyReference());
    }

    @Test
    void agencyReferenceAcceptsExactMaximumLength() {
        String reference = "A".repeat(100);
        MemberApplication application = new MemberApplication(1L, MemberApplicationChannel.AGENCY, MAKER, reference, CREATED);
        assertEquals(reference, application.getAgencyReference());
    }

    @ParameterizedTest
    @ValueSource(strings = { "A", " " })
    void agencyReferenceRejectsOversizedInputEvenWithTrimmableWhitespace(String extra) {
        assertThrows(IllegalArgumentException.class,
                () -> new MemberApplication(1L, MemberApplicationChannel.AGENCY, MAKER, "A".repeat(100) + extra, CREATED));
    }

    @ParameterizedTest
    @ValueSource(strings = { "reject", "return.for.correction" })
    void decisionReasonIsTrimmedBeforeStorage(String action) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        decide(application, action, CHECKER, " \tCorrect applicant details\n ", DECIDED);
        assertEquals("Correct applicant details", application.getDecisionReason());
    }

    @ParameterizedTest
    @ValueSource(strings = { "reject", "return.for.correction" })
    void decisionReasonAcceptsExactMaximumLength(String action) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        String reason = "R".repeat(500);
        decide(application, action, CHECKER, reason, DECIDED);
        assertEquals(reason, application.getDecisionReason());
    }

    @ParameterizedTest
    @ValueSource(strings = { "reject", "return.for.correction" })
    void decisionReasonRejectsOversizedInputWithoutChangingApplication(String action) {
        MemberApplication application = atState(MemberApplicationWorkflowStatus.UNDER_REVIEW);
        assertCodeUnchanged(application, action + ".reason.too.long", () -> decide(application, action, CHECKER, "R".repeat(501), DECIDED));
        assertCodeUnchanged(application, action + ".reason.too.long",
                () -> decide(application, action, CHECKER, "R".repeat(500) + " ", DECIDED));
    }

    private static void decide(MemberApplication application, String action, Long checker, String reason, OffsetDateTime at) {
        switch (action) {
            case "approve" -> application.approve(checker, at);
            case "reject" -> application.reject(checker, reason, at);
            case "return.for.correction" -> application.returnForCorrection(checker, reason, at);
            default -> throw new AssertionError("Unexpected action: " + action);
        }
    }

    private static void assertCodeUnchanged(MemberApplication application, String suffix,
            org.junit.jupiter.api.function.Executable action) {
        List<Object> before = snapshot(application);
        assertCode(suffix, action);
        assertEquals(before, snapshot(application), "A rejected operation must leave every entity field unchanged");
    }

    private static List<Object> snapshot(MemberApplication application) {
        return Arrays.asList(application.getId(), application.getClientId(), application.getChannel(), application.getWorkflowStatus(),
                application.getMakerAppUserId(), application.getCheckerAppUserId(), application.getAgencyReference(),
                application.getSubmittedOn(), application.getReviewStartedOn(), application.getDecidedOn(), application.getDecisionReason(),
                application.getVersion(), application.getCreatedOn(), application.getLastModifiedOn());
    }

    private static MemberApplication draft() {
        return new MemberApplication(1L, MemberApplicationChannel.DIRECT, MAKER, null, CREATED);
    }

    private static MemberApplication atState(MemberApplicationWorkflowStatus status) {
        MemberApplication application = draft();
        if (status == MemberApplicationWorkflowStatus.DRAFT) {
            return application;
        }
        if (status == MemberApplicationWorkflowStatus.WITHDRAWN) {
            application.withdraw(DECIDED);
            return application;
        }
        application.submit(SUBMITTED);
        if (status == MemberApplicationWorkflowStatus.SUBMITTED) {
            return application;
        }
        application.startReview(CHECKER, REVIEWED);
        switch (status) {
            case APPROVED -> application.approve(CHECKER, DECIDED);
            case REJECTED -> application.reject(CHECKER, "Not eligible", DECIDED);
            case RETURNED_FOR_CORRECTION -> application.returnForCorrection(CHECKER, "Correct details", DECIDED);
            default -> {
            }
        }
        return application;
    }

    private static void assertCode(String suffix, org.junit.jupiter.api.function.Executable action) {
        InvalidMemberApplicationStateTransitionException exception = assertThrows(InvalidMemberApplicationStateTransitionException.class,
                action);
        assertEquals("error.msg.nsimbi.member.application." + suffix, exception.getGlobalisationMessageCode());
    }

    private static void assertReviewUnchanged(MemberApplication application) {
        assertEquals(MemberApplicationWorkflowStatus.UNDER_REVIEW, application.getWorkflowStatus());
        assertEquals(CHECKER, application.getCheckerAppUserId());
        assertEquals(REVIEWED, application.getReviewStartedOn());
        assertEquals(REVIEWED, application.getLastModifiedOn());
        assertNull(application.getDecisionReason());
        assertNull(application.getDecidedOn());
    }
}
