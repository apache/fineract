/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_group")
public class Group extends AbstractPersistable<Long> {

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Group> children = new LinkedList<Group>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Group parent;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff loanOfficer;
    
    @SuppressWarnings("unused")
    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(name = "hierarchy", length = 100)
    private String hierarchy;

    @Column(name = "name", length = 100, unique = true)
    private String name;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private Set<Client> clientMembers;

    public Group() {
        this.name = null;
        this.externalId = null;
        this.clientMembers = new HashSet<Client>();
    }

    public static Group newGroup(final Office office, final Staff loanOfficer , final Group parent ,final long levelId , final String name, final String externalId, final Set<Client> clientMembers) {
        return new Group(office, loanOfficer , parent , levelId , name, externalId, clientMembers);
    }

    public Group(final Office office, final Staff loanOfficer , final Group parent ,final long levelId , final String name, final String externalId, final Set<Client> clientMembers) {
        this.office = office;
        this.loanOfficer = loanOfficer;
        this.levelId =levelId;
        this.parent = parent;
        
        if (parent != null) {
            this.parent.addChild(this);
        }
        
        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        if (clientMembers != null) {
            this.clientMembers = clientMembers;
        }
    }

    private void addChild(final Group group) {
        this.children.add(group);
    }

    public Long getOfficeId() {
        return this.office.getId();
    }

    public Long getLoanOfficerId() {
        return this.loanOfficer.getId();
    }

    public void update(final GroupCommand command, final Office groupOffice, final Staff newLoanOfficer, final Set<Client> clientMembers) {
        if (command.isExternalIdChanged()) {
            this.externalId = command.getExternalId();
        }

        if (command.isOfficeIdChanged()) {
            this.office = groupOffice;
        }

        if (command.isLoanOfficerChanged()) {
            this.loanOfficer = newLoanOfficer;
        }

        if (command.isNameChanged()) {
            this.name = command.getName();
        }

        if (clientMembers != null && command.isClientMembersChanged()) {
            this.clientMembers = clientMembers;
        }
    }

    public void assigLoanOfficer(final GroupCommand command , final Staff newLoanOfficer){
        if (command.isLoanOfficerChanged()) {
            this.loanOfficer = newLoanOfficer;
        }
    }

    public void unassigLoanOfficer(final GroupCommand command) {
        if (command.isLoanOfficerChanged()) {
            this.loanOfficer = null;
        }
    }

    public void addClientMember(final Client member) {
        this.clientMembers.add(member);
    }

    public boolean hasClientAsMember(final Client client) {
        return this.clientMembers.contains(client);
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on group so it wont appear
     * in query/report results.
     * 
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.externalId = this.getId() + "_" + this.externalId;
        this.name = this.getId() + "_" + this.name;
    }

    public boolean isDeleted() {
        return deleted;
    }
    
    public void generateHierarchy() {

        if (parent != null) {
            this.hierarchy = this.parent.hierarchyOf(this.getId());
        } else {
            this.hierarchy = "." + this.getId() + ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

}
