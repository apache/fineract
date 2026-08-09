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
package org.apache.fineract.accounting.provisioning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.provisioning.exception.InvalidProvisioningEntryStateTransitionException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.AppUser;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "m_provisioning_history")
public class ProvisioningEntry extends AbstractPersistableCustom<Long> {

    @Column(name = "journal_entry_created")
    private Boolean isJournalEntryCreated;

    @Column(name = "status_enum", nullable = false)
    private Integer statusEnum = ProvisioningEntryStatus.DRAFT.getValue();

    @Column(name = "approved_on_date")
    private LocalDate approvedOnDate;

    @OneToOne
    @JoinColumn(name = "approvedby_id")
    private AppUser approvedByUser;

    @Column(name = "rejected_on_date")
    private LocalDate rejectedOnDate;

    @OneToOne
    @JoinColumn(name = "rejectedby_id")
    private AppUser rejectedByUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductProvisioningEntry> provisioningEntries = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "createdby_id")
    private AppUser createdBy;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @OneToOne
    @JoinColumn(name = "lastmodifiedby_id")
    private AppUser lastModifiedBy;

    @Column(name = "lastmodified_date")
    private LocalDate lastModifiedDate;

    public void setProvisioningEntries(Collection<LoanProductProvisioningEntry> provisioningEntries) {
        if (this.provisioningEntries == null) {
            this.provisioningEntries = new HashSet<>();
        }
        this.provisioningEntries.addAll(provisioningEntries);
    }

    public Collection<LoanProductProvisioningEntry> getLoanProductProvisioningEntries() {
        return this.provisioningEntries;
    }

    public ProvisioningEntryStatus status() {
        return ProvisioningEntryStatus.fromInt(this.statusEnum);
    }

    /**
     * Transitions this entry from DRAFT to APPROVED. Only a DRAFT entry may be approved - approving an already approved
     * or rejected entry is a domain-rule violation, since re-approval has no well-defined meaning.
     */
    public void approve(final AppUser approvedByUser, final LocalDate approvedOnDate) {
        if (!status().isDraft()) {
            throw new InvalidProvisioningEntryStateTransitionException("approve", "not.in.draft.state",
                    "Provisioning entry with id `" + getId() + "` cannot be approved because it is not in draft status.", getId());
        }
        this.approvedByUser = approvedByUser;
        this.approvedOnDate = approvedOnDate;
        this.statusEnum = ProvisioningEntryStatus.APPROVED.getValue();
    }

    /**
     * Transitions this entry from DRAFT to REJECTED. Only a DRAFT entry may be rejected.
     */
    public void reject(final AppUser rejectedByUser, final LocalDate rejectedOnDate) {
        if (!status().isDraft()) {
            throw new InvalidProvisioningEntryStateTransitionException("reject", "not.in.draft.state",
                    "Provisioning entry with id `" + getId() + "` cannot be rejected because it is not in draft status.", getId());
        }
        this.rejectedByUser = rejectedByUser;
        this.rejectedOnDate = rejectedOnDate;
        this.statusEnum = ProvisioningEntryStatus.REJECTED.getValue();
    }

    /**
     * Reverts an APPROVED entry back to DRAFT, clearing the approval audit fields. Only permitted while journal entries
     * have not yet been created for this entry - once journal entries exist, the approval that produced them must not
     * be silently undone.
     */
    public void undoApproval() {
        if (!status().isApproved()) {
            throw new InvalidProvisioningEntryStateTransitionException("undo.approval", "not.in.approved.state",
                    "Provisioning entry with id `" + getId() + "` cannot have its approval undone because it is not approved.", getId());
        }
        if (Boolean.TRUE.equals(this.isJournalEntryCreated)) {
            throw new InvalidProvisioningEntryStateTransitionException("undo.approval", "journal.entries.already.created",
                    "Provisioning entry with id `" + getId() + "` cannot have its approval undone because journal entries have "
                            + "already been created for it.",
                    getId());
        }
        this.approvedByUser = null;
        this.approvedOnDate = null;
        this.statusEnum = ProvisioningEntryStatus.DRAFT.getValue();
    }

    /**
     * A DRAFT entry may still be regenerated; an APPROVED or REJECTED entry may not, since regenerating its rows would
     * silently change what was actually approved or rejected.
     */
    public void validateCanBeRegenerated() {
        if (!status().isDraft()) {
            throw new InvalidProvisioningEntryStateTransitionException("recreate", "not.in.draft.state",
                    "Provisioning entry with id `" + getId() + "` cannot be regenerated because it is not in draft status.", getId());
        }
    }

    /**
     * Journal entries may only be created for an APPROVED entry.
     */
    public void validateCanCreateJournalEntries() {
        if (!status().isApproved()) {
            throw new InvalidProvisioningEntryStateTransitionException("create.journal.entries", "not.in.approved.state",
                    "Provisioning entry with id `" + getId() + "` cannot have journal entries created because it is not approved.",
                    getId());
        }
    }

}
