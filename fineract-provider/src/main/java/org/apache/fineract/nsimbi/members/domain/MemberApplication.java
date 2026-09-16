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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;
import org.apache.fineract.nsimbi.members.exception.InvalidMemberApplicationStateTransitionException;

@Entity
@Table(name = "nsimbi_member_application")
@Getter
public class MemberApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private MemberApplicationChannel channel;
    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", nullable = false, length = 40)
    private MemberApplicationWorkflowStatus workflowStatus;
    @Column(name = "maker_appuser_id", nullable = false)
    private Long makerAppUserId;
    @Column(name = "checker_appuser_id")
    private Long checkerAppUserId;
    @Column(name = "agency_reference", length = 100)
    private String agencyReference;
    @Column(name = "submitted_on")
    private OffsetDateTime submittedOn;
    @Column(name = "review_started_on")
    private OffsetDateTime reviewStartedOn;
    @Column(name = "decided_on")
    private OffsetDateTime decidedOn;
    @Column(name = "decision_reason", length = 500)
    private String decisionReason;
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    @Column(name = "last_modified_on")
    private OffsetDateTime lastModifiedOn;

    protected MemberApplication() {}

    public MemberApplication(Long clientId, MemberApplicationChannel channel, Long makerAppUserId, String agencyReference,
            OffsetDateTime createdOn) {
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        this.makerAppUserId = Objects.requireNonNull(makerAppUserId, "makerAppUserId must not be null");
        this.createdOn = Objects.requireNonNull(createdOn, "createdOn must not be null");
        if (channel == MemberApplicationChannel.AGENCY && (agencyReference == null || agencyReference.isBlank())) {
            throw new IllegalArgumentException("AGENCY requires a nonblank agencyReference");
        }
        if (channel == MemberApplicationChannel.DIRECT && agencyReference != null) {
            throw new IllegalArgumentException("DIRECT must not have an agencyReference");
        }
        if (agencyReference != null && agencyReference.length() > 100) {
            throw new IllegalArgumentException("agencyReference must not exceed 100 characters");
        }
        this.agencyReference = agencyReference == null ? null : agencyReference.strip();
        this.workflowStatus = MemberApplicationWorkflowStatus.DRAFT;
    }

    public void submit(OffsetDateTime at) {
        requireState("submit", MemberApplicationWorkflowStatus.DRAFT, MemberApplicationWorkflowStatus.RETURNED_FOR_CORRECTION);
        requireTimestamp("submit", at);
        submittedOn = at;
        checkerAppUserId = null;
        reviewStartedOn = null;
        decidedOn = null;
        decisionReason = null;
        transitionTo(MemberApplicationWorkflowStatus.SUBMITTED, at);
    }

    public void startReview(Long checkerAppUserId, OffsetDateTime at) {
        requireState("start.review", MemberApplicationWorkflowStatus.SUBMITTED);
        requireChecker("start.review", checkerAppUserId);
        requireTimestamp("start.review", at);
        this.checkerAppUserId = checkerAppUserId;
        reviewStartedOn = at;
        transitionTo(MemberApplicationWorkflowStatus.UNDER_REVIEW, at);
    }

    public void returnForCorrection(Long checkerAppUserId, String reason, OffsetDateTime at) {
        requireReviewAction("return.for.correction", checkerAppUserId, at);
        String normalizedReason = validateReason("return.for.correction", reason);
        decisionReason = normalizedReason;
        transitionTo(MemberApplicationWorkflowStatus.RETURNED_FOR_CORRECTION, at);
    }

    public void approve(Long checkerAppUserId, OffsetDateTime at) {
        requireReviewAction("approve", checkerAppUserId, at);
        decidedOn = at;
        transitionTo(MemberApplicationWorkflowStatus.APPROVED, at);
    }

    public void reject(Long checkerAppUserId, String reason, OffsetDateTime at) {
        requireReviewAction("reject", checkerAppUserId, at);
        String normalizedReason = validateReason("reject", reason);
        decisionReason = normalizedReason;
        decidedOn = at;
        transitionTo(MemberApplicationWorkflowStatus.REJECTED, at);
    }

    public void withdraw(OffsetDateTime at) {
        if (workflowStatus.isTerminal()) {
            throw invalid("withdraw", "invalid.state", "Terminal member applications cannot be withdrawn.");
        }
        requireTimestamp("withdraw", at);
        decidedOn = at;
        transitionTo(MemberApplicationWorkflowStatus.WITHDRAWN, at);
    }

    private void requireReviewAction(String action, Long checker, OffsetDateTime at) {
        requireState(action, MemberApplicationWorkflowStatus.UNDER_REVIEW);
        requireAssignedChecker(action, checker);
        requireTimestamp(action, at);
    }

    private void requireAssignedChecker(String action, Long actingCheckerAppUserId) {
        requireChecker(action, actingCheckerAppUserId);
        if (!actingCheckerAppUserId.equals(checkerAppUserId)) {
            throw invalid(action, "checker.not.assigned", "Only the assigned checker may decide this member application.");
        }
    }

    private void requireState(String action, MemberApplicationWorkflowStatus... allowed) {
        for (MemberApplicationWorkflowStatus status : allowed) {
            if (workflowStatus == status) {
                return;
            }
        }
        throw invalid(action, "invalid.state", "Action is not allowed in the current member application state.");
    }

    private void requireChecker(String action, Long checker) {
        if (checker == null) {
            throw invalid(action, "checker.required", "A checker is required.");
        }
        if (makerAppUserId.equals(checker)) {
            throw invalid(action, "maker.cannot.be.checker", "The maker cannot check their own member application.");
        }
    }

    private void requireTimestamp(String action, OffsetDateTime at) {
        if (at == null) {
            throw invalid(action, "timestamp.required", "A transition timestamp is required.");
        }
        // Every transition records lastModifiedOn, including correction returns that have no final decidedOn.
        // Comparing instants also enforces chronology when callers supply different UTC offsets.
        if (at.isBefore(createdOn) || (lastModifiedOn != null && at.isBefore(lastModifiedOn))) {
            throw invalid(action, "timestamp.before.previous.transition",
                    "A transition timestamp must not precede creation or the previous workflow transition.");
        }
    }

    private String validateReason(String action, String reason) {
        if (reason == null || reason.isBlank()) {
            throw invalid(action, "reason.required", "A nonblank decision reason is required.");
        }
        if (reason.length() > 500) {
            throw invalid(action, "reason.too.long", "A decision reason must not exceed 500 characters.");
        }
        return reason.strip();
    }

    private InvalidMemberApplicationStateTransitionException invalid(String action, String postfix, String message) {
        return new InvalidMemberApplicationStateTransitionException(action, postfix, message);
    }

    private void transitionTo(MemberApplicationWorkflowStatus status, OffsetDateTime at) {
        workflowStatus = status;
        lastModifiedOn = at;
    }
}
