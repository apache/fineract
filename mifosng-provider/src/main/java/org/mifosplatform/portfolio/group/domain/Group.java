/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.domain.Client;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_group")
public final class Group extends AbstractPersistable<Long> {

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    /**
     * A value from {@link GroupingTypeStatus}.
     */
    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "activation_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date activationDate;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Group parent;

    /*
     * TODO - kw - it might be possible to just move this to be a java enum type
     * rather than 'levels' table.
     */
    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    private GroupLevel groupLevel;

    @Column(name = "display_name", length = 100, unique = true)
    private String name;

    @Column(name = "hierarchy", length = 100)
    private String hierarchy;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Group> groupMembers = new LinkedList<Group>();

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private Set<Client> clientMembers;

    public Group() {
        this.name = null;
        this.externalId = null;
        this.clientMembers = new HashSet<Client>();
    }

    public static Group newGroup(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel,
            final String name, final String externalId, final boolean active, final LocalDate activationDate,
            final Set<Client> clientMembers, final Set<Group> groupMembers) {

        GroupingTypeStatus status = GroupingTypeStatus.PENDING;
        if (active) {
            status = GroupingTypeStatus.ACTIVE;
        }

        return new Group(office, staff, parent, groupLevel, name, externalId, status, activationDate, clientMembers, groupMembers);
    }

    public Group(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel, final String name,
            final String externalId, final GroupingTypeStatus status, final LocalDate activationDate, final Set<Client> clientMembers,
            final Set<Group> groupMembers) {
        this.office = office;
        this.staff = staff;
        this.groupLevel = groupLevel;
        this.parent = parent;

        if (parent != null) {
            this.parent.addChild(this);
        }

        this.status = status.getValue();
        if (activationDate != null) {
            this.activationDate = activationDate.toDate();
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
        if (groupMembers != null) {
            this.groupMembers.addAll(groupMembers);
        }
    }

    public boolean isNotPending() {
        return !isPending();
    }

    public boolean isPending() {
        return GroupingTypeStatus.fromInt(this.status).isPending();
    }

    private void addChild(final Group group) {
        this.groupMembers.add(group);
    }

    public Long getOfficeId() {
        return this.office.getId();
    }

    public Staff getStaff() {
        return this.staff;
    }

    public void setStaff(final Staff staff) {
        this.staff = staff;
    }

    public Long getStaffId() {
        return this.staff.getId();
    }

    public GroupLevel getGroupLevel() {
        return this.groupLevel;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getName() {
        return this.name;
    }

    public Set<Client> getClientMembers() {
        return this.clientMembers;
    }

    public Group getParent() {
        return this.parent;
    }

    public void setParent(final Group parent) {
        this.parent = parent;
    }

    public void setClientMembers(final Set<Client> clientMembers) {
        this.clientMembers = clientMembers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public void setLevelId(final GroupLevel groupLevel) {
        this.groupLevel = groupLevel;
    }

    public void assigStaff(final Staff newStaff) {
        this.staff = newStaff;
    }

    public void unassigStaff() {
        this.staff = null;
    }

    public void addClientMember(final Client member) {
        this.clientMembers.add(member);
    }

    public boolean hasClientAsMember(final Client client) {
        return this.clientMembers.contains(client);
    }

    public void generateHierarchy() {

        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = "." + getId() + ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public boolean isOfficeIdentifiedBy(final Long officeId) {
        return this.office.identifiedBy(officeId);
    }

    public Long officeId() {
        return this.office.getId();
    }
}